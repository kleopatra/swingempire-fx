/*
 * Created on 29.08.2018
 *
 */
package de.swingempire.fx.scene.layout;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52062291/203657
 * this is the answer by marc - does not address the underlying problem
 * 
 * Dialog shown partly off screen.
 * Part of the problem is that the size is not yet available in onShown (NaN)
 * happens only with showAndWait
 * 
 * - at the time of receiving the onShown, the window is not yet showing if using shwoAndWait
 */
public class DialogDebugSizeMarcAnswer extends Application {

    @Override
    public void start(Stage stage) {

        Pane pane = new Pane();
        Scene scene = new Scene(pane, 800, 500);

        Button button = new Button("Alert");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("This is an alert!");
                alert.setContentText(
                        "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
               alert.initOwner(stage);

                alert.setOnShown(new EventHandler<DialogEvent>() {
                    @Override
                    public void handle(DialogEvent event) {
                        System.out.println(alert.getOwner().getX());
                        System.out.println(alert.getOwner().getY());

                        //Values from screen
                        int screenMaxX = (int) Screen.getPrimary().getVisualBounds().getMaxX();
                        int screenMaxY = (int) Screen.getPrimary().getVisualBounds().getMaxY();

                        //Values from stage
                        int width = (int) stage.getWidth();
                        int height = (int) stage.getHeight();
                        int stageMaxX = (int) stage.getX();
                        int stageMaxY = (int) stage.getY();

                        //Maximal values your stage
                        int paneMaxX = screenMaxX - width;
                        int paneMaxY = screenMaxY - height;

                        //Check if the position of your stage is not out of screen 
                        if (stageMaxX > paneMaxX || stageMaxY > paneMaxY) {
                            //Set stage where ever you want
                        }
                    }
                });

                alert.showAndWait();

            }
        });

        pane.getChildren().add(button);
        stage.setScene(scene);
        stage.show();
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}