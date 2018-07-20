/*
 * Created on 01.02.2016
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.Slider;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * How to get notified if value snapped into min/max?
 * Problem if mouse had been dragged.
 * 
 * http://stackoverflow.com/q/35129670/203657
 * 
 * seems to be working in fx9 - no, none of the listeners is reliable:
 * - listening to isValueChanging is fine for the boundary values, incorrect for
 *   intermediate (returns the value before snapping)
 * - listening to value (either via Invalidation or Change) isn't notified
 *   on boundary but gives correct values in-between  
 * 
 * new question: https://stackoverflow.com/q/51089812/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SliderExample extends Application {
    public static void main(String[] args) { launch(args); }

    public static class MSliderSkin extends SliderSkin {

        Node thumbAlias;
        /**
         * Idea: revert order in thumb.mouseReleased - done in behaviour, which is final
         * field, no way. But then: do the handling manually in custom mouseReleased handler
         * 
         * doesn't help: now we don't get notification on value with state !changing
         * (because we are only interested in those)
         * 
         */
        public MSliderSkin(Slider control) {
            super(control);
            thumbAlias = control.lookup(".thumb");
            thumbAlias.setOnMouseReleased(this::thumbReleased);
        }
        
        private void thumbReleased(MouseEvent me) {
            final Slider slider = getSkinnable();
            // RT-15207 When snapToTicks is true, slider value calculated in drag
            // is then snapped to the nearest tick on mouse release.
            slider.adjustValue(slider.getValue());
            slider.setValueChanging(false);

        }
        
    }
    @Override
    public void start(Stage primaryStage) {
        Slider slider = new Slider(0.25, 2.0, 1.0) {

//            @Override
//            protected Skin<?> createDefaultSkin() {
//                return new MSliderSkin(this);
//            }
            
        };
//        slider.setSkin(new XSliderSkin(slider));
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.25);
        slider.setMinorTickCount(0);

        slider.setSnapToTicks(true);    // !!!!!!!!!!
        
        slider.valueProperty().addListener( n -> {
            if (!slider.isValueChanging()) {
//                System.err.println("valueInvalid: " + ((ObservableValue) n).getValue());
            }
        });

        slider.valueProperty().addListener((src, ov, nv) -> {
            if (!slider.isValueChanging()) {
              System.err.println("valueChange: " + nv);
          }
            
        });
        slider.valueChangingProperty().addListener( (prop, oldVal, newVal) -> {
            // NOT the final value when newVal == false!!!!!!!
            if (!newVal) {
//                System.err.println("changing: " + "/" + oldVal + "/" + newVal); 
                System.err.println("committed value: " + slider.getValue());
            }
         });
        Scene scene = new Scene(slider, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
   }
}