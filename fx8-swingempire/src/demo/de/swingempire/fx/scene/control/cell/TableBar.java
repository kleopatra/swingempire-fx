/*
 * Created on 26.01.2015
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * from SO, how to show relative bars with colors of 
 * a related chart
 * 
 * http://stackoverflow.com/a/28141421/203657
 * 
 * that's a solution with manually calculating and
 * filling a rectangle with base chart colors
 * 
 * In 8u40b20, the colors aren't found (fine for 8u20)
 * 
 * Also trying to use StackedBarChart .. problems as noted.
 * Extracted TableStackedBarChart for SO question.
 */
public class TableBar extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        ObservableList<Data> data = FXCollections.observableArrayList();
        for (int i = 0; i<10; i++) data.add(new Data());

        TableView<Data> tv = new TableView(data);
        TableColumn<Data, Number> col1 = new TableColumn("num1");
        TableColumn<Data, Number> col2 = new TableColumn("num2");
        col1.setCellValueFactory((p)->{return p.getValue().num1;});
        col2.setCellValueFactory((p)->{return p.getValue().num2;});

        //make this column hold the entire Data object so we can access all fields
        TableColumn<Data, Data> col3 = new TableColumn("bar");
        col3.setPrefWidth(500);
        col3.setCellValueFactory((p)->{return new ReadOnlyObjectWrapper<>(p.getValue());});
//        col3.setCellFactory((TableColumn<Data, Data> param) -> {
//            return new TableCell<Data, Data>(){
//
//                @Override
//                protected void updateItem(Data item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (empty) setGraphic (null);
//                    else {
//                        double tot = item.num1.get() + item.num2.get();
//                        double ratio1 = item.num1.get() / tot;
//                        double ratio2 = item.num2.get() / tot;
//
//                        Rectangle r1 = new Rectangle();
//                        //the param is the column, bind so rects resize with column
//                        r1.widthProperty().bind(param.widthProperty().multiply(ratio1));
//                        r1.heightProperty().bind(this.getTableRow().heightProperty().multiply(0.5));
//                        r1.setStyle("-fx-fill: CHART_COLOR_1;");
//
//                        Rectangle r2 = new Rectangle(0, 20);
//                        r2.widthProperty().bind(param.widthProperty().multiply(ratio2));
//                        r2.setStyle("-fx-fill:CHART_COLOR_2;");
//
//                        HBox hbox = new HBox(r1,r2);
//                        hbox.setAlignment(Pos.CENTER_LEFT);
//                        setGraphic(hbox);
//                        setText(null);
//                    }
//                }
//
//            };
//        });

        col3.setCellFactory(p -> new StackedBarChartCell(2000.));
        tv.getColumns().addAll(col1,col2,col3);
        tv.setFixedCellSize(50.);
        
        Scene scene = new Scene(tv);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * TableCell that uses a StackedBarChart to visualize relation of 
     * data.
     * 
     * Problems:
     * - can't max the height of the chart (so it's cut-off in the middle
     * - runs amok without fixedCellSize on tableView
     * - updating the series leaves empty patches horizontally
     * - re-setting the series changes colors randomly
     */
    public static class StackedBarChartCell extends TableCell<Data, Data> {
        
        NumberAxis xAxisHoriz = new NumberAxis();
        CategoryAxis yAxisHoriz = new CategoryAxis();
        StackedBarChart<Number, String> sbcHoriz = new StackedBarChart<>(xAxisHoriz, yAxisHoriz);
        XYChart.Series<Number, String> series1Horiz = new XYChart.Series<>();
        XYChart.Series<Number, String> series2Horiz = new XYChart.Series<>();
        
        
        public StackedBarChartCell(double upperBound) {
            yAxisHoriz.setTickLabelsVisible(false);
            yAxisHoriz.setTickMarkVisible(false);
            yAxisHoriz.setStyle("-fx-border-color: transparent transparent transparent transparent;");
            
            xAxisHoriz.setTickLabelsVisible(false);
            xAxisHoriz.setTickMarkVisible(false);
            xAxisHoriz.setMinorTickVisible(false);
            xAxisHoriz.setStyle("-fx-border-color: transparent transparent transparent transparent;");
            xAxisHoriz.setAutoRanging(false);
            xAxisHoriz.setUpperBound(upperBound);
            xAxisHoriz.setLowerBound(0.);
            
            sbcHoriz.setHorizontalGridLinesVisible(false);
            sbcHoriz.setVerticalGridLinesVisible(false);
            sbcHoriz.setLegendVisible(false);
            sbcHoriz.setAnimated(false);
            
            // scenario A: set series initially
            sbcHoriz.getData().setAll(series1Horiz, series2Horiz);
            sbcHoriz.setCategoryGap(0);
            // no effect
            sbcHoriz.setMaxHeight(20);
        }
        @Override
        protected void updateItem(Data item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(sbcHoriz);
                // scenario B: set new series
                // uncomment for scenario A
//                XYChart.Series<Number, String> series1Horiz = new XYChart.Series<>();
//                XYChart.Series<Number, String> series2Horiz = new XYChart.Series<>();
//                sbcHoriz.getData().setAll(series1Horiz, series2Horiz);
                //---- end of scenario B
                series1Horiz.getData().setAll(new XYChart.Data(item.num1.get(), "none"));
                series2Horiz.getData().setAll(new XYChart.Data(item.num2.get(), "none"));
            }
        }
        
    }
    
    public static class StackedBarChartListCell extends ListCell<Data> {

        final NumberAxis xAxisHoriz = new NumberAxis();
        final CategoryAxis yAxisHoriz = new CategoryAxis();
        final StackedBarChart<Number, String> sbcHoriz =
                new StackedBarChart<>(xAxisHoriz, yAxisHoriz);
        final XYChart.Series<Number, String> series1Horiz =
                new XYChart.Series<>();
        final XYChart.Series<Number, String> series2Horiz =
                new XYChart.Series<>();
        private HBox pane;
        
        
        public StackedBarChartListCell(double upperBound) {
            yAxisHoriz.setTickLabelsVisible(false);
            yAxisHoriz.setTickMarkVisible(false);
            yAxisHoriz.setStyle("-fx-border-color: transparent transparent transparent transparent;");
            
            xAxisHoriz.setTickLabelsVisible(false);
            xAxisHoriz.setTickMarkVisible(false);
            xAxisHoriz.setMinorTickVisible(false);
            xAxisHoriz.setStyle("-fx-border-color: transparent transparent transparent transparent;");
            xAxisHoriz.setAutoRanging(false);
            xAxisHoriz.setUpperBound(upperBound);
            xAxisHoriz.setLowerBound(0.);
            
            sbcHoriz.setHorizontalGridLinesVisible(false);
            sbcHoriz.setVerticalGridLinesVisible(false);
            sbcHoriz.setLegendVisible(false);
            
            sbcHoriz.getData().setAll(series1Horiz, series2Horiz);
            sbcHoriz.setPrefHeight(20.);
            sbcHoriz.setMaxHeight(20.);
            sbcHoriz.setAnimated(false);
//            pane = new BorderPane(sbcHoriz);
            pane =  new HBox(sbcHoriz);
        }
        @Override
        protected void updateItem(Data item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(pane);
                series1Horiz.getData().setAll(new XYChart.Data(item.num1.get(), "none"));
                series2Horiz.getData().setAll(new XYChart.Data(item.num2.get(), "none"));
            }
        }
        
    }
    
    class Data{
        private SimpleIntegerProperty num1 = new SimpleIntegerProperty((int)(Math.random()*1000));
        private SimpleIntegerProperty num2 = new SimpleIntegerProperty((int)(Math.random()*1000));

        public SimpleIntegerProperty num1Property(){return num1;}
        public SimpleIntegerProperty num2Property(){return num2;}
    }
}