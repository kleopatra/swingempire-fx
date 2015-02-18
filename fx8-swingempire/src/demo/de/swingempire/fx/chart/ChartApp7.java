package de.swingempire.fx.chart;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import de.swingempire.fx.util.FXUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ChartApp7 extends Application {


    public static void main(String[] args) {
        launch(args);
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

    private ObservableList<XYChart.Series<String, Double>> getChartData() {
        double javaValue = 17.56;
        double cValue = 17.06;
        double cppValue = 8.25;
        ObservableList<XYChart.Series<String, Double>> answer = FXCollections.observableArrayList();
        Series<String, Double> java = new Series<String, Double>();
        Tooltip t = new Tooltip("sometip");//;data.getYValue().toString() + '\n' + data.getXValue());
        t.setOnShowing(e -> {
            Scene scene = t.getScene();
            Parent root = scene.getRoot();
            t.setText("x/y: " + t.getX() + t.getY());
//            LOG.info("showing: " + t.getX() + "/" + t.getY());
        });
        java.nodeProperty().addListener(new ChangeListener<Node>()
        { 
            @Override
            public void changed(ObservableValue<? extends Node> arg0, Node arg1,
                Node arg2)
            {
                Tooltip.install(java.getNode(), t);
                LOG.info("got a node? " + java.getNode());
                java.nodeProperty().removeListener(this);
            }
        });
        Series<String, Double> c = new Series<String, Double>();
        Series<String, Double> cpp = new Series<String, Double>();
        java.setName("java");
        c.setName("C");
        cpp.setName("C++");
        
        for (int i = 2011; i < 2021; i++) {
            // adding a tooltip to the data node
            final XYChart.Data data = new XYChart.Data(Integer.toString(i), javaValue);
            // node is created when data added to chart
            // here it's only added to the series which isn't yet attached to the
            // chart, so need to listen for changes 
//            data.nodeProperty().addListener(new ChangeListener<Node>()
//            {
//                @Override
//                public void changed(ObservableValue<? extends Node> arg0, Node arg1,
//                    Node arg2)
//                {
//                    Tooltip t = new Tooltip(data.getYValue().toString() + '\n' + data.getXValue());
//                    Tooltip.install(data.getNode(), t);
//                    LOG.info("got a node? " + data.getNode());
//                    data.nodeProperty().removeListener(this);
//                }
//            });
            java.getData().add(data);
            javaValue = javaValue + Math.random() - .5;
            c.getData().add(new XYChart.Data(Integer.toString(i), cValue));
            cValue = cValue + Math.random() - .5;
            cpp.getData().add(new XYChart.Data(Integer.toString(i), cppValue));
            cppValue = cppValue + Math.random() - .5;
        }
        answer.addAll(java); //, c, cpp);
        return answer;
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ChartApp7.class
            .getName());
}