/*
 * Created on 07.10.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * throws oncllickin g header, only if unselected?
 * https://javafx-jira.kenai.com/browse/RT-38884
 * 
 * might be related to:
 * https://javafx-jira.kenai.com/browse/RT-38341
 * 
 */
public class TableViewSelectionBugRT_38884 extends Application {

    ObservableList<Person> items = FXCollections.observableArrayList();

    TableView<Person> table = new TableView<Person>();

    public TableViewSelectionBugRT_38884() {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        table.setItems(items);
        TableColumn<Person, String> firstNameCol = new TableColumn<Person, String>(
                "First Name");
        firstNameCol
                .setCellValueFactory(new PropertyValueFactory<Person, String>(
                        "firstName"));
        TableColumn<Person, String> lastNameCol = new TableColumn<Person, String>(
                "Last Name");
        lastNameCol
                .setCellValueFactory(new PropertyValueFactory<Person, String>(
                        "lastName"));
        table.getColumns().setAll(firstNameCol, lastNameCol);
        table.setSortPolicy(this::sortPolicy);
        table.getSortOrder().addListener(
                (Change<? extends TableColumn<Person, ?>> c) -> {
                    sortOrderChanged();
                });
        table.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);

        addPersons();
        // also throws: 
//        table.getSelectionModel().select(0);
//        items.clear();
        //
        Scene scene = new Scene(table, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle(System.getProperty("java.version"));
        primaryStage.show();
    }

    // simulate server call, usually this would take sort order property into
    // account
    private void addPersons() {
        for (int i = 0; i < 50; i++) {
            Person p = new Person();
            int idx = items.size();
            p.setFirstName(idx + " John");
            p.setLastName(idx + "Doe");
            items.add(p);
        }
    }

    private Boolean sortPolicy(TableView<Person> table) {
        return true;
    }

    private void sortOrderChanged() {
        items.clear();
        addPersons();
    }

    private void selectionChanged(Change<? extends Person> c) {
        while (c.next()) {
            if (c.wasRemoved()) {
                System.out.println(c.getRemoved());
                // ^^^ will throw Exception in thread
                // "JavaFX Application Thread" java.util.NoSuchElementException,
                // when a column header is clicked
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public class Person {
        private StringProperty firstName;

        public void setFirstName(String value) {
            firstNameProperty().set(value);
        }

        public String getFirstName() {
            return firstNameProperty().get();
        }

        public StringProperty firstNameProperty() {
            if (firstName == null)
                firstName = new SimpleStringProperty(this, "firstName");
            return firstName;
        }

        private StringProperty lastName;

        public void setLastName(String value) {
            lastNameProperty().set(value);
        }

        public String getLastName() {
            return lastNameProperty().get();
        }

        public StringProperty lastNameProperty() {
            if (lastName == null)
                lastName = new SimpleStringProperty(this, "lastName");
            return lastName;
        }
    }
}
