/*
 * Created on 04.05.2015
 *
 */
package de.swingempire.fx.scene.control.table;

import de.swingempire.fx.demobean.Person;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Auto-update sort order on changes to a contained item
 * http://stackoverflow.com/q/21171948/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewSortSample extends Application {

    private Parent getContent() {
        // wrap the backing list into an observableList with an extractor
        ObservableList<Person> persons = FXCollections.observableList(
                Person.persons(),
                person -> new Observable[] {person.lastNameProperty(), person.firstNameProperty()}            
        );
        // wrap the observableList into a sortedList
        SortedList<Person> sortedPersons = new SortedList<>(persons);
        // set the sorted list as items to a tableView
        TableView<Person> table = new TableView<>(sortedPersons);
        // bind the comparator of the sorted list to the table's comparator
        sortedPersons.comparatorProperty().bind(table.comparatorProperty());
        TableColumn<Person, String> firstName = new TableColumn<>("First Name");
        firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        TableColumn<Person, String> lastName = new TableColumn<>("Last Name");
        lastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        table.getColumns().addAll(firstName, lastName);
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Person person = table.getSelectionModel().getSelectedItem();
            if (person != null) {
                person.setLastName("z" + person.getLastName());
            }
        });
        VBox pane = new VBox(table, edit);
        return pane;
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
