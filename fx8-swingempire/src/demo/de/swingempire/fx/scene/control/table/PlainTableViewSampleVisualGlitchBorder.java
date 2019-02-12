/*
 * Created on 11.02.2019
 *
 */
package de.swingempire.fx.scene.control.table;

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
 * Visual layout glitch at left/right border of a table: at some
 * values of hbar the border is (partly?) hidden by cell.
 * 
 * To reproduce:
 * - run and make sure that the vbar is not visible, the hbar is visible 
 *     and there are enough pixels for scrolling horizontally
 * - move hbar pixel-wise and look closely at the right border of table
 * - expected: right border visible at its full width at all times
 * - actual: at some values, the right border is much thinner than most of the time
 * 
 * actually similar at left border, but harder to detect: at both sides, there's
 * a detectable jitter of the border when scrolling quickly (look at the empty rows) 
 * - probably produced by cutting the border width on/off somehow.
 * 
 * Could be a rounding error in clipping? Just guessing ...
 * 
 * reported: 
 * https://bugs.openjdk.java.net/browse/JDK-8218745
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class PlainTableViewSampleVisualGlitchBorder extends Application {

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
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(PlainTableViewSampleVisualGlitchBorder.class.getName());

}
