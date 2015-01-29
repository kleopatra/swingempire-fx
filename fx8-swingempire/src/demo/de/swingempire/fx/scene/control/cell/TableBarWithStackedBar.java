/*
 * Created on 26.01.2015
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


/**
 * How to show relative bars with colors of 
 * a related chart
 * http://stackoverflow.com/a/28141421/203657
 * That's a solution with manually calculating and
 * filling a rectangle with base chart colors
 * 
 * Assuming RT-39884 is fixed, this is all about 
 * - improve the cell with theming the chart
 * - solve the same color as related chart
 * 
 * @see TableStackedBarModified
 */
public class TableBarWithStackedBar extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        ObservableList<DataX> data = FXCollections.observableArrayList();
        for (int i = 0; i<10; i++) data.add(new DataX());

        TableView<DataX> tv = new TableView<>(data);
        TableColumn<DataX, Number> col1 = new TableColumn<>("num1");
        TableColumn<DataX, Number> col2 = new TableColumn<>("num2");
        TableColumn<DataX, Number> colN = new TableColumn<>("num3");
        col1.setCellValueFactory((p)->{return p.getValue().num1;});
        col2.setCellValueFactory((p)->{return p.getValue().num2;});
        colN.setCellValueFactory((p)->{return p.getValue().num3;});

        //make this column hold the entire Data object so we can access all fields
        TableColumn<DataX, DataX> col3 = new TableColumn<>("bar");
        col3.setPrefWidth(500);
        col3.setCellValueFactory((p)->{return new ReadOnlyObjectWrapper<>(p.getValue());});
        // for values
//        col3.setCellFactory(p -> new StackedBarChartCell(2000.));
        // for ratio
        col3.setCellFactory(p -> new StackedBarChartCell(100., d -> d.getNum1Ratio(), /*d-> 0.,*/ d -> d.getNum3Ratio()));

        // answer SO: use cell that recreates the chart on each call
//        setCreateAlwaysFactory(col3);
        tv.getColumns().addAll(col1,col2,colN, col3);
        // need to set a fixed size
        tv.setFixedCellSize(30.);
        
        int sum1 = sum(data, d-> d.num1.get()); //data.stream().mapToInt(d -> d.num1.get()).sum();
        int sum2 = sum(data, d -> d.num2.get()); //data.stream().mapToInt(d -> d.num2.get()).sum();
        int sum3 = sum(data, d-> d.num3.get()); //data.stream().mapToInt(d -> d.num3.get()).sum();
        
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("num1", sum1),
                new PieChart.Data("num2", sum2),
                new PieChart.Data("num3", sum3)
                );
        PieChart pie = new PieChart(pieData);
        
        BorderPane pane = new BorderPane(tv);
        
        pane.setLeft(pie);
        Scene scene = new Scene(pane);

        stage.setScene(scene);
        // remove padding around chartContent - any way to do it in code?
        scene.getStylesheets().add(
                getClass().getResource("chartcell.css").toExternalForm());
        scene.getRoot().getStyleClass().add(".chart-root");
        stage.setTitle(System.getProperty("java.version")+ "-" + System.getProperty("java.vm.version"));
        stage.show();
    }

    private int sum(List<DataX> list, ToIntFunction<DataX> function) {
        int sum = list.stream().mapToInt(function).sum();
        return sum;
    }
    /**
     * TableCell that uses a StackedBarChart to visualize relation of 
     * data.
     * 
     * Problems with updating items:
     * - scenario A: updating the series leaves empty patches horizontally
     * - scenario B: re-setting the series changes colors randomly
     * 
     * Other problems
     * - runs amok without fixedCellSize on tableView
     * - can't max the height of the chart (so it's cut-off in the middle
     */
    private static class StackedBarChartCell extends TableCell<DataX, DataX> {
        
        private StackedBarChart<Number, String> bar; 
//        private XYChart.Series<Number, String> series1Horiz = new XYChart.Series<>();
//        private XYChart.Series<Number, String> series2Horiz = new XYChart.Series<>();
        private double upperBound;
        List<Function<DataX, Number>> providers;
        
        public StackedBarChartCell(double upperBound, Function<DataX, Number>... provider) {
            getStyleClass().add("chart-cell");
            this.upperBound = upperBound;
            this.providers = FXCollections.observableArrayList(provider);
//            bar = createChart();
        }
        
        protected StackedBarChart<Number, String> createChart() {
            NumberAxis xAxis = new NumberAxis();
            CategoryAxis yAxis = new CategoryAxis();
            xAxis.setAutoRanging(false);
            xAxis.setUpperBound(this.upperBound);

            StackedBarChart<Number, String> bar =  new StackedBarChart<>(xAxis, yAxis);
            bar.setAnimated(false);

            // scenario A: set series initially
//            bar.getData().setAll(series1Horiz, series2Horiz);
            providers.stream().forEachOrdered(p -> bar.getData().add(new XYChart.Series<>()));
            // debug: shows the padding around the plot content 
            bar.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, null,  null)));
            return bar;
        }
        
        @Override
        protected void updateItem(DataX item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                if (bar == null) {
                    bar = createChart();
                }
                setGraphic(bar);
                // scenario B: set new series
                // uncomment for scenario A
//                XYChart.Series<Number, String> series1Horiz = new XYChart.Series<>();
//                XYChart.Series<Number, String> series2Horiz = new XYChart.Series<>();
//                bar.getData().setAll(series1Horiz, series2Horiz);
                //---- end of scenario B
//                series1Horiz.getData().setAll(new XYChart.Data(item.num1.get(), "none"));
//                series2Horiz.getData().setAll(new XYChart.Data(item.num2.get(), "none"));
                for (int i = 0; i < providers.size(); i++) {
                    bar.getData().get(i).getData().setAll(new XYChart.Data<>(providers.get(i).apply(item), "none"));
                }
//                series1Horiz.getData().setAll(new XYChart.Data(item.getNum1Ratio(), "none"));
//                series2Horiz.getData().setAll(new XYChart.Data(item.getNum3Ratio(), "none"));
            }
        }
        
    }

    private static class DataX{
        private SimpleIntegerProperty num1 = new SimpleIntegerProperty((int)(Math.random()*1000));
        private SimpleIntegerProperty num2 = new SimpleIntegerProperty((int)(Math.random()*1000));
        private SimpleIntegerProperty num3 = new SimpleIntegerProperty((int) (Math.random()* 1000));
        private NumberBinding sum = Bindings.add(num1, Bindings.add(num2, num3));
//                new DoubleBinding() {
//            {
//                bind(num1, num2, num3);
//            }
//            @Override
//            protected double computeValue() {
//                return num1.get() + num2.get() + num3.get();
//            }
//            
//        };
        public SimpleIntegerProperty num1Property(){return num1;}
        public SimpleIntegerProperty num2Property(){return num2;}
        public SimpleIntegerProperty num3Property() {return num3;}
        
        public double getNum1Ratio() {
            return ratio(num1);
        }
        
        public double getNum2Ratio() {
            return ratio(num2);
        }
        
        public double getNum3Ratio() {
            return ratio(num3);
        }
        
        private double ratio(IntegerProperty value) {
            return (value.get() * 100) / sum.getValue().doubleValue();
        }
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TableBarWithStackedBar.class
            .getName());
}