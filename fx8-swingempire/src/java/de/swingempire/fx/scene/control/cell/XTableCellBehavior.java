/*
 * Created on 12.08.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

import com.sun.javafx.scene.control.behavior.TableCellBehavior;

import de.swingempire.fx.scene.control.XTableView;

/**
 * Trying to intercept the selection process to not cancel an edit.
 * 
 * No luck: 
 * 
 * - the control of editing is centered completely inside the cell itself
 * - there's no way to get hold of the actual editing cell, it's only the
 * TablePosition of the cell that's available.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XTableCellBehavior<S, T> extends TableCellBehavior<S, T>{

    /**
     * @param control
     */
    public XTableCellBehavior(TableCell<S, T> control) {
        super(control);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        LOG.info("pressed: " + getControl().isEditing());
         
        super.mousePressed(event);
    }
    
    

    @Override
    protected void simpleSelect(MouseEvent e) {
        TableCell cell = getControl();
        TableView table = cell.getTableColumn().getTableView();
        if (table instanceof XTableView) {
            ((XTableView) table).terminateEdit();
        }
        super.simpleSelect(e);
    }



    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(XTableCellBehavior.class
            .getName());
}
