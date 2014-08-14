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
 * Issues:
 * - the control of editing is centered completely inside the cell itself
 * - there's no way to get hold of the actual editing cell, it's only the
 *   TablePosition of the cell that's available
 * - on mousePressed (actually simpleSelect) this behaviour is playing
 *   squash: the table is the wall, the target is the editing fellow cell
 *   whose edit is rudely cancelled
 * - with the current api, this behaviour can't do much better: table's
 *   api is too weak, one method only  
 * - hack here: add api to tableView that supports the notion of 
 *   terminate an edit and use it (note: needs cooperation of all cells
 *   in the table) 
 * - astonished: _why_ the need to cancel (or otherwise end an edit anywhere
 *   outside this cell) - shouldn't the selection/focus update automagically
 *   trigger the other cell to update its editing?
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

//    @Override
//    public void mousePressed(MouseEvent event) {
//        super.mousePressed(event);
//    }
    
    

    @Override
    protected void simpleSelect(MouseEvent e) {
        TableCell<S, T> cell = getControl();
        TableView<S> table = cell.getTableColumn().getTableView();
        if (table instanceof XTableView) {
            ((XTableView<S>) table).terminateEdit();
        }
        super.simpleSelect(e);
    }



    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(XTableCellBehavior.class
            .getName());
}
