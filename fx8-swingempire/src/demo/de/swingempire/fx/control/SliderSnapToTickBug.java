/*
 * Created on 03.12.2015
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
public class SliderSnapToTickBug extends Application {

    double lowerBound = 5;
    double upperBound = 25;
    double initialValue = 15;
    @Override
    public void start(Stage primaryStage) {
        Slider slider = new Slider(5, 25, 15);
        // show ticks
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(10);
        
        Label valueLabel = new Label();
        valueLabel.textProperty().bind(slider.valueProperty().asString());
        
        Button snapTicks = new Button("snap to ticks");
        snapTicks.setOnAction(e -> {
            slider.setSnapToTicks(!slider.isSnapToTicks());
        });
        VBox root = new VBox(10, slider, valueLabel, snapTicks);
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();
    }

  
    public static void main(String[] args) {
        launch(args);
    }
    
}