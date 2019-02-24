/*
 * Created on 19.02.2019
 *
 */
package de.swingempire.fx.scene.control.cell.focus;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SyncTwoTablesMain extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
           Parent root = FXMLLoader.load(getClass().getResource("synchtwotables.fxml"));
            Scene scene = new Scene(root,400,400);

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}