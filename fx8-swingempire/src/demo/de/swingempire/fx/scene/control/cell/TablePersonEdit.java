/*
 * Created on 05.02.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.scene.control.XTableView;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

/**
 * Triggered by:
 * http://stackoverflow.com/q/35279377/203657
 * 
 * Problem: starting edit of newly added item - not started. Looks like the table
 * is not-yet ready when listening to changes. At that time the cells are not yet
 * updated to new content, making the target cell (still old!) appear not editable
 * thus not starting.
 * 
 * Attempt to dig into internals: how's the interplay between table.edit and recivers?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TablePersonEdit extends Application {

    private final ObservableList<Person> data =
            // Person from Tutorial - with Properties exposed!
            FXCollections.observableArrayList(
                    new Person("Jacob", "Smith", "jacob.smith@example.com"),
                    new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
                    new Person("Ethan", "Williams", "ethan.williams@example.com"),
                    new Person("Emma", "Jones", "emma.jones@example.com"),
                    new Person("Michael", "Brown", "michael.brown@example.com")
//                    , standIn
                    );

   
    private Parent getContent() {

        TableView<Person> table = new XTableView<>();
        table.setItems(data);
        table.setEditable(true);
        
        TableColumn<Person, String> firstName = new TableColumn<>("First Name");
        firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        // PENDING JW: edit prev has weir behaviour with custom cell!
        firstName.setCellFactory(p -> new XTextFieldTableCell(new DefaultStringConverter()));
//        firstName.setCellFactory(TextFieldTableCell.forTableColumn());
        ListChangeListener l = c -> {
            while (c.next()) {
                // true added only
                if (c.wasAdded() && ! c.wasRemoved()) {
                    // force the re-layout before starting the edit
                    table.layout();
                    table.edit(c.getFrom(), firstName);
                    return;
                }
            };
        };
        // install the listener to the items after the skin has registered
        // its own
        ChangeListener skinListener = (src, ov, nv) -> {
            table.getItems().removeListener(l);
            table.getItems().addListener(l);
        };
        table.skinProperty().addListener(skinListener);
        table.getColumns().addAll(firstName);
        
        Button add = new Button("AddAndEdit");
        add.setOnAction(e -> {
            int standInIndex = 1;
            int index = standInIndex < 0 ? table.getItems().size() : standInIndex;
            index =1;
            Person person = createNewItem("edit", index);
            table.getItems().add(index, person);
            
        });
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            int index = 1;//table.getItems().size() -2;
            table.scrollTo(index);
            table.requestFocus();
            table.edit(index, firstName);
        });
        // using an editor which commit on focusLost
        // can't implement something like edit-next: already committed
        // at the time the button action is done
        Button editNext = new Button("Edit Next");
        editNext.setOnAction(e -> {
            TablePosition position = table.getEditingCell();
            if (position == null || position.getRow() <0) return;
            table.edit(position.getRow() + 1, position.getTableColumn());
        });
        table.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F6) {
                TablePosition position = table.getEditingCell();
                if (position == null || position.getRow() <0) return;
                table.edit(position.getRow() + 1, position.getTableColumn());
                e.consume();
            }
        });
        Button editPrev = new Button("Edit Previous");
        editPrev.setOnAction(e -> {
            TablePosition position = table.getEditingCell();
            if (position == null || position.getRow() <0) return;
            table.edit(position.getRow() - 1, position.getTableColumn());
        });
        table.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F5) {
                TablePosition position = table.getEditingCell();
                if (position == null || position.getRow() <0) return;
                table.edit(position.getRow() -1, position.getTableColumn());
                e.consume();
            }
        });
        HBox buttons = new HBox(10, add, edit, editNext, editPrev);
        BorderPane content = new BorderPane(table);
        content.setBottom(buttons);
        return content;
    }
    
    public static class TTableView<S> extends TableView<S> {

        /**
         * Overridden to force a layout before calling super.
         */
        @Override
        public void edit(int row, TableColumn<S, ?> column) {
            layout();
            super.edit(row, column);
        }
        
    }
    
    private Person createNewItem(String text, int index) {
        return new Person(text + index, text + index, text);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 400, 150));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TablePersonEdit.class.getName());
}
