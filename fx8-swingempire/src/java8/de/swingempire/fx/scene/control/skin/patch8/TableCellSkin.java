/*
 * Created on 21.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch8;

import javafx.scene.control.TableCell;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCellSkin<T, S>
        extends com.sun.javafx.scene.control.skin.TableCellSkin<T, S> {

    /**
     * @param tableCell
     */
    public TableCellSkin(TableCell<T, S> tableCell) {
        super(tableCell);
    }

}
