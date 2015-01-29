/*
 * Created on 26.01.2015
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


/**
 * How to show relative bars with colors of 
 * a related chart
 * 
 * http://stackoverflow.com/a/28141421/203657
 * That's a solution with manually calculating and
 * filling a rectangle with base chart colors
 * 
 * Here trying to use StackedBarChart .. problems as noted in cell doc.
 * Extracted TableStackedBarChart for SO question.
 * http://stackoverflow.com/q/28152250/203657
 * 
 * Reported: https://javafx-jira.kenai.com/browse/RT-39884
 * 
 * Seems fixed in 8u40b24 (about Jonathan's current local)
 * TODO: verify fix!!
 * 
 * Note: this diverged from both reported and asked example,
 * (see report/question for original) 
 * trying to improve visuals
 */
public class TableStackedBarModified extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        ObservableList<Data> data = FXCollections.observableArrayList();
        for (int i = 0; i<10; i++) data.add(new Data());

        TableView<Data> tv = new TableView<>(data);
        // remove padding around chartContent - any way to do it in code?
        tv.getStylesheets().add(
                getClass().getResource("chartcell.css").toExternalForm());
        TableColumn<Data, Number> col1 = new TableColumn<>("num1");
        TableColumn<Data, Number> col2 = new TableColumn<>("num2");
        col1.setCellValueFactory((p)->{return p.getValue().num1;});
        col2.setCellValueFactory((p)->{return p.getValue().num2;});

        //make this column hold the entire Data object so we can access all fields
        TableColumn<Data, Data> col3 = new TableColumn<>("bar");
        col3.setPrefWidth(500);
        col3.setCellValueFactory((p)->{return new ReadOnlyObjectWrapper<>(p.getValue());});

        col3.setCellFactory(p -> new StackedBarChartCell(2000.));

        // answer SO: use cell that recreates the chart on each call
//        setCreateAlwaysFactory(col3);
        tv.getColumns().addAll(col1,col2,col3);
        // need a fixed height, 40 is the absolute minimum to see the bar
        // as long as we can't get rid of the padding 
        tv.setFixedCellSize(50.);
        
        Scene scene = new Scene(tv);

        stage.setScene(scene);
        stage.setTitle(System.getProperty("java.version")+ "-" + System.getProperty("java.vm.version"));
        stage.show();
    }

    /**
     * TableCell that uses a StackedBarChart to visualize relation of 
     * data.
     * 
     * Problems with updating items:
     * - scenario A: updating the series leaves empty patches horizontally
     * - scenario B: re-setting the series changes colors randomly
     * 
     * Note: scenario A solved if animated (feels funny, though, seeing the
     * data animated while scrolling)
     * 
     * Other problems
     * - runs amok without fixedCellSize on tableView
     * - can't max the height of the chart (so it's cut-off in the middle
     */
    private static class StackedBarChartCell extends TableCell<Data, Data> {
        
        NumberAxis xAxisHoriz = new NumberAxis();
        CategoryAxis yAxisHoriz = new CategoryAxis();
        StackedBarChart<Number, String> sbcHoriz = new StackedBarChart<>(xAxisHoriz, yAxisHoriz);
        XYChart.Series<Number, String> series1Horiz = new XYChart.Series<>();
        XYChart.Series<Number, String> series2Horiz = new XYChart.Series<>();
        
        
        public StackedBarChartCell(double upperBound) {

            yAxisHoriz.setTickLabelsVisible(false);
            yAxisHoriz.setTickMarkVisible(false);
            // no effect
            yAxisHoriz.setBorder(null);
            // not much effect (the gray gets lighter)
            yAxisHoriz.setStyle("-fx-border-color: transparent transparent transparent transparent;");
            // no effect
            yAxisHoriz.setStyle("-fx-border-color: -fx-background;");
            // fill the height of the plotcontent
            yAxisHoriz.setEndMargin(0);
            yAxisHoriz.setStartMargin(0);
            yAxisHoriz.setPrefWidth(0);
            
            xAxisHoriz.setTickLabelsVisible(false);
            xAxisHoriz.setTickMarkVisible(false);
            xAxisHoriz.setMinorTickVisible(false);
            // to make the border invisible
//            xAxisHoriz.setStyle("-fx-border-color: transparent transparent transparent transparent;");
            xAxisHoriz.setPadding(Insets.EMPTY);
            // effectively remove the xAxis
            // NOTE: implementation detail of layout code in XYChart layoutChartChildren
            xAxisHoriz.setPrefHeight(0);
            xAxisHoriz.setAutoRanging(false);
            xAxisHoriz.setUpperBound(upperBound);
            xAxisHoriz.setLowerBound(0.);
            
            sbcHoriz.setHorizontalGridLinesVisible(false);
            sbcHoriz.setVerticalGridLinesVisible(false);
            sbcHoriz.setLegendVisible(false);
            sbcHoriz.setAnimated(false);
            
            // scenario A: set series initially
            sbcHoriz.getData().setAll(series1Horiz, series2Horiz);
            sbcHoriz.setCategoryGap(1);
            // unpredictable effect?
//            sbcHoriz.setMaxHeight(50);
//            sbcHoriz.setPrefHeight(40);
            // setting the minHeight > 0 makes the bar fit into a fixedCellSize
            sbcHoriz.setMinHeight(1);
            // shows the padding around the plot content 
            sbcHoriz.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, null,  null)));
            // it's two part: on the chart itself (about half of total
            // and around chartContent which we can get rid of with
            // style override .chart-content only
            sbcHoriz.setPadding(Insets.EMPTY);
            // no additional effect
//            sbcHoriz.setBorder(null);
            xAxisHoriz.setBackground(new Background(new BackgroundFill(Color.BEIGE, null,  null)));
            // no effect
//            xAxisHoriz.setBorder(null);
            setAlignment(Pos.BASELINE_CENTER);
            // not much effect, shouldn't to keep alignment with other cells?
//            setPadding(Insets.EMPTY);
            // debug
//            setBackground(new Background(new BackgroundFill(Color.YELLOW, null,  null)));
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
//                LOG.info("" + sbcHoriz.getPrefHeight());
            }
        }
        
    }
    
    
    /**
     * Answer from SO:
     * Recreates the chart on each call to updateItem, hack around - but
     * chart not adjusted to cellWidth
     * 
     * @param col3
     */
    protected void setCreateAlwaysFactory(TableColumn<Data, Data> col3) {
        col3.setCellFactory((TableColumn<Data, Data> param) -> {
            return new TableCell<Data, Data>() {
    
                @Override
                protected void updateItem(Data item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) setGraphic(null);
                    else {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            NumberAxis xAxisHoriz = new NumberAxis(0, 2000, 1000);
                            CategoryAxis yAxisHoriz = new CategoryAxis(FXCollections.observableArrayList(""));
                            XYChart.Series<Number, String> series1Horiz = new XYChart.Series<>();
                            XYChart.Series<Number, String> series2Horiz = new XYChart.Series<>();
                            StackedBarChart<Number, String> sbcHoriz = new StackedBarChart<>(xAxisHoriz, yAxisHoriz);
                            sbcHoriz.getData().setAll(series1Horiz, series2Horiz);
    
                            yAxisHoriz.setStyle("-fx-border-color: transparent transparent transparent transparent;"
                                    + "-fx-tick-labels-visible: false;"
                                    + "-fx-tick-mark-visible: false;"
                                    + "-fx-minor-tick-visible: false;"
                                    + "-fx-padding: 0 0 0 0;");
    
                            xAxisHoriz.setStyle("-fx-border-color: transparent transparent transparent transparent;"
                                    + "-fx-tick-labels-visible: false;"
                                    + "-fx-tick-mark-visible: false;"
                                    + "-fx-minor-tick-visible: false;"
                                    + "-fx-padding: 0 0 0 0;");
    
                            sbcHoriz.setHorizontalGridLinesVisible(false);
                            sbcHoriz.setVerticalGridLinesVisible(false);
                            sbcHoriz.setLegendVisible(false);
                            sbcHoriz.setAnimated(false);
    
                            xAxisHoriz.setMaxWidth(100);
                            sbcHoriz.setMaxWidth(100);
                            sbcHoriz.setPadding(Insets.EMPTY);
    
                            sbcHoriz.setCategoryGap(0);
                            setGraphic(sbcHoriz);
                            series1Horiz.getData().setAll(new XYChart.Data(item.num1.get(), ""));
                            series2Horiz.getData().setAll(new XYChart.Data(item.num2.get(), ""));
                        }
                    }
                }
            };
        });
    }

    private static class Data{
        private SimpleIntegerProperty num1 = new SimpleIntegerProperty((int)(Math.random()*1000));
        private SimpleIntegerProperty num2 = new SimpleIntegerProperty((int)(Math.random()*1000));

        public SimpleIntegerProperty num1Property(){return num1;}
        public SimpleIntegerProperty num2Property(){return num2;}
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TableStackedBarModified.class
            .getName());
}