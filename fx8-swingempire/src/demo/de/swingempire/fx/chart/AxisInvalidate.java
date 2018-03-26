/*
 * Created on 07.12.2015
 *
 */
package de.swingempire.fx.chart;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Description on
 * https://stackoverflow.com/q/34139076/203657
 * 
 * This example now behaves as expected, though the related bug
 * https://bugs.openjdk.java.net/browse/JDK-8144920
 * is not yet fixed as of 9ea-175 (and the example AxisConversionBug still
 * misbehaving)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AxisInvalidate extends Application {

    
    public static class AxisInRegion extends Region {
        NumberAxis axis;
        Control thumb;
        IntegerProperty value = new SimpleIntegerProperty(50);
        
        private double thumbWidth;
        private double thumbHeight;
        
        public AxisInRegion() {
            axis = new NumberAxis(0, 100, 25);
            thumb = new CheckBox();
            getChildren().addAll(axis, thumb);
        }

        @Override
        protected void layoutChildren() {
            thumbWidth = snapSize(thumb.prefWidth(-1));
            thumbHeight = snapSize(thumb.prefHeight(-1));
            thumb.resize(thumbWidth, thumbHeight);
            
            double axisHeight = axis.prefHeight(-1);
            axis.resizeRelocate(0, getHeight() /4, getWidth(), axisHeight);
            // doesn't make a difference, shouldn't be needed anyway
            //axis.requestAxisLayout();
            // hack from bug report:
//            axis.layout();
            // seems to work 9ea-u175 aka sticks to its position
            //without any hack (what was the problem?)
            double pixelOnAxis = axis.getDisplayPosition(value.getValue());
//            Platform.runLater(() -> {
//                LOG.info("diff " + (pixelOnAxis - axis.getDisplayPosition(value.getValue())));
//            });
            thumb.relocate(pixelOnAxis, getHeight() /4);
            
        }
        
    }

    private Parent getContent() {
        AxisInRegion region = new AxisInRegion();
        BorderPane content = new BorderPane(region);
        return content;
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
