/*
 * Created on 29.08.2018
 *
 */
package control;

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
 * 
 * used for bug report:
 * https://bugs.openjdk.java.net/browse/JDK-8210037
 */
public class DialogOnShown extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("This is an alert!");
            alert.setContentText("hello again!");
            alert.initOwner(primaryStage);
            alert.setOnShown(shown -> {
                // SHOWS ALL NaN NaN NaN NaN
                System.out.println("alert must be showing:" + alert.isShowing());

            });
            alert.showAndWait();
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