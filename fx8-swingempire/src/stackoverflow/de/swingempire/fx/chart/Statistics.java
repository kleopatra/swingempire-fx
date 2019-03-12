/*
 * Created on 12.03.2019
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
import javafx.stage.Stage;

/**
 * Issues with StackedBarChart and negative values.
 * https://stackoverflow.com/q/41902008/203657
 * 
 * different in fx11 (compared to the screenshot in the question), negative values 
 * not shown at all
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class Statistics extends Application {
    final CategoryAxis xAxis = new CategoryAxis();
    final NumberAxis yAxis = new NumberAxis();
    final StackedBarChart<String, Number> sbc = new StackedBarChart<>(xAxis, yAxis);
    final XYChart.Series<String, Number> incoming = new XYChart.Series<>();
    final XYChart.Series<String, Number> outgoing = new XYChart.Series<>();

    @Override
    public void start(Stage stage) {
        xAxis.setLabel("Month");
        xAxis.setCategories(FXCollections.observableArrayList(
                Arrays.asList("Jan", "Feb", "Mar")));
        yAxis.setLabel("Value");
        
        yAxis.setLowerBound(- 10000.);
        
        incoming.setName("Incoming");
        incoming.getData().add(new XYChart.Data("Jan", 25601.34));
        incoming.getData().add(new XYChart.Data("Feb2", 20148.82));
        incoming.getData().add(new XYChart.Data("Mar2", 10000));
        outgoing.setName("Outgoing");
        outgoing.getData().add(new XYChart.Data("Jan", -7401.85));
        outgoing.getData().add(new XYChart.Data("Feb2", -1941.19));
        outgoing.getData().add(new XYChart.Data("Mar2", -5263.37));
        Scene scene = new Scene(sbc, 800, 600);
        sbc.getData().addAll(outgoing, incoming);
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}

