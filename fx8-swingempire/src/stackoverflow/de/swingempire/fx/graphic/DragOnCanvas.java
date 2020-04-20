/*
 * Created on 19.04.2020
 *
 */
package de.swingempire.fx.graphic;

import java.io.File;

import de.swingempire.fx.util.FXUtils;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DragOnCanvas extends Application {
    public void start(Stage stage) throws Exception {
        StackPane root = new StackPane();
        Canvas canvas = new Canvas(500, 500);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        File file = new File("src/Image.png");
//    URI uri = 
        gc.drawImage(FXUtils.getIcon(), 0, 0); // new
                                               // Image(file.toURI().toString()),0,0);

        Circle circle = new Circle(32, 32, 32);

        Line path = new Line(circle.getCenterX(), circle.getCenterY(), -200, -200);
//        Path path = new Path();
//        path.getElements().addAll(new LineTo(200, 200));
        PathTransition move = new PathTransition();
        move.setDuration(Duration.millis(2000));
        move.setNode(circle);
        move.setPath(path);
        move.setAutoReverse(false);
        move.setCycleCount(1);
        move.play();

        root.getChildren().addAll(canvas, circle);
        Scene scene = new Scene(root, 500, 500);
        stage.setScene(scene);
        stage.show();
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }

}