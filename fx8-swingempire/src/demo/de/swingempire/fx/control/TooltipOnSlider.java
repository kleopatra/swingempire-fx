/*
 * Created on 03.12.2015
 *
 */
package de.swingempire.fx.control;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;

import com.sun.javafx.scene.control.skin.SliderSkin;

/**
 * http://stackoverflow.com/a/34057532/203657
 * convert mouse location to value - not really easy!
 * 
 * This is the solution by James_D, very good! Except not working
 * without one of ticks/labels.
 * 
 * Both using axis/thumb have a slight offset against value. 
 * For axis, that's caused by a bug in core which is twofold:
 * <li> Incorrect calc of tracklength in SliderSkin.layoutChildren:
 * adjusts to thumbWidth when it means trackRadius. 
 * <li> not adjusting for offset (aka: radius) in mouseHandler.
 * Using track can't work.
 */
public class TooltipOnSlider extends Application {

    private boolean useAxis;
    @Override
    public void start(Stage primaryStage) {
        Slider slider = new Slider(5, 25, 15);
        useAxis = true;
        // force an axis to be used
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setSnapToTicks(false); 
        slider.setMajorTickUnit(5);
        
        // hacking around the bugs in a custom skin
//        slider.setSkin(new MySliderSkin(slider));

        Label label = new Label();
        Popup popup = new Popup();
        popup.getContent().add(label);

        double offset = 30 ;

        slider.setOnMouseMoved(e -> {
            NumberAxis axis = (NumberAxis) slider.lookup(".axis");
            StackPane track = (StackPane) slider.lookup(".track");
            StackPane thumb = (StackPane) slider.lookup(".thumb");
            if (useAxis) {
                // James: use axis to convert value/position
                Point2D locationInAxis = axis.sceneToLocal(e.getSceneX(), e.getSceneY());
                double mouseX = locationInAxis.getX() ;
                double value = axis.getValueForDisplay(mouseX).doubleValue() ;
                if (value >= slider.getMin() && value <= slider.getMax()) {
                    label.setText("" + value);
                } else {
                    label.setText("Value: ---");
                }
                
            } else {
                // this can't work because we don't know the internals of the track
                Point2D locationInAxis = track.sceneToLocal(e.getSceneX(), e.getSceneY());
                double mouseX = locationInAxis.getX();
                double trackLength = track.getWidth();
                double percent = mouseX / trackLength;
                double value = slider.getMin() + ((slider.getMax() - slider.getMin()) * percent);
                if (value >= slider.getMin() && value <= slider.getMax()) {
                    label.setText("" + value);
                } else {
                    label.setText("Value: ---");
                }
            }
            popup.setAnchorX(e.getScreenX());
            popup.setAnchorY(e.getScreenY() + offset);
        });

        slider.setOnMouseEntered(e -> popup.show(slider, e.getScreenX(), e.getScreenY() + offset));
        slider.setOnMouseExited(e -> popup.hide());

        Label valueLabel = new Label("empty");
        valueLabel.textProperty().bind(slider.valueProperty().asString());
        BorderPane root = new BorderPane(slider);
        root.setBottom(valueLabel);
        primaryStage.setScene(new Scene(root, 350, 100));
        primaryStage.show();
        primaryStage.setTitle("useAxis: " + useAxis + " mySkin: " + (slider.getSkin() instanceof MySliderSkin));
    }

    
    /**
     * Trying to get down to the slight offset.
     */
    public static class MySliderSkin extends SliderSkin {

        // hard-copy of hard-coded value in super
        double trackToTickGap = 2;
        
        /**
         * Hook for replacing mouseListener installed by super.
         * Here only doing so for mousePressed on track.
         */
        protected void installListeners() {
            StackPane track = (StackPane) getSkinnable().lookup(".track");
            track.setOnMousePressed(e -> {
                invokeSetField("trackClicked", true);
                // original:
                // incorrect because radius not taken into account
                //getBehavior().trackPress(e, (e.getX() / trackLength));

                double trackLength = invokeGetField("trackLength");
                double trackStart = invokeGetField("trackStart");
                double mouseX = snapPosition(e.getX());
                double position;
                if (mouseX < trackStart) {
                    position = 0;
                } else if (mouseX > trackStart + trackLength) {
                    position = 1;
                } else {
                   position = (mouseX - trackStart) / trackLength;
                }
                getBehavior().trackPress(e, position);
                invokeSetField("trackClicked", false);
            });
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            StackPane thumb = (StackPane) getSkinnable().lookup(".thumb");
            StackPane track = (StackPane) getSkinnable().lookup(".track");
            NumberAxis tickLine = (NumberAxis) getSkinnable().lookup(".axis");
            boolean showTickMarks = getSkinnable().isShowTickLabels() || getSkinnable().isShowTickMarks();
            // calculate the available space
            // resize thumb to preferred size
            double thumbWidth = snapSize(thumb.prefWidth(-1));
            invokeSetField("thumbWidth", thumbWidth);
            double thumbHeight = snapSize(thumb.prefHeight(-1));
            invokeSetField("thumbHeight", thumbHeight);
            thumb.resize(thumbWidth, thumbHeight);
            // we are assuming the is common radius's for all corners on the track
            double trackRadius = getTrackRadius(track);
            if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
                double tickLineHeight =  (showTickMarks) ? tickLine.prefHeight(-1) : 0;
                double trackHeight = snapSize(track.prefHeight(-1));
                double trackAreaHeight = Math.max(trackHeight,thumbHeight);
                double totalHeightNeeded = trackAreaHeight  + ((showTickMarks) ? trackToTickGap+tickLineHeight : 0);
                double startY = y + ((h - totalHeightNeeded)/2); // center slider in available height vertically
                // original: incorrect tracklength
                // double trackLength = snapSize(w - thumbWidth);
                double trackLength = snapSize(w - 2 * trackRadius);
                invokeSetField("trackLength", trackLength);
                // original: incorrect track offset
                // double trackStart = snapPosition(x + (thumbWidth/2));
                double trackStart = snapPosition(x + trackRadius);
                invokeSetField("trackStart", trackStart);
                double trackTop = (int)(startY + ((trackAreaHeight-trackHeight)/2));
                double thumbTop = (int)(startY + ((trackAreaHeight-thumbHeight)/2));
                invokeSetField("thumbTop", thumbTop);
                
                // positionThumb(false);
                invokeMethod("positionThumb", false);
                // layout track - original ... possible but unnecessarily complex
                // it's filling the complete width 
                track.resizeRelocate((int)(trackStart - trackRadius),
                        trackTop ,
                        (int)(trackLength + trackRadius + trackRadius),
                        trackHeight);
                // simplifying seems to work
                //track.resizeRelocate((int)(x),
                //          trackTop ,
                //          (int)(w),
                //          trackHeight);
                // layout tick line
                if (showTickMarks) {
                    tickLine.setLayoutX(trackStart);
                    tickLine.setLayoutY(trackTop+trackHeight+trackToTickGap);
                    tickLine.resize(trackLength, tickLineHeight);
                    tickLine.requestAxisLayout();
                } else {
                    if (tickLine != null) {
                        tickLine.resize(0,0);
                        tickLine.requestAxisLayout();
                    }
                    tickLine = null;
                }
            } else {
                super.layoutChildren(x, y, w, h);
            }
        }

        /**
         * Calc the offset of track start (extracted for convience).
         */
        protected double getTrackRadius(StackPane track) {
            double trackRadius = track.getBackground() == null ? 0 : track.getBackground().getFills().size() > 0 ?
                    track.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius() : 0;
            return trackRadius;
        }

        //------from here: hacking around scope of super fields/methods
        private void invokeMethod(String string, boolean b) {
            Class clazz = SliderSkin.class;
            try {
                Method method = clazz.getDeclaredMethod(string, Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(this, b);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        private double invokeGetField(String name) {
            Class clazz = SliderSkin.class;
            Field field;
            try {
                field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field.getDouble(this);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return 0.;
        }
        
        private void invokeSetField(String name, Object value) {
            Class clazz = SliderSkin.class;
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                field.set(this, value);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        /**
         * Constructor - replaces listener on track.
         * @param slider
         */
        public MySliderSkin(Slider slider) {
            super(slider);
            installListeners();
        }
        
    }
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TooltipOnSlider.class
            .getName());
}