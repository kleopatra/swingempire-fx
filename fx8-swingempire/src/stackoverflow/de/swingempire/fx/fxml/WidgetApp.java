/*
 * Created on 30.10.2020
 *
 */
package de.swingempire.fx.fxml;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/64600828/203657
 * indirect loading doesn't work - class not found in scenebuilder
 * 
 * worksforme - here, didn't test scenebuilder
 */
public class WidgetApp extends Application {

    private Parent createContent() {
        return new Widget();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(WidgetApp.class.getName());

}
