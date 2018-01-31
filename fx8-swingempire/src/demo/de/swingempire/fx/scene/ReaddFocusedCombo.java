/*
 * Created on 31.01.2018
 *
 */
package de.swingempire.fx.scene;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/48538763/203657
 * re-added combo is focused but not clickable
 */
public class ReaddFocusedCombo extends Application {

    @Override
    public void start(Stage stage) {
        VBox root = new VBox();

        final ComboBox<String> choices = new ComboBox<>();
        choices.getItems().add("Test1");
        choices.getItems().add("Test2");
        root.getChildren().add(choices);

        choices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // guess by sillyfly: combo gets confused if popup still open 
            choices.hide();
            root.getChildren().clear();
            root.getChildren().add(choices);
            // suggested in answer: working but then the choice isn't focused
            //root.requestFocus();
            // doesn't work
            //  choices.requestFocus();
        });

        stage.setScene(new Scene(root));
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
    
}

