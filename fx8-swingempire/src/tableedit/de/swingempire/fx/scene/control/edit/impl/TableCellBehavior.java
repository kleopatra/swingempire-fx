/*
 * Created on 21.03.2016
 *
 */
package de.swingempire.fx.scene.control.edit.impl;

import javafx.scene.control.TableCell;

/**
 * Does nothing ... just getting out of non-patch packages.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCellBehavior<S, T>
        extends com.sun.javafx.scene.control.behavior.TableCellBehavior<S, T> {

    /**
     * @param control
     */
    public TableCellBehavior(TableCell<S, T> control) {
        super(control);
    }
    
//--------------- compatibility
    
    public TableCell<S, T> getControl() {
        return getNode();
    }

}
