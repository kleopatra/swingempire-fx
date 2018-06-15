/*
 * Created on 15.06.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import de.swingempire.fx.scene.control.table.TableWithExternalCounterOrg.Dog;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class TableWithExternalCounterOrg extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        BorderPane root = new BorderPane();
        TableView<Animal> table = new TableView<>();
        TableColumn<Animal, String> count = new TableColumn<>("Count");
        TableColumn<Animal, String> name = new TableColumn<>("Name");
        TableColumn<Animal, String> sound = new TableColumn<>("Sound");
        TableColumn<Animal, String> commandsKnown = new TableColumn<>("Commands Known");
        table.getColumns().addAll(count, name, sound, commandsKnown);
        root.setCenter(table);

        count.setCellFactory(callback -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    if (getTableView().getItems().get(getIndex()) instanceof Dog) {
                        int count = setDogCount(getTableView(), getIndex(), 0);
                        setText(String.valueOf(count));
                    } else {
                        setText("");
                    }
                } else {
                    setText("");
                }
            }
        });

        name.setCellValueFactory(data -> data.getValue().nameProperty());
        sound.setCellValueFactory(data -> data.getValue().soundProperty());
        commandsKnown.setCellValueFactory(data -> {
            if(data.getValue() instanceof Dog){
                return ((Dog) data.getValue()).commandsKnownProperty();
            }
            return new SimpleStringProperty("");
        });

        ObservableList<Animal> animals = FXCollections.observableArrayList();
        animals.add(new Dog("Tweeter", "Woof", "Sit, rollover, shake, drop"));
        animals.add(new Dog("Sub Woofer", "Woof", "Sit, rollover, shake"));
        animals.add(new Cat("Kitter Cat", "Meow"));
        animals.add(new Dog("Bass", "Woof", "Sit, rollover, shake, fetch"));

        table.setItems(animals);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private int setDogCount(TableView<Animal> table, int index, int count){
        if(index == 0){
            if(table.getItems().get(index) instanceof Dog) {
                return count + 1;
            } else {
                return count;
            }
        }
        if(table.getItems().get(index) instanceof Dog){
            return setDogCount(table, --index, ++count);
        }else {
            return setDogCount(table, --index, count);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

    public class Animal{
        StringProperty name = new SimpleStringProperty();
        StringProperty sound = new SimpleStringProperty();

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getSound() {
            return sound.get();
        }

        public StringProperty soundProperty() {
            return sound;
        }

        public void setSound(String sound) {
            this.sound.set(sound);
        }
    }

    public class Dog extends Animal{
        StringProperty commandsKnown = new SimpleStringProperty();

        public Dog(String name, String sound, String commandsKnown){
            setName(name);
            setSound(sound);
            setCommandsKnown(commandsKnown);
        }

        public String getCommandsKnown() {
            return commandsKnown.get();
        }

        public StringProperty commandsKnownProperty() {
            return commandsKnown;
        }

        public void setCommandsKnown(String commandsKnown) {
            this.commandsKnown.set(commandsKnown);
        }
    }

    public class Cat extends Animal{

        public Cat(String name, String sound){
            setName(name);
            setSound(sound);
        }
    }
}

