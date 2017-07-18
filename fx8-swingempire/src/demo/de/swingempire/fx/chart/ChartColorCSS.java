/*
 * Created on 27.01.2015
 *
 */
package de.swingempire.fx.chart;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Regression: CHART_COLOR_XX not accessible in 8u40b20, 
 * works fine in 8u20
 * 
 * Reported: https://javafx-jira.kenai.com/browse/RT-39899
 * Might be intentional, as CHART_COLOR_XX isn't specified.
 * 
 * Evaluated as bug, marked as fixed for 8u60
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChartColorCSS extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Rectangle r = new Rectangle(300, 400);
        Button button = new Button("style");
        button.setOnAction(e -> 
            r.setStyle("-fx-fill:CHART_COLOR_1;"));
        HBox root = new HBox(r, button);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
//        primaryStage.setTitle(System.getProperty("java.version"));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }
    
}
