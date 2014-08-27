/*
 * Created on 12.08.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

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
 * Didn't compile in jdk8_u20 (joy of hacking ;-) : 
 * - simpleSelect method signature changed
 * - edit handling at the end of former simpleSelect was extracted
 *   to handleClicks (method in CellBehaviour, probably good move for
 *   consistency across across cell containers)
 * - so now we override the latter and try terminate edits   
 *       
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
    
    /**
     * This method is called prior to jdk8_u20. Signature changed
     * in jdk8_u20.
     * 
     * @param e
     */
//    @Override
//    protected void simpleSelect(MouseEvent e) {
//        tryTerminateEdit();
//        super.simpleSelect(e);
//    }
    /**
     * Tries to terminate edits if containing table is of type
     * XTableView.
     */
    protected void tryTerminateEdit() {
        TableCell<S, T> cell = getControl();
        TableView<S> table = cell.getTableColumn().getTableView();
        if (table instanceof XTableView) {
            ((XTableView<S>) table).terminateEdit();
        }
    }


    /**
     * This method is introduced in jdk8_u20. It's the editing
     * handling part of the former simpleSelect. 
     * 
     * Overridden to try terminating the edit before calling
     * super.
     * 
     */
    @Override
    protected void handleClicks(MouseButton button, int clickCount,
            boolean isAlreadySelected) {
        tryTerminateEdit();
        super.handleClicks(button, clickCount, isAlreadySelected);
    }



    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(XTableCellBehavior.class
            .getName());
}
