/*
 * Created on 23.04.2015
 *
 */
package de.swingempire.fx.property;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
/**
 * Custom bidi-binding that guards against loops.
 * from:
 * http://wittcarl.deneb.uberspace.de/wordpress/customized-bidirectional-bindings-in-javafx/
 */
public class BidirectionalBindingX {

    /** Executes updateB when propertyA is changed. Executes updateA when propertyB is changed.
     * Makes sure that no update loops are caused by mutual updates.
     */
    public static <A,B> void bindBidirectional(ObservableValue<A> propertyA, ObservableValue<B> propertyB, ChangeListener<A> updateB, ChangeListener<B> updateA){

        addFlaggedChangeListener(propertyA, updateB);
        addFlaggedChangeListener(propertyB, updateA);

    }

    /**
     * Adds a change listener to a property that will not react to changes caused (transitively) by itself (i.e. from an update call in the call tree that is a descendant of itself.)
     * @param property the property to add a change listener to
     * @param updateProperty the logic to execute when the property changes
     * @param <T> the type of the observable value
     */
    private static <T> void addFlaggedChangeListener(ObservableValue<T> property, ChangeListener<T> updateProperty){
        property.addListener(new ChangeListener<T>() {

        private boolean alreadyCalled = false;
        
        @Override public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
            if(alreadyCalled) return;
            try {
                alreadyCalled = true;
                updateProperty.changed(observable,oldValue,newValue);
            }
            finally { alreadyCalled = false; }
        }
        });
    }

}