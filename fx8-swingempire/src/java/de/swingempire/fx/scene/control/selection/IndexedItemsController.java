/*
 * Created on 04.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.collections.ObservableList;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public interface IndexedItemsController<T> {

    /**
     * Returns the list of indices.
     * 
     * @return
     */
    ObservableList<Integer> getIndices();
    
    /**
     * Returns the indexed items.
     * 
     * @return
     */
    ObservableList<T> getIndexedItems();
    
    /**
     * Sets the given indices. All previously set indices are
     * cleared.
     * 
     * Does nothing if null or empty.
     * 
     * @param indices positions in source list
     */
    void setIndices(int... indices);
    
    /**
     * Adds the given indices. Does nothing if null or empty.
     * 
     * @param indices
     */
    void addIndices(int... indices);
    
    /**
     * Clears the given indices. Does nothing if null or empty.
     * @param indices position in sourceList
     */
    void clearIndices(int... indices);
    
    /**
     * Sets all indices. 
     * 
     * PENDING JW: notification on already set?
     */
    void setAllIndices();
    
    /**
     * Clears all indices.
     */
    void clearAllIndices();
    
    /**
     * Returns the index of the item in the backing list if it is indexed 
     * or -1 if not.
     * 
     * @param item
     * @return
     */
    int sourceIndexOf(T item);
    
    // PENDING JW: really want to expose backing properties?
    int getSourceSize();
    
    // PENDING JW: really want to expose backing properties?
    ObservableList<? extends T> getSource();
    
}
