/*
 * Created on 06.09.2016
 *
 */
package de.saxsys.jfx.tabellen.model;

import java.time.LocalDate;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Person {

        private static final long SOLVENCY_UPDATE_RATE = 3000L;
        private final StringProperty firstname = new SimpleStringProperty();
        private final StringProperty lastname = new SimpleStringProperty();

        private final ObjectProperty<LocalDate> birth = new SimpleObjectProperty<>();
        private final SimpleObjectProperty<Town> town = new SimpleObjectProperty<>();

        private final DoubleProperty solvency = new SimpleDoubleProperty();

        public Person() {
//                fakeSolvencyProgress();
        }

//        private void fakeSolvencyProgress() {
//                new Thread(() -> {
//                        try {
//                                Thread.sleep(SOLVENCY_UPDATE_RATE - new Random().nextInt((int) SOLVENCY_UPDATE_RATE / 2));
//                        } catch (Exception e) {
//                                e.printStackTrace();
//                        }
//                        Platform.runLater(() -> solvency.set(new Random().nextDouble()));
//                        fakeSolvencyProgress();
//                }).start();
//
//        }

        public String getFirstname() {
                return this.firstname.get();
        }

        public String getLastname() {
                return this.lastname.get();
        }

        public LocalDate getBirth() {
                return this.birth.get();
        }

        public Town getTown() {
                return this.town.get();
        }

        public void setFirstname(String firstname) {
                this.firstname.set(firstname);
        }

        public void setLastname(String lastname) {
                this.lastname.set(lastname);
        }

        public void setBirth(LocalDate birth) {
                this.birth.set(birth);
        }

        public void setTown(Town town) {
                this.town.set(town);
        }

        public void setSolvency(double solvency) {
                this.solvency.set(solvency);
        }

        public double getSolvency() {
                return solvency.get();
        }

        public StringProperty firstnameProperty() {
                return firstname;
        }

        public StringProperty lastnameProperty() {
                return lastname;
        }

        public DoubleProperty solvencyProperty() {
                return solvency;
        }

        public ObjectProperty<LocalDate> birthProperty() {
                return birth;
        }

        public SimpleObjectProperty<Town> townProperty() {
                return town;
        }

}
