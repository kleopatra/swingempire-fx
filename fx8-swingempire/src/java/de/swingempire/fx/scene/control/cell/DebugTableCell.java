/*
 * Created on 12.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import de.swingempire.fx.scene.control.ControlUtils;
import de.swingempire.fx.scene.control.edit.TablePersonCoreAddAndEdit;
import de.swingempire.fx.util.FXUtils;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.Event;
import javafx.scene.control.ListView;
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
 * <li> same problem as in ListCell, skin cancels edit 
 *      during a commit - happens on TableView only if items have extractor on
 *      the editing column
 *      see DebugListCell for details    
 * <li> in contrast to listCell, we need the ignoreCancel (if cellSelectionEnabled)
 *      even with the changed sequence     
 * </ul>
 * 
 * @param <S> The type of the TableView generic type (i.e. S == TableView&lt;S&gt;).
 *           This should also match with the first generic type in TableColumn.
 * @param <T> The type of the item contained within the Cell.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugTableCell<S, T> extends TableCell<S, T> implements CellDecorator<TableView<S>, T> {

    
    private boolean ignoreCancel;

    public DebugTableCell() {
        focusedProperty().addListener((src, ov, nv) -> {
            if(isEditing() && !isFocused()) {
//                attemptEditCommit();
            }
        });
    }
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
     * <li> do nothing if !canStartEdit
     * </ul>
     * 
     * @see #canStartEdit()
     */
    @Override 
    public void startEdit() {
        // PENDING JW: core is inconsistent if editing - tree returns, list/table start again
        //if (isEditing()) return;
        if (!canStartEdit()) return;
        final TableView<S> table = getTableView();
        final TableColumn<S,T> column = getTableColumn();
        if (! isEditable() ||
                (table != null && ! table.isEditable()) ||
                (column != null && ! getTableColumn().isEditable())) {
            return;
        }
        // it makes sense to get the cell into its editing state before firing
        // the event to listeners below, so that's what we're doing here
        // by calling super.startEdit().
//        super.startEdit();
        cellStartEdit();

        // PENDING JW:shouldn't we back out if !isEditing? That is when
        // super refused to switch into editing state?
        if (!isEditing()) return;
        // We check the boolean lockItemOnEdit field here, as whilst we want to
        // updateItem normally, when it comes to unit tests we can't have the
        // item change in all circumstances.
//        if (! lockItemOnEdit) {
        // PENDING JW:why do we need this? start edit should only happen on 
        // fully configured cells?
        if (!lockItemOnEdit()) {
//            invokeUpdateItem(-1);
        }


        // Inform the tableView of the edit starting.
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
     * Changed sequence of handlers, removed the flag again
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
            TablePosition<S, ?> editingCell = new TablePosition<>(getTableView(), getIndex(), getTableColumn());
            TablePosition<S, ?> tableEditingCell = table.getEditingCell();
            if (!editingCell.equals(tableEditingCell)) {
                // JW: not true? table's editing might have been changed by some other class? 
                // we get here if
                // cell-selection enabled
                // by-pass cell's focusListener (divert to commit)
                // addItem in commitHandler and start edit in itemsListener and cell is re-used (?)
                // that is, the next after the last is visible and empty, no scrollbar
                // fine if scrolling before edit
                throw new IllegalStateException("on commitEdit, table editing location must be same as my own: "
                        + editingCell + " but was: " + tableEditingCell);
            }
            
            // PENDING JW: trying to do this before firing the event
            //will blow  ... yeah, because we use table.getEditingCell to build the event
            // reset the editing cell on the TableView
            ignoreCancel = true;
            table.edit(-1, null);
            // experiment around commit-fires-cancel:
            // surround with ignore-cancel to not react if skin
            // cancels our edit due to data change triggered by this commit
            // Inform the TableView of the edit being ready to be committed.
            @SuppressWarnings({ "rawtypes", "unchecked" })
            CellEditEvent editEvent = new CellEditEvent(
                table,
                editingCell,
//                table.getEditingCell(),
                TableColumn.editCommitEvent(),
                newValue
            );

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
     * <li> hmm getting weird index inconsistencies (as seen in demo)
     *     do nothing if cancel request originated from cell's focused handler
     *     cant nail ... same for core
     * </ul>
     * 
     * @see DebugListCell#commitEdit(Object)
     * @see TablePersonCoreAddAndEdit
     */
    @Override 
    public void cancelEdit() {
        if (ignoreCancel()) return;
        if (cancelFromCell()) {
            handleCancelFromCell();
            return;
        }
//        super.cancelEdit();
        cellCancelEdit();
        
        final TableView<S> table = getTableView();
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
//            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(table);
            // if another cell isEditing, it's rude as well
            if (ControlUtils.isCurrentFocusOwnerChildOf(table) && table.getEditingCell() == null)
                table.requestFocus();

            CellEditEvent<S,?> editEvent = new CellEditEvent<>(
                table,
                editingCell,
                TableColumn.editCancelEvent(),
                null
            );

            Event.fireEvent(getTableColumn(), editEvent);
        }
    }

    /**
     * Unused: idea was to replace the cancel with our own handling
     * Doing it by calling attemptEditCommit is wrong, because the cell
     * handler is still on the stacktrace. Need to check once and 
     * do our own entirely, if any. For now, simply remove.
     */
    protected void handleCancelFromCell() {
        if (ignoreCancel()) return;
        getEditorValue().ifPresent(this::commitEdit);
//        attemptEditCommit();
    }

    /**
     * @return
     */
    private boolean cancelFromCell() {
        Exception ex = new RuntimeException("dummy");
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            if (stackTrace[i].getClassName().contains("Cell$1")) {
                LOG.info("CELL-TRIGGERED " + getEditorValue() + isEditing() + " / item: " + getItem() + " " + stackTrace[i].getClassName());
                return true;
            }
        }
        return false;
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

    @Override
    public ReadOnlyObjectProperty<TableView<S>> controlProperty() {
        return tableViewProperty();
    }

    /**
     * Implemented check if the tableView is editable and the column is not null and
     * editable in addition to super.
     */
    @Override
    public boolean canStartEdit() {
        return CellDecorator.super.canStartEdit()
                && getTableView().isEditable()
                && getTableColumn() != null && getTableColumn().isEditable();
    }
    

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DebugTableCell.class.getName());
}
