/*
 * Created on 20.10.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Locale;
import java.util.function.Predicate;

import de.swingempire.fx.collection.FilteredListX;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Filtering looses selection state
 * 
 * To reproduce:
 * - run, select several items starting with "A"
 * - press F1 to filter all starting with  "A"
 * - expected: all previously selected and focused item/s 
 *    still selected/focused
 * - actual: only one item selected (selectedIndex?), focus at 0    
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableViewFilterSelectionRT_39289 extends Application {

    Predicate<Locale> always = p -> true;
    int count;
    @Override
    public void start(Stage primaryStage) throws Exception {
        ObservableList<Locale> items = FXCollections.observableArrayList(Locale.getAvailableLocales());
        FilteredList<Locale> filtered = new FilteredList<>(items, always);
        // inverse check: patched filteredList keeps selection
//        FilteredListX<Locale> filtered = new FilteredListX<>(items, always);
//        filtered.setPredicate(null);
        TableView tableView = new TableView();
        tableView.setItems(filtered);
        TableColumn column = new TableColumn("Column");
        column.setCellValueFactory(new PropertyValueFactory("displayName"));
        tableView.getColumns().addAll(column); 
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tableView.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F1) {
                Predicate old = filtered.getPredicate();
                filtered.setPredicate(old == always ? p -> p.getLanguage().startsWith("a") : always );
            }
        });

        Parent pane = new BorderPane(tableView);
        primaryStage.setScene(new Scene(pane));
        primaryStage.setTitle(System.getProperty("java.version"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        launch(args);
    }

}
