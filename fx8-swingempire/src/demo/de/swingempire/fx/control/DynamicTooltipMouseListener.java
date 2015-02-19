/*
 * Created on 17.02.2015
 *
 */
package de.swingempire.fx.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.stage.Stage;
import de.swingempire.fx.util.FXUtils;

/**
 * Tooltip that dynamically updates its text with mouse coordinates.
 * 
 * A: approximation - set text on isShowing to anchor
 *  
 * @author Jeanette Winzenburg, Berlin
 */
public class DynamicTooltipMouseListener extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        VBox pane = new VBox();
        pane.getChildren().addAll(createControls());
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
        stage.setTitle(FXUtils.version());
    }

    protected List<Control> createControls() {
        List<Control> controls = new ArrayList<>();
        controls.add(createButton(null));
        Arrays.stream(AnchorLocation.values()).forEach(p -> controls.add(createButton(p)));
        return controls;
    }
    protected Button createButton(AnchorLocation location) {
        Tooltip t = new Tooltip("button");
        String text = location != null ? location.toString() : t.getAnchorLocation().toString() + " (default)";
        if (location != null) {
            t.setAnchorLocation(location);
        }
        t.setOnShowing(e -> {
            // side-effect: tooltip hidden/shown
//            t.setText("x/y: " + t.getX() + "/" + t.getY());
            // here we get a stable tooltip
//            t.textProperty().set("x/y: " + t.getX() + "/" + t.getY() + "\n" +
//                    "ax/y: " + t.getAnchorX() + "/" + t.getAnchorY());
        });
        t.setOnShown(e -> {
            // side-effect: tooltip hidden/shown
//            t.setText("x/y: " + t.getX() + "/" + t.getY());
            // here we get a stable tooltip
            t.textProperty().set("x/y: " + t.getX() + "/" + t.getY() + "\n" +
                    "ax/y: " + t.getAnchorX() + "/" + t.getAnchorY());
            LOG.info("bounds on shown " +"x/y: " + t.getX() + "/" + t.getY() + "\n       " +
                    "ax/y: " + (t.getAnchorX() - 10) + "/" + (t.getAnchorY() - 7));
        });
        Button button = new Button(text);
        button.setTooltip(t);
        button.setOnContextMenuRequested(e -> {
            LOG.info("context: " + text + "\n      " +
                    "scene/screen/source " + e.getSceneX() + " / " + e.getScreenX() + " / " + e.getX());
        });
        button.setOnMouseMoved(e -> {
            LOG.info("moved: " + text + "\n      " +
            "scene/screen/source " + e.getSceneX() + " / " + e.getScreenX() + " / " + e.getX());
        });
        return button;
    }
    
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DynamicTooltipMouseListener.class.getName());
}
