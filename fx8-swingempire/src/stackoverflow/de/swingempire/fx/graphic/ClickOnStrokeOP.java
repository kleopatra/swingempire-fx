/*
 * Created on 27.04.2020
 *
 */
package de.swingempire.fx.graphic;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;

/**
 * Solution by OP
 * https://stackoverflow.com/q/61433210/203657
 * check if click happens on stroke of shape
 * 
 * doesn't survive resizing
 */
public class ClickOnStrokeOP extends Application {

    private  Arc arc1, arc2;
    private StackPane root;

  @Override
  public void start(Stage primaryStage) {
      arc1 = getArc(200, 200, 200, 200, 90, 90, "arc1");
      arc2 = getArc(0, 200, 200, 200, 0, 90, "arc2");
      root = new StackPane(arc1, arc2);
      root.setOnMouseClicked(event -> onStackPaneClick(event.getX(), event.getY()));
      Scene scene = new Scene(root, 200, 200);
      primaryStage.setScene(scene);
      primaryStage.show();
  }

  private Arc getArc(double x, double y,  double radiusX, double radiusY, double startAngle, double endAngle, String userData){    
      Arc arc = new Arc(x,y, radiusX,radiusY,startAngle, endAngle);
      arc.setStroke(Color.BLACK);
      arc.setFill(Color.TRANSPARENT);
      arc.setStrokeWidth(10);
      arc.setType(ArcType.OPEN);
      arc.setPickOnBounds(false);
      arc.setUserData(userData);
      return arc;
  }

  private void onStackPaneClick(double x, double y) {
    for (Node itNode : root.getChildren()) {
        if (itNode.contains(new Point2D(x, y))) {
            if (itNode instanceof Arc) {
                Arc itArc = (Arc) itNode;
                double centerDistance = Math.sqrt(
                        (x - itArc.getCenterX()) * (x - itArc.getCenterX()) +
                        (y - itArc.getCenterY()) * (y - itArc.getCenterY()));
                if (centerDistance >= itArc.getRadiusX() - itArc.getStrokeWidth() / 2) {
                    System.out.println(itArc.getUserData());
                    break;
                }
            }
        }
    }
  }

  public static void main(String[] args) {
      launch(args);
  }
}