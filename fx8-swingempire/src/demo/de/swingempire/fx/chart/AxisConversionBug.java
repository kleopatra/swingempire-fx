/*
 * Created on 08.12.2015
 *
 */
package de.swingempire.fx.chart;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;


/**
 * Bug report:
 * https://bugs.openjdk.java.net/browse/JDK-8144920
 * 
 * Press F1 to change size then look at printout:
 * 
 * expected: 
 * 1. "after" different from before, 
 * 2. "after" should be half of the current width of the axis
 *
 * actual:
 * after same as before
 *
 * 
 * 
 * still open as of 9ea-175
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AxisConversionBug extends Application {

    private Parent getContent() {
        NumberAxis axis = new NumberAxis(0, 100, 25);
        Region region = new Region() {
            {
                
                getChildren().add(axis);
            }
        };
        BorderPane pane = new BorderPane(region);
        axis.setManaged(false);
        double initial = 100;
        axis.resize(initial, axis.getPrefHeight());
        axis.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F1) {
                
                double positionBefore = axis.getDisplayPosition(50);
                double axisWidth = axis.getWidth() < region.getWidth() 
                        ? region.getWidth() : initial;
                axis.resize(axisWidth, axis.getHeight());
                // following doen't make a difference
                // axis.requestAxisLayout();
                // region.requestLayout();
                // workaround suggested by Vadim in bug report
                // works fine
//                axis.layout();
                double positionAfter = axis.getDisplayPosition(50);
                LOG.info("position before/after resize: " + positionBefore + 
                        " / " + positionAfter + " width: " + axis.getWidth()) ;
            }
        });
        axis.setFocusTraversable(true);
        axis.requestFocus();
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 500, 200));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
 
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(AxisInvalidate.class
            .getName());

}
