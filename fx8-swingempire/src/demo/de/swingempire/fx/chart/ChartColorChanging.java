/*
 * Created on 07.04.2015
 *
 */
package de.swingempire.fx.chart;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Duration;
/**
 * scene not updated to new chart.
 * Real problem: color changes each time the series is reset.
 * http://stackoverflow.com/q/29480247/203657
 * 
 * can't reproduce color change in 8u60b5, seems to be fixed with 
 * https://javafx-jira.kenai.com/browse/RT-40104
 */
public class ChartColorChanging extends Application
{
    private StackedBarChart<String, Number> schart;
    private CategoryAxis xAxis = new CategoryAxis();
    private NumberAxis yAxis = new NumberAxis();

    private StackedBarChart<String, Number> inits()
    {
        schart = new StackedBarChart<>(xAxis, yAxis);
        schart.setAnimated(false);

        schart.setTitle("Some_value");

        return schart;
    }

    GenDatService data = new GenDatService();

    @Override
    public void start(Stage stage) throws Exception
    {

        StackedBarChart<String, Number> inits = inits();



        data.setPeriod(new Duration(1000));
        data.setOnSucceeded(new EventHandler<WorkerStateEvent>()
        {
            @Override
            public void handle(final WorkerStateEvent workerStateEvent)
            {

                HashMap<String, Integer> value = data.getValue();

                visuData(value);

            }
        });
        data.setOnFailed(new EventHandler<WorkerStateEvent>()
        {
            @Override
            public void handle(WorkerStateEvent t)
            {
            }
        });
        data.start();

        Scene scene = new Scene(schart);

        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();
    }

    public void visuData(HashMap<String, Integer> map)
    {
        Set<String> keySet = map.keySet();

        ObservableList<XYChart.Series<String, Number>> observableArrayList = FXCollections.observableArrayList();

        ObservableList<String> observabtd = FXCollections.observableArrayList();

        Iterator<String> iterator = keySet.iterator();

        while (iterator.hasNext())
        {
            String column = iterator.next();
            Integer value = map.get(column);

            XYChart.Series<String, Number> series1 = new XYChart.Series<>();
            XYChart.Data<String, Number> dataS1 = new XYChart.Data<>();

            observabtd.add(column);

            series1.setName(column);
            dataS1.setXValue(column);
            dataS1.setYValue(value);
            series1.getData().add(dataS1);

            observableArrayList.add(series1);
        }

        xAxis.setCategories(observabtd);
        yAxis.setLabel("Some Value");

        Scene scene = schart.getScene();
        schart = new StackedBarChart<>(xAxis, yAxis);
        scene.setRoot(schart);
        schart.setAnimated(false);

        schart.setData(observableArrayList);
    }

    public static void main(String[] args)
    {
        launch(args);
    }

    class GenDatService extends ScheduledService<HashMap<String, Integer>>
    {
        @Override
        protected Task<HashMap<String, Integer>> createTask()
        {
            return new Task<HashMap<String, Integer>>()
            {
                @Override
                protected HashMap<String, Integer> call() throws Exception
                {
                    HashMap<String, Integer> usageData = new HashMap<>();
                    usageData.put("val_1", (int) (Math.random() * 100));

                    usageData.put("val_2", (int) (Math.random() * 100));

                    usageData.put("val_3", (int) (Math.random() * 100));

                    usageData.put("val_4", (int) (Math.random() * 100));

                    return usageData;
                }
            };
        }
    }
}

