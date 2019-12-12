/*
 * Created on 12.12.2019
 *
 */
package de.swingempire.fx.scene.control;

import de.swingempire.testfx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/59255211/203657
 * value in DatePicker reverted back to empty on showing the alert
 * works expected in fx11
 */
public class DatePickerCommit extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // Simple Interface
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        DatePicker datePicker = new DatePicker();
        datePicker.focusedProperty().addListener((src, ov, nv) -> {
            System.out.println("value on focusChange " + datePicker.getValue());
        });

        // Add listener on DatePicker
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("old/new: " + oldValue + "/" + newValue);

            if (newValue != null) {
                System.out.println("before showing .." + datePicker.getValue());
                // Show an Alert
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("You selected " + newValue);
                alert.show();
                alert.setY(alert.getY()+100);
            }

        });

        root.getChildren().add(datePicker);

        // Show the stage
        primaryStage.setScene(new Scene(root, 300, 100));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }
}

