/*
 * Created on 26.03.2018
 *
 */
package de.swingempire.fx.fxml;

import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;

/**
 * PieChart not showing. 
 * https://stackoverflow.com/q/49467170/203657
 * 
 * Was: replaced field without inserting. 
 */
public class Operations implements Initializable {

    public Button buttonsubmit;

    public PieChart piechart;

    public Pane pane1, pane2;

    public BarChart barchart;

    public ComboBox combobox;

    Connection con;

    public void combo(ActionEvent e) {

    }

    @FXML
    private void handleOnPieChartAction() {

        try {
            System.out.println("You clicked the Pie Chart!");
//            piechart = new PieChart();
            ObservableList<PieChart.Data> pieChartData = FXCollections
                    .observableArrayList(new PieChart.Data("Negative", 45),
                            new PieChart.Data("Positive", 55));
            piechart.setData(pieChartData);
            System.out.println("You clicked the Pie Chart2!");
            piechart.setVisible(true);
        } catch (Exception e1) {
            // TODO: handle exception
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // TODO Auto-generated method stub
        buttonsubmit.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // TODO Auto-generated method stub
                handleOnPieChartAction();

            }
        });
    }
}

