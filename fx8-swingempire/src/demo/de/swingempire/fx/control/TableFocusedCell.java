/*
 * Created on 14.07.2014
 *
 */
package de.swingempire.fx.control;

import java.util.Locale;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

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
 *   
 * Plus: incorrect focus cell on adding items at/above the focus
 * reported https://javafx-jira.kenai.com/browse/RT-38491  
 *   - select first row
 *   - press f1 to insert row at top
 *   - expected: second row selected and focused
 *   - actual: second row selected and third row focused [1]
 *   - press shift-down to extend selection
 *   - expected: second and third row selected [2]
 *   - actual: first to fourth row selected 
 *    
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
        
        // quick addition to make table editable in second column
        // for issue ?? : not able to start editing with f2
//        table.setEditable(true);
//        country.setCellFactory(TextFieldTableCell.forTableColumn());
        
        // quick check for http://stackoverflow.com/q/25740177/203657
        // right click doesn't always select before showing context menu
        // can't reproduce
//        data.remove(8, data.size() -1);
//        table.setRowFactory(new Callback<TableView<Locale>, TableRow<Locale>>() {
//            @Override
//            public TableRow<Locale> call(TableView<Locale> tableView) {
//                final TableRow<Locale> row = new TableRow<>();
//                final ContextMenu contextMenu = new ContextMenu();
//                final MenuItem mnuItemAnalyze = new MenuItem("Analyze");
//                mnuItemAnalyze.setOnAction(new EventHandler<ActionEvent>() {
//                    @Override
//                    public void handle(ActionEvent event) {
//                        //logic for menu item
//                        LOG.info("Menu: ");
//                    }
//                });
//                contextMenu.getItems().add(mnuItemAnalyze);
//                // Set context menu on row, but use a binding to make it only show for non-empty rows:  
//                row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
//                return row;
//            }
//        });


        
        table.setItems(data);
        table.getColumns().addAll(language, country);
        
        // https://javafx-jira.kenai.com/browse/RT-38491
        // incorrect extend selection after inserting item
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F1) {
                data.add(0, new Locale("dummy"));
            }
        });
        
        // extend selection after programmatically select?
        table.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F3) {
                table.getSelectionModel().clearAndSelect(2);
            }
        });
        
        // extend selection after programmatically select?
        table.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F4) {
                table.getSelectionModel().clearSelection(table.getSelectionModel().getSelectedIndex());
//                table.getSelectionModel().clearSelection();
                table.getSelectionModel().selectRange(2, 4);
                LOG.info("anchor after range: " + table.getProperties().get("anchor"));
            }
        });
        

        Button button = new Button("Add");
        button.setOnAction(ev -> {
            data.add(0, new Locale("dummy"));
        });
        BorderPane root = new BorderPane(table);
        Scene scene = new Scene(root);
//        scene.getStylesheets().add(getClass().getResource("focusedtablecell.css").toExternalForm());
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