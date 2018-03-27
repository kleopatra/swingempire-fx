/*
 * Created on 30.01.2015
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * "Remove" empty rows: http://stackoverflow.com/a/27949038/203657
 * suggestion by Eckig - doesn't work
 */
public class TableVisibleRowCountEckig extends Application {

    @Override
    public void start(Stage primaryStage) {

        TableView<String> tableView = new TableView<>();
        tableView.setFixedCellSize(25);
        tableView.prefHeightProperty().bind(tableView.fixedCellSizeProperty().multiply(Bindings.size(tableView.getItems()).add(1.01)));
        tableView.minHeightProperty().bind(tableView.prefHeightProperty());
        tableView.maxHeightProperty().bind(tableView.prefHeightProperty());
        // testing the extended TableView - works fine
//        TableView<String> tableView = new TableViewWithVisibleRowCount<>();
        TableColumn<String, String> col1 = new TableColumn<>();
        col1.setCellValueFactory(cb -> new SimpleStringProperty(cb.getValue()));
        tableView.getColumns().add(col1);
        IntStream.range(0, 10).mapToObj(Integer::toString).forEach(tableView.getItems()::add);


        BorderPane root = new BorderPane(tableView);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
