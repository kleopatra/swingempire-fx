/*
 * Created on 21.11.2019
 *
 */
package de.swingempire.fx.event;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ViewPopupApp extends Application {

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("viewpopup.fxml"));
        AnchorPane pane = loader.load();
        primaryStage.setScene(new Scene(pane, 200, 200));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}