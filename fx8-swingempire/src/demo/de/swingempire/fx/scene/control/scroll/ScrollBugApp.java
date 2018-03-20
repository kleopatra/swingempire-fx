/*
 * Created on 20.03.2018
 *
 */
package de.swingempire.fx.scene.control.scroll;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8183399
 * scrolling stutter, reported for OSX
 * seen when scrolling by touchpad
 * 
 * can't reproduce ... no idea how to scroll the label via touchpad?
 */
public class ScrollBugApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Label label = new Label("Scroll On Me!");
//        label.setPrefSize(2000, 2000);
        label.setAlignment(Pos.CENTER);
        label.setOnScrollStarted(evt ->  System.out.println("--- Scroll started ----"));
        label.setOnScroll(evt -> System.out.println(evt.getDeltaY()));
        // JW added scrollpane
        

        Rectangle rect = new Rectangle(2000, 2000, Color.RED);
        ScrollPane s1 = new ScrollPane();
//        s1.setPrefSize(1200, 1200);
        s1.setContent(rect);

        ScrollPane scroll = new ScrollPane(label);
//        scroll.setMaxHeight(Double.MAX_VALUE);
        Scene scene = new Scene(scroll);
        primaryStage.setScene(scene);
        primaryStage.setWidth(200);
        primaryStage.setHeight(200);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
