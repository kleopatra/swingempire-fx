/*
 * Created on 01.02.2016
 *
 */
package de.swingempire.fx.control;

import de.swingempire.fx.scene.control.slider.XSliderSkin;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

/**
 * How to get notified if value snapped into min/max?
 * Problem if mouse had been dragged.
 * 
 * http://stackoverflow.com/q/35129670/203657
 * 
 * seems to be working in fx9
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SliderExample extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        Slider slider = new Slider(0.25, 2.0, 1.0);
//        slider.setSkin(new XSliderSkin(slider));
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.25);
        slider.setMinorTickCount(0);

        slider.setSnapToTicks(true);    // !!!!!!!!!!
        
        slider.valueProperty().addListener( n -> {
            if (!slider.isValueChanging()) {
                System.err.println(n);
            }
        });

        slider.valueChangingProperty().addListener( (prop, oldVal, newVal) -> {
            // NOT the final value when newVal == false!!!!!!!
            if (!newVal) {
                System.err.println(prop + "/" + oldVal + "/" + newVal); 
                System.err.println("committed value: " + slider.getValue());
            }
         });
        Scene scene = new Scene(slider, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
   }
}