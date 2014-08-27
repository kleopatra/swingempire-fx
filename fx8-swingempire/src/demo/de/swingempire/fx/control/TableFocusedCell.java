/*
 * Created on 14.07.2014
 *
 */
package de.swingempire.fx.control;

import java.util.Locale;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import de.swingempire.fx.scene.control.cell.FocusableTableCell;

/**
 * Reported: https://javafx-jira.kenai.com/browse/RT-38326
 * Simple demo to demonstrate the focusedCell bug.
 *
 * To reproduce:
 * 
 * - click into any cell: focusedCell has both row and column
 * - move around with arrow keys: focusedCell changes both row and column
 *   according to the navigation (expected behaviour)
 * - move up/down with ctrl-up/down: focusedCell row
 *   keeps updating according to navigation, column is set to null (bug) 
 */
public class TableFocusedCell extends Application {
    private final ObservableList<Locale> data =
            FXCollections.observableArrayList(Locale.getAvailableLocales()
                    );
   
    private final TableView<Locale> table = new TableView<>(data);
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Table FocusedCell Bug");
        // add a listener to see loosing the column
        table.getFocusModel().focusedCellProperty().addListener((p, oldValue, newValue)-> {
            LOG.info("old/new " + oldValue + "\n  " + newValue);
        });
        TableColumn<Locale, String> language = new TableColumn<>(
                "Language");
        language.setCellValueFactory(new PropertyValueFactory<>("displayLanguage"));
        TableColumn<Locale, String> country = new TableColumn<>("Country");
        country.setCellValueFactory(new PropertyValueFactory<>("country"));
        table.setItems(data);
        table.getColumns().addAll(language, country);
        Scene scene = new Scene(new BorderPane(table));
        scene.getStylesheets().add(getClass().getResource("focusedtablecell.css").toExternalForm());
//        Callback cf = p -> new FocusableTableCell<>();
//        language.setCellFactory(cf);
//        country.setCellFactory(cf);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TableFocusedCell.class
            .getName());
}