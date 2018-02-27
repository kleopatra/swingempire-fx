/*
 * Created on 14.10.2014
 *
 */
package de.swingempire.fx.chart;


import java.util.logging.Logger;

import de.swingempire.fx.util.ListChangeReport;
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
 * 
 * @author johan
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CustomPieChartExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        
        FilteredList<Data> filtered = getChartData();
        ListChangeReport report = new ListChangeReport(filtered);
        
        PieChart pieChart = new PieChart() {

            /**
             * Quick check for https://stackoverflow.com/q/43082626/203657
             * Hide legend for empty data - need to hide Text in chart as well.
             * 
             * The idea would be to bind the visible property of legend/text to
             * a predicate. Need to explore where that should be updated.
             */
//            @Override
//            protected ObservableList<Node> getChartChildren() {
//                ObservableList<Node> children = super.getChartChildren();
//                Node legend = getLegend();
//                if (legend instanceof Parent)
//                    LOG.info("legend: " + ((Parent) getLegend()).getChildrenUnmodifiable());
//                LOG.info("children:" + children); 
//                return children;
//
//            }
            
        };
        // pieChart can't handle data modification if animated:
        // error is "duplicate children added"
        pieChart.setAnimated(false);
//        pieChart.setData(filtered);
        
        Slider slider =  new Slider(-1., 100., -1.);
        slider.valueProperty().addListener((src, ov, nv) -> {
            // actually, cannot handle data modification at all ... need to clear out first ...
            // it's a bug in dataChangeListener: doesn't handle replaced
//            filtered.setPredicate(data -> false);
            filtered.setPredicate(data -> data.getPieValue() > nv.doubleValue());
            report.prettyPrint();
        });
        primaryStage.setTitle("PieChart 2");
        Pane root = new VBox(pieChart, slider); //, reset, modify);
//        root.getChildren().add(pieChart);
        // beware: don't use fixed sizes, let the layoutManager handle it!
        // here the size isn't big enough to fit the legend, so it is left out
//        primaryStage.setScene(new Scene(root, 300, 250));
        Scene scene = new Scene(root);
//        scene.getStylesheets().add(PieChartExample.class.getResource("chart2.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }

    private FilteredList<Data> getChartData() {
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
        return new FilteredList(answer);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(CustomPieChartExample.class
            .getName());

}