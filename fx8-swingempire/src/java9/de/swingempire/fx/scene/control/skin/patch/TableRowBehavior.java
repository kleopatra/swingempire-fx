/*
 * Created on 01.04.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import javafx.scene.control.TableRow;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableRowBehavior<T>
        extends com.sun.javafx.scene.control.behavior.TableRowBehavior<T> {

    /**
     * @param control
     */
    public TableRowBehavior(TableRow<T> control) {
        super(control);
    }

//--------------- compatibility
    
    public TableRow<T> getControl() {
        return getNode();
    }

}
