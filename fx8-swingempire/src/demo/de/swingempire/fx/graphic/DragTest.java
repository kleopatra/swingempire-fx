/*
 * Created on 01.10.2015
 *
 */
package de.swingempire.fx.graphic;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * Performance
 * https://bugs.openjdk.java.net/browse/JDK-8087922
 * 
 * Drag circle around and see cursor moving ahead (outside)
 * of the circle 
 * 
 * Still virulent fx9.
 */
public class DragTest extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FlowPane flowPane = new FlowPane();
        Scene scene = new Scene(flowPane, 500, 500);
        stage.setScene(scene);
        double circleRadius = 20;
        Circle circle = new Circle(circleRadius);
        circle.setOnMouseDragged(mouseEvent -> {
            circle.setTranslateX(mouseEvent.getSceneX() - circleRadius);
            circle.setTranslateY(mouseEvent.getSceneY() - circleRadius);
        });
        flowPane.getChildren().add(circle);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}