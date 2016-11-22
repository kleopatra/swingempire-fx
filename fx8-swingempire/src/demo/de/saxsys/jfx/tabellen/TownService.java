/*
 * Created on 06.09.2016
 *
 */
package de.saxsys.jfx.tabellen;

import java.util.Optional;

import de.saxsys.jfx.tabellen.model.Town;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TownService {
        private final ListProperty<Town> towns = new SimpleListProperty<>(FXCollections.observableArrayList());

        public TownService() {
                Town dresden = new Town("Dresden", 12345);
                Town leipzig = new Town("Leipzig", 123456);
                towns.addAll(dresden, leipzig);
        }

        public Town getTownForZip(int zip) {
                Optional<Town> findFirst = towns.stream().filter(t -> {
                        if (t.getZipCode() == zip) {
                                return true;
                        }
                        return false;
                }).findFirst();

                if (findFirst.isPresent()) {
                        return findFirst.get();
                }

                return null;
        }

        public Town getTownForName(String name) {
                Optional<Town> findFirst = towns.stream().filter(t -> {
                        if (t.getName().equals(name)) {
                                return true;
                        }
                        return false;
                }).findFirst();

                if (findFirst.isPresent()) {
                        return findFirst.get();
                }

                return null;
        }

        public ObservableValue<? extends ObservableList<Town>> getTowns() {
                return towns;
        }

}
