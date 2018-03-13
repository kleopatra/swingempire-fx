/*
 * Created on 28.02.2018
 *
 */
package de.swingempire.fx.chart;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BarChartMain extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("barchart.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(BarChartMain.class.getResource("barfillcolor.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
      launch(args);
    }
  }

