/*
 * Created on 14.10.2014
 *
 */
package de.swingempire.fx.scene.control.table;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-38892
 * Replacing a column with selected cell disables keyboard navigation
 * (and blocks cpu!)
 * 
 * Suggested fix (from Jonathan) is hard-coded focus to (0,0)
 * might be not overly user-friendly
 */
public class TableViewFocusTestRT_38892 extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FlowPane flowPane = new FlowPane();
        Scene scene = new Scene(flowPane, 500, 500);
        stage.setScene(scene);

        ObservableList<Person> data = FXCollections.observableArrayList(
                new Person("Jacob", "Smith"),
                new Person("Isabella", "JohnsoJn"),
                new Person("Ethan", "Williams"),
                new Person("Emma", "Jones"),
                new Person("Michael", "Brown")
        );

        TableView<Person> table = new TableView<>();
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Person,String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        table.getColumns().addAll(firstNameCol, create2ndColumn(), create2ndColumn());
        table.setItems(data);

        Button button = new Button("Replace 2nd Column");
        button.setFocusTraversable(false);
        button.setOnAction(actionEvent -> {
            int last = table.getColumns().size() - 1;
//            table.getColumns().remove(last);
            table.getColumns().set(last, create2ndColumn());
        });
        flowPane.getChildren().addAll(table, button);
        stage.show();
    }

    public TableColumn<Person,String> create2ndColumn() {
        TableColumn<Person,String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        return lastNameCol;
    }

    public static class Person {
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private Person(String fName, String lName) {
            this.firstName = new SimpleStringProperty(fName);
            this.lastName = new SimpleStringProperty(lName);
        }
        public String getFirstName() {return firstName.get();}
        public void setFirstName(String fName) {firstName.set(fName);}
        public String getLastName() {return lastName.get();}
        public void setLastName(String fName) {lastName.set(fName);}
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
