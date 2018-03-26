/*
 * Created on 28.09.2016
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

/**
 * Reported:
 * https://bugs.openjdk.java.net/browse/JDK-8166832
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class BugAcceleratorNotNode extends Application {

    private Parent getContent() {
        
        ContextMenu circleMenu = new ContextMenu();
        MenuItem circleItem = new MenuItem("in Circle");
        circleMenu.getItems().add(circleItem);
        circleItem.setAccelerator(KeyCombination.valueOf("F9"));
        circleItem.setOnAction(e -> LOG.info("action in Circle"));
        Shape circle = new Circle(200);
        circle.setOnContextMenuRequested(e -> {
            LOG.info("requested: " + e);
            circleMenu.show(circle, e.getScreenX(), e.getScreenY());
        });
        // uncomment to make accelerator work
        //ControlAcceleratorSupport.addAcceleratorsIntoScene(circleMenu.getItems(), circle);

        BorderPane pane = new BorderPane(circle);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 600, 400));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(BugAcceleratorNotNode.class.getName());
}
