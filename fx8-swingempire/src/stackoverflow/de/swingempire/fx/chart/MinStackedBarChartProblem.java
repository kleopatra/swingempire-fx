/*
 * Created on 10.08.2018
 *
 */
package de.swingempire.fx.chart;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 * Doesn't clean up after clearing series data
 * https://stackoverflow.com/q/35397223/203657
 * 
 * fixed in fx10
 */
public class MinStackedBarChartProblem extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        StackedBarChart<String, Number> root = new StackedBarChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        root.getData().add(series);
        series.getData().add(new XYChart.Data<>("test", 1.0d));
        series.getData().clear();
        series.getData().add(new XYChart.Data<>("test", 1.0d));
        show(primaryStage, root);
    }

    private void show(Stage primaryStage, Parent root) {
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}

