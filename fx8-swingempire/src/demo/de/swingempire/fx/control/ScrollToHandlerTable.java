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
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * http://stackoverflow.com/q/35392740/203657
 * detect when virtualized onctrol scrolled to bottom
 * 
 * Would expect a scrollTo eventHandler to be notified, but isn't?
 */
public class ScrollToHandlerTable extends Application {
    private final ObservableList<Locale> data =
            FXCollections.observableArrayList(Locale.getAvailableLocales()
                    );
   
    private final TableView<Locale> table = new TableView<>(data);
   
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Table FocusedCell Bug");
        configureTable(table);
        Button leftScrollTo = new Button("left scrollto middle");
        leftScrollTo.setOnAction(e -> {
            table.scrollTo(table.getItems().size() /2);
        });
        
        table.setOnScrollTo(e -> {
            LOG.info("" + e);
        });
        table.setOnScroll(e -> {
            LOG.info("" + e);
        });
        HBox root = new HBox(leftScrollTo, table);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
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
    private static final Logger LOG = Logger.getLogger(ScrollToHandlerTable.class
            .getName());
}