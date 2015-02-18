/*
 * Created on 17.02.2015
 *
 */
package de.swingempire.fx.chart;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * http://stackoverflow.com/questions/28560561/tooltip-on-live-linechart
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TooltipOnLineChart extends Application
{
    private static final int MAX_DATA_POINTS = 50;

    private Series series;
    private Series series2;
    private int xSeriesData = 0;
    private ConcurrentLinkedQueue<Number> dataQ = new ConcurrentLinkedQueue<Number>();
    private ConcurrentLinkedQueue<Number> dataQ2 = new ConcurrentLinkedQueue<Number>();
    private ExecutorService executor;
    private AddToQueue addToQueue;
    private NumberAxis xAxis;

    private void init(Stage primaryStage)
    {
        xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);

        //-- Chart
        final AreaChart<Number, Number> sc = new AreaChart<Number, Number>(xAxis, yAxis)
        {
            // Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item)
            {
            }
        };
        sc.setAnimated(false);
        sc.setId("liveAreaChart");
        sc.setTitle("Animated Area Chart");

        //-- Chart Series
        series = new AreaChart.Series<Number, Number>();
        series.setName("Area Chart Series");
        series2 = new AreaChart.Series<Number, Number>();
        series2.setName("Area Chart Series");
        sc.getData().addAll(series, series2);

        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);

        BorderPane pane = new BorderPane(sc);
        Button stop = new Button("stop");
        stop.setOnAction(e -> {
            executor.shutdownNow();
        });
        pane.setBottom(stop);
        primaryStage.setScene(new Scene(pane));
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        init(primaryStage);
        primaryStage.show();

        //-- Prepare Executor Services
        executor = Executors.newCachedThreadPool(new ThreadFactory()
        {
            @Override
            public Thread newThread(Runnable r)
            {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
        addToQueue = new AddToQueue();
        executor.execute(addToQueue);
        //-- Prepare Timeline
        prepareTimeline();
    }

    public static void main(String[] args)
    {
        launch(args);
    }

    private class AddToQueue implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                // add a item of random data to queue
                dataQ.add(Math.random());
                dataQ2.add(Math.random());
                Thread.sleep(500);
                executor.execute(this);
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(TooltipOnLineChart.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //-- Timeline gets called in the JavaFX Main thread
    private void prepareTimeline()
    {
        // Every frame to take any data from queue and add to chart
        new AnimationTimer()
        {
            @Override
            public void handle(long now)
            {
                addDataToSeries();
            }
        }.start();
    }

    private void addDataToSeries()
    {
        for (int i = 0; i < 20; i++)
        { //-- add 20 numbers to the plot+
            if (dataQ.isEmpty())
                break;
            Data data = new AreaChart.Data(xSeriesData++, dataQ.remove());

            series.getData().add(data);

            data.nodeProperty().addListener(new ChangeListener<Node>()
            {
                @Override
                public void changed(ObservableValue<? extends Node> arg0, Node arg1,
                    Node arg2)
                {
                    Tooltip t = new Tooltip(data.getYValue().toString() + '\n' + data.getXValue());
                    Tooltip.install(arg2, t);
                    data.nodeProperty().removeListener(this);
                }
            });

 
            if (dataQ2.isEmpty())
                break;
            series2.getData().add(new AreaChart.Data(xSeriesData, dataQ2.remove()));
        }
        // remove points to keep us at no more than MAX_DATA_POINTS
        if (series.getData().size() > MAX_DATA_POINTS)
        {
            series.getData().remove(0, series.getData().size() - MAX_DATA_POINTS);
        }

        // remove points to keep us at no more than MAX_DATA_POINTS
        if (series2.getData().size() > MAX_DATA_POINTS)
        {
            series2.getData().remove(0, series2.getData().size() - MAX_DATA_POINTS);
        }

        // update
        xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
        xAxis.setUpperBound(xSeriesData - 1);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TooltipOnLineChart.class
            .getName());
}