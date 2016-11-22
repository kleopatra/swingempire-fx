/*
 * Created on 06.09.2016
 *
 */
package de.saxsys.jfx.tabellen;

import java.time.LocalDate;
import java.util.UUID;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import de.saxsys.jfx.tabellen.model.Person;

public class PersonService {

        private static final int PERSON_COUNT = 100;
        private final ListProperty<Person> persons = new SimpleListProperty<>(FXCollections.observableArrayList());

//        @Inject
        public PersonService(TownService towns) {
                for (int i = 0; i < PERSON_COUNT; i++) {
                        Person alex = new Person();
                        alex.setBirth(LocalDate.of(1983, 5, 2));
                        alex.setFirstname("Alexander" + UUID.randomUUID().toString().charAt(0));
                        alex.setLastname("Casall");
                        alex.setTown(towns.getTowns().getValue().get(0));
                        alex.setSolvency(0.8);
                        Person steffi = new Person();
                        steffi.setBirth(LocalDate.of(1987, 5, 2));
                        steffi.setFirstname("Stefanie");
                        steffi.setLastname("Alberecht");

                        steffi.setSolvency(0.7);
                        steffi.setTown(towns.getTowns().getValue().get(0));

                        Person stefan = new Person();
                        stefan.setBirth(LocalDate.of(1988, 5, 2));
                        stefan.setSolvency(0.3);
                        stefan.setFirstname("Stefan");
                        stefan.setLastname("Heinze");
                        stefan.setTown(towns.getTowns().getValue().get(1));

                        persons.addAll(alex, steffi, stefan);
                }
        }

        public ListProperty<Person> getPersons() {
                return persons;
        }

}
