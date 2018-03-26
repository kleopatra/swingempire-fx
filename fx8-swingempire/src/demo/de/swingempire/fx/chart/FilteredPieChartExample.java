/*
 * Created on 14.10.2014
 *
 */
package de.swingempire.fx.chart;


import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Hide empty (or otherwise unwanted data) from bar chart
 * https://stackoverflow.com/a/49027208/203657
 */
public class FilteredPieChartExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        
        FilteredList<Data> filtered = getChartData();
        //ListChangeReport report = new ListChangeReport(filtered);
        
        PieChart pieChart = new PieChart(filtered);
        // bug in pieChart: can't handle data modification with animation on
        pieChart.setAnimated(false);
        
        // use slider to set lower threshhold for value of data to show in pie
        Slider slider =  new Slider(-1., 100., -1.);
        slider.valueProperty().addListener((src, ov, nv) -> {
            // actually, cannot handle data modification at all ... need to clear out first ...
            // bug in pieChart.dataChangeListener: doesn't handle replaced correctly
            filtered.setPredicate(data -> false);
            filtered.setPredicate(data -> data.getPieValue() > nv.doubleValue());
            //report.prettyPrint();
        });
        primaryStage.setTitle("PieChart");
        Pane root = new VBox(pieChart, slider); 
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private FilteredList<Data> getChartData() {
        // use ObservableList with extractor on pieValueProperty
        ObservableList<Data> answer = FXCollections.observableArrayList(
                e -> new Observable[] {e.pieValueProperty()}
                );
        answer.addAll(
                new Data("java", 17.56), 
                new Data("C", 17.06), 
                new Data("C++", 8.25), 
                new Data("C#", 8.20), 
                new Data("ObjectiveC", 6.8), 
                new Data("PHP", 6.0), 
                new Data("(Visual)Basic", 4.76),
                new Data("Other", 31.37),
                new Data("empty", 0)
                );
        return new FilteredList<>(answer);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(FilteredPieChartExample.class
            .getName());

}