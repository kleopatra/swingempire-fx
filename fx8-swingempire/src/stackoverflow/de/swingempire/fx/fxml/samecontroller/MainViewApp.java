/*
 * Created on 09.07.2019
 *
 */
package de.swingempire.fx.fxml.samecontroller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainViewApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainview.fxml"));
            Parent rootElement = (Parent) loader.load();
            Scene scene = new Scene(rootElement, 800, 600);
            primaryStage.setTitle("JavaFX");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {    
        launch(args);
    }
}

