package de.swingempire.fx.chart;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ToolTipOnChartSeries extends Application {

    private static final Object MOUSE_TRIGGER_LOCATION = "tooltip-last-location";

    private ObservableList<XYChart.Series<String, Double>> getChartData() {
        double javaValue = 17.56;
        ObservableList<XYChart.Series<String, Double>> answer = FXCollections.observableArrayList();
        Series<String, Double> java = new Series<String, Double>();
        java.setName("java");
        Tooltip t = new Tooltip();
        t.setOnShowing(e -> {
            Point2D screen = (Point2D) t.getProperties().get(MOUSE_TRIGGER_LOCATION);
            if (screen == null) return;
            XYChart chart = java.getChart();
            double localX = chart.getXAxis().screenToLocal(screen).getX();
            double localY = chart.getYAxis().screenToLocal(screen).getY();
            Object xValue = chart.getXAxis().getValueForDisplay(localX);
            Object yValue = chart.getYAxis().getValueForDisplay(localY);
            t.textProperty().set("x/y: " + t.getX() + " / " + t.getY() 
                    + "\n localX " + localX + "/" + xValue 
                    + "\n localY " + localY + "/" + yValue 
                    
                    );
        });
        java.nodeProperty().addListener(new ChangeListener<Node>()
        { 
            @Override
            public void changed(ObservableValue<? extends Node> arg0, Node arg1,
                Node node)
            {
                Tooltip.install(node, t);
                node.setOnMouseMoved(e -> {
                    Point2D screen = new Point2D(e.getScreenX(), e.getScreenY());
                    t.getProperties().put(MOUSE_TRIGGER_LOCATION, screen);
                });
                java.nodeProperty().removeListener(this);
            }
        });
        for (int i = 2011; i < 2021; i++) {
            // adding a tooltip to the data node
            final XYChart.Data data = new XYChart.Data(Integer.toString(i), javaValue);
            java.getData().add(data);
            javaValue = javaValue + Math.random() - .5;
        }
        answer.addAll(java); //, c, cpp);
        return answer;
    }
    
    @Override
    public void start(Stage primaryStage) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart lineChart = new LineChart(xAxis, yAxis);
        lineChart.setCreateSymbols(false);
        lineChart.setData(getChartData());
        lineChart.setTitle("speculations");
        primaryStage.setTitle("LineChart example");
    
        StackPane root = new StackPane();
        root.getChildren().add(lineChart);
        primaryStage.setScene(new Scene(root)); //, 400, 250));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ToolTipOnChartSeries.class
            .getName());
}