/*
 * Created on 11.04.2018
 *
 */
package de.swingempire.fx.chart;


import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Share data between charts. https://stackoverflow.com/q/49770442/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public class TestChart extends Application {
        
        // the shared x/y data pairs
        private ObservableList<XYData<Integer, Integer>> backingData;
    
        /**
         * Custom xy data class which exposes its values as properties and nothing
         * else.
         */
        public static class XYData<X, Y> {
            private ObjectProperty<X> xValue;
    
            private ObjectProperty<Y> yValue;
    
            public XYData(X xValue, Y yValue) {
                this.xValue = new SimpleObjectProperty<>(this, "xValue", xValue);
                this.yValue = new SimpleObjectProperty<>(this, "yValue", yValue);
            }
    
            public ObjectProperty<X> xValueProperty() {
                return xValue;
            }
    
            public void setXValue(X value) {
                xValueProperty().set(value);
            }
    
            public X getXValue() {
                return xValueProperty().get();
            }
    
            public ObjectProperty<Y> yValueProperty() {
                return yValue;
            }
    
            public void setYValue(Y value) {
                yValueProperty().set(value);
            }
    
            public Y getYValue() {
                return yValueProperty().get();
            }
        }
    
        @Override
        public void start(Stage stage) {
            if (backingData == null) {
                backingData = createBackingData();
            }
            
           LineChart lineChart = new LineChart(new NumberAxis(), new NumberAxis());
    
            XYChart.Series serie = new XYChart.Series("Test", createChartData());
    
            lineChart.getData().add(serie);
            lineChart.setCursor(Cursor.CROSSHAIR);
            lineChart.setTitle("First Chart");
    
            stage.setTitle("First Chart");
            stage.setScene(new Scene(lineChart, 500, 400));
            stage.show();
    
            secondHandChart();
        }
    
        private void secondHandChart() {
            LineChart lineChart = new LineChart(new NumberAxis(), new NumberAxis());
            XYChart.Series serie = new XYChart.Series("Test", createChartData());
    
            lineChart.getData().add(serie);
    
            lineChart.setCursor(Cursor.CROSSHAIR);
    
            lineChart.setTitle("Second Chart");
            Stage stage = new Stage();
            stage.setTitle("Second Chart");
            
            Button change = new Button("change value");
            change.setOnAction(e -> {
                int next= backingData.get(2).getYValue() + 5;
                backingData.get(2).setYValue(next);
            });
            
            Button add = new Button("add data");
            add.setOnAction(e -> {
                backingData.add(new XYData(backingData.size(), 30));
            });
            HBox buttons = new HBox(10, change, add);
            BorderPane pane = new BorderPane(lineChart);
            pane.setBottom(buttons);
            stage.setScene(new Scene(pane, 500, 400));
            stage.show();
        }
    
        private ObservableList<XYData<Integer, Integer>> createBackingData() {
            // removed extractor, using direct binding
            ObservableList<XYData<Integer, Integer>> data = FXCollections
                    .observableArrayList();
            Integer[] values = { 23, 14, 15, 24, 34, 36, 22, 45, 43, 17, 29, 25 };
            for (int i = 0; i < values.length; i++) {
                data.add(new XYData(i, values[i]));
            }
            return data;
        }
    
        private ObservableList<XYChart.Data<Integer, Integer>> createChartData() {
            ObservableList<XYChart.Data<Integer, Integer>> chartData = FXCollections
                    .observableArrayList();
            for (int i = 0; i < backingData.size(); i++) {
                XYData<Integer, Integer> b = backingData.get(i);
                XYChart.Data<Integer, Integer> cd = new XYChart.Data<>(
                        b.getXValue(), b.getYValue());
                cd.XValueProperty().bind(b.xValueProperty());
                cd.YValueProperty().bind(b.yValueProperty());
                cd.setNode(new HoveredThresholdNode(
                        (i == 0) ? 0 : backingData.get(i - 1).getYValue(),
                        backingData.get(i).getYValue()));
                chartData.add(cd);
            }
            return chartData;
        }
    
        /**
         * a node which displays a value on hover, but is otherwise empty
         */
        class HoveredThresholdNode extends StackPane {
            HoveredThresholdNode(int priorValue, int value) {
                setPrefSize(8, 8);
    
                final Label label = createDataThresholdLabel(priorValue, value);
    
                setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        getChildren().setAll(label);
                        setCursor(Cursor.NONE);
                        toFront();
                    }
                });
                setOnMouseExited(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        getChildren().clear();
                        setCursor(Cursor.CROSSHAIR);
                    }
                });
            }
    
            private Label createDataThresholdLabel(int priorValue, int value) {
                final Label label = new Label(value + "");
                label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
    
                label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
                return label;
            }
        }
    
        public static void main(String[] args) {
            launch(args);
        }
    
        // original: sharing XYChart.Data between charts is not supported
        // private ObservableList<XYChart.Data<Integer, Integer>> getTestValues() {
        // return plot(23, 14, 15, 24, 34, 36, 22, 45, 43, 17, 29, 25);
        // }
        //
        // public ObservableList<XYChart.Data<Integer, Integer>> plot(int... y) {
        // if (_serie == null) {
        // _serie = FXCollections.observableArrayList();
        // int i = 0;
        // while (i < y.length) {
        // XYChart.Data<Integer, Integer> data = new XYChart.Data<>(i + 1, y[i]);
        // data.setNode(
        // new HoveredThresholdNode(
        // (i == 0) ? 0 : y[i - 1],
        // y[i]
        // )
        // );
        //
        // _serie.add(data);
        // i++;
        // }
        // return _serie;
        // }
        // else {
        // return FXCollections.observableArrayList(_serie);
        // }
        // }
        //
    
    }

