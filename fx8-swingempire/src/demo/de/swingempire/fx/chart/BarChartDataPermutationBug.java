/*
 * Created on 29.01.2020
 *
 */
package de.swingempire.fx.chart;

import java.util.Comparator;

import de.swingempire.fx.util.ListChangeReport;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8238081
 * 
 * BarChart ignores data permutation
 */
public class BarChartDataPermutationBug extends Application {
    public static void main(String[] pArgs) {
        launch(pArgs);
    }

    @Override
    public void start(Stage pStage) {
        final ObservableList<XYChart.Data<String, Number>> seriesData = 
                FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
        
        
        seriesData.addAll(
                        new XYChart.Data<>("alpha", 0),
                        new XYChart.Data<>("bravo", 1),
                        new XYChart.Data<>("charlie", 2),
                        new XYChart.Data<>("delta", 3));
        XYChart.Data<String, Number> first = seriesData.get(0);
        
        ListChangeReport reportBase = new ListChangeReport(seriesData);
        
        
        final Comparator<XYChart.Data<String, Number>> ascendingComparator = Comparator
                .comparing(XYChart.Data::getXValue);
        
        final Comparator<XYChart.Data<String, Number>> descendingComparator = ascendingComparator
                .reversed();
        final SortedList<XYChart.Data<String, Number>> sortedSeriesData = new SortedList<>(
                seriesData, ascendingComparator);
        
        ListChangeReport reportSorted = new ListChangeReport(sortedSeriesData);
        System.out.println("Sorted Data: " + sortedSeriesData);
        first.setXValue("X");
        reportBase.prettyPrintAll();
        reportSorted.prettyPrintAll();
        System.out.println("Sorted Data: first to last? " + sortedSeriesData);

        
        final CheckBox checkBox = new CheckBox("Ascending");
        checkBox.selectedProperty().addListener((pObs, pOldVal, pNewVal) -> {
            sortedSeriesData.setComparator(
                    pNewVal ? ascendingComparator : descendingComparator);
            System.out.println(String.format("Comparator: %s. List content: %s",
                    pNewVal ? "Ascending" : "Descending", sortedSeriesData));
        });

        CheckBox toggleValue = new CheckBox("toggleFirstValue");
        toggleValue.selectedProperty().addListener((src, ov, nv) -> {
            if (nv) {
                first.setXValue("X");
            } else {
                first.setXValue("A");
            }
            System.out.println("Sorted Data: " + sortedSeriesData);

        });
        final BarChart<String, Number> chart = new BarChart<>(
                new CategoryAxis(), new NumberAxis(),
                FXCollections.singletonObservableList(
                        new XYChart.Series<>("test", sortedSeriesData)));
        VBox.setVgrow(chart, Priority.ALWAYS);

        pStage.setScene(new Scene(new VBox(chart, checkBox, toggleValue), 400, 300));
        pStage.show();
    }
}
