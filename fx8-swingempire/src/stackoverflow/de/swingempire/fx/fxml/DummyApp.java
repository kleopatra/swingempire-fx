/*
 * Created on 01.08.2019
 *
 */
package de.swingempire.fx.fxml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Quick check of validity
 * answer: https://stackoverflow.com/a/57289714/203657
 * 
 * is valid, though a bit useless (the question isn't clear)
 * 
 */
public class DummyApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("dummyview.fxml")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String... args) {
        launch(args);
    }
}