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
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * http://stackoverflow.com/a/25742558/203657
 * 
 * force 2 scrollbars to same value
 * 
 * OP's self-solution was a listener - why not bind?
 */
public class SynchScrollBars extends Application {
    private final ObservableList<Locale> data =
            FXCollections.observableArrayList(Locale.getAvailableLocales()
                    );
   
    private final TableView<Locale> table = new TableView<>(data);
    private final TableView<Locale> table2 = new TableView<>(data);
    
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Table FocusedCell Bug");
        configureTable(table);
        configureTable(table2);
        HBox root = new HBox(table, table2);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        ScrollBar one =  (ScrollBar) table.lookup(".scroll-bar");
        ScrollBar other = (ScrollBar) table2.lookup(".scroll-bar");
        other.valueProperty().bindBidirectional(one.valueProperty());
    }

    protected void configureTable(TableView<Locale> table) {
        TableColumn<Locale, String> language = new TableColumn<>(
                "Language");
        language.setCellValueFactory(new PropertyValueFactory<>("displayLanguage"));
        TableColumn<Locale, String> country = new TableColumn<>("Country");
        country.setCellValueFactory(new PropertyValueFactory<>("country"));
        table.setItems(data);
        table.getColumns().addAll(language, country);
    }

    public static void main(String[] args) {
        launch(args);
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(SynchScrollBars.class
            .getName());
}