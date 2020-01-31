/*
 * Created on 31.01.2020
 *
 */
package de.swingempire.fx.event;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/59973807/203657
 * playing with mouse transparent/pickbounds
 */
public class MouseTransparentApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mousetransparent.fxml"));
        root.getStylesheets().add(getClass().getResource("mousetransparent.css").toExternalForm());
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}