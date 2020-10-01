/*
 * Created on 01.10.2020
 *
 */
package de.swingempire.fx.fxml;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * from https://www.vojtechruzicka.com/javafx-fxml-scene-builder/
 * was possible to use javascript in fxml (fx 11)
 * 
 * no longer in fx15? https://stackoverflow.com/questions/64150836/javascript-in-fxml
 * guessing: Nashorn removed in java15, no engine? Or has it a replacement?
 * https://openjdk.java.net/jeps/372
 * 
 * PENDING: check against java/fx15
 */
public class ScriptApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = getClass().getResource("script.fxml");
        loader.setLocation(xmlUrl);
        Parent root = loader.load();

        primaryStage.setScene(new Scene(root));
        primaryStage.show();   
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
