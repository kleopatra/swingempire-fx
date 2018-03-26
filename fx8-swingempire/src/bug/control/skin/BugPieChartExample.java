/*
 * Created on 14.10.2014
 *
 */
package control.skin;


import java.util.logging.Logger;

import javafx.application.Application;
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
 * Bug: PieChart must cope with all change notifications from data.
 * reported: https://bugs.openjdk.java.net/browse/JDK-8198823
 * 
 * bubbled up when trying to use a filteredList (to hide data entries)
 * https://stackoverflow.com/a/49027208/203657
 * 
 * Here: fails on replaced 
 * if animated: throws IllegalArgumentException: duplicate children added (stacktrace A)
 * if not animated: fails with NPE (stacktrace B)
 */
public class BugPieChartExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        
        ObservableList<Data> data = getChartData();
//        ListChangeReport report = new ListChangeReport(data);
        
        PieChart pieChart = new PieChart(data);
        // pieChart can't handle data modification if animated:
        // error is "duplicate children added"
        pieChart.setAnimated(false);
        
        Button replaceData = new Button("replace");
        replaceData.setOnAction(e -> {
            // actually, cannot handle data modification correctly
            // it's at least one (or more, didn't dig further) 
            // bug in dataChangeListener: doesn't handle replaced correctly
            ObservableList<Data> current = FXCollections.observableArrayList(pieChart.getData());
            pieChart.getData().setAll(current);
//            report.prettyPrint();
        });
        primaryStage.setTitle("PieChart 2");
        Pane root = new VBox(pieChart, replaceData);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }

    private ObservableList<Data> getChartData() {
        ObservableList<Data> answer = FXCollections.observableArrayList(
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
        return answer;
    }
    
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(BugPieChartExample.class
            .getName());
    
/*Stacktrace A:
 * Exception in thread "JavaFX Application Thread" java.lang.IllegalArgumentException: Children: duplicate children added: parent = Chart$1@144ad720[styleClass=chart-content]
        at javafx.graphics/javafx.scene.Parent$3.onProposedChange(Parent.java:580)
        at javafx.base/com.sun.javafx.collections.VetoableListDecorator.add(VetoableListDecorator.java:206)
        at javafx.controls/javafx.scene.chart.PieChart.dataItemAdded(PieChart.java:417)
    
    
  Stacktrace B:
    Exception in thread "JavaFX Application Thread" java.lang.NullPointerException
        at javafx.controls/javafx.scene.chart.PieChart.dataItemAdded(PieChart.java:417)
        at javafx.controls/javafx.scene.chart.PieChart.lambda$new$0(PieChart.java:168)
  
 */

}