/*
 * Created on 27.09.2020
 *
 */
package de.swingempire.fx.chart;

import java.net.URL;

import javafx.application.Application;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/64040215/203657
 * change color of selected symbol (and all others back)
 * 
 */
public class ChartSymbolColorSelected extends Application {
    private PseudoClass selected = PseudoClass.getPseudoClass("selected");
    private Node selectedSymbol;
    
    protected void setSelectedSymbol(Node symbol) {
        if (selectedSymbol != null) {
            selectedSymbol.pseudoClassStateChanged(selected, false);
        }
        selectedSymbol = symbol;
        if (selectedSymbol != null) {
            selectedSymbol.pseudoClassStateChanged(selected, true);
        }
    }
    @Override
    public void start(Stage primaryStage) throws Exception {  

//-------Create Chart--------------
      NumberAxis xAxis = new NumberAxis();
      NumberAxis yAxis = new NumberAxis();
      
      XYChart.Series<Number,Number> dataSeries1 = new XYChart.Series();
      ScatterChart chart = new ScatterChart(xAxis,yAxis); 
      
      dataSeries1.getData().add(new XYChart.Data( 1, 567));
      dataSeries1.getData().add(new XYChart.Data( 5, 612));
      dataSeries1.getData().add(new XYChart.Data(10, 800));

      chart.getData().add(dataSeries1);

   //-----Select node and change color -----

      for(final XYChart.Data<Number,Number> data : dataSeries1.getData()) { 
          data.getNode().setOnMouseClicked(e-> {
              //that does not work 
//          dataSeries1.getNode().lookup(".chart-symbol").setStyle("-fx-background-color: red"); 
//          data.getNode().setStyle("-fx-background-color: blue" );
//          data.getNode().pseudoClassStateChanged(special, true);
          setSelectedSymbol(data.getNode());
      });
      }

      VBox vbox = new VBox(chart);

      Scene scene = new Scene(vbox, 400, 200);

      primaryStage.setScene(scene);
      primaryStage.setHeight(300);
      primaryStage.setWidth(1200);

      URL uri = getClass().getResource("chartsymbol.css");
      primaryStage.getScene().getStylesheets().add(uri.toExternalForm());

      primaryStage.show();
  }
  
    public static void main(String[] args) {
        Application.launch(args);
    }
}

