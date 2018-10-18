/*
 * Created on 16.10.2018
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52838444/203657
 * disallow selection change if selected item is not valid
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class VetoableSelection extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    boolean changing;
    @Override
    public void start(Stage primaryStage) {

        // Simple Interface
        HBox root = new HBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        ObservableList<Person> persons = FXCollections.observableArrayList(
                p -> new Observable[]{p.nameProperty(), p.emailProperty()});
        persons.setAll(new Person("Jack", "j@email.com"),
                new Person("Bill", "bill@email.com"),
                new Person("Diane", "dd@email.com"));

        // Simple ListView
        ListView<Person> listView = new ListView<>(persons);
//        listView.getItems().setAll(
//                new Person("Jack", "j@email.com"),
//                new Person("Bill", "bill@email.com"),
//                new Person("Diane", "dd@email.com")
//        );

        // TextFields to edit values
        TextField txtName = new TextField();
        TextField txtEmail = new TextField();

        // Add controls to the root layout
        root.getChildren().addAll(
                listView,
                new VBox() {{
                    getChildren().addAll(
                            new Label("Name:"),
                            txtName,
                            new Label("Email:"),
                            txtEmail);
                }}
        );

        // Add listener to update bindings for the TextFields
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            LOG.info("old: " + oldValue +  "new " + newValue);
            // Unbind previously bound values
            if (oldValue != null) {

                // If name or email are missing, prevent the change
                if (!changing && !validate(oldValue)) {
                    changing = true;
                    Platform.runLater(() -> {
                        listView.getSelectionModel().select(oldValue);
                        changing = false;
                        
                    });
                    return; // *** This is where I need help as this obviously is not correct *** //
                    
                }

                txtName.textProperty().unbindBidirectional(oldValue.nameProperty());
                txtEmail.textProperty().unbindBidirectional(oldValue.emailProperty());
            }

            // Bind the new values
            if (newValue != null) {
                txtName.textProperty().bindBidirectional(newValue.nameProperty());
                txtEmail.textProperty().bindBidirectional(newValue.emailProperty());

                // Refresh the ListView to show changes
//                listView.refresh();
            }

        });

        // Show the stage
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Sample");
        primaryStage.show();
    }

    private boolean validate(Person oldValue) {
        return !(oldValue.getName().trim().isEmpty() || oldValue.getName().trim().isEmpty());
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(VetoableSelection.class.getName());
}

class Person {

    private StringProperty name = new SimpleStringProperty();
    private StringProperty email = new SimpleStringProperty();

    public Person(String name, String email) {
        this.name.set(name);
        this.email.set(email);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    @Override
    public String toString() {
        return name.get();
    }
}

