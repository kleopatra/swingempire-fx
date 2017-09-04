/*
 * Created on 04.09.2017
 *
 */
package test.tableeditcore;

import java.util.Optional;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.event.Event;
import javafx.scene.control.Cell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;

/**
 * Trying to mock environment for proposed fix for commit-on-focusLost
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCellJon<S, T> extends TableCell<S, T> {
    
    
    
    
    /**
     * 
     */
    public TableCellJon() {
        focusedProperty().addListener(observable -> {
            // fx9 code
//            if (!isFocused() && isEditing()) {
//                cancelEdit();
//            }
            LOG.info("in focusInvalidation - isFocused?" + isFocused());
            // fx10 code
            // PENDING JW: really try on both on/off?
            attemptEditCommit();
        });
    }

    /**
     * take over from TableCell and call cellCommitEdit because order of 
     * method calls changed in Jon's fix
     * 
     * Fx9: fireEditCommit on tableColumn -> super.commitEdit 
     *     -> updateItem(newValue, false) -> table.edit(-1, null)
     * 
     * Fx10: super.commitEdit -> fireEditCommit on tableColumn
     *     -> updateItem(newValue, false) -> table.edit(-1, null) 
     */
    @Override public void commitEdit(T newValue) {
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
            // Inform the TableView of the edit being ready to be committed.
            CellEditEvent editEvent = new CellEditEvent(
                table,
                // this line is new, replaced the old table.getEditingCell
                new TablePosition<>(getTableView(), getIndex(), getTableColumn()),
                TableColumn.editCommitEvent(),
                newValue
            );

            Event.fireEvent(getTableColumn(), editEvent);
        }

        // update the item within this cell, so that it represents the new value
        updateItem(newValue, false);

        if (table != null) {
            // reset the editing cell on the TableView
            table.edit(-1, null);

            // request focus back onto the table, only if the current focus
            // owner has the table as a parent (otherwise the user might have
            // clicked out of the table entirely and given focus to something else.
            // It would be rude of us to request it back again.
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(table);
        }
    }

    /**
     * Implemented to by-pass super fx9
     */
    @Override public void cancelEdit() {
        if (! isEditing()) return;

        final TableView<S> table = getTableView();

        cellCancelEdit();

        // reset the editing index on the TableView
        if (table != null) {
            TablePosition<S,?> editingCell = table.getEditingCell();
            table.edit(-1, null);

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

    void attemptEditCommit() {
        // The user has shifted focus, so we should cancel the editing on this
        // cell
        getEditorValue().ifPresentOrElse(this::commitEdit, this::cancelEdit);
    }

    /**
     * @return
     */
    protected Optional<T> getEditorValue() {
        return Optional.empty();
    }

    /**
     * Hook into Cell's commitEdit - used to by-pass current fx9 TableCell's implementation
     * of commitEdit
     * @param value
     */
    protected void cellCommitEdit(T value) {
        if (isEditing()) {
            invokeSetEditing(false);
//            setEditing(false);
        }
    }
    
    /**
     * Hook into Cell's cancelEdit - used to by-pass current fx9 TableCell's implementation
     * of cancelEdit
     * @param value
     */
    protected void cellCancelEdit() {
        if (isEditing()) {
            invokeSetEditing(false);
//            setEditing(false);
        }
        
    }
 //---------------------- reflection acrobatics
    
    protected void invokeSetEditing(boolean selected) {
        FXUtils.invokeGetMethodValue(Cell.class, this, "setEditing", Boolean.TYPE, selected);
    }
    
    protected void invokeSetSelected(boolean selected) {
        FXUtils.invokeGetMethodValue(Cell.class, this, "setSelected", Boolean.TYPE, selected);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableCellJon.class.getName());
}
