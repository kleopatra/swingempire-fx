/*
 * Created on 11.02.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.net.URL;
import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Suspected: row padding is substracted from each cell width?
 * no, not applied to each cell, but now cell columns not aligned with header
 * (no wonder ;)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class PlainTableViewRowPadding extends Application {

    private Parent createContent() {
        TableView<Person> table = createPlainTable();
        BorderPane content = new BorderPane(table);
        return content;
    }

    private TableView<Person> createPlainTable() {
        TableView<Person> table =  new TableView<>(Person.persons());
        table.getColumns().addAll(createColumn("firstName"), 
                createColumn("lastName"), createColumn("email"), createColumn("secondaryMail"));
        return table;
    }
    
    private TableColumn<Person, String> createColumn(String property) {
        TableColumn<Person, String> column = new TableColumn<>(property);
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
            .getLogger(PlainTableViewRowPadding.class.getName());

}
