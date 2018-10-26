/*
 * Created on 10.10.2018
 *
 */
package de.swingempire.fx.scene.control;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * want to get action on any selection (even if same)
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboBoxSelection extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox layout = new VBox();
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Hello", "World");
        comboBox.setOnAction(event -> System.out.println("Selected " + comboBox.getValue()));
        layout.getChildren().addAll(comboBox);

        Scene scene = new Scene(layout);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

