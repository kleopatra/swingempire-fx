/*
 * Created on 04.12.2015
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import de.swingempire.fx.control.TooltipOnSlider.MySliderSkin;
import de.swingempire.fx.scene.control.slider.XSliderSkin;

/**
 * Slider: slider value differs from selected real value
 * https://bugs.openjdk.java.net/browse/JDK-8089730
 * 
 * MySliderSkin is better, but not optimal - rounding error?
 */
public class SliderBug_8089730 extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Slider s = new Slider();
//        s.setSkin(new MySliderSkin(s)); 
        s.setSkin(new XSliderSkin(s));
        s.setMinWidth(500.0);
        s.setPrefWidth(500.0);
        s.setMax(200.0);
        s.setMin(-50.0);
        s.setShowTickMarks(true);
        s.setShowTickLabels(true);
        Label lbl = new Label();
        lbl.textProperty().bind(s.valueProperty().asString());

//        HBox root = new HBox(20d);
//        root.getChildren().addAll(s, lbl);
        BorderPane root = new BorderPane(s);
        root.setBottom(lbl);
        Scene scene = new Scene(root); //, 300, 500);

        stage.setScene(scene);
        stage.setTitle(System.getProperty("java.runtime.version") + "; "
                + System.getProperty("javafx.runtime.version"));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
