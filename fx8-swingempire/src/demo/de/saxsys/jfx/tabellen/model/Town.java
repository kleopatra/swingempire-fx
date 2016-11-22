/*
 * Created on 06.09.2016
 *
 */
package de.saxsys.jfx.tabellen.model;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

public class Town {

        private final ReadOnlyStringWrapper name = new ReadOnlyStringWrapper();
        private final ReadOnlyIntegerWrapper zipCode = new ReadOnlyIntegerWrapper();

        public Town(String name, int zipCode) {
                this.name.set(name);
                this.zipCode.set(zipCode);
        }

        public ReadOnlyStringProperty nameProperty() {
                return name.getReadOnlyProperty();
        }

        public ReadOnlyIntegerProperty zipCodeProperty() {
                return zipCode.getReadOnlyProperty();
        }

        public String getName() {
                return name.get();
        }

        public int getZipCode() {
                return zipCode.get();
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj)
                        return true;
                if (obj == null)
                        return false;
                if (getClass() != obj.getClass())
                        return false;
                Town other = (Town) obj;
                if (name == null) {
                        if (other.name != null)
                                return false;
                } else if (!name.equals(other.name))
                        return false;
                if (zipCode == null) {
                        if (other.zipCode != null)
                                return false;
                } else if (!zipCode.equals(other.zipCode))
                        return false;
                return true;
        }

}
