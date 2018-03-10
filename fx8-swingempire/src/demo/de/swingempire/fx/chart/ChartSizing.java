/*
 * Created on 07.03.2018
 *
 */
package de.swingempire.fx.chart;

import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Upper chart should have 2* the height of the lower (or the other way round)
 * https://stackoverflow.com/q/49149752/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChartSizing extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Line Chart Sample");
        final CategoryAxis xAxis1 = new CategoryAxis();
        final NumberAxis yAxis1 = new NumberAxis();
        xAxis1.setLabel("Month");
        final LineChart<String, Number> lineChart1 =
                new LineChart<>(xAxis1, yAxis1);

        final CategoryAxis xAxis2 = new CategoryAxis();
        xAxis2.setLabel("Month");
        final NumberAxis yAxis2 = new NumberAxis();
        final LineChart<String, Number> lineChart2 =
                new LineChart<>(xAxis2, yAxis2);

        lineChart1.setLegendVisible(false);
        lineChart2.setLegendVisible(false);

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Portfolio 1");

        series1.getData().add(new XYChart.Data("Jan", 23));
        series1.getData().add(new XYChart.Data("Feb", 14));
        series1.getData().add(new XYChart.Data("Mar", 15));
        series1.getData().add(new XYChart.Data("Apr", 24));
        series1.getData().add(new XYChart.Data("May", 34));
        series1.getData().add(new XYChart.Data("Jun", 36));
        series1.getData().add(new XYChart.Data("Jul", 22));
        series1.getData().add(new XYChart.Data("Aug", 45));
        series1.getData().add(new XYChart.Data("Sep", 43));
        series1.getData().add(new XYChart.Data("Oct", 17));
        series1.getData().add(new XYChart.Data("Nov", 29));
        series1.getData().add(new XYChart.Data("Dec", 25));

        XYChart.Series series2 = new XYChart.Series();
        series2.setName("Portfolio 2");
        series2.getData().add(new XYChart.Data("Jan", 33));
        series2.getData().add(new XYChart.Data("Feb", 34));
        series2.getData().add(new XYChart.Data("Mar", 25));
        series2.getData().add(new XYChart.Data("Apr", 44));
        series2.getData().add(new XYChart.Data("May", 39));
        series2.getData().add(new XYChart.Data("Jun", 16));
        series2.getData().add(new XYChart.Data("Jul", 55));
        series2.getData().add(new XYChart.Data("Aug", 54));
        series2.getData().add(new XYChart.Data("Sep", 48));
        series2.getData().add(new XYChart.Data("Oct", 27));
        series2.getData().add(new XYChart.Data("Nov", 37));
        series2.getData().add(new XYChart.Data("Dec", 29));

        lineChart1.getData().addAll(series1);
        lineChart2.getData().addAll(series2);

//        Scene scene = new Scene(createVBoxLayout(lineChart1, lineChart2), 800, 600);
        Scene scene = new Scene(createGridLayout(lineChart1, lineChart2), 800, 600);

        stage.setScene(scene);
        stage.show();
    }

    private Parent createVBoxLayout(XYChart chart1, XYChart chart2) {
        VBox vBox = new VBox(chart1, chart2);
        chart1.prefHeight(200);
        chart1.minHeight(200);
        chart1.maxHeight(200);
        chart2.prefHeight(400);
        chart2.minHeight(400);
        chart2.maxHeight(400);
        return vBox;
    }

    private Parent createGridLayout(XYChart chart1, XYChart chart2) {
        GridPane gridPane = new GridPane();
        gridPane.add(chart1, 0, 0, 1, 1);
        gridPane.add(chart2, 0, 1, 1, 2);

        return gridPane;
    }

    public static void main(String[] args) {
        launch(args);
    }

}

