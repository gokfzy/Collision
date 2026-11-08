package io.github.gokfzy.gui;

import io.github.gokfzy.core.Config;
import io.github.gokfzy.core.util.BigIntegerPack;
import io.github.gokfzy.core.util.block.blockAttribute.Data;
import io.github.gokfzy.core.util.block.blockAttribute.Pos;
import io.github.gokfzy.core.util.block.enums.Direction;
import io.github.gokfzy.core.util.block.enums.Type;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.github.gokfzy.Main.*;
import static io.github.gokfzy.core.util.Constants.*;
import static io.github.gokfzy.core.util.util.nextData;
import static io.github.gokfzy.core.util.util.translationString;

public class MainGuiController {

    @FXML private Region view;
    @FXML private Canvas axisCanvas;
    @FXML private StackPane canvasContainer;
    @FXML private SplitMenuButton runButton;

    private Stage stage;

    private static final double MIN_CELL_SIZE = 10;
    private static final double MAX_CELL_SIZE = 400;
    private static final double ZOOM_FACTOR = 1.1;

    private double cellSize = 20;

    private double camX, camY, startX, startY, pressX, pressY;
    private boolean dragging;
    private boolean needsRedraw = true;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private sealed interface Drawable permits Arrow, CellFill, GridRectFill { }
    private record Arrow(BigInteger col, BigInteger row, double rot, Color color) implements Drawable { }
    private record CellFill(BigInteger col, BigInteger row, Color color) implements Drawable { }
    private record GridRectFill(double gridX, double gridY,
                                double gridWidth, double gridHeight, Color color) implements Drawable { }
    private final List<Drawable> drawables = new ArrayList<>();

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> future;

    private final Runnable task = () -> {
        try {
            updateAndDraw();
        } catch (Throwable ignored) {}
    };

    @FXML
    private void initialize() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ms-timer");
            t.setDaemon(true);
            return t;
        });
        view.setPickOnBounds(true);
        view.setOnMousePressed(this::onPressed);
        view.setOnMouseDragged(this::onDragged);
        view.setOnMouseClicked(this::onClicked);
        view.setOnScroll(this::onScroll);

        canvasContainer.widthProperty().addListener((_, _, nv) -> {
            axisCanvas.setWidth(nv.doubleValue());
            requestRedraw();
        });
        canvasContainer.heightProperty().addListener((_, _, nv) -> {
            axisCanvas.setHeight(nv.doubleValue());
            requestRedraw();
        });

        new AnimationTimer() {
            @Override public void handle(long now) {
                if (needsRedraw) { needsRedraw = false; drawScene(); }
            }
        }.start();

        Platform.runLater(() -> {
            axisCanvas.setWidth(canvasContainer.getWidth());
            axisCanvas.setHeight(canvasContainer.getHeight());
            camX = axisCanvas.getWidth() / 2;
            camY = axisCanvas.getHeight() / 2;
            requestRedraw();
            if (!Config.isSetLanguage()) {
                choiceLanguage();
            }
        });
    }

    @FXML
    private void setItems() {
        for (MenuItem item : runButton.getItems()) {
            item.setText(translationString((String) item.getProperties().get("translation")));
        }
    }

    @FXML
    private void clickAutoUpdateButton() {
        if (enableAutoUpdate) {
            runButton.getItems().getFirst().getProperties().put("translation", "gui.run.start_auto_update");
            future.cancel(true);
            future = null;
        } else {
            runButton.getItems().getFirst().getProperties().put("translation", "gui.run.stop_auto_update");
            int update_interval = (Integer) Config.config.get("update_interval");
            future = scheduler.scheduleWithFixedDelay(task, update_interval, update_interval, TimeUnit.MILLISECONDS);
        }
        enableAutoUpdate = !enableAutoUpdate;
    }

    @FXML private void handleClickRunButton() { this.updateAndDraw(); }
    @FXML private void handleSetting() {
        SettingsDialog dlg = new SettingsDialog(stage, translationString("gui.setting.setting"));

        dlg.addSection(translationString("gui.setting.run"));
        dlg.addIntField("delete_distance", translationString("gui.setting.survive_step"), (Integer) Config.config.get("delete_distance"));
        dlg.addIntField("updated_step", translationString("gui.setting.updated_step_count"), (Integer) Config.config.get("updated_step"));
        dlg.addIntField("update_interval", translationString("gui.setting.update_interval"), (Integer) Config.config.get("update_interval"));
        dlg.addCheckBox("enable_traceback", translationString("gui.setting.enable_traceback"), (Boolean) Config.config.get("enable_traceback"));

        dlg.addSection(translationString("gui.setting.other"));
        dlg.addComboBox("language", translationString("gui.setting.select_language"), List.copyOf(LANGUAGE_AND_TAG.keySet()), TAG_AND_LANGUAGE.get((String) Config.config.get("language")));

        dlg.setValidator(() -> {
            if (dlg.getInt("updated_step") < 1) {
                return translationString("gui.setting.error_updated_step_count");
            } else if (dlg.getInt("update_interval") < 1) {
                return translationString("gui.setting.error_update_interval");
            }
            return null;
        }).onApply(_ -> updateConfig(dlg)).onOk(d -> {
            updateConfig(dlg);
            d.close();
        }).show();
    }

    private void addArrow(BigInteger col, BigInteger row, Color color) { addArrow(col, row, 0, color); }

    private void addArrow(BigInteger col, BigInteger row, double rotationDegrees, Color color) {
        runOnFxThread(() -> {
            drawables.add(new Arrow(col, row, rotationDegrees, color));
            requestRedraw();
        });
    }

    private void addCellFill(BigInteger col, BigInteger row, Color color) {
        runOnFxThread(() -> {
            drawables.add(new CellFill(col, row, color));
            requestRedraw();
        });
    }

    private void clearDrawables() {
        runOnFxThread(() -> { drawables.clear(); requestRedraw(); });
    }

    private void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) action.run();
        else Platform.runLater(action);
    }

    private void requestRedraw() { needsRedraw = true; }

    private void onPressed(MouseEvent e) {
        startX = pressX = e.getSceneX();
        startY = pressY = e.getSceneY();
        dragging = false;
    }

    private void onDragged(MouseEvent e) {
        if (Math.hypot(e.getSceneX() - startX, e.getSceneY() - startY) > 5) dragging = true;
        camX += e.getSceneX() - pressX;
        camY += e.getSceneY() - pressY;
        pressX = e.getSceneX();
        pressY = e.getSceneY();
        requestRedraw();
    }

    private void onScroll(ScrollEvent e) {
        if (!e.isControlDown()) return;

        double dy = e.getDeltaY();
        if (dy == 0) return;

        double factor = dy > 0 ? ZOOM_FACTOR : 1 / ZOOM_FACTOR;
        double newSize = cellSize * factor;
        if (newSize < MIN_CELL_SIZE || newSize > MAX_CELL_SIZE) {
            e.consume();
            return;
        }

        Point2D p = view.sceneToLocal(e.getSceneX(), e.getSceneY());
        double gridX = (p.getX() - camX) / cellSize;
        double gridY = (camY - p.getY()) / cellSize;

        camX = p.getX() - gridX * newSize;
        camY = p.getY() + gridY * newSize;
        cellSize = newSize;

        requestRedraw();
        e.consume();
    }

    private void onClicked(MouseEvent e) {
        if (dragging) return;
        Point2D p = view.sceneToLocal(e.getSceneX(), e.getSceneY());
        BigInteger col = toFloor((p.getX() - camX) / cellSize);
        BigInteger row = toFloor((camY - p.getY()) / cellSize);

        Data oldData = memory.get(col, row);
        memory.set(col, row, nextData(oldData));
        this.clearDrawables();
        drawMemory();
    }

    private static BigInteger toFloor(double v) {
        return Double.isFinite(v)
                ? BigDecimal.valueOf(v).setScale(0, RoundingMode.FLOOR).toBigIntegerExact()
                : BigInteger.ZERO;
    }

    private double gx(double gridX) { return camX + gridX * cellSize; }
    private double gy(double gridY) { return camY - gridY * cellSize; }

    private void drawScene() {
        double W = axisCanvas.getWidth(), H = axisCanvas.getHeight();
        if (W <= 0 || H <= 0) return;

        GraphicsContext gc = axisCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, W, H);

        gc.setStroke(Color.web("#e0e0e0"));
        for (long k = (long) Math.floor(-camX / cellSize) - 1,
             kMax = (long) Math.ceil((W - camX) / cellSize) + 1; k <= kMax; k++) {
            double sx = camX + k * cellSize + 0.5;
            gc.strokeLine(sx, 0, sx, H);
        }
        for (long j = (long) Math.floor((camY - H) / cellSize) - 1,
             jMax = (long) Math.ceil(camY / cellSize) + 1; j <= jMax; j++) {
            double sy = camY - j * cellSize + 0.5;
            gc.strokeLine(0, sy, W, sy);
        }

        for (Drawable d : drawables) {
            switch (d) {
                case CellFill f -> {
                    gc.setFill(f.color());
                    gc.fillRect(gx(f.col().doubleValue()), gy(f.row().doubleValue() + 1), cellSize, cellSize);
                }
                case GridRectFill r -> {
                    gc.setFill(r.color());
                    gc.fillRect(gx(r.gridX()), gy(r.gridY() + r.gridHeight()),
                            r.gridWidth() * cellSize, r.gridHeight() * cellSize);
                }
                default -> { }
            }
        }

        for (Drawable d : drawables) {
            if (d instanceof Arrow(BigInteger col, BigInteger row, double rot, Color color)) paintArrow(gc, col, row, rot, color);
        }
    }

    private void paintArrow(GraphicsContext gc, BigInteger col, BigInteger row, double rotationDegrees, Color color) {
        gc.save();
        gc.beginPath();
        try {
            gc.translate(gx(col.doubleValue() + 0.5), gy(row.doubleValue() + 0.5));
            if (rotationDegrees != 0) gc.rotate(rotationDegrees);
            gc.translate(-0.375 * cellSize, -0.4125 * cellSize);
            gc.scale(cellSize, cellSize);

            gc.moveTo(0.375, 0.825);
            gc.lineTo(0.42525, 0.825);
            gc.lineTo(0.42525, 0.0542625);
            gc.lineTo(0.375, 0.0542625);
            gc.lineTo(0.32475, 0.0542625);
            gc.lineTo(0.32475, 0.825);
            gc.closePath();

            gc.moveTo(0.410625, 0.01875);
            gc.bezierCurveTo(0.39075, -0.00075, 0.35925, -0.00075, 0.339375, 0.01875);
            gc.lineTo(0.19725, 0.23175);
            gc.bezierCurveTo(0.1875, 0.241875, 0.1875, 0.257625, 0.19725, 0.267375);
            gc.bezierCurveTo(0.207, 0.277125, 0.223125, 0.277125, 0.232875, 0.267375);
            gc.lineTo(0.375, 0.12525);
            gc.lineTo(0.517125, 0.267375);
            gc.bezierCurveTo(0.526875, 0.277125, 0.543, 0.277125, 0.55275, 0.267375);
            gc.bezierCurveTo(0.5625, 0.257625, 0.5625, 0.241875, 0.55275, 0.23175);
            gc.lineTo(0.410625, 0.01875);
            gc.closePath();

            gc.setFill(color);
            gc.fill();
            gc.setStroke(color);
            gc.setLineWidth(0.02);
            gc.stroke();
        } finally {
            gc.restore();
        }
    }


    private void drawMemory(boolean isTraceback) {
        Color arrowColor = isTraceback ? Color.BLACK.deriveColor(0, 1, 1, 0.25) : Color.BLACK;
        for (Map.Entry<Pos, Data> entry : memory) {
            Pos pos = entry.getKey();
            Data data = entry.getValue();
            this.addCellFill(pos.x(), pos.y(), isTraceback ? TYPE_AND_COLOR.get(data.type()).deriveColor(0, 1, 1, 0.25) : TYPE_AND_COLOR.get(data.type()));
            for (Direction direction : data.directionSet()) {
                switch (direction) {
                    case UP -> this.addArrow(pos.x(), pos.y(), arrowColor);
                    case RIGHT -> this.addArrow(pos.x(), pos.y(), 90, arrowColor);
                    case DOWN -> this.addArrow(pos.x(), pos.y(), 180, arrowColor);
                    case LEFT -> this.addArrow(pos.x(), pos.y(), 270, arrowColor);
                }
            }
        }
    }

    private void drawMemory() {
        drawMemory(false);
    }

    private void updateAndDraw() {
        this.clearDrawables();
        if ((Boolean) Config.config.get("enable_traceback")) drawMemory(true);
        for (int i = 0; i < (Integer) Config.config.get("updated_step"); i++) {
            run.update();
        }
        drawMemory();
        afterUpdate();
    }

    private void updateConfig(SettingsDialog dlg) {
        Config.config.put("delete_distance", dlg.getInt("delete_distance"));
        Config.config.put("updated_step", dlg.getInt("updated_step"));
        Config.config.put("update_interval", dlg.getInt("update_interval"));
        Config.config.put("enable_traceback", dlg.getBoolean("enable_traceback"));
        Config.config.put("language", LANGUAGE_AND_TAG.get(dlg.getText("language")));
        try {
            Config.saveConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void choiceLanguage() {
        List<String> options = List.copyOf(LANGUAGE_AND_TAG.keySet());
        ChoiceDialog<String> dialog = new ChoiceDialog<>(TAG_AND_LANGUAGE.get((String) Config.config.get("language")), options);
        dialog.setTitle(translationString("gui.setting.select_language"));
        dialog.setHeaderText(translationString("gui.setting.select_language") + ":");

        String result = dialog.showAndWait().orElse(null);

        if (result != null) {
            Config.config.put("language", LANGUAGE_AND_TAG.get(result));
            try {
                Config.saveConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void afterUpdate() {
        Map<Pos, Data> snapshot = memory.getBlockMap();
        final BigIntegerPack leftBorder = new BigIntegerPack(null, true);
        final BigIntegerPack rightBorder = new BigIntegerPack(null, false);
        final BigIntegerPack bottomBorder = new BigIntegerPack(null, true);
        final BigIntegerPack topBorder = new BigIntegerPack(null, false);

        BigInteger steps = BigInteger.valueOf(((Number) Config.config.get("delete_distance")).longValue());

        snapshot.forEach( (pos, data) -> {
            if (data.type() == Type.FACTORY) {
                if (leftBorder.compareTo(pos.x().subtract(steps)) > 0) {
                    leftBorder.setInteger(pos.x().subtract(steps));
                }
                if (rightBorder.compareTo(pos.x().add(steps)) < 0) {
                    rightBorder.setInteger(pos.x().add(steps));
                }
                if (bottomBorder.compareTo(pos.y().subtract(steps)) > 0) {
                    bottomBorder.setInteger(pos.y().subtract(steps));
                }
                if (topBorder.compareTo(pos.y().add(steps)) < 0) {
                    topBorder.setInteger(pos.y().add(steps));
                }
            }
        });
        snapshot.entrySet().stream().filter(entry -> entry.getValue().type() == Type.MOVING_BLOCK).forEach(entry -> {
                Pos pos = entry.getKey();

                if (leftBorder.compareTo(pos.x()) + rightBorder.compareTo(pos.x()) != 0 ||
                        topBorder.compareTo(pos.y()) + bottomBorder.compareTo(pos.y()) != 0) {
                    memory.set(pos, Data.empty());
                }
            }
        );
    }

    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }
}