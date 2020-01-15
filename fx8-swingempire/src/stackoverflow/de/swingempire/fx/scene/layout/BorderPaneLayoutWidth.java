/*
 * Created on 15.01.2020
 *
 */
package de.swingempire.fx.scene.layout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/59748485/203657
 * 
 * layoutWidth returns 0 "early" in its life-cycle
 * worksforme fx11, reported against fx8, though - works as well 8_152
 */
public class BorderPaneLayoutWidth extends Application {

    @Override
    public void start(Stage primaryStage) {

        Button btn = new Button();
        btn.setText("Left side of BorderPane");
        BorderPane root = new BorderPane();
        root.setLeft(btn);
        Scene scene = new Scene(root, 640, 480);
        primaryStage.setScene(scene);

        primaryStage.show();

        // ... later on retrieve the width of the left side:
        System.out.println(root.getLeft().getLayoutBounds().getWidth());
    }

    public static void main(String[] args) {
        launch(args);
    }
}

