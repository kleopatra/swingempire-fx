/*
 * Created on 17.02.2015
 *
 */
package de.swingempire.fx.control;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.stage.Stage;

public class DynamicTooltipMouseLocation extends Application {

    private static final Object MOUSE_TRIGGER_LOCATION = "tooltip-last-location";

    protected Button createButton(AnchorLocation location) {
        Tooltip t = new Tooltip("");
        String text = location != null ? location.toString() 
                : t.getAnchorLocation().toString() + " (default)";
        if (location != null) {
            t.setAnchorLocation(location);
        }
        t.setOnShown(e -> {
            // here we get a stable tooltip
            t.textProperty().set("x/y: " + t.getX() + "/" + t.getY() + "\n" +
                    "stored: " + t.getProperties().get(MOUSE_TRIGGER_LOCATION) + "\n" +
                    "ax/y: " + t.getAnchorX() + "/" + t.getAnchorY());
        });
        Button button = new Button(text);
        button.setTooltip(t);
        button.setOnContextMenuRequested(e -> {
            LOG.info("context: " + text + "\n      " +
                    "scene/screen/source " + e.getSceneX() + " / " + e.getScreenX() + " / " + e.getX());
        });
        button.setOnMouseMoved(e -> {
            Point2D screen = new Point2D(e.getScreenX(), e.getScreenY());
            t.getProperties().put(MOUSE_TRIGGER_LOCATION, screen);
            LOG.info("moved: " + text + "\n      " +
            "scene/screen/source " + e.getSceneX() + " / " + e.getScreenX() + " / " + e.getX());
        });
        return button;
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        VBox pane = new VBox(createButton(AnchorLocation.CONTENT_TOP_LEFT));
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DynamicTooltipMouseLocation.class.getName());
}
