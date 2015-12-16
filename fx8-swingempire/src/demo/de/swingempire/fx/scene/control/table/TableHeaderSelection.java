/*
 * Created on 16.12.2015
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * http://stackoverflow.com/q/34313738/203657
 * selection listener not called when clicking header
 * 
 * looks okay
 */
public class TableHeaderSelection extends Application {

    final TableView<String[]> table = new TableView<String[]>();
    ObservableList<String []> data = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {

        Pane pane = new Pane();

        String[][] dat = new String[][] { { "C1", "C2", "C3" },
                { "1", "1", "0" }, { "0", "0", "1" }, { "1", "1", "1" } };

        data.addAll(Arrays.asList(dat));
        data.remove(0);

        for (int i = 0; i < dat[0].length; i++) {
            TableColumn tc = new TableColumn(dat[0][i]);
            final int colNo = i;

            // Adds data to table and calls updateItem, which also colors
            // heatmap
            tc.setCellValueFactory(new Callback<CellDataFeatures<String[], String>, ObservableValue<String>>() {
                public ObservableValue<String> call(
                        CellDataFeatures<String[], String> p) {
                    return new SimpleStringProperty((p.getValue()[colNo]));
                }
            });

            table.getColumns().add(tc);
        }

        table.setItems(data);
        table.setVisible(false);
        table.setVisible(true);

        pane.getChildren().add(table);

        data.addListener(new ListChangeListener<String[]>() {
            @Override
            public void onChanged(Change change) {
                System.out.println("data: ListChangeListener");
                // This prints when a column header is clicked
            }
        });

        table.getSelectionModel().setCellSelectionEnabled(true);
        ObservableList<TablePosition> selectedCells = table.getSelectionModel()
                .getSelectedCells();
        selectedCells.addListener(new ListChangeListener<TablePosition>() {
            @Override
            public void onChanged(Change change) {
                List<Integer> selectedRowIndices = new ArrayList<Integer>();
                List<Integer> selectedColumnIndices = new ArrayList<Integer>();
                try {
                    selectedRowIndices.clear();
                    selectedColumnIndices.clear();

                    for (TablePosition pos : selectedCells) {
                        selectedRowIndices.add(pos.getRow());
                        selectedColumnIndices.add(pos.getColumn());
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage() + e.getCause());
                }
                System.out
                        .println("ListChangeListener<TablePosition>: selectedRows: "
                                + selectedRowIndices
                                + "selectedColumns "
                                + selectedColumnIndices);
                // This never prints even when a column header is selected or
                // deselected
            }
        });

        stage.setScene(new Scene(pane));
        stage.show();
    }

    public static void main(String[] args) {
      launch(args);
    }
  }