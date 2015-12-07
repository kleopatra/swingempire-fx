/*
 * Created on 03.12.2015
 *
 */
package de.swingempire.fx.control;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import com.sun.javafx.scene.control.skin.SliderSkin;

import de.swingempire.fx.scene.control.slider.XSliderSkin;

/**
 * http://stackoverflow.com/a/34057532/203657
 * convert mouse location to value - not really easy!
 * 
 * This is the solution by James_D, very good! Except not working
 * without one of ticks/labels.
 * 
 * Both using axis/thumb have a slight offset against value. 
 * For axis, that's caused by a bug in core
 * <li> not adjusting for offset (aka: radius) in mouseHandler.
 * 
 * Using track (instead of axis) can't work because we don't know the
 * internal layout.
 */
public class TooltipOnSlider extends Application {

    private boolean useAxis;
    private boolean started;
    @Override
    public void start(Stage primaryStage) {
        Slider slider = new Slider(5, 25, 15);
        useAxis = true;
        // force an axis to be used
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
//        slider.setSnapToTicks(true); 
        slider.setMajorTickUnit(5);
        
//        slider.setOrientation(Orientation.VERTICAL);
        // hacking around the bugs in a custom skin
//        slider.setSkin(new MySliderSkin(slider));
        slider.setSkin(new XSliderSkin(slider));
        
        Label label = new Label();
        Popup popup = new Popup();
        popup.getContent().add(label);

        double offset = 30 ;

        slider.setOnMouseMoved(e -> {
            showValue(slider, e, label, popup, offset);
        });

        slider.setOnMouseDragged(e -> {
            showValue(slider, e, label, popup, offset);
        });

        slider.setOnMouseEntered(e -> popup.show(slider, e.getScreenX(), e.getScreenY() + offset));
        slider.setOnMouseExited(e -> popup.hide());

        Label valueLabel = new Label("empty");
        valueLabel.textProperty().bind(slider.valueProperty().asString());
//        BorderPane root = new BorderPane(slider);
//        root.setBottom(valueLabel);
        VBox root = new VBox(slider, valueLabel);
        Label pixelForValue = new Label("pixel: ");
        Button show = new Button("show pixelForValue");
        Task task = new Task() {

            @Override
            protected Object call() throws Exception {
                for (int i = 0; i < 10000; i++) {
                    updateValue(i);
//                    LOG.info("running " + i);
                    Thread.sleep(1000);
                }
                return null;
            }
            
        };
        task.valueProperty().addListener((s, o, nv) -> {
          NumberAxis axis = (NumberAxis) slider.lookup(".axis");
          double pix = axis.getDisplayPosition(slider.getValue());
          LOG.info("" + primaryStage.isMaximized() + " " + pix);
//          pixelForValue.setText("pixel: " + pix); 
////            pixelForValue.setText("" + nv);
        });
        primaryStage.maximizedProperty().addListener(o -> {
            NumberAxis axis = (NumberAxis) slider.lookup(".axis");
            double pix = axis.getDisplayPosition(slider.getValue());
            LOG.info("max changed: " + pix);
            if (show.isDisable()) return;
            show.setDisable(true);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        });
        show.setOnAction(e -> {
            show.setDisable(true);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
//            NumberAxis axis = (NumberAxis) slider.lookup(".axis");
//            double pix = axis.getDisplayPosition(slider.getValue());
//            pixelForValue.setText("pixel: " + pix); 
        });
        HBox view = new HBox(show, pixelForValue);
//        BorderPane view = new BorderPane(pixelForValue);
//        view.setLeft(show);
        root.getChildren().add(view);
//        DoubleProperty countProperty = (DoubleProperty)slider.getProperties().get("count");
//        if (countProperty != null) {
//            layoutCount.textProperty().bind(countProperty.asString());
//            root.setTop(layoutCount);  
//        }
        primaryStage.setScene(new Scene(root, 350, 100));
        primaryStage.show();
        primaryStage.setTitle("useAxis: " + useAxis + " mySkin: " + slider.getSkin().getClass().getSimpleName());
    }

    protected void showValue(Slider slider, MouseEvent e, Label label,
            Popup popup, double offset) {
        NumberAxis axis = (NumberAxis) slider.lookup(".axis");
        StackPane track = (StackPane) slider.lookup(".track");
        StackPane thumb = (StackPane) slider.lookup(".thumb");
        if (useAxis) {
            // James: use axis to convert value/position
            Point2D locationInAxis = axis.sceneToLocal(e.getSceneX(), e.getSceneY());
            boolean isHorizontal = slider.getOrientation() == Orientation.HORIZONTAL;
            double mouseX = isHorizontal ? locationInAxis.getX() : locationInAxis.getY() ;
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
    }

    
    /**
     * Trying to work around down to the slight offset.
     */
    public static class MySliderSkin extends SliderSkin {
        
        /**
         * Hook for replacing the mouse pressed handler that's installed by super.
         */
        protected void installListeners() {
            StackPane track = (StackPane) getSkinnable().lookup(".track");
            track.setOnMousePressed(me -> {
                invokeSetField("trackClicked", true);
                double trackLength = invokeGetField("trackLength");
                double trackStart = invokeGetField("trackStart");
                // convert coordinates into slider
                MouseEvent e = me.copyFor(getSkinnable(), getSkinnable());
                double mouseX = e.getX(); 
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