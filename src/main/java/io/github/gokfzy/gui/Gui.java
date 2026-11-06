package io.github.gokfzy.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Gui extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Gui.class.getResource("/gui/gui.fxml"));
        Parent root = loader.load();
        MainGuiController controller = loader.getController();
        controller.setStage(stage);

        stage.setTitle("Collision");
        stage.setScene(new Scene(root, 640, 480));
        stage.setOnCloseRequest(e -> controller.shutdown());
        stage.show();
    }
}