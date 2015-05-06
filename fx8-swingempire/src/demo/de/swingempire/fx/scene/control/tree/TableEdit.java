/*
 * Created on 28.04.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.swingempire.fx.demobean.Person;

/**
 * Impossible to reliably start editing after
 * inserting an item.
 * 
 * http://stackoverflow.com/q/29863095/203657
 * 
 * In TableView, only showing if calling scrollTo(row) before
 * starting the edit, hack same (call tableView.laoyut()).
 * 
 * http://stackoverflow.com/q/30060978/203657
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 * @see TreeEdit
 */
public class TableEdit extends Application {

    private Parent getContent() {
        TableView<Person> table = new TableView<>(Person.persons()); 
        table.setEditable(true);
        TableColumn<Person, String> lastName = new TableColumn<>("lastName");
        lastName.setCellValueFactory(new PropertyValueFactory("lastName"));
        lastName.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn<Person, String> firstName = new TableColumn<>("FirstName");
        firstName.setCellValueFactory(new PropertyValueFactory("firstName"));
        firstName.setCellFactory(TextFieldTableCell.forTableColumn());
        table.getColumns().addAll(firstName, lastName);
        
        Button button = new Button("add Person");
        button.setOnAction(e -> {
            Person person = new Person(null, null, null);
            table.getItems().add(person);
            table.getSelectionModel().select(person);
            table.requestFocus();
            int row = table.getItems().size() - 1;
            table.scrollTo(row);
            table.layout();
            table.edit(row, table.getColumns().get(0));
        });
        VBox box = new VBox(table, button);
        return box;
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
