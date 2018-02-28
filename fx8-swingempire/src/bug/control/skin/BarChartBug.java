/*
 * Created on 28.02.2018
 *
 */
package control.skin;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Problem: CategoryAxis doesn't auto-range if data added dynamically.
 * https://stackoverflow.com/q/48995257/203657
 * 
 * reported: https://bugs.openjdk.java.net/browse/JDK-8198830
 * @author Jeanette Winzenburg, Berlin
 */
public class BarChartBug extends Application {

    private NumberAxis yAxis;
    private CategoryAxis xAxis;
    private BarChart<String, Number> barChart;

    private Parent createContent() {
        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        
        Button initData = new Button("init");
        initData.setOnAction(e -> {
            xAxis.setLabel("Numer indeksu");
            yAxis.setLabel("Ilo punktw");
            XYChart.Series<String, Number> series1 = new XYChart.Series<>();
            series1.getData().add(new XYChart.Data<String, Number>("Tom", 10));
            series1.getData().add(new XYChart.Data<String, Number>("Andrew", 7));
            series1.getData().add(new XYChart.Data<String, Number>("Patrick", 5));

            // hack-around:
            // xAxis.setCategories(FXCollections.observableArrayList("Tom", "Andrew", "Patrick"));
            barChart.getData().addAll(series1);
            
            initData.setDisable(true);
            
        });
        BorderPane pane = new BorderPane(barChart);
        pane.setBottom(initData);
        return pane;
        
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(BarChartBug.class.getName());

}
