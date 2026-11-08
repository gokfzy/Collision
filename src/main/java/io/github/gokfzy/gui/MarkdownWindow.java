package io.github.gokfzy.gui;

import one.jpro.platform.mdfx.MarkdownView;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class MarkdownWindow extends Stage {

    private static final double DEFAULT_WIDTH  = 800;
    private static final double DEFAULT_HEIGHT = 600;
    private static final String DEFAULT_TITLE  = "Window";

    public MarkdownWindow(Window owner, String title, String fileName) throws IOException {
        String markdownContent = new String(Objects.requireNonNull(MarkdownWindow.class.getClassLoader().getResourceAsStream("md/" + fileName)).readAllBytes(), StandardCharsets.UTF_8);
        if (owner != null) {
            initOwner(owner);
        }
        setTitle(title == null || title.isBlank() ? DEFAULT_TITLE : title);

        MarkdownView markdownView = new MarkdownView(markdownContent);
        StackPane root = new StackPane(markdownView);
        root.setStyle("-fx-background-color: white;");

        setScene(new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }
}