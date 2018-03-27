/*
 * Created on 27.03.2018
 *
 */
package de.swingempire.fx.scene.control;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Second last tick label not shown
 * https://stackoverflow.com/q/49511856/203657
 * 
 * size-restriction: resize to min, then not enough space available
 */
public class SliderLabelNotShown extends Application
{

    @Override
    public void start(Stage stage)
    {

        HBox box = new HBox();
        PionnenSlider slider = new PionnenSlider();
        box.getChildren().add(slider);
        Scene scene = new Scene(box, 500, 300);
        stage.setScene(scene);
        stage.setTitle("SliderMinimalCode");
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.show();
    }

    public class PionnenSlider extends Slider
    {

        public PionnenSlider()
        {
            setMinorTickCount(0);
            setBlockIncrement(1);
            setMajorTickUnit(1);  

            setValue(0);
            setMax(10);
            setShowTickMarks(true);
            setShowTickLabels(true);
            setSnapToTicks(true);

        }
    }


    public static void main(String[] args)
    {
        launch(args);
    }

}

