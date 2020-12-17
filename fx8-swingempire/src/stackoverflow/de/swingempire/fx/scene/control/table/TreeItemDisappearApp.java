package de.swingempire.fx.scene.control.table;

import java.io.IOException;
import java.lang.ModuleLayer.Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 */
public class TreeItemDisappearApp extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Tree Table View Samples");
        FXMLLoader loader = new FXMLLoader();
        Parent sceneRoot = loader.load(getClass().getResource("treeitemdisappear.fxml"));
//        TreeItemDisappearController controller = new TreeItemDisappearController();
//        loader.setController(controller);
        final Scene scene = new Scene(sceneRoot, 450, 400);
        stage.setScene(scene);
        stage.show();
    }
}
