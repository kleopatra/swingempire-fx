/*
 * Created on 04.10.2019
 *
 */
package de.swingempire.fx.fxml;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
/**
 * https://stackoverflow.com/q/58203401/203657
 * passing parameters
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class CustomerApp extends Application {

    public CustomerApp() {
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("customermanager.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Customer {
        StringProperty name ;
        StringProperty city;
        int age;
        
        public Customer(int age, String name, String city) {
            this.name = new SimpleStringProperty(this, "name", name);
            this.city = new SimpleStringProperty(this, "city", city);
            this.age = age;
        }
        
        public StringProperty nameProperty() {
            return name;
        }
        
        public StringProperty cityProperty() {
            return city;
        }
    }
}