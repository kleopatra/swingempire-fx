/*
 * Created on 02.08.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 
 * example from https://bugs.openjdk.java.net/browse/JDK-8157687
 * 
 * resizing should honor prefWidth
 * 
 * initially: 
 * - expected: width distributed such that pref width is honored 
 * - actual: columns have equal width
 * 
 * resizing table: 
 * - expected: width distributed proportionally to their pref
 * width (because of binding) 
 * - actual: width distributed equally
 * 
 * resize one of the columns, then resize the table 
 * - expected and actual: table resize distributes excess width proportionally
 * 
 * Dig from reporter: incorrect useage of isFirstRun in constrainedPolicy
 * 
 * 
 * @author
 */
public class TableColumnResizePrefWidthBug extends Application {
    private ObservableList<TableTestItem> tableItems = FXCollections
            .observableArrayList(new TableTestItem("Alpha", "Echo", "Inda"),
                    new TableTestItem("Bravo", "Foxtrot", "Juliett"),
                    new TableTestItem("Charlie", "Golf", "Kilo"),
                    new TableTestItem("Delta", "Hotel", "Lima"));

    @Override
    public void start(Stage primaryStage) {

        TableColumn<TableTestItem, String> tableColumn0 = new TableColumn<>(
                "First Column");
        tableColumn0.setCellValueFactory(p -> p.getValue().columns.get(0));

        TableColumn<TableTestItem, String> tableColumn1 = new TableColumn<>(
                "Second Column");
        tableColumn1.setCellValueFactory(p -> p.getValue().columns.get(1));

        TableColumn<TableTestItem, String> tableColumn2 = new TableColumn<>(
                "Third Column");
        tableColumn2.setCellValueFactory(p -> p.getValue().columns.get(2));

        TableView<TableTestItem> table = new TableView<>();
        table.getColumns().setAll(tableColumn0, tableColumn1, tableColumn2);
        table.setItems(tableItems);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableColumn0.prefWidthProperty()
                .bind(table.widthProperty().multiply(0.20));
        tableColumn1.prefWidthProperty()
                .bind(table.widthProperty().multiply(0.50));
        tableColumn2.prefWidthProperty()
                .bind(table.widthProperty().multiply(0.30));

        Scene scene = new Scene(new VBox(table), 500, 150);

        primaryStage.setTitle("Table Column Sizing");
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    static class TableTestItem {
        List<StringProperty> columns = new ArrayList<>();

        TableTestItem(String column0, String column1, String column2) {
            columns.add(new SimpleStringProperty(column0));
            columns.add(new SimpleStringProperty(column1));
            columns.add(new SimpleStringProperty(column2));
        }
    }
}
