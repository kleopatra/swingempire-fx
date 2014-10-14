/*
 * Created on 14.10.2014
 *
 */
package de.swingempire.fx.chart;


import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 
 * @author johan
 */
public class PieChartExample extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void start(Stage primaryStage) {
        ObjectProperty<ObservableList> listProperty;
        PieChart pieChart = new PieChart();
        pieChart.setData(getChartData());
        Button reset = new Button("Reset Data");
        reset.setOnAction(e -> {
            // cant test: !data.equals(getChartData())
            ObservableList old = pieChart.getData();
            ObservableList data = getChartData();//FXCollections.observableArrayList(old);
            LOG.info("isEqual? " + data.equals(getChartData()) + "\n" + data + "\n" + getChartData());
            pieChart.setData(data);
        });
        
        Button modify = new Button("Modify");
        modify.setOnAction(e -> {
            pieChart.getData().remove(0);
        });
//         pieChart.setTitle("Tiobe index");
//         pieChart.setLegendSide(Side.LEFT);
//         pieChart.setClockwise(false);
//         pieChart.setLabelsVisible(false);
        primaryStage.setTitle("PieChart 2");
        Pane root = new VBox(pieChart, reset, modify);
//        root.getChildren().add(pieChart);
        // beware: don't use fixed sizes, let the layoutManager handle it!
        // here the size isn't big enough to fit the legend, so it is left out
//        primaryStage.setScene(new Scene(root, 300, 250));
        Scene scene = new Scene(root);
//        scene.getStylesheets().add(PieChartExample.class.getResource("chart2.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ObservableList<Data> getChartData() {
        ObservableList<Data> answer = FXCollections.observableArrayList();
        answer.addAll(new PieChart.Data("java", 17.56), new PieChart.Data("C",
                17.06), new PieChart.Data("C++", 8.25), new PieChart.Data("C#",
                8.20), new PieChart.Data("ObjectiveC", 6.8), new PieChart.Data(
                "PHP", 6.0), new PieChart.Data("(Visual)Basic", 4.76),
                new PieChart.Data("Other", 31.37));
        return answer;
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(PieChartExample.class
            .getName());

}