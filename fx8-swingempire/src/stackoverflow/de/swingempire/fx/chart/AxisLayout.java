/*
 * Created on 21.12.2018
 *
 */
package de.swingempire.fx.chart;

import java.net.URL;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Layout axis such that line can be customized.
 * https://stackoverflow.com/q/53879734/203657
 * 
 * Solution: use a custom slider style
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AxisLayout extends Application {

    private Parent createContent() {
        
        NumberAxis top = new NumberAxis(100, 200, 10);
        top.setSide(Side.TOP);
        
        NumberAxis bottom = new NumberAxis(0, 100, 10);
        bottom.setSide(Side.BOTTOM);
        
        NumberAxis left = new NumberAxis(100, 200, 10);
        left.setSide(Side.LEFT);
        
        NumberAxis right = new NumberAxis(0, 100, 10);
        right.setSide(Side.RIGHT);
        
        BorderPane axisContent = new BorderPane(new Button("dummy"), top, right, bottom, left );
        
        Slider sliderRight = new Slider(0, 100, 10);
        sliderRight.setOrientation(Orientation.VERTICAL);
        sliderRight.setShowTickLabels(true);
        sliderRight.setShowTickMarks(true);
        
        Slider sliderLeft = new Slider(100, 200, 10);
        sliderLeft.setOrientation(Orientation.VERTICAL);
        sliderLeft.setShowTickLabels(true);
        sliderLeft.setShowTickMarks(true);
        sliderLeft.getStyleClass().add("axis-left");
        
        Slider sliderBottom = new Slider(0, 100, 10);
        sliderBottom.setOrientation(Orientation.HORIZONTAL);
        sliderBottom.setShowTickLabels(true);
        sliderBottom.setShowTickMarks(true);
        
        Slider sliderTop = new Slider(100, 200, 10);
        sliderTop.setOrientation(Orientation.HORIZONTAL);
        sliderTop.setShowTickLabels(true);
        sliderTop.setShowTickMarks(true);
        sliderTop.getStyleClass().add("axis-top");
        
        BorderPane sliderContent = new BorderPane(new Button("sliders"), sliderTop, sliderRight, sliderBottom, sliderLeft);
        
        TabPane pane = new TabPane();
        pane.getTabs().addAll(
                new Tab("Slider", sliderContent),
                new Tab("Axis", axisContent)
                );
        
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 400, 400));
        URL uri = getClass().getResource("ruler.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AxisLayout.class.getName());

}
