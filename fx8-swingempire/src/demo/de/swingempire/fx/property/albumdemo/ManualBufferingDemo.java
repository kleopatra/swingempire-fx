/*
 * Created on 29.04.2015
 *
 */
package de.swingempire.fx.property.albumdemo;

import java.util.function.Consumer;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.swingempire.fx.demobean.Person;

/**
 * Bind disable property of commit/cancel button to actual change. 
 * http://stackoverflow.com/q/29935643/203657
 */
public class ManualBufferingDemo extends Application {

    private Parent getContent() {
        ObservableList<Person> persons = FXCollections.observableList(Person.persons(), 
                person -> new Observable[] {person.lastNameProperty()});
        ListView<Person> listView = new ListView<>(persons);
        
        TextField lastName = new TextField();
        Consumer<String> committer = text -> System.out.println("committing: " + text);
        BufferedTextInput buffer = new BufferedTextInput(lastName, committer);
        Button save = new Button("Save");
        save.setOnAction(e -> {
            buffer.commit();
        });
        save.disableProperty().bind(Bindings.not(buffer.bufferingProperty()));
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> {
           buffer.flush(); 
        });
        listView.getSelectionModel().selectedItemProperty().addListener((source, old, current) -> {
            buffer.setSubject(current.lastNameProperty());
        });
        cancel.disableProperty().bind(Bindings.not(buffer.bufferingProperty()));
        VBox content = new VBox(listView, lastName, save, cancel);
        return content;
    }

    public static class BufferedTextInput {

        private ReadOnlyBooleanWrapper buffering;
        private StringProperty value;
        private TextField input;
        private Consumer<String> committer;

        public BufferedTextInput(TextField input, Consumer<String> committer) {
            buffering = new ReadOnlyBooleanWrapper(this, "buffering", false);
            value = new SimpleStringProperty(this, "");
            this.input = input;
            this.committer = committer;
            input.textProperty().addListener((source, old, current) -> {
                updateState(old, current);
            });
            input.setOnAction(e -> commit());
        }
        
        private void updateState(String old, String current) {
            if (isBuffering()) return;
            if (value.get().equals(current)) return;
            setBuffering(true);
        }

        public void setSubject(StringProperty value) {
            this.value = value;
            input.setText(value.get());
            setBuffering(false);
        }
        
        public void commit() {
            committer.accept(input.getText());
            this.value.set(input.getText());
            setBuffering(false);
        }
        
        public void flush() {
            input.setText(value.get());
            setBuffering(false);
        }
        
        public boolean isBuffering() {
            return buffering.get();
        }
        
        public ReadOnlyBooleanProperty bufferingProperty() {
            return buffering.getReadOnlyProperty();
        }
        
        private void setBuffering(boolean buffer) {
            buffering.set(buffer);
        }
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
