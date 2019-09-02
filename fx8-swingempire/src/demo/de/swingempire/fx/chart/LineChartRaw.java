/*
 * Created on 02.12.2015
 *
 */
package de.swingempire.fx.chart;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Show plain line only (without axis, legend, background)
 * <p>
 * https://stackoverflow.com/q/57752877/203657
 * <p>
 * This is the example from tutorial with experiments.
 * 
 * <p>
 * <ul>
 * <li> use xy-/line-/chart api to set all xxVisible to false
 * <li> astonishingly, there is no api to hide the axis: setting
 *    all its tick/label properties to invisible leaves a line itself
 *    visible - need to remove the axis from its parent!
 * </ul>
 * 
 * Consider filing an rfe
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class LineChartRaw extends Application {
    
    @Override public void start(Stage stage) {
        stage.setTitle("Line Chart FocusableNode");
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        
        //creating the chart
        final LineChart<Number,Number> lineChart = 
                new LineChart<Number,Number>(xAxis, yAxis);
        undecorate(lineChart);        
        
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
        
        Button focusable = new Button("remove axis");
//        focusable.setOnAction(e -> {
//            if (xAxis.getParent() instanceof Pane) {
//                Pane parent = (Pane) xAxis.getParent();
//                parent.getChildren().removeAll(xAxis, yAxis);
//                focusable.setDisable(false);
//            }
//        });
        
        content.setBottom(focusable);
        Scene scene  = new Scene(content,800,600);
        lineChart.getData().add(series);
       
        stage.setScene(scene);
        stage.show();
    }

    protected void undecorate(LineChart chart) {
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setHorizontalZeroLineVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setVerticalZeroLineVisible(false);
        undecorateAxis(chart.getXAxis());
        undecorateAxis(chart.getYAxis());
    }
    
    protected void undecorateAxis(Axis axis) {
        // trying to hide: not working
        // axis.setVisible(false);
        // configure all ticks/label to not visible doesn't work
        // if (axis instanceof ValueAxis)
        //    ((ValueAxis<?>) axis).setMinorTickVisible(false);
        // axis.setTickLabelsVisible(false);
        //axis.setTickMarkVisible(false);
        // remove from parent does work
        Pane parent = (Pane) axis.getParent();
        parent.getChildren().removeAll(axis);
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(LineChartRaw.class
            .getName());
}