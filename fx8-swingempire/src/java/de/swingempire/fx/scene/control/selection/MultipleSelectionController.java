/*
 * Created on 04.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.collections.ObservableList;
import de.swingempire.fx.collection.IndexMappedList;
import de.swingempire.fx.collection.IndicesList;

/**
 * Encapsulates control of selectedIndices/selectedItems, that is
 * multiple selection state.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class MultipleSelectionController<T> implements IndexedItemsController<T>{
    
    protected IndicesList<T> indicesList;
    protected IndexMappedList<T> indexedItems;

    public MultipleSelectionController(ObservableList<T> items) {
        indicesList = new IndicesList<>(items);
        indexedItems = new IndexMappedList<>(indicesList);
    }
    
    @Override
    public ObservableList<Integer> getIndices() {
        return indicesList;
    }

    @Override
    public ObservableList<T> getIndexedItems() {
        return indexedItems;
    }

    @Override
    public void setIndices(int... indices) {
        indicesList.setIndices(indices);
    }

    @Override
    public void addIndices(int... indices) {
        indicesList.addIndices(indices);
    }

    @Override
    public void clearIndices(int... indices) {
        indicesList.clearIndices(indices);
    }

    @Override
    public void setAllIndices() {
        indicesList.setAllIndices();
    }

    @Override
    public void clearAllIndices() {
        indicesList.clearAllIndices();
    }

    @Override
    public int sourceIndexOf(T item) {
        int index = indexedItems.indexOf(item);
        return index < 0 ? -1 : indicesList.getSourceIndex(index);
    }

    @Override
    public int getSourceSize() {
        return getSource().size();
    }

    @Override
    public ObservableList<? extends T> getSource() {
        return indicesList.getSource();
    }

    
}
