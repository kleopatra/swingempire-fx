/*
 * Created on 20.11.2018
 *
 */
package de.swingempire.fx.event;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/53381625/203657
 * ghost image when dropping (a file) to the app.
 * 
 * worksform, fx8, 
 * 
 */
public class DnDWithAlertOnDrop extends Application {

    private Parent root = new VBox();

    private void onDragOver(DragEvent dragEvent) {
        if (dragEvent.getDragboard().hasFiles()) {
            dragEvent.acceptTransferModes(TransferMode.COPY);
        }
    }

    private void isUserSure() {
        Alert alert = new Alert(Alert.AlertType.WARNING, "", ButtonType.OK);
        alert.showAndWait();
    }

    @Override
    public void start(Stage primaryStage) {
        root.setOnDragOver((event) -> onDragOver(event));
        root.setOnDragDropped((event) -> isUserSure());
        primaryStage.setTitle("ghost demo");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
