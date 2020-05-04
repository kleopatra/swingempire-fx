/*
 * Created on 27.04.2020
 *
 */
package de.swingempire.fx.graphic;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.PickResult;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/61433210/203657
 * check if click happens on stroke of shape
 * 
 */
public class ClickOnStroke extends Application {

    @Override
    public void start(Stage primaryStage) {
        Arc arc1 = new Arc(100, 100, 100, 100, 90, 90);
        Arc arc2 = new Arc(0, 100, 100, 100, 0, 90);

        arc1.setStroke(Color.BLACK);
        arc2.setStroke(Color.GRAY);
        arc1.setUserData("arc1");
        arc2.setUserData("arc2");
        arc1.setFill(Color.TRANSPARENT);
        arc2.setFill(Color.TRANSPARENT);
        arc1.setStrokeWidth(10);
        arc2.setStrokeWidth(10);
        arc1.setType(ArcType.OPEN);
        arc2.setType(ArcType.OPEN);

        arc1.setPickOnBounds(false);
        arc2.setPickOnBounds(false);

//        arc1.setOnMouseClicked(event -> System.out.println("arc1"));
//        arc2.setOnMouseClicked(event -> System.out.println("arc2"));

        installHandler(arc1);
        installHandler(arc2);
      StackPane root = new StackPane();
      root.getChildren().add(arc1);
      root.getChildren().add(arc2);

      Scene scene = new Scene(root, 300, 250);

      primaryStage.setTitle("Picking Issues");
      primaryStage.setScene(scene);
      primaryStage.show();
    }

    protected void installHandler(Shape shape) {
        shape.setOnMouseClicked(e -> {
            double x = e.getX();
            double y = e.getY();
            PickResult pick = e.getPickResult();
            System.out.println(shape.getUserData() + " : " + shape.contains(x, y));
            
        });
    }
    public static void main(String[] args) {
        launch(args);
    }
}

