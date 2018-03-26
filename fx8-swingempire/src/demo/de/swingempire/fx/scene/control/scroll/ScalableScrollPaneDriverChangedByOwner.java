/*
 * Created on 04.12.2017
 *
 */
package de.swingempire.fx.scene.control.scroll;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

/**
 * Detect when node leaves visible part of scrollPane
 * https://stackoverflow.com/q/47622660/203657
 * 
 * 
 * q deleted by owner ... 
 * 
 * this is the last version before deletion, basically removing the
 * listener and update the content scaling on mouseReleased plus
 * scaling the content, not the zoomPane. Looks okay to me
 * 
 * descr of requirement in comment:
 * I'm trying to create a ScrollPane which scales it's content, 
 * so that the full viewport boundaries are used. Moreover I'd like 
 * to center the content horizontally and vertically. 
 * The original question arose, as I tried to recompute the scale factor 
 * as soon as the a node leaves the viewport. With this in place, the 
 * ScrollPane would recompute its content when needed
 * 
 * @see ScalableScrollPaneDriver
 */
public class ScalableScrollPaneDriverChangedByOwner extends Application {

    private double prevSceneX;
    private double prevSceneY;

    @Override
    public void start(Stage primaryStage) {
      try {
        ScalableScrollPane pane = new ScalableScrollPane();
        Circle circle = new Circle(20, 40, 20);
        circle.setFill(Color.BLUE);
        Circle circle2 = new Circle(100, 150, 20);

        pane.addNodes(circle, circle2);

        addDragSupport(circle, pane);
        addDragSupport(circle2, pane);

        Scene scene = new Scene(pane, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    public static void main(String[] args) {
      launch(args);
    }

    private void addDragSupport(Circle node, ScalableScrollPane graphPane) {


            node.setOnMousePressed(e -> {
              double scale = graphPane.getScaleValue();

              double eventX = e.getSceneX();
              double eventY = e.getSceneY();

              double zommPaneX = node.getBoundsInParent().getMinX();
              double zoomPaneY = node.getBoundsInParent().getMinY();

              prevSceneX = (zommPaneX * scale) - eventX;
              prevSceneY = (zoomPaneY * scale) - eventY;

              e.consume();
            });

            node.setOnMouseDragged(e -> {

              double eventX = e.getSceneX();
              double eventY = e.getSceneY();

              double scale = graphPane.getScaleValue();

              double newX = (eventX + prevSceneX) / scale;
              double newY = (eventY + prevSceneY) / scale;

              node.relocate(newX, newY);

              e.consume();
            });

            node.setOnMouseReleased(e -> {
              graphPane.zoomToContent();
            });
          }
//      node.setOnMouseReleased(e -> {
//          DebugUtils.addBoundsInParent(node.getParent(), node);
//      });
    
    public static class ScalableScrollPane extends ScrollPane {

        private final StackPane centerPane;
        private final Group zoomGroup;
        private final Scale scaleTransform;

        private static final int MAX_SCALE = 4;

        public ScalableScrollPane() {

          setPadding(new Insets(20));
          setStyle("-fx-background-color: blue");

//          setFitToHeight(true);
          scaleTransform = new Scale(1.0, 1.0, 0, 0);

          zoomGroup = new Group();

          centerPane = new StackPane();
          centerPane.getTransforms().add(scaleTransform);
          centerPane.setStyle("-fx-background-color: aliceblue");
          centerPane.getChildren().add(zoomGroup);
          setContent(centerPane);
        }

        public void zoomToContent() {
          double contentWidth = zoomGroup.getBoundsInLocal().getWidth();
          double contentHeight = zoomGroup.getBoundsInLocal().getHeight();

          if (contentWidth > 0 && contentHeight > 0) {

            double viewPortWidth = getViewportBounds().getWidth();
            double viewPortHeight = getViewportBounds().getHeight();

            double scaleX = viewPortWidth / contentWidth;
            double scaleY = viewPortHeight / contentHeight;

            double scale = Math.min(scaleX, scaleY);
            scale = Math.min(scale, MAX_SCALE);

            scaleTransform.setX(scale);
            scaleTransform.setY(scale);
          }
        }

        public void addNodes(Node... nodes) {
          zoomGroup.getChildren().addAll(nodes);
        }

        public void clear() {
          zoomGroup.getChildren().clear();
        }

        public double getScaleValue() {
          return scaleTransform.getX();
        }
 }
}

