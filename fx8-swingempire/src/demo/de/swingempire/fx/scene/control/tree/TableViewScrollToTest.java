/*
 * Created on 06.05.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TableViewScrollToTest extends Application {

    private final TableView<Person> table = new TableView<>();
    private final ObservableList<Person> data = FXCollections.observableArrayList();
    private final HBox hb = new HBox();
    private final Button addButton = new Button("_Add");
    private int seq = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(new Group());
        primaryStage.setTitle("Table View Scroll To Test");
        primaryStage.setWidth(450);
        primaryStage.setHeight(300);

        table.setEditable(true);

        TableColumn<Person, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("firstName"));
        firstNameCol.setCellFactory(TextFieldTableCell.<Person> forTableColumn());
        firstNameCol.setOnEditCommit((CellEditEvent<Person, String> event) -> {
            event.getRowValue().setFirstName(event.getNewValue());
        });

        TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("lastName"));
        lastNameCol.setCellFactory(TextFieldTableCell.<Person> forTableColumn());
        lastNameCol.setOnEditCommit((CellEditEvent<Person, String> event) -> {
            event.getRowValue().setLastName(event.getNewValue());
        });

        TableColumn<Person, String> emailCol = new TableColumn<>("Email");
        emailCol.setMinWidth(200);
        emailCol.setCellValueFactory(new PropertyValueFactory<Person, String>("email"));
        emailCol.setCellFactory(TextFieldTableCell.<Person> forTableColumn());
        emailCol.setOnEditCommit((CellEditEvent<Person, String> event) -> {
            event.getRowValue().setEmail(event.getNewValue());
        });

        table.setItems(data);
        table.getColumns().addAll(firstNameCol, lastNameCol, emailCol);
        table.setPrefHeight(165);

        addButton.setOnAction(event -> {
            table.getSelectionModel().clearSelection();
            Person p = new Person("First Name", "Last Name " + seq++, "");
            data.add(p);
            table.getSelectionModel().select(p);
            Platform.runLater(() -> {
                table.scrollTo(p); //causes buggy behavior
                table.edit(data.indexOf(p), firstNameCol);
            });
        });

        hb.getChildren().add(addButton);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(table, hb);

        ((Group)scene.getRoot()).getChildren().addAll(vbox);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static class Person {

        private StringProperty firstName;
        private StringProperty lastName;
        private StringProperty email;

        public Person(String first, String last, String email) {
            firstName = new SimpleStringProperty(this, "firstName", first);
            lastName = new SimpleStringProperty(this, "lastName", last);
            this.email = new SimpleStringProperty(this, "email", email);
        }

        public void setFirstName(String value) { firstNameProperty().set(value); }
        public String getFirstName() { return firstNameProperty().get(); }
        public StringProperty firstNameProperty() { 
            if (firstName == null) firstName = new SimpleStringProperty(this, "firstName", "First");
            return firstName; 
        }

        public void setLastName(String value) { lastNameProperty().set(value); }
        public String getLastName() { return lastNameProperty().get(); }
        public StringProperty lastNameProperty() { 
            if (lastName == null) lastName = new SimpleStringProperty(this, "lastName", "Last");
            return lastName; 
        } 

        public void setEmail(String value) { emailProperty().set(value); }
        public String getEmail() { return emailProperty().get(); }
        public StringProperty emailProperty() {
            if(email == null) {
                email = new SimpleStringProperty(this, "email", "");
            }
            return email;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}