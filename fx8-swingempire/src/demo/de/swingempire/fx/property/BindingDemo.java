/*
 * Created on 30.10.2014
 *
 */
package de.swingempire.fx.property;

import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class BindingDemo {
    
    public static class Demo1<T> {
        
        private ChangeListener<T> l = (source, old, value) -> valueChanged(source, old, value);

        /**
         * @param source
         * @param old
         * @param value
         * @return
         */
        protected void valueChanged(ObservableValue<? extends T> source, T old,
                T value) {
            LOG.info("" + getClass().getSimpleName());
        }

        public Demo1(ObservableValue<T> source) {
            source.addListener(l);
        }
    }

    public static class Demo2<T> extends Demo1<T>{
        
        public Demo2(ObservableValue<T> source) {
            super(source);
        }

        @Override
        protected void valueChanged(ObservableValue<? extends T> source, T old,
                T value) {
            LOG.info("I'm the constant");
        }
        
        
    }
    
    public static void main(String[] args) {
        StringProperty p = new SimpleStringProperty("my-my");
        Demo1 first = new Demo1(p);
        Demo2 second = new Demo2(p);
        p.set("newValue?");
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(BindingDemo.class
            .getName());
}
