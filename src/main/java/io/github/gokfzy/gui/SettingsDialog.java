package io.github.gokfzy.gui;

import io.github.gokfzy.core.util.Translation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingsDialog {

    private record ConfigItem(Supplier<?> getter, Runnable reSetter) {}

    private final Map<String, ConfigItem> items = new LinkedHashMap<>();
    private final Stage stage;
    private final VBox content;
    private final Label statusLabel;

    private Consumer<SettingsDialog> onApply;
    private Consumer<SettingsDialog> onOk;
    private Supplier<String> validator;

    public SettingsDialog(Window owner, String title) {
        stage = new Stage();
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        }
        stage.setTitle(title);

        content = new VBox(12);
        content.setPadding(new Insets(16));

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #c0392b;");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Button applyBtn  = new Button(Translation.translationString("gui.setting.apply"));
        Button cancelBtn = new Button(Translation.translationString("gui.setting.cancel"));
        Button okBtn     = new Button(Translation.translationString("gui.setting.ok"));
        okBtn.setDefaultButton(true);
        cancelBtn.setCancelButton(true);

        applyBtn.setOnAction(e -> { if (validateAndMark()) fireApply(); });
        okBtn.setOnAction(e -> {
            if (validateAndMark()) { fireApply(); fireOk(); close(); }
        });
        cancelBtn.setOnAction(e -> close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttons = new HBox(10, statusLabel, spacer, applyBtn, cancelBtn, okBtn);
        buttons.setPadding(new Insets(10, 16, 12, 16));
        buttons.setAlignment(Pos.CENTER_LEFT);

        BorderPane root = new BorderPane();
        root.setCenter(scroll);
        root.setBottom(buttons);

        stage.setScene(new Scene(root, 540, 460));
    }

    public TextField addTextField(String key, String label, String defaultValue) {
        TextField tf = new TextField(defaultValue);
        addRow(key, label, tf, tf::getText, () -> tf.setText(defaultValue));
        return tf;
    }

        public void addNumberField(String key, String label, double defaultValue) {
        TextField tf = new TextField(String.valueOf(defaultValue));
        addRow(key, label, tf,
                () -> {
                    try { return Double.parseDouble(tf.getText().trim()); }
                    catch (NumberFormatException e) { return defaultValue; }
                },
                () -> tf.setText(String.valueOf(defaultValue)));
    }

    public void addIntField(String key, String label, int defaultValue) {
        TextField tf = new TextField(String.valueOf(defaultValue));
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("^[+-]?\\d+$")) { return change; }
            return null;
        });
        tf.setTextFormatter(formatter);
        addRow(key, label, tf,
                () -> {
                    try { return Integer.parseInt(tf.getText().trim()); }
                    catch (NumberFormatException e) { return defaultValue; }
                },
                () -> tf.setText(String.valueOf(defaultValue)));
    }

    public void addComboBox(String key, String label,
                                        List<String> options, String defaultValue) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(options);
        cb.setValue(defaultValue);
        cb.setMaxWidth(Double.MAX_VALUE);
        addRow(key, label, cb, cb::getValue, () -> cb.setValue(defaultValue));
    }

    public void addCheckBox(String key, String label, boolean defaultValue) {
        CheckBox cb = new CheckBox();
        cb.setSelected(defaultValue);
        addRow(key, label, cb, cb::isSelected, () -> cb.setSelected(defaultValue));
    }

    public void addSection(String title) {
        Label g = new Label(title);
        g.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-padding: 8 0 0 0;");
        content.getChildren().add(g);
    }


    public String getText(String key)     { return (String) getValue(key); }
    public boolean getBoolean(String key) { return (Boolean) getValue(key); }
    public double getNumber(String key)   { return ((Number) getValue(key)).doubleValue(); }
    public int getInt(String key)   { return ((int) getValue(key)); }

    private Object getValue(String key) {
        ConfigItem item = items.get(key);
        if (item == null) throw new IllegalArgumentException("未找到配置项: " + key);
        return item.getter.get();
    }

    public SettingsDialog onApply(Consumer<SettingsDialog> cb) { this.onApply = cb; return this; }
    public SettingsDialog onOk(Consumer<SettingsDialog> cb)    { this.onOk = cb;    return this; }

    public SettingsDialog setValidator(Supplier<String> validator) {
        this.validator = validator;
        return this;
    }

    private void fireApply() { if (onApply != null) onApply.accept(this); }
    private void fireOk()    { if (onOk != null)    onOk.accept(this); }

    private boolean validateAndMark() {
        if (validator != null) {
            String err = validator.get();
            if (err != null && !err.isBlank()) {
                statusLabel.setText("⚠ " + err);
                return false;
            }
        }
        statusLabel.setText("");
        return true;
    }

    public void show()        { stage.show(); }
    public void close()       { stage.close(); }

    private void addRow(String key, String label, Node control,
                        Supplier<?> getter, Runnable reSetter) {
        if (items.containsKey(key))
            throw new IllegalArgumentException();

        Button btn = new Button(label);
        btn.setMinWidth(150);
        btn.setPrefWidth(150);
        btn.setTooltip(new Tooltip(btn.getText()));
        btn.setOnAction(e -> { reSetter.run(); statusLabel.setText(""); });

        if (control instanceof Region r) {
            HBox.setHgrow(r, Priority.ALWAYS);
            r.setMaxWidth(Double.MAX_VALUE);
        }

        HBox row = new HBox(10, btn, control);
        row.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().add(row);

        items.put(key, new ConfigItem(getter, reSetter));
    }
}