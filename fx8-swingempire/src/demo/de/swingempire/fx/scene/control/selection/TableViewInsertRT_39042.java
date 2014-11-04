/*
 * Created on 20.10.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Locale;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-39042
 * TableView is fine (except the usual focus off by one): 
 * reason is that the selectionModel/FocusModel _don't_
 * listen to the items property, updating the contentListener is done
 * exclusively in the itemsProperty of tableView.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableViewInsertRT_39042 extends Application {

    int count;
    @Override
    public void start(Stage primaryStage) throws Exception {
//        ObservableList items = FXCollections.observableArrayList("one", "two", "three");     
        ObservableList<Locale> items = FXCollections.observableArrayList(Locale.getAvailableLocales());

        TableView tableView = new TableView();
        tableView.setItems(items);
        TableColumn column = new TableColumn("Column");
        column.setCellValueFactory(new PropertyValueFactory<>("language"));
        tableView.getColumns().addAll(column);
        Button add = new Button("Insert at selection");
        add.setOnAction(e -> {
            if (tableView.getSelectionModel().getSelectedIndex() < 0) return;
            tableView.getItems().add(tableView.getSelectionModel().getSelectedIndex(), 
                    new Locale("dummy " + count++));
//            DebugUtils.printSelectionState(listView);
        });
        // check if always on new items ... yes
        Button setItems = new Button("Set items");
        setItems.setOnAction(e -> {
            ObservableList newItems = FXCollections.observableArrayList(Locale.getAvailableLocales());
            tableView.setItems(newItems);
        });
        Button remove = new Button("Remove at selection");
        remove.setOnAction(e -> {
            // remove seems okay
            if (tableView.getSelectionModel().getSelectedIndex() < 0) return;
            tableView.getItems().remove(tableView.getSelectionModel().getSelectedIndex());
        });
        Parent pane = new VBox(tableView, add, setItems, remove);
        primaryStage.setScene(new Scene(pane));
        primaryStage.setTitle(System.getProperty("java.version"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
