/*
 * Created on 07.08.2018
 *
 */
package de.swingempire.fx.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

/**
 * Simple example taken from oracle tutorial.
 * https://docs.oracle.com/javafx/2/charts/line-chart.htm
 * 
 * Problem: legenditems should be editable
 * https://stackoverflow.com/q/57179252/203657
 * 
 * Legend can be set (it's an arbitrary node) but then we have to take
 * over completely - Legend is-a TilePane which is a internal class 
 * 
 * Legend.LegendItem is a class that encapsulates the text/symbol, default
 * is shown in a label. But: users (like Legend) privately access its field
 * label and go from there ... even though the type of the item is not part
 * of the api. So we need to replace the items with our own?
 * 
 * needs mor work, this quick hack doesn't report the edited value back to the
 * series!
 */
//@SuppressWarnings({ "unchecked", "rawtypes" })
public class LineChartWithEditableLegendItems extends Application {
    
    public static class LegendItemWrapper {

        private LegendItem item;
        private Node symbol;
        private TextField field;
        private Pane pane;
        private int index;
        
        ReadOnlyStringWrapper text = new ReadOnlyStringWrapper(this, "text");
        
        public LegendItemWrapper(LegendItem item) {
           this(item, -1);
        }
        
        public LegendItemWrapper(LegendItem item, int index) {
            this.item = item;
            this.index = index;
        }
        
        public Node getLegendItemNode() {
            if (pane == null) {
                symbol = item.getSymbol();
                field = new TextField(item.getText());
                text.bind(field.textProperty());
                pane = new HBox(10, symbol, field);
            }
            return pane;
        }
        
        public ReadOnlyStringProperty textProperty() {
            return text.getReadOnlyProperty();
        }
        
        public int getIndex() {
            return index;
        }
    }
    
    public static class MyLineChart<X, Y> extends LineChart<X, Y> {

        public MyLineChart(Axis<X> xAxis, Axis<Y> yAxis) {
            super(xAxis, yAxis);
        }

        private TilePane legendAlias;
        private FlowPane legendReplacement;
        
        private ChangeListener<String> textListener = (src, ov, nv) -> handleTextChange(src, nv);
        
        @Override
        protected void updateLegend() {
            
            // let super do the setup
            super.updateLegend();
            Node legend = getLegend();
            if (legend instanceof TilePane) {
                legendAlias = (TilePane) legend;
                legendReplacement = new FlowPane(10, 10);
                setLegend(legendReplacement);
                
            }
            if (legendAlias != null && legendAlias.getChildren().size() > 0) {
                List<LegendItem> original = ((Legend) legendAlias).getItems();
                List<LegendItemWrapper> wrapper = new ArrayList<>();
                for (int i = 0; i < original.size(); i++) {
                    LegendItemWrapper iw = new LegendItemWrapper(original.get(i), i);
                    iw.textProperty().addListener(textListener);
                    wrapper.add(iw);
                }
                
//                        original.stream()
//                        .map(LegendItemWrapper::new)
//                        .collect(Collectors.toList());
                List<Node> wrapperNodes = wrapper.stream()
                        .map(LegendItemWrapper::getLegendItemNode)
                        .collect(Collectors.toList());
                legendAlias.getChildren().clear();
                legendReplacement.getChildren().setAll(wrapperNodes);
                setLegend(legendReplacement);
            }
        }
        
        private void handleTextChange(ObservableValue<? extends String> src, String nv) {
            LegendItemWrapper wrapper = (LegendItemWrapper)  ((ReadOnlyStringProperty) src).getBean();
            LOG.info("getting change? from " + wrapper);
            int index = wrapper.getIndex();
            Series series = getData().get(index);
            series.setName(nv);
            
        }
        
    }
    
    @Override public void start(Stage stage) {
        stage.setTitle("Line Chart Sample");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
         xAxis.setLabel("Month");
        final LineChart<String,Number> lineChart = 
                new MyLineChart<String,Number>(xAxis,yAxis);
       
        lineChart.setTitle("Stock Monitoring, 2010");
                          
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Portfolio 1");
        
        series1.getData().add(new XYChart.Data<>("Jan", 23));
        series1.getData().add(new XYChart.Data<>("Feb", 14));
        series1.getData().add(new XYChart.Data<>("Mar", 15));
        series1.getData().add(new XYChart.Data<>("Apr", 24));
        series1.getData().add(new XYChart.Data<>("May", 34));
        series1.getData().add(new XYChart.Data<>("Jun", 36));
        series1.getData().add(new XYChart.Data<>("Jul", 22));
        series1.getData().add(new XYChart.Data<>("Aug", 45));
        series1.getData().add(new XYChart.Data<>("Sep", 43));
        series1.getData().add(new XYChart.Data<>("Oct", 17));
        series1.getData().add(new XYChart.Data<>("Nov", 29));
        series1.getData().add(new XYChart.Data<>("Dec", 25));
        
        XYChart.Series<String, Number>  series2 = new XYChart.Series<>();
        series2.setName("Portfolio 2");
        series2.getData().add(new XYChart.Data<>("Jan", 33));
        series2.getData().add(new XYChart.Data<>("Feb", 34));
        series2.getData().add(new XYChart.Data<>("Mar", 25));
        series2.getData().add(new XYChart.Data<>("Apr", 44));
        series2.getData().add(new XYChart.Data<>("May", 39));
        series2.getData().add(new XYChart.Data<>("Jun", 16));
        series2.getData().add(new XYChart.Data<>("Jul", 55));
        series2.getData().add(new XYChart.Data<>("Aug", 54));
        series2.getData().add(new XYChart.Data<>("Sep", 48));
        series2.getData().add(new XYChart.Data<>("Oct", 27));
        series2.getData().add(new XYChart.Data<>("Nov", 37));
        series2.getData().add(new XYChart.Data<>("Dec", 29));
        
        XYChart.Series<String, Number>  series3 = new XYChart.Series<>();
        series3.setName("Portfolio 3");
        series3.getData().add(new XYChart.Data<>("Jan", 44));
        series3.getData().add(new XYChart.Data<>("Feb", 35));
        series3.getData().add(new XYChart.Data<>("Mar", 36));
        series3.getData().add(new XYChart.Data<>("Apr", 33));
        series3.getData().add(new XYChart.Data<>("May", 31));
        series3.getData().add(new XYChart.Data<>("Jun", 26));
        series3.getData().add(new XYChart.Data<>("Jul", 22));
        series3.getData().add(new XYChart.Data<>("Aug", 25));
        series3.getData().add(new XYChart.Data<>("Sep", 43));
        series3.getData().add(new XYChart.Data<>("Oct", 44));
        series3.getData().add(new XYChart.Data<>("Nov", 45));
        series3.getData().add(new XYChart.Data<>("Dec", 44));
        
        lineChart.getData().addAll(series1, series2, series3);
        
        
        BooleanProperty removed = new SimpleBooleanProperty(false);
        Button remove = new Button("remove");
        remove.setOnAction(e -> {
            lineChart.getData().remove(series1);
            removed.set(true);
        });
        remove.disableProperty().bind(removed);
        
        Button add = new Button("add");
        add.setOnAction(e -> {
            
            lineChart.getData().add(series1);
            removed.set(false);
        }
           );
        add.disableProperty().bind(removed.not());
        
        Button editName = new Button("edit name");
        editName.setOnAction(e -> {
            series1.setName(series1.getName() + "X");
        });
        
        HBox buttons = new HBox(10, remove, add, editName);
        BorderPane root = new BorderPane(lineChart);
        root.setBottom(buttons);
        Scene scene  = new Scene(root,800,600);       
       
        stage.setScene(scene);
        stage.show();
    }
 
 
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(LineChartWithEditableLegendItems.class.getName());
}


