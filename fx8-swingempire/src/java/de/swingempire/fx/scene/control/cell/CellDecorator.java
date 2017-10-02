/*
 * Created on 09.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Optional;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Labeled;

/**
 * Interface for decorating cells: contains reflective access to super's hidden api.
 * Side-effect is to open too many methods into public scope - don't use in production!
 * <p>
 * Must not be implemented by classes that are not of type IndexedCell or any of its
 * subclasses.
 * 
 * <p>
 * PENDING: itemDirty/update in layoutChildren
 * <li>Cell: has itemDirty, set in updateSelected and reset in layoutChildren + call updateItem(getItem(), isEmpty()
 * <li> TableCell: has itemDirty, set in listener to observableValue of cell content, reset in layout + call updateItem(-1)
 * <li> TableRow: none
 * <li>TreeCell: none 
 * 
 * @param <C> The type of the virtualized control that contains cells decorated
 *    by this
 * @param <T> The type of the item contained within the Cell.
 * @author Jeanette Winzenburg, Berlin
 */
public interface CellDecorator<C extends Control, T> {
    
    /**
     * Debug only - subs can identify cells uniquely to analyse re-use.
     * @return
     */
    default int getCounter() {
        return -1;
    }
    
//----------------- item/index
    
    /**
     * Returns the index of this cell.
     * 
     * @return
     * 
     * @see IndexedCell#getIndex()
     */
    int getIndex();
    
    /**
     * Returns the item of this cell
     * @return
     * 
     * @see Cell#getItem()
     */
    T getItem();
    
    /**
     * Sets the item of this cell. Note: hidden api don't use!
     * 
     * @param item
     * @see Cell#setItem(T)
     */
    void setItem(T item);
    
    /**
     * Returns a boolean indicating whether or not this cell is empty.
     * @return
     * @see Cell#isEmpty()
     */
    boolean isEmpty();
    

    /**
     * Converter method from item to string.
     * <p>
     * This implementation returns the item.toString() or explicit "no item" if not/null, 
     * respectively
     * 
     * @param item
     * @return
     */
    default String itemToString(T item) {
        return item != null ? item.toString() : "no item";
    }
    
    /**
     * Simple visual update, implementors can delegate their updateItem to this.
     * <p>
     * This implementation sets the text/graphic property: null if empty,
     * graphic to item if is-node or text to itemToString otherwise.
     * <p>
     * PENDING: used anywhere? copied from a default implementation - check and spec
     * which? Really useful?
     * 
     * @param item
     * @param empty
     * 
     * @see Cell#updateItem(T, boolean)
     */
    default void updateItemNode(T item, boolean empty) {
        if (empty) {
            setText(null);
            setGraphic(null);
        } else if (item instanceof Node) {
            setText(null);
            setGraphic((Node) item);
        } else {
            /**
             * This label is used if the item associated with this cell is to be
             * represented as a String. While we will lazily instantiate it
             * we never clear it, being more afraid of object churn than a minor
             * "leak" (which will not become a "major" leak).
             */
            setText(itemToString(item));
            setGraphic(null);
        }
        
    }
//---------------- containing control
    
    /**
     * Returns the containing control.
     * 
     * @return
     */
    default C getControl() {
        return controlProperty().get();
    };
    
    /**
     * Property that holds the containing control. Implementors must
     * delegate to their respective specialized types.
     * @return
     * 
     * @see TableCell#tableViewProperty()
     * @see TreeCell#TreeViewProperty()
     * @see ListCell#ListViewProperty()
     */
    ReadOnlyObjectProperty<C> controlProperty();
    
//------------------------- labeled
    
    /**
     * Sets the text of this cell.
     * 
     * @param text
     * @see Labeled#setText(String)
     */
    void setText(String text);
    
    /**
     * Sets the graphic of this cell.
     * @param graphic
     * 
     * @see Labeled#setGraphic(Node)
     */
    void setGraphic(Node graphic);
    
//------------------------- editing     
    
    /**
     * Returns a boolean that indicates whether or not edit can be started. 
     * This implementation will return true if this cell is editable, not editing, 
     * not empty and the containing control not null. If any of the conditions
     * are not met, it will return false. 
     * 
     * @return
     */
    default boolean canStartEdit() {
        return !isEditing() && isEditable() && !isEmpty() && getControl() != null;
    }
    
    /**
     * Returns a boolean indicating whether or not this cell is editing.
     * @return
     * 
     * @see Cell#isEditing()
     */
    boolean isEditing();

    /**
     * Returns a boolean indicating whether or not this cell is editable
     * @return
     * 
     * @see Cell#isEditable()
     */
    boolean isEditable();
    /**
     * Start editing, implementations should respect canStartEdit, that is do 
     * nothing if false and try to switch into editing state if true.
     * 
     * @see Cell#startEdit()
     */
    void startEdit();
    
    /**
     * Commits the given value and terminates an edit. Does nothing if not 
     * editing.
     * 
     * @param value
     * @see Cell#commitEdit(T)
     */
    void commitEdit(T value);
    
    /**
     * Cancels an edit if editing, does nothing otherwise.
     * 
     * @see Cell#cancelEdit()
     */
    void cancelEdit();
    
    /**
     * Experimental ... from Jonathan's fix for commitOnFocusLost
     */
    default void attemptEditCommit() {
        LOG.info("attempt?");
        // The user has shifted focus, so we should cancel the editing on this cell
        getEditorValue().ifPresentOrElse(this::commitEdit, this::cancelEdit);
    }

    /**
     * Experimental ... from Jonathan's fix for commitOnFocusLost
     */
    default Optional<T> getEditorValue() {
        return Optional.empty();
    }


    
//------------ access cell-level state
    
    /**
     * 
     * @param selected
     * 
     * @see Cell#updateSelected(boolean)
     */
    void updateSelected(boolean selected);
    
    boolean isSelected();
    
    /**
     * Implemented Cell.updateItem, c&p.
     * 
     * @param item
     * @param empty
     * 
     * @see Cell#updateItem(T, boolean)
     */
    default void cellUpdateItem(T item, boolean empty) {
        setItem(item);
        invokeSetEmpty(empty);
//        setEmpty(empty);
        if (empty && isSelected()) {
            updateSelected(false);
        }
    }

    /**
     * Implemented Cell.startEdit(), c&p plus reflective access to hidden api.
     */
    default void cellStartEdit() {
        if (isEditable() && !isEditing() && !isEmpty()) {
//            setEditing(true);
            invokeSetEditing(true);
        }
    }

    /**
     * Hook into Cell's commitEdit - used to by-pass current fx9 TableCell's implementation
     * of commitEdit
     * Implemented Cell.commitEdit(), c&p plus reflective access to hidden api.
     * @param value
     */
    default void cellCommitEdit(T value) {
        if (isEditing()) {
            invokeSetEditing(false);
//            setEditing(false);
        }
    }
    
    /**
     * Hook into Cell's cancelEdit - used to by-pass current fx9 TableCell's implementation
     * of cancelEdit
     * Implemented Cell.cancelEdit(), c&p plus reflective access to hidden api.
     * @param value
     */
    default void cellCancelEdit() {
        if (isEditing()) {
            invokeSetEditing(false);
//            setEditing(false);
        }
    }
    

    //---------------------- reflection acrobatics
    
    private void invokeSetEditing(boolean selected) {
        FXUtils.invokeGetMethodValue(Cell.class, this, "setEditing", Boolean.TYPE, selected);
    }
    
    private void invokeSetSelected(boolean selected) {
        FXUtils.invokeGetMethodValue(Cell.class, this, "setSelected", Boolean.TYPE, selected);
    }
    
    private void invokeSetEmpty(boolean empty) {
        FXUtils.invokeGetMethodValue(Cell.class, this, "setEmpty", Boolean.TYPE, empty);
    }
    

    @SuppressWarnings("unused")
    static final Logger LOG = Logger
            .getLogger(CellDecorator.class.getName());
}
