/*
 * Created on 15.08.2014
 *
 */
package de.swingempire.fx.demobean;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Example fx bean. Copied from tutorial with added
 * property accessors.
 */
public class Person {
    private final SimpleStringProperty firstName;
    private final SimpleStringProperty lastName;
    private final SimpleStringProperty email;
    private final SimpleStringProperty secondaryMail;
    
    public Person(String fName, String lName) {
        this(fName, lName, "");
    }
    public Person(String fName, String lName, String email) {
        this.firstName = new SimpleStringProperty(fName);
        this.lastName = new SimpleStringProperty(lName);
        this.email = new SimpleStringProperty(email);
        this.secondaryMail = new SimpleStringProperty("xx" + email);
    }
    
    public String getFirstName() {
        return firstName.get();
    }
    public void setFirstName(String fName) {
        firstName.set(fName);
    }
    
    public StringProperty firstNameProperty() {
        return firstName;
    }
    public String getLastName() {
        return lastName.get();
    }
    public void setLastName(String fName) {
        lastName.set(fName);
    }
    
    public StringProperty lastNameProperty() {
        return lastName;
    }
    public String getEmail() {
        return email.get();
    }
    public void setEmail(String fName) {
        email.set(fName);
    }
    
    public StringProperty emailProperty() {
        return email;
    }
    
    public String getSecondaryMail() {
        return secondaryMailProperty().get();
    }
    
    public void setSecondaryMail(String mail) {
        secondaryMailProperty().set(mail);
    }
    public StringProperty secondaryMailProperty() {
        return secondaryMail;
    }
    
    @Override
    public String toString() {
        return getLastName() + ", " + getFirstName();
    }
    public static ObservableList<Person> persons() {
        return FXCollections.observableArrayList(
                new Person("Jacob", "Smith", "jacob.smith@example.com"),
                new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
                new Person("Ethan", "Williams", "ethan.williams@example.com"),
                new Person("Emma", "Jones", "emma.jones@example.com"),
                new Person("Lucinda", "Micheals", "lucinda.micheals@example.com"),
                new Person("Michael", "Brown", "michael.brown@example.com"),
                new Person("Barbara", "Pope", "barbara.pope@example.com"),
                new Person("Penelope", "Rooster", "penelope.rooster@example.com"),
                new Person("Raphael", "Adamson", "raphael.adamson@example.com"));
        
    }
}