/*
 * Created on 05.08.2020
 *
 */
package de.swingempire.fx.fxml;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/63256644/203657
 * 
 * cell factory returns null for rowItem
 */
public class SearchMoviesApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL url = getClass().getResource("searchmovies.fxml");

        try {
            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setX(primaryStage.getX() - 200);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}