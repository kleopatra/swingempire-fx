/*
 * Created on 09.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import de.swingempire.fx.util.FXUtils;
import javafx.scene.Node;
import javafx.scene.control.Cell;

/**
 * Interface for decorating cells: contains reflective access to super's hidden api.
 * Side-effect is to open too many methods into public scope - don't use in production!
 * <p>
 * Must not be implemented by classes that are not of type Cell or any of its
 * subclasses.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface CellDecorator<T> {
    
//----------------- item
    
    // ? final method cannot be decorated by an interface
    T getItem();
    
    void setItem(T item);
    
    default String itemToString(T item) {
        return item != null ? item.toString() : "null";
    }
    
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
//------------------------- labeled
    
    void setText(String text);
    
    void setGraphic(Node graphic);
    
//------------------------- editing     
    boolean isEditing();

    void startEdit();
    
    void commitEdit(T value);
    
    void cancelEdit();
    
//------------ access cell-level state
    
    void updateSelected(boolean selected);
    
    boolean isSelected();
    
    default void cellUpdateItem(T item, boolean empty) {
        setItem(item);
        invokeSetEmpty(empty);
//        setEmpty(empty);
        if (empty && isSelected()) {
            updateSelected(false);
        }
    }
    
    /**
     * Hook into Cell's commitEdit - used to by-pass current fx9 TableCell's implementation
     * of commitEdit
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
     * @param value
     */
    default void cellCancelEdit() {
        if (isEditing()) {
            invokeSetEditing(false);
//            setEditing(false);
        }
        
    }
    //---------------------- reflection acrobatics
    
    default void invokeSetEditing(boolean selected) {
        FXUtils.invokeGetMethodValue(Cell.class, this, "setEditing", Boolean.TYPE, selected);
    }
    
    default void invokeSetSelected(boolean selected) {
        FXUtils.invokeGetMethodValue(Cell.class, this, "setSelected", Boolean.TYPE, selected);
    }
    
    default void invokeSetEmpty(boolean empty) {
        FXUtils.invokeGetMethodValue(Cell.class, this, "setEmpty", Boolean.TYPE, empty);
    }
    

}
