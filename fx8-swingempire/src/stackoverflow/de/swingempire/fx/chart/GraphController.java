/*
 * Created on 29.11.2019
 *
 */
package de.swingempire.fx.chart;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;

/**
 * https://stackoverflow.com/q/59106098/203657 overlay barchart with linechart
 * 
 */
public class GraphController implements Initializable {

    @FXML
    StackPane stackPane;

    @FXML
    BarChart barChart;

    @FXML
    CategoryAxis xAxis;

    @FXML
    NumberAxis yAxis;

    @FXML
    NumberAxis yAxisRight;

    @FXML
    LineChart lineChartEffective;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        xAxis.setCategories(FXCollections.<String> observableArrayList("Jan",
                "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
                "Nov", "Dec"));

        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(10);

        yAxisRight.setSide(Side.RIGHT);
        yAxisRight.setLowerBound(0);
        yAxisRight.setUpperBound(50);
        yAxisRight.setTickUnit(1);

        barChart.setLegendVisible(false);
        barChart.setAnimated(false);

        barChart.setLegendVisible(false);
        barChart.setAnimated(false);

        barChart.setAlternativeRowFillVisible(false);
        barChart.setAlternativeColumnFillVisible(false);
        barChart.setHorizontalGridLinesVisible(false);
        barChart.setVerticalGridLinesVisible(false);
        barChart.getXAxis().setVisible(true);
        barChart.getYAxis().setVisible(true);

        barChart.getData().add(getAverageLoadPerFTE());

        xAxis.setLabel("Month");
        yAxis.setLabel("Level (%)");
        yAxisRight.setLabel("Level (u)");

        lineChartEffective.setLegendVisible(false);
        lineChartEffective.setAnimated(false);
        lineChartEffective.setCreateSymbols(true);
        lineChartEffective.setAlternativeRowFillVisible(false);
        lineChartEffective.setAlternativeColumnFillVisible(false);
        lineChartEffective.setHorizontalGridLinesVisible(false);
        lineChartEffective.setVerticalGridLinesVisible(false);
        lineChartEffective.getXAxis().setVisible(false);
        lineChartEffective.getYAxis().setVisible(true);
        lineChartEffective.getStylesheets().addAll(
                getClass().getResource("effectivs.css").toExternalForm());
        lineChartEffective.getData().add(getEffectiveCustomerSupport());

    }

    private XYChart.Series<String, Number> getAverageLoadPerFTE() {
        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        series.getData().add(new XYChart.Data<String, Number>("Jan", 58));
        series.getData().add(new XYChart.Data<String, Number>("Feb", 54));
        series.getData().add(new XYChart.Data<String, Number>("Mar", 47));
        series.getData().add(new XYChart.Data<String, Number>("Apr", 34));
        series.getData().add(new XYChart.Data<String, Number>("May", 33));
        series.getData().add(new XYChart.Data<String, Number>("Jun", 32));
        series.getData().add(new XYChart.Data<String, Number>("Jul", 30));
        series.getData().add(new XYChart.Data<String, Number>("Aug", 36));
        series.getData().add(new XYChart.Data<String, Number>("Sep", 29));
        series.getData().add(new XYChart.Data<String, Number>("Oct", 33));
        series.getData().add(new XYChart.Data<String, Number>("Nov", 0));
        series.getData().add(new XYChart.Data<String, Number>("Dec", 0));

        series.setName("Average load per FTE");

        return series;
    }

    private XYChart.Series<String, Number> getEffectiveCustomerSupport() {
        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        series.getData().add(new XYChart.Data<String, Number>("Jan", 2.9));
        series.getData().add(new XYChart.Data<String, Number>("Feb", 2.7));
        series.getData().add(new XYChart.Data<String, Number>("Mar", 3.3));
        series.getData().add(new XYChart.Data<String, Number>("Apr", 3.4));
        series.getData().add(new XYChart.Data<String, Number>("May", 3.3));
        series.getData().add(new XYChart.Data<String, Number>("Jun", 3.2));
        series.getData().add(new XYChart.Data<String, Number>("Jul", 3.3));
        series.getData().add(new XYChart.Data<String, Number>("Aug", 4));
        series.getData().add(new XYChart.Data<String, Number>("Sep", 3.8));
        series.getData().add(new XYChart.Data<String, Number>("Oct", 4.1));
        series.getData().add(new XYChart.Data<String, Number>("Nov", 0));
        series.getData().add(new XYChart.Data<String, Number>("Dec", 0));

        series.setName("Effective customer support");

        return series;
    }

}