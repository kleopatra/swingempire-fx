/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import de.swingempire.fx.collection.IndexMappedList;
import de.swingempire.fx.collection.IndicesList;

/**
 * Replacement of MultipleSelectionModelBase. Uses TransformLists to handle selectedIndices/-items.
 * 
 * Note: for now, this assumes an observableList as backing items.<p>
 * 
 * PENDING JW: nearly everything :-) On the bright side: from 150+ tests, 35 are failing and
 * we get 3 errors, not bad for a very quick poc
 * 
 * - enforce mode always
 * - fully support uncontained selectedItem
 * - implement navigational methods
 * - implement anchored and selectionModelJ 
 * - even with indicesList, model must listen to items changes to take care of uncontained 
 *   selection (f.i., there are other parts the indices can't handle for itself, like special
 *   casing selectedIndex)
 * - think about eventBus: would it help to make the model first receive the notification itself,
 *   and then pass it on to the indices? That's more or less the current process in core 
 *   implementations  (which has all handling hard-coded)
 *   
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class AbstractSelectionModelBase<T> extends MultipleSelectionModel<T> {

    protected IndicesList<T> indicesList;
    protected IndexMappedList<T> indexedItems;
    
    
    public AbstractSelectionModelBase() {
        selectedIndexProperty().addListener(valueModel -> {
            // we used to lazily retrieve the selected item, but now we just
            // do it when the selection changes. This is hardly likely to be
            // expensive, and we still lazily handle the multiple selection
            // cases over in MultipleSelectionModel.
            setSelectedItem(getModelItem(getSelectedIndex()));
        });
        
    }
    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return indicesList;
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return indexedItems;
    }

    @Override
    public void selectIndices(int index, int... indices) {
        int toBeSelectedIndex;
        int[] all;
        int indicesSize = indices != null ? indices.length : 0;
        if (indicesSize == 0) {
            all = new int[1];
            all[0] = index;
            toBeSelectedIndex = index;
        } else {
            all = new int[indicesSize + 1];
            all[0] = index;
            System.arraycopy(indices, 0, all, 1, indicesSize);
            // PENDING JW: check for allowed
            toBeSelectedIndex = indices[indices.length - 1];
        }   
        if (getSelectionMode() == SelectionMode.SINGLE) {
            indicesList.setIndices(all);
        } else {
            indicesList.addIndices(all);
        }
        setSelectedIndex(toBeSelectedIndex);
    }

    @Override
    public void selectAll() {
        if (getSelectionMode() == SelectionMode.SINGLE) return;
        indicesList.setAllIndices();
        setSelectedIndex(getItemCount() - 1);
    }

    @Override
    public void clearAndSelect(int index) {
        indicesList.setIndices(index);
        setSelectedIndex(index);
    }

    @Override
    public void select(int index) {
        if (getSelectionMode() == SelectionMode.SINGLE) {
            clearAndSelect(index);
        } else {
            indicesList.addIndices(index);
            setSelectedIndex(index);
        }
    }

    @Override
    public void select(T obj) {
        int index = indicesList.getSource().indexOf(obj);
        if (index > 0) {
            select(index);
        } else {
            setSelectedIndex(-1);
            setSelectedItem(obj);
            
        }
    }

    @Override
    public void clearSelection(int index) {
        indicesList.clearIndices(index);
    }

    @Override
    public void clearSelection() {
        indicesList.clearAllIndices();
        setSelectedIndex(-1);
    }

    @Override
    public boolean isSelected(int index) {
        return indicesList.contains(index);
    }

    @Override
    public boolean isEmpty() {
        return indicesList.isEmpty();
    }

    @Override
    public void selectPrevious() {
        // TODO Auto-generated method stub

    }

    @Override
    public void selectNext() {
        // TODO Auto-generated method stub

    }

    @Override
    public void selectFirst() {
        // TODO Auto-generated method stub
    
    }
    @Override
    public void selectLast() {
        // TODO Auto-generated method stub
    
    }
    /**
     * Returns the number of items in the data model that underpins the control.
     * An example would be that a ListView selection model would likely return
     * <code>listView.getItems().size()</code>. The valid range of selectable
     * indices is between 0 and whatever is returned by this method.
     */
    protected int getItemCount() {
        return indicesList.getSource().size();
    };
    
    /**
     * Returns the item at the given index. An example using ListView would be
     * <code>listView.getItems().get(index)</code>.
     * 
     * @param index The index of the item that is requested from the underlying
     *      data model.
     * @return Returns null if the index is out of bounds, or an element of type
     *      T that is related to the given index.
     */
    protected T getModelItem(int index) {
        if (index < 0 || index >= getItemCount()) return null;
        return indicesList.getSource().get(index);
    };
    
    protected abstract void focus(int index);
    protected abstract int getFocusedIndex();
    

}
