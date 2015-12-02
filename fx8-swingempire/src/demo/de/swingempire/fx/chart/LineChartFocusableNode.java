/*
 * Created on 02.12.2015
 *
 */
package de.swingempire.fx.chart;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Make data nodes focusable such that they are reachable by keyboard.
 * 
 * http://stackoverflow.com/q/34034118/203657
 * 
 * This is the example from tutorial with experiments.
 * 
 * Problem: the symbol's focusTraversable is bound to an assistive property
 * (which is true if accessibility is enabled) thus making it impossible to
 * actively set in a custom context.
 * 
 * The solution (with the obvious drawback to get into the way of
 * accessiblity) is to first unbind and then set to true. Actually, the
 * current implementation is a bug: what's needed is a conditional binding,
 * allowing a false as long as assistive tech is disabled, and enforcing the
 * true if enabled. 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class LineChartFocusableNode extends Application {
    
    @Override public void start(Stage stage) {
        stage.setTitle("Line Chart FocusableNode");
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Number of Month");
        //creating the chart
        final LineChart<Number,Number> lineChart = 
                new LineChart<Number,Number>(xAxis,yAxis);
                
        lineChart.setTitle("Stock Monitoring, 2010");
        //defining a series
        XYChart.Series<Number, Number> series = new XYChart.Series();
        series.setName("My portfolio");
        //populating the series with data
        series.getData().add(new XYChart.Data(1, 23));
        series.getData().add(new XYChart.Data(2, 14));
        series.getData().add(new XYChart.Data(3, 15));
        series.getData().add(new XYChart.Data(4, 24));
        series.getData().add(new XYChart.Data(5, 34));
        series.getData().add(new XYChart.Data(6, 36));
        series.getData().add(new XYChart.Data(7, 22));
        series.getData().add(new XYChart.Data(8, 45));
        series.getData().add(new XYChart.Data(9, 43));
        series.getData().add(new XYChart.Data(10, 17));
        series.getData().add(new XYChart.Data(11, 29));
        series.getData().add(new XYChart.Data(12, 25));
        
        BorderPane content = new BorderPane(lineChart);
        
        Button focusable = new Button("focusSecond");
        focusable.setOnAction(e -> {
            XYChart.Data<Number, Number> second = series.getData().get(1);
            makeNodeFocusable(second);
        });
        
        content.setBottom(focusable);
        Scene scene  = new Scene(content,800,600);
        lineChart.getData().add(series);
       
        stage.setScene(scene);
        stage.show();
    }

    protected void makeNodeFocusable(XYChart.Data data) {
        Node node = data.getNode();
        node.focusTraversableProperty().unbind();
        node.setFocusTraversable(true);
        node.focusedProperty().addListener((s, ov, nv) -> {
            LOG.info("focused: " + nv + node);
            
        });
    }
 
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(LineChartFocusableNode.class
            .getName());
}