/*
 * Created on 11.03.2019
 *
 */
package de.swingempire.fx.chart;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 * StackedBarChart seems to have problems with negative values
 * not showing at all
 * 
 * earlier questions: 
 * https://stackoverflow.com/q/41902008/203657
 * at least they were showing, though not as expected
 * 
 * https://stackoverflow.com/q/15410153/203657
 * has answer with custom stacked chart, pointing to github
 * old issue: https://bugs.openjdk.java.net/browse/JDK-8114956
 * reported as fixed in fx8
 * 
 * fixed issue: not showing if lower y bound > 0
 * https://bugs.openjdk.java.net/browse/JDK-8097159 
 * 
 * @author Jeanette Winzenburg, Berlin
 * @see Statistics
 */
public class JavaGraphTest extends Application {

    @Override
    public void start(Stage stage) {
        try {

            stage.setTitle("Lotto Tracking Graph");

            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            final StackedBarChart<String,Number> bc =   new StackedBarChart<>(xAxis,yAxis);
//            final BarChart<String,Number> bc =   new BarChart<>(xAxis,yAxis);
            
            Scene scene = new Scene(bc, 800,600);

            yAxis.setLabel("Count");

            yAxis.setAutoRanging(false);
            yAxis.setForceZeroInRange(false);

 // The is the only change made

    // working         
//            yAxis.setLowerBound(-10.0);
//     working          
//            yAxis.setLowerBound(0.0);
//     not working      
            yAxis.setLowerBound(5.0);



            yAxis.setUpperBound(60);

            XYChart.Series<String, Number> series1 = new XYChart.Series<>();
            XYChart.Series<String, Number> series2 = new XYChart.Series<>();

            Integer x, y=-6;
            for(x = 1; x<55 ; x++) {

                series1.getData().add(new XYChart.Data<String, Number>(x.toString(), x + y));
                series2.getData().add(new XYChart.Data<String, Number>(x.toString(),  y));

            };
            bc.getData().addAll(series2);
            bc.getData().addAll(series1);
            stage.setScene(scene);
//            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}

