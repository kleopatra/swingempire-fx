/*
 * Created on 12.08.2014
 *
 */
package de.swingempire.fx.scene.control.edit;

import java.util.logging.Logger;

import de.swingempire.fx.scene.control.edit.impl.TableCellBehavior;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

/**
 * Trying to intercept the selection process to not cancel an edit.
 * <p>
 * Sketch of interplay of the collaborators:
 * https://www.flickr.com/photos/65941667@N02/6004789460/in/photostream/
 * <p>
 * 
 * old suggestion for enhanced api
 * https://community.oracle.com/thread/2264713?tstart=0
 * 
 * <p>
 * Issues:
 * <li> the control of editing is centered completely inside the cell itself
 * <li> there's no way to get hold of the actual editing cell, it's only the
 *   TablePosition of the cell that's available
 * <li> on mousePressed (actually simpleSelect) this behaviour is playing
 *   squash: the table is the wall, the target is the editing fellow cell
 *   whose edit is rudely cancelled
 * <li> with the current api, this behaviour can't do much better: table's
 *   api is too weak, one method only  
 * <li> hack here: add api to tableView that supports the notion of 
 *   terminate an edit and use it (note: needs cooperation of all cells
 *   in the table) 
 * <li> astonished: _why_ the need to cancel (or otherwise end an edit anywhere
 *   outside this cell) - shouldn't the selection/focus update automagically
 *   trigger the other cell to update its editing?
 * </ul>  
 *   
 * <p>  
 * Didn't compile in jdk8_u20 (joy of hacking ;-) : 
 * <li> simpleSelect method signature changed
 * <li> edit handling at the end of former simpleSelect was extracted
 *   to handleClicks (method in CellBehaviour, probably good move for
 *   consistency across across cell containers)
 * <li> so now we override the latter and try terminate edits   
 *       
 * <p>      
 * JDK9: no longer working - editing already stopped - now really running into canceled by 
 * cell.focused? No - is bug in InputMap: removing mappings installed by super behavior
 * aren't completely removed. On disposing the old behavior, we need to remove the
 * left-over mappings as well.    
 * 
 * <p> 
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

// debugging hooks! KEEP 
    
//    /**
//     * Debugging only!
//     */
//    @Override
//    protected void doSelect(double x, double y, MouseButton button,
//            int clickCount, boolean shiftDown, boolean shortcutDown) {
//        super.doSelect(x, y, button, clickCount, shiftDown, shortcutDown);
//    }
//    /**
//     * Debugging only!
//     */
//    @Override
//    public void mousePressed(MouseEvent e) {
//        LOG.info("cell/row index " + getControl().getIndex() + " / " + getControl().getTableRow().getIndex());
//        super.mousePressed(e);
//    }
//
//    /**
//     * Debugging only!
//     */
//    @Override
//    protected void simpleSelect(MouseButton button, int clickCount,
//            boolean shortcutDown) {
//        super.simpleSelect(button, clickCount, shortcutDown);
//    }

    
    
    /**
     * This method is called in jdk8_u5. Signature changed
     * in jdk8_u20.
     * 
     * KEEP to remember!
     */
//    @Override
//    protected void simpleSelect(MouseButton button, int clickCount, boolean alreadySelected) {
//        LOG.info("editing: " + getNode().getItem() + " / " + getNode().getTableView().getEditingCell());
//        tryTerminateEdit();
//        super.simpleSelect(button, clickCount, alreadySelected);
//    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(XTableCellBehavior.class
            .getName());
}
