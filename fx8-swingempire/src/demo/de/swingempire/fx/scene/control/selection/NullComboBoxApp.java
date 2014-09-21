/*
 * Created on 18.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
/**
 * http://stackoverflow.com/q/25877323/203657
 * add null to items throws in 8u20 
 * it's a non-sensical requirement, though.
 */
public class NullComboBoxApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList(
                "Option 1",
                "Option 2",
                "Option 3"
        ));

        cb.getItems().add(null);

        StackPane root = new StackPane();
        root.getChildren().add(cb);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Null ComboBox Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 

