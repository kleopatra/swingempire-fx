/*
 * Created on 02.04.2015
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Provide method for re-rendering choicebox selected item.
 * https://javafx-jira.kenai.com/browse/RT-38955
 * 
 * Secondary example: usage errror - use extractor helps.
 * 
 * The original might be similar to https://javafx-jira.kenai.com/browse/RT-22599
 * (provide method to refresh table), which is abour force-refresh as 
 * the very last resort, if notification can't be fired/relied on.
 */
public class ChoiceUpdateItem_38955 extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            VBox root = new VBox();
            Scene scene = new Scene(root, 400, 400);

            /* ChoiceBox, ComboBox, ListView */

            ObservableList<Person> someGuys = FXCollections
                    .observableArrayList(
                            new Callback<Person, Observable[]>() {
                                @Override
                                public Observable[] call(Person player) {
                                    return new Observable[] { player.nameProperty };
                                }
                            }

                            );
            someGuys.add(new Person("Jim"));
            someGuys.add(new Person("Jack"));
            someGuys.add(new Person("Johnny"));

            // alternative binding declaration
            // ObjectProperty<ObservableList<Person>> someItemsProperty = new
            // SimpleObjectProperty<ObservableList<Person>>(someGuys);
            // ChoiceBox<Person> choiceBox = new ChoiceBox<Person>(); //
            // implements Menu
            // choiceBox.itemsProperty().bind(someItemsProperty);
            // ComboBox<Person> comboBox = new ComboBox<Person>(); // implements
            // ListView
            // comboBox.itemsProperty().bind(someItemsProperty);
            // ListView<Person> listView = new ListView<Person>();
            // listView.itemsProperty().bind(someItemsProperty);

            ChoiceBox<Person> choiceBox = new ChoiceBox<Person>(someGuys); // implements
                                                                           // Menu
            ComboBox<Person> comboBox = new ComboBox<Person>(someGuys); // implements
                                                                        // ListView
            ListView<Person> listView = new ListView<Person>(someGuys);

            choiceBox.getSelectionModel().select(0);
            comboBox.getSelectionModel().select(0);
            listView.getSelectionModel().select(0);

            Button removeButton = new Button("Remove Jack");
            removeButton.setOnAction((event) -> {
                for (Person guy : someGuys) {
                    if (guy.getName().equals("Jack")) {
                        someGuys.remove(guy);
                        break;
                    }
                }
            });

            Button addButton = new Button("Add Jameson");
            addButton.setOnAction((event) -> {
                someGuys.add(new Person("Jameson"));
            });

            Button renameButton = new Button("Give Jim a nicer name");
            renameButton.setOnAction((event) -> {
                for (Person guy : someGuys) {
                    if (guy.getName().equals("Jim")) {
                        guy.setName("Jim Beam");
                    }
                }
            });

            root.getChildren().addAll(choiceBox, comboBox, listView, addButton,
                    removeButton, renameButton);

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private class Person {

        public Person(String name) {
            this.nameProperty.set(name);
        }

        StringProperty nameProperty = new SimpleStringProperty();

        public void setName(String value) {
            nameProperty.set(value);
        }

        public String getName() {
            return nameProperty.get();
        }

        @Override
        public String toString() {
            return "Alcoholic " + getName();
        }

    }

}
