/*
 * Created on 09.04.2020
 *
 */
package de.swingempire.fx.scene.control;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class ContextMenuProblem extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        PersonTable personTable1 = new PersonTable(Arrays.asList(new Person("Max", "Muster"), new Person("John", "Doe")));
        Tab tab1 = new Tab("Tab 1");
        tab1.setContent(personTable1);

        PersonTable personTable2 = new PersonTable(Arrays.asList(new Person("Petar", "Petrovi"), new Person("Jane", "Q")));
        Tab tab2 = new Tab("Tab 2");
        tab2.setContent(personTable2);

        TabPane tabPane = new TabPane(tab1, tab2);

        stage.setScene(new Scene(tabPane));
        stage.show();
    }

    static class Person {
        final String givenName;
        final String surname;

        Person(String givenName, String surname) {
            this.givenName = givenName;
            this.surname = surname;
        }
    }

    static class PersonTable extends TableView<Person> {
        TableColumn<Person, String> columnGivenName = new TableColumn<>("Given name");
        TableColumn<Person, String> surname = new TableColumn<>("Surname");

        PersonTable(List<Person> persons) {
            columnGivenName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().givenName));
            surname.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().surname));

            getColumns().addAll(columnGivenName, surname);
            getItems().addAll(persons);
            setContextMenu(new ContextMenuPersonTable(this));
        }
    }

    static class ContextMenuPersonTable extends ContextMenu {
        MenuItem menuItemAlertSurname = new MenuItem("Alert Surname");

        ContextMenuPersonTable(PersonTable table) {
            setUserData(table);

            menuItemAlertSurname.setAccelerator(KeyCombination.keyCombination("CTRL+SHIFT+S"));
            menuItemAlertSurname.setOnAction(ContextMenuProblem::handleAlertAction);
            getItems().add(menuItemAlertSurname);
        }
    }

    static void handleAlertAction(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();
        ContextMenu contextMenu = menuItem.getParentPopup();
        PersonTable table = (PersonTable) contextMenu.getUserData();
        TableView.TableViewSelectionModel<Person> selectionModel = table.getSelectionModel();
        Person selectedItem = selectionModel.getSelectedItem();

        if (selectedItem != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(">>> " + selectedItem.surname);
            alert.showAndWait();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
