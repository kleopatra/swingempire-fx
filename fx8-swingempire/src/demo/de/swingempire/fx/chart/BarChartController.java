/*
 * Created on 28.02.2018
 *
 */
package de.swingempire.fx.chart;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Problem: labels not properly shown
 * https://stackoverflow.com/q/48995257/203657
 * 
 * works as expected
 */
public class BarChartController  { //implements Initializable{

    @FXML
    private BarChart<?, ?> barChartHistogram;

    private SortedMap<String, Integer> _points;

    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

//    @Override
//    public void initialize(URL url, ResourceBundle rb) {
////        onLoad(null);
//    }
//    
    public void onLoad(SortedMap<String, Integer> points) {
        xAxis.setLabel("Numer indeksu");
        yAxis.setLabel("Ilo punktw");
        //barChartHistogram.setBarGap(0);
        XYChart.Series series1 = new XYChart.Series();
        int a = 10;
        series1.getData().add(new XYChart.Data("Tom", 10));
        series1.getData().add(new XYChart.Data("Andrew", 7));
        series1.getData().add(new XYChart.Data("Patrick", 5));


//        for (Map.Entry<String, Integer> p: points.entrySet()) {
//            series1.getData().add(new XYChart.Data<>(Integer.toString(a), p.getValue()));
//            a += 10;
//        }
        barChartHistogram.getData().addAll(series1);
        LOG.info("getting here: " + barChartHistogram);
        _points = points;
    }

    @FXML
    private void buttonShowPressed(ActionEvent event) {
        xAxis.setLabel("Numer indeksu");
        yAxis.setLabel("Ilo punktw");
        //barChartHistogram.setBarGap(0);
        int a = 10;
        // just a quick check to see how bar-fill works with more than 1 series
        // same color for all
        XYChart.Series series2 = new XYChart.Series();
        series2.getData().add(new XYChart.Data("Tom", 10));
        series2.getData().add(new XYChart.Data("Andrew", 7));
        series2.getData().add(new XYChart.Data("Patrick", 5));
        
        XYChart.Series series1 = new XYChart.Series();
        series1.getData().add(new XYChart.Data("Tom", 10));
        series1.getData().add(new XYChart.Data("Andrew", 7));
        series1.getData().add(new XYChart.Data("Patrick", 5));


//        for (Map.Entry<String, Integer> p: points.entrySet()) {
//            series1.getData().add(new XYChart.Data<>(Integer.toString(a), p.getValue()));
//            a += 10;
//        }
        barChartHistogram.getData().addAll(series1, series2);
        barChartHistogram.requestLayout();
        LOG.info("getting here: " + barChartHistogram);
//        xAxis.setLabel("Numer indeksu");
//        yAxis.setLabel("Ilość punktów");
//        barChartHistogram.setCategoryGap(0);
//        barChartHistogram.setBarGap(0);
//        barChartHistogram.setTitle("XDDDDDDDDDDDDDDD");
//        barChartHistogram.setMaxHeight(1000);
//        XYChart.Series series = new XYChart.Series();
//        series.setName("Histogram");
//        for (Map.Entry<String, Integer> p: _points.entrySet()) {
//            series.getData().add(new XYChart.Data(p.getKey(), p.getValue()));
//        }
//        barChartHistogram.getData().addAll(series);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(BarChartController.class.getName());
}

