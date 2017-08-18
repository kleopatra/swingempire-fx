/*
 * Created on 09.08.2017
 *
 */
package de.swingempire.fx.control;

import java.util.Locale;
import java.util.function.Predicate;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableWithPagination extends Application {
    // data
    ObservableList<Locale> locales = FXCollections.observableArrayList(Locale.getAvailableLocales()); 
    // data filtered by user input
    FilteredList<Locale> filteredLocales = locales.filtered(null);
    // filtered data per page
    FilteredList<Locale> virtualLocales = filteredLocales.filtered(null);
    int itemCount = 15;
    TableView<Locale> table;
    Pagination pagination;
    
    private Parent getContent() {
        // setup of table
        table = new TableView<>(virtualLocales);
        TableColumn<Locale, String> country = new TableColumn<>("Name");
        country.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        TableColumn<Locale, String> language = new TableColumn<>("Language");
        language.setCellValueFactory(new PropertyValueFactory<>("language"));
        table.getColumns().addAll(country, language);
        
        // setup of pagination
        pagination = new Pagination(locales.size() / itemCount);
        virtualLocales.setPredicate(createPaginationPredicate());
        
        pagination.setPageFactory(e -> {
            virtualLocales.setPredicate(createPaginationPredicate());
            return table;
        });
        
        // setup of filtering
        TextField field = new TextField();
        field.textProperty().addListener((src, ov, nv) -> {
            Predicate<Locale> filterPredicate = locale -> {
                if ((nv == null) || nv.isEmpty()) return true;
                return locale.getDisplayName().contains(nv);
            };
            filteredLocales.setPredicate(filterPredicate);
            pagination.setPageCount(filteredLocales.size() / itemCount + 1);
        });
        
        BorderPane pane = new BorderPane(pagination);
        pane.setBottom(field);
        return pane;
    }

    private Predicate<Locale> createPaginationPredicate() {
        return t -> {
            int indexOf = filteredLocales.indexOf(t);
            int currentItemCount = pagination.getCurrentPageIndex() * itemCount;
            if (indexOf > currentItemCount
                    && indexOf < currentItemCount + itemCount)
                return true;
            else
                return false;
        };

    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableWithPagination.class.getName());
}
