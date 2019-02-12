/*
 * Created on 11.02.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.net.URL;
import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TableRowSkin: incorrect layout if row has padding.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class BugTableBugRowPadding extends Application {

    private Parent createContent() {
        TableView<Locale> table = createPlainTable();
        BorderPane content = new BorderPane(table);
        return content;
    }

    private TableView<Locale> createPlainTable() {
        TableView<Locale> table =  new TableView<>(
                FXCollections.observableArrayList(Locale.getAvailableLocales()));
        table.getColumns().addAll(createColumn("displayLanguage"), 
                createColumn("displayCountry"), createColumn("displayLanguage"));
        return table;
    }
    
    private TableColumn<Locale, String> createColumn(String property) {
        TableColumn<Locale, String> column = new TableColumn<>(property);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        URL uri = getClass().getResource("rowpadding.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(BugTableBugRowPadding.class.getName());

}
