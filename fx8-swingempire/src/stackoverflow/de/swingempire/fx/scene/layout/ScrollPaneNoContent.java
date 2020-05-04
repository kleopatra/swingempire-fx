/*
 * Created on 21.04.2020
 *
 */
package de.swingempire.fx.scene.layout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ScrollPaneNoContent extends Application {
    @Override
    public void start(final Stage primaryStage) {
        Label test = new Label();
        test.setText("Testing JavaFX........");
        VBox box = new VBox();
        ScrollPane pane = new ScrollPane(box);
        pane.setPrefSize(300.0, 300.0);
        box.getChildren().add(test);
        Scene main = new Scene(pane);
        primaryStage.setScene(main);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}