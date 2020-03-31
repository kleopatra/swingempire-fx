/*
 * Created on 16.03.2020
 *
 */
package de.swingempire.fx.property;

import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Divers tests around properties
 * @author Jeanette Winzenburg, Berlin
 */
public class PropertyTest {

    // name changing in property allowed?
    
    public static class MutableNameBooleanProperty extends BooleanPropertyBase {
        
        private StringProperty name;
        private Object bean;
        
        public MutableNameBooleanProperty(String name, Object bean, boolean initialValue) {
            super(initialValue);
            this.name = new SimpleStringProperty(this, "name", name);
            this.bean = bean;
        }

        @Override
        public Object getBean() {
            return bean;
        }

        @Override
        public String getName() {
            return nameProperty().get();
        }
        
        public void setName(String name) {
            nameProperty().set(name);
        }
        
        public StringProperty nameProperty() {
            return name;
        }
        
    }
}
