/*
 * Created on 07.04.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ListSelection_36911 extends Application {
    ListView<Item> tv;

    @Override
    public void start(Stage primaryStage) {
        tv = new ListView<>();
        tv.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tv.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener() {

                    @Override
                    public void changed(ObservableValue observable,
                            Object oldValue, Object newValue) {
                        // select what is selected - should be no-op
                        int row = tv.getItems().indexOf(newValue);
                        System.out.println("old/new " + oldValue + "/" + newValue + " row: " + row + "selectedIndex: " + tv.getSelectionModel().getSelectedIndex());
                        tv.getSelectionModel().clearAndSelect(row);
                    }
                });
        tv.getItems().addAll(new Item("A"), new Item("B"), new Item("C"));

        StackPane root = new StackPane();
        root.getChildren().add(tv);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("TableView Selection Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Item {
        StringProperty name = new SimpleStringProperty(this, "name");

        public Item(String n) {
            name.set(n);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String n) {
            name.set(n);
        }

        public StringProperty nameProperty() {
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }
        
        
    }
}
