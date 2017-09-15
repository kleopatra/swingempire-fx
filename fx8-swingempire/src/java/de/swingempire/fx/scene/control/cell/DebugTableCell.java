/*
 * Created on 12.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import de.swingempire.fx.scene.control.ControlUtils;
import de.swingempire.fx.util.FXUtils;
import javafx.event.Event;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;

/**
 * Custom TableCell which overrides start/commit/cancelEdit and
 * takes over completely (c&p from super and reflective access to Cell methods).
 * This is (mostly) done to understand the editing mechanism. 
 * <p>
 * 
 * Bug fixes:
 * <ul>
 * <li> https://bugs.openjdk.java.net/browse/JDK-8187474 -
 *      in startEdit update editingCell of tableView,
 * <li> https://bugs.openjdk.java.net/browse/JDK-8187229   
 *    in startEdit and cancelEdit, pass correct editingCell into editEvent
 * <li> not yet reported, but same problem as in ListCell, skin cancels edit 
 *      during a commit - happens on TableView only if items have extractor on
 *      the editing column
 *      see DebugListCell for details    
 * </ul>
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugTableCell<S, T> extends TableCell<S, T> implements CellDecorator<T> {

    private boolean ignoreCancel;

    /** 
     * {@inheritDoc} <p>
     * 
     * Basically, a c&p of super except:
     * 
     * <ul>
     * <li> pass correct editingCell into editStartEvent, 
     *  fix https://bugs.openjdk.java.net/browse/JDK-8187229   
     * <li> update editingCell of tableView, 
     *  fix https://bugs.openjdk.java.net/browse/JDK-8187474
     * </ul>
     */
    @Override 
    public void startEdit() {
        final TableView<S> table = getTableView();
        final TableColumn<S,T> column = getTableColumn();
        if (! isEditable() ||
                (table != null && ! table.isEditable()) ||
                (column != null && ! getTableColumn().isEditable())) {
            return;
        }

        // We check the boolean lockItemOnEdit field here, as whilst we want to
        // updateItem normally, when it comes to unit tests we can't have the
        // item change in all circumstances.
//        if (! lockItemOnEdit) {
        if (!lockItemOnEdit()) {
            invokeUpdateItem(-1);
        }

        // it makes sense to get the cell into its editing state before firing
        // the event to listeners below, so that's what we're doing here
        // by calling super.startEdit().
//        super.startEdit();
        cellStartEdit();

        if (column != null) {
            TablePosition<S, ?> editingCell = new TablePosition<>(getTableView(), getIndex(), getTableColumn());
            CellEditEvent<S,?> editEvent = new CellEditEvent<>(
                table,
                editingCell,
//                table.getEditingCell(),
                TableColumn.editStartEvent(),
                null
            );

            Event.fireEvent(column, editEvent);
            // missing in core
            getTableView().edit(getIndex(), getTableColumn());
        }
    }

    /** 
     * {@inheritDoc} 
     * Basically, a c&p of super except:
     * 
     * <ul>
     * <li> surround firing of editCommitEvent with ignoreCancel
     * </ul>
     * 
     * @see DebugListCell#commitEdit(Object)
     */
    @Override 
    public void commitEdit(T newValue) {
        if (! isEditing()) return;

        // inform parent classes of the commit, so that they can switch us
        // out of the editing state.
        // This MUST come before the updateItem call below, otherwise it will
        // call cancelEdit(), resulting in both commit and cancel events being
        // fired (as identified in RT-29650)
//        super.commitEdit(newValue);
        cellCommitEdit(newValue);
        final TableView<S> table = getTableView();
        if (table != null) {
            // experiment around commit-fires-cancel:
            // surround with ignore-cancel to not react if skin
            // cancels our edit due to data change triggered by this commit
//            ignoreCancel = true;
            // Inform the TableView of the edit being ready to be committed.
            @SuppressWarnings({ "rawtypes", "unchecked" })
            CellEditEvent editEvent = new CellEditEvent(
                table,
                table.getEditingCell(),
                TableColumn.editCommitEvent(),
                newValue
            );

            // PENDING JW: trying to do this before firing the event
            //will blow
            // reset the editing cell on the TableView
            table.edit(-1, null);
            Event.fireEvent(getTableColumn(), editEvent);
            ignoreCancel= false;
        }

        // update the item within this cell, so that it represents the new value
        // PENDING JW: probably regrab tree's value? otherwise we
        // show incorrect data if handler rejects the edit
        // https://bugs.openjdk.java.net/browse/JDK-8187314  
        updateItem(newValue, false);

        if (table != null) {
            // request focus back onto the table, only if the current focus
            // owner has the table as a parent (otherwise the user might have
            // clicked out of the table entirely and given focus to something else.
            // It would be rude of us to request it back again.
//            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(table);
        }
    }

    /** 
     * {@inheritDoc} 
     * Basically, a c&p of super except:
     * 
     * <ul>
     * <li> do nothing if ignoreCancel
     * <li> fire editCancelEvent with correct editingCell, 
     *  fix for https://bugs.openjdk.java.net/browse/JDK-8187229
     * </ul>
     * 
     * @see DebugListCell#commitEdit(Object)
     */
    @Override 
    public void cancelEdit() {
        if (ignoreCancel()) return;
        final TableView<S> table = getTableView();

//        super.cancelEdit();
        cellCancelEdit();
        // reset the editing index on the TableView
        if (table != null) {
            // use editingCell based on cell state, not control state
            TablePosition<S, ?> editingCell = new TablePosition<>(getTableView(), getIndex(), getTableColumn());
//            TablePosition<S,?> editingCell = table.getEditingCell();
            if (resetTableEditingCellInCancel()) table.edit(-1, null);

            // request focus back onto the table, only if the current focus
            // owner has the table as a parent (otherwise the user might have
            // clicked out of the table entirely and given focus to something else.
            // It would be rude of us to request it back again.
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(table);

            CellEditEvent<S,?> editEvent = new CellEditEvent<>(
                table,
                editingCell,
                TableColumn.editCancelEvent(),
                null
            );

            Event.fireEvent(getTableColumn(), editEvent);
        }
    }

    protected boolean ignoreCancel() {
        return !isEditing() || ignoreCancel;
    }
    

//------------ reflection acrobatics
    
    protected void invokeUpdateItem(int index) {
        FXUtils.invokeGetMethodValue(TableCell.class, this, "updateItem", Integer.TYPE, index);
    }
    
    /**
     * Returns a flag indicating whether the list editingIndex should be 
     * reset in cancelEdit. Implemented to reflectively access super's
     * hidden field <code>updateEditingIndex</code>
     * @return
     */
    protected boolean lockItemOnEdit() {
        return (boolean) FXUtils.invokeGetFieldValue(TableCell.class, this, "lockItemOnEdit");
    }
    /**
     * Returns a flag indicating whether the table editingCell should be 
     * reset in cancelEdit. Implemented to reflectively access super's
     * hidden field <code>updateEditingIndex</code>
     * @return
     */
    protected boolean resetTableEditingCellInCancel() {
        return (boolean) FXUtils.invokeGetFieldValue(TableCell.class, this, "updateEditingIndex");
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DebugTableCell.class.getName());
}
