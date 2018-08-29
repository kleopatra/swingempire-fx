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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52062291/203657
 * 
 * Dialog shown partly off screen.
 * Part of the problem is that the size is not yet available in onShown (NaN)
 * happens only with showAndWait
 * 
 * - at the time of receiving the onShown, the window is not yet showing if using shwoAndWait
 * - workaround: use showing listener to adjust size/location
 */
public class DialogDebugSize extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("This is an alert!");
                alert.setContentText(
                        "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
                alert.initOwner(primaryStage);
                alert.setOnShown(new EventHandler<DialogEvent>() {
                    @Override
                    public void handle(DialogEvent event) {

                        double x = alert.getX();
                        double y = alert.getY();
                        double w = alert.getWidth();
                        double h = alert.getHeight();

                        // SHOWS ALL NaN NaN NaN NaN
                        System.out.println(x + " " + y + " " + w + " " + h + alert.isShowing());

                    }
                });
        alert.showingProperty().addListener((src, ov, nv) -> {
            double x = alert.getX();
            double y = alert.getY();
            double w = alert.getWidth();
            double h = alert.getHeight();

            // as example just adjust if location top/left is off 
            if (x < 0) {
                alert.setWidth(w + x);
                alert.setY(0);
            }
            if (y <0) {
                alert.setHeight(h + y);
                alert.setY(0);
            }
            
        });
        alert.showAndWait();

            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}