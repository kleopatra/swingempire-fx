/*
 * Created on 29.07.2015
 *
 */
package de.swingempire.fx.scene.control.pagination;

import javafx.beans.property.IntegerProperty;

/**
 * Model to encapsulate the navigation across a pagination control.
 * 
 * This is a model for the sake of the navigation ui, which is 
 * under the control of the pagination skin. Design goal is to encapsulate
 * the control such that the navigation control is pluggable.
 * 
 * This is similar to the index-related part of a SelectionModel:  
 * it has a current (aka: selected)
 * index and navigation services next/prev/first/last. It also 
 * (similar to some implementations of SelectionModel) has the notion of 
 * size which might be INDETERMINATE if unknown. 
 * 
 * It differs from SelectionModel in 
 * <li> not having an item corresponding to the index
 * <li> cannot be empty: minimum size is 1
 * 
 * The default implementation assumes that the properties enforce the 
 * constraints 0 <= current < size
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface NavigationModel {
    /**
     * Value for indicating that the page count is indeterminate.
     */
    public static final int INDETERMINATE = Integer.MAX_VALUE;

    IntegerProperty currentProperty();
    
    default int getCurrent() {
        return currentProperty().get();
    }
    
    default void setCurrent(int current) {
        currentProperty().set(current);
    }
    
    IntegerProperty sizeProperty();
    
    /**
     * 
     * @return
     */
    default int getSize() {
        return sizeProperty().get();
    }
    
    default void next() {
        int current = getCurrent();
        if (current < getSize() -1) {
            setCurrent(current + 1);
        }
    }
    
    default void previous() {
        int current = getCurrent();
        if (current > 0) {
            setCurrent(current - 1);
        }
    }
    
    default void last() {
        setCurrent(getSize() - 1);
    }
    
    default void first() {
        setCurrent(0);
    }
}
