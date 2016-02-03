/*
 * Created on 12.08.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.TableCellBehavior;

import de.swingempire.fx.scene.control.XTableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

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
 * JDK9: no longer working - editing already stopped - now really running into canceled by 
 * cell.focused? No - is bug in InputMap: removing mappings installed by super behavior
 * aren't completely removed. On disposing the old behavior, we need to remove the
 * left-over mappings as well.    
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XTableCellBehavior<S, T> extends TableCellBehavior<S, T>{

    public XTableCellBehavior(TableCell<S, T> cell) {
        super(cell);
    }
    
    /**
     * Tries to terminate edits if containing table is of type
     * XTableView.
     */
    protected void tryTerminateEdit() {
        TableCell<S, T> cell = getNode();
        TableView<S> table = cell.getTableColumn().getTableView();
        if (table instanceof XTableView) {
            ((XTableView<S>) table).terminateEdit();
        }
    }
    
    /**
     * This method is called in jdk8_u5. Signature changed
     * in jdk8_u20.
     * 
     * @param e
     */
//    @Override
//    protected void simpleSelect(MouseButton button, int clickCount, boolean alreadySelected) {
//        LOG.info("editing: " + getNode().getItem() + " / " + getNode().getTableView().getEditingCell());
//        tryTerminateEdit();
//        super.simpleSelect(button, clickCount, alreadySelected);
//    }

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
