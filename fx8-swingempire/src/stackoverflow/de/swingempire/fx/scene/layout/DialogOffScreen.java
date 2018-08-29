/*
 * Created on 29.08.2018
 *
 */
package de.swingempire.fx.scene.layout;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52062291/203657
 * 
 * Dialog shown partly off screen
 * listen to showingProperty and do size adjustments when notified
 * 
 * #see DialogDebugSize
 */
public class DialogOffScreen extends Application {

    @Override
    public void start(Stage primaryStage) {
        Bounds bounds = computeAllScreenBounds();
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("This is an alert!");
                alert.setContentText(
                        "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
                alert.initOwner(primaryStage);

                alert.showingProperty().addListener((src, ov, nv) -> {

                    double x = alert.getX();
                    double y = alert.getY();
                    double w = alert.getWidth();
                    double h = alert.getHeight();
                    if (x < bounds.getMinX()) {
                        alert.setX(bounds.getMinX());
                    }
                    if (x + w > bounds.getMaxX()) {
                        alert.setX(bounds.getMaxX() - w);
                    }
                    if (y < bounds.getMinY()) {
                        alert.setY(bounds.getMinY());
                    }
                    if (y + h > bounds.getMaxY()) {
                        alert.setY(bounds.getMaxY() - h);
                    }
                });
                alert.showAndWait();

            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private static Bounds computeAllScreenBounds() {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D screenBounds = screen.getBounds();
            if (screenBounds.getMinX() < minX) {
                minX = screenBounds.getMinX();
            }
            if (screenBounds.getMinY() < minY) {
                minY = screenBounds.getMinY();
            }
            if (screenBounds.getMaxX() > maxX) {
                maxX = screenBounds.getMaxX();
            }
            if (screenBounds.getMaxY() > maxY) {
                maxY = screenBounds.getMaxY();
            }
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

}