/*
 * Created on 30.05.2014
 *
 */
package de.swingempire.fx.property;

import javafx.beans.property.ReadOnlyBooleanPropertyBase;

/**
 * Marker property: always same value (false) but allows to manually fire an invalidation event.
 * 
 * Trying to implement a dynamic boolean or binding.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class BooleanChangeMarker extends ReadOnlyBooleanPropertyBase {

    
    /**
     * Overridden to allow external access.
     */
    @Override
    public void fireValueChangedEvent() {
        super.fireValueChangedEvent();
    }

    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean get() {
        return false;
    }

}
