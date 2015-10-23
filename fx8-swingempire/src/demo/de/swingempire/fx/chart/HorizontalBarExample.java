/*
 * Created on 07.10.2015
 *
 */
package de.swingempire.fx.chart;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 * Category axis rotates on getting smaller, such that the labels are no longer
 * visible
 * 
 * http://stackoverflow.com/q/32979001/203657
 */
public class HorizontalBarExample extends Application {
    @Override
    public void start(Stage stage)  {
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();
        BarChart<Number, String> bc = new BarChart<Number, String>(xAxis, yAxis);
        bc.setBarGap(0d);
        bc.setCategoryGap(0);

        xAxis.setTickLabelRotation(90);
        yAxis.tickLabelRotationProperty().set(0d);

        XYChart.Series<Number, String> series1 = new XYChart.Series<>();
        series1.setName("example");
        ObservableList<String> categories = FXCollections.observableArrayList();
        for (int i = 0; i < 10; i++) {
            String yValue = "long data label number" + i;
            series1.getData().add(new XYChart.Data<Number, String>(Math.random() * 5000, yValue));
            categories.add(yValue);
        }

        bc.getData().add(series1);
        
        // without auto-ranging, the labels are not rotated
        // requires manual setting of categories
        // layout suboptimal!
//        yAxis.setAutoRanging(false);
//        yAxis.setCategories(categories);
        
        series1.getData();
        Scene scene = new Scene(bc, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args)  {
        launch(args);
    }
}

