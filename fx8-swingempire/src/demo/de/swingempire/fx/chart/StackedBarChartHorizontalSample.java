/*
 * Created on 26.01.2015
 *
 */
package de.swingempire.fx.chart;

import java.util.Arrays;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.stage.Stage;

/**
 * Example from tutorial:
 * http://docs.oracle.com/javase/8/javafx/user-interface-tutorial/bar-chart.htm#CIHJFHDE
 * 
 */
public class StackedBarChartHorizontalSample extends Application {
    
    final static String austria = "Austria";
    final static String brazil = "Brazil";
    final static String france = "France";
    final static String italy = "Italy";
    final static String usa = "USA";
    final CategoryAxis xAxis = new CategoryAxis();
    final NumberAxis yAxis = new NumberAxis();
    final StackedBarChart<String, Number> sbc =
            new StackedBarChart<>(xAxis, yAxis);
    final XYChart.Series<String, Number> series1 =
            new XYChart.Series<>();
    final XYChart.Series<String, Number> series2 =
            new XYChart.Series<>();
    final XYChart.Series<String, Number> series3 =
            new XYChart.Series<>();
 
    final NumberAxis xAxisHoriz = new NumberAxis();
    
    final CategoryAxis yAxisHoriz = new CategoryAxis();
    final StackedBarChart<Number, String> sbcHoriz =
            new StackedBarChart<>(xAxisHoriz, yAxisHoriz);
    final XYChart.Series<Number, String> series1Horiz =
            new XYChart.Series<>();
    final XYChart.Series<Number, String> series2Horiz =
            new XYChart.Series<>();
    final XYChart.Series<Number, String> series3Horiz =
            new XYChart.Series<>();
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Bar Chart Sample");
        sbc.setTitle("Country Summary");
        xAxis.setLabel("Country");
        xAxis.setCategories(FXCollections.<String>observableArrayList(
                Arrays.asList(austria, brazil, france, italy, usa)));
        yAxis.setLabel("Value");
        createBaseSeries();
        sbc.getData().addAll(series1, series2, series3);
        
        // create horiz data from base
        createHorizSeries();
        sbcHoriz.setTitle("Inverted");
        // inverted config
//        yAxisHoriz.setLabel("Country");
//        yAxisHoriz.setCategories(FXCollections.<String>observableArrayList(
//                Arrays.asList(austria, brazil, france, italy, usa)));
//        xAxisHoriz.setLabel("Value");
        
//        yAxisHoriz.setVisible(false);
        yAxisHoriz.setTickLabelsVisible(false);
        yAxisHoriz.setTickMarkVisible(false);
        yAxisHoriz.setStyle("-fx-border-color: transparent transparent transparent transparent;");
        yAxisHoriz.setBorder(new Border((BorderStroke)null));
        yAxisHoriz.setMaxHeight(20.);
//        sbcHoriz.setCategoryGap(0);
        sbcHoriz.setMaxHeight(100);
        xAxisHoriz.setTickLabelsVisible(false);
        xAxisHoriz.setTickMarkVisible(false);
        xAxisHoriz.setMinorTickVisible(false);
        xAxisHoriz.setStyle("-fx-border-color: transparent transparent transparent transparent;");
        xAxisHoriz.setAutoRanging(false);
        xAxisHoriz.setUpperBound(200000.);
        
        sbcHoriz.setHorizontalGridLinesVisible(false);
        sbcHoriz.setVerticalGridLinesVisible(false);
        sbcHoriz.setLegendVisible(false);
        
        sbcHoriz.getData().addAll(series1Horiz, series2Horiz, series3Horiz);
        Scene scene = new Scene(sbcHoriz);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Note: base must be filled before calling this
     */
    protected void createHorizSeries() {
        invertDataTo(series1, series1Horiz);
        invertDataTo(series2, series2Horiz);
        invertDataTo(series3, series3Horiz);
    }

    private void invertDataTo(Series<String, Number> source, 
            Series<Number, String> inverted) {
//        inverted.setName(source.getName());
        for (XYChart.Data<String, Number> element : source.getData()) {
            inverted.getData().add(new XYChart.Data<>(element.getYValue(), element.getXValue()));
        }
    }

    protected void createBaseSeries() {
        series1.setName("2003");
        series1.getData().add(new XYChart.Data<>(austria, 25601.34));
        series1.getData().add(new XYChart.Data<>(brazil, 20148.82));
        series1.getData().add(new XYChart.Data<>(france, 10000));
        series1.getData().add(new XYChart.Data<>(italy, 35407.15));
        series1.getData().add(new XYChart.Data<>(usa, 12000));
        series2.setName("2004");
        series2.getData().add(new XYChart.Data<>(austria, 57401.85));
        series2.getData().add(new XYChart.Data<>(brazil, 41941.19));
        series2.getData().add(new XYChart.Data<>(france, 45263.37));
        series2.getData().add(new XYChart.Data<>(italy, 117320.16));
        series2.getData().add(new XYChart.Data<>(usa, 14845.27));
        series3.setName("2005");
        series3.getData().add(new XYChart.Data<>(austria, 45000.65));
        series3.getData().add(new XYChart.Data<>(brazil, 44835.76));
        series3.getData().add(new XYChart.Data<>(france, 18722.18));
        series3.getData().add(new XYChart.Data<>(italy, 17557.31));
        series3.getData().add(new XYChart.Data<>(usa, 92633.68));
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}

