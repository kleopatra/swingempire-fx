/*
 * Created on 20.04.2020
 *
 */
package chart;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8243128
 * weird behavior on replacing the data in the series
 * 
 * borderline useage error: data cannot be reused
 */
public class LineChartRepro extends Application {
    public void start(Stage stage) throws Exception {
        LineChart<Number, Number> chart = new LineChart<>(new NumberAxis(), new NumberAxis());
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setData(FXCollections.observableList(Arrays.asList(
                new XYChart.Data<>(0,0),
                new XYChart.Data<>(1,1))));
        chart.getData().setAll(Collections.singletonList(series));
        Button addChartData = new Button("Add Chart data");
        addChartData.setOnAction(e -> {
            ArrayList<XYChart.Data<Number, Number>> newList = new ArrayList<>();//new ArrayList<>(series.getData());
            series.getData().forEach(data -> newList.add(new XYChart.Data(data.getXValue(), data.getYValue())));
            newList.add(new XYChart.Data<>(newList.size(), newList.size()));
            series.setData(FXCollections.observableList(newList));
        });
        stage.setScene(new Scene(new VBox(chart, addChartData)));
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}