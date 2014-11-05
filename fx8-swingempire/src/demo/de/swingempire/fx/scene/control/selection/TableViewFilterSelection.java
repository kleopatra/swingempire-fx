/*
 * Created on 20.10.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Locale;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * (Astonishingly) selection copes with filtering, but only selectedIndex?
 * Except for weird scrolling? Also selection lost if removing filter.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableViewFilterSelection extends Application {

    Predicate<Locale> always = p -> true;
    int count;
    @Override
    public void start(Stage primaryStage) throws Exception {
        ObservableList<Locale> items = FXCollections.observableArrayList(Locale.getAvailableLocales());
        FilteredList<Locale> filtered = new FilteredList<>(items, always);
        SortedList<Locale> sorted = new SortedList(filtered);
        
        TableView tableView = new TableView();
        tableView.setItems(sorted);
        sorted.comparatorProperty().bind(tableView.comparatorProperty());
        TableColumn column = new TableColumn("Column");
        column.setCellValueFactory(new PropertyValueFactory("displayName"));
        column.setSortable(true);
        tableView.getColumns().addAll(column); //, display);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Button filter = new Button("toggle filter");
        filter.setOnAction(e -> {
            Predicate old = filtered.getPredicate();
            filtered.setPredicate(old == always ? p -> p.getLanguage().startsWith("a") : always );
        });
        Button add = new Button("Insert at selection");
        add.setOnAction(e -> {
            if (tableView.getSelectionModel().getSelectedIndex() < 0) return;
            int sourceIndex = filtered.getSourceIndex(tableView.getSelectionModel().getSelectedIndex());
            Locale locale = new Locale("Adummy " + count++);
            items.add(sourceIndex, locale);
//            DebugUtils.printSelectionState(listView);
        });
        Button remove = new Button("Remove at selection");
        remove.setOnAction(e -> {
            // remove seems okay
            if (tableView.getSelectionModel().getSelectedIndex() < 0) return;
            int sourceIndex = filtered.getSourceIndex(tableView.getSelectionModel().getSelectedIndex());
            items.remove(sourceIndex);
        });
        Parent pane = new VBox(tableView, filter, add, remove);
        primaryStage.setScene(new Scene(pane));
        primaryStage.setTitle(System.getProperty("java.version"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewFilterSelection.class.getName());
}
