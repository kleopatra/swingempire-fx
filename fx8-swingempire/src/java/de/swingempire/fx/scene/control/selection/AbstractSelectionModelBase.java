/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

/**
 * Replacement of MultipleSelectionModelBase. Lets IndexedItemsController manage 
 * the technicalities of selectedIndices/-Items lists. Handles itself semantics of 
 * multiple selection and sync to single selection state. 
 * <p>
 * We still have issues
 * with correlated properties: selectedItems/Indices, selectedItem/Index 
 * are not orthogonal to each other. The pair of lists is implemented such that
 * at notification time it is safe to access the other half. It is unsafe to access
 * single selection while being notified by the list changes because it it not yet
 * updated. Also unsafe to access the index while being notified about item change.
 * (see https://javafx-jira.kenai.com/browse/RT-39552) 
 * 
 * <p> 
 * 
 * NOTE: for now, this assumes an ObservableList as backing items. Most probably
 * extendable to not (need another implementation of IndexedItems) such that
 * applicable for tree structures as well. 
 * 
 * Experimental: separated out the sync of single selection state (aka: listening to
 * backing data and update selectedItem/-Index after multiple state is updated) 
 * into ListBasedSelectionHelper. 
 * 
 * With that in place, tree-based 
 * data would need a IndexedItemsController and TreeBasedSelectionHelper, both
 * listening and to TreeItem events and updating themselves accordingly. Those 
 * would be injected into this class without requiring much further change.
 * 
 * Needs access to protected api for now, might be possible to get rid of. 
 * 
 * <p>
 * 
 * PENDING JW: 
 * <p>
 * <li> DONE: enforce mode always
 * <li> DONE: fully support uncontained selectedItem
 * <li> DONE: implement navigational methods
 * <li> implement anchored and selectionModelJ 
 * <li> DONE: even with indicesList, model must listen to items changes to take care of uncontained 
 *   selection (f.i., there are other parts the indices can't handle for itself, like special
 *   casing selectedIndex)
 *   
 * Partly done (hacked for now):  
 * <li> think about eventBus: would it help to make the model first receive the notification itself,
 *   and then pass it on to the indices? That's more or less the current process in core 
 *   implementations  (which has all handling hard-coded)
 * <li> relying on the sequence of calling listeners (first-in, first-notified): clients
 *   must first create the IndexedItemsController, then register themselves.
 *   It's brittle, obviously.   
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class AbstractSelectionModelBase<T> extends MultipleSelectionModel<T> {

    protected IndexedItemsController<T> controller;

    
    public AbstractSelectionModelBase() {
        // PENDING JW: better not, need to special case re-setting same 
        // selectedIndex anyway
//        selectedIndexProperty().addListener(valueModel -> {
//            // we used to lazily retrieve the selected item, but now we just
//            // do it when the selection changes. This is hardly likely to be
//            // expensive, and we still lazily handle the multiple selection
//            // cases over in MultipleSelectionModel.
//            setSelectedItem(getModelItem(getSelectedIndex()));
//        });
        
    }
    
//-------------- MultipleSelectionModel api
    
    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return controller.getIndices();
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return controller.getIndexedItems();
    }

    /**
     * @param index must be valid?
     * @param indices 
     */
    @Override
    public void selectIndices(int index, int... indices) {
        if (indices == null) indices = new int[0];
        List<Integer> validIndices = new ArrayList<>();
        if (isSelectable(index)) validIndices.add(index);
        for (Integer value : indices) {
            if (isSelectable(value)) validIndices.add(value);
        }
        if (validIndices.isEmpty()) return;
        // arggghhh ....
        int[] all = new int[validIndices.size()];
        for (int i = 0; i < validIndices.size(); i++) {
            all[i] = validIndices.get(i);
        }
        doSelectIndices(true, all);
    }

    /**
     * {@inheritDoc} <p>
     * Overridden to do nothing if start or end are off range.
     * 
     * partly reverted: end might be -1 for descending range - is
     * handled correctly by super.
     * 
     * @param start one boundary of the range, must be strictly valid
     * @param end the other boundary of the range, may be one off range because
     *    it is exclusiv
     */
    @Override
    public void selectRange(int start, int end) {
        if (!isSelectable(start) || end < -1) return;
        super.selectRange(start, end);
    }

    @Override
    public void selectAll() {
        if (getSelectionMode() == SelectionMode.SINGLE) return;
        controller.setAllIndices();
        syncSingleSelectionState(getItemCount() - 1);
    }

//------------------- SelectionModel api
    
    @Override
    public void clearAndSelect(int index) {
        if (!isSelectable(index)) return;
        doSelectIndices(false, index);
    }

    @Override
    public void select(int index) {
        if (!isSelectable(index)) return;
        doSelectIndices(true, index);
    }

    @Override
    public void select(T obj) {
        int index = controller.sourceIndexOf(obj);
        if (index > -1) {
            select(index);
        } else {
            selectExternalItem(obj);
        }
    }

    /**
     * PENDING JW: this incorrectly used to clear the selectedIndex/Item even if != index.
     * Need to check what really should happen, similar to remove selectedItem in 
     * list? Unsolved in core as well .. though core doesn't touch focus/selectedIndex.
     * <p>
     * For now, doing similar: clear out single selection state (but leaving focus untouched) 
     * if index was last selected, do nothing otherwise.
     * 
     * @see MultipleSelectionIssues#testIndicesSelectedIndexIsUpdatedAfterUnselect()
     */
    @Override
    public void clearSelection(int index) {
        if (!isSelected(index)) return;
        controller.clearIndices(index);
        if (isEmpty()) {
            syncSingleSelectionState(-1, false);
        }    
    }

    @Override
    public void clearSelection() {
        controller.clearAllIndices();
        syncSingleSelectionState(-1);
    }

    @Override
    public boolean isSelected(int index) {
        return getSelectedIndices().contains(index);
    }

    @Override
    public boolean isEmpty() {
        return getSelectedIndices().isEmpty();
    }

//------------------- navigational methods    
    @Override
    public void selectPrevious() {
        int previous = getCurrentIndex() - 1;
        if (previous < 0) previous = getItemCount() - 1;
        if (previous < 0) return;
        select(previous);
    }

    @Override
    public void selectNext() {
        int current = getCurrentIndex();
        if (current < 0 && getItemCount() == 0) return;
        int next = current + 1;
        select(next);
    }

    @Override
    public void selectFirst() {
        if (getItemCount() == 0) return;
        select(0);
    }

    @Override
    public void selectLast() {
        if (getItemCount() == 0) return;
        select(getItemCount() - 1);
    }

//--------------------- workhorsesd 
    
    /**
     * Adds/sets the given indices. Enforces selectionMode. Last index in
     * array will be selectedIndex.
     * 
     * @param add flag to control add/set, if true will be added, will 
     *    be set otherwise.
     * @param indices the indices to set, must have minimal length of 1 and only
     *   contain indices that are in-range.
     * @throws NullPointerException if indices are null
     */
    protected void doSelectIndices(boolean add, int... indices) {
        Objects.requireNonNull(indices, "indices must not be null");
        if (getSelectionMode() == SelectionMode.SINGLE) {
            add = false;
            indices = new int[] {indices[indices.length -1]};
        }        
        if (add) {    
            controller.addIndices(indices);
        } else {
            controller.setIndices(indices);
        }
        syncSingleSelectionState(indices[indices.length - 1]);
    }
    
    /**
     * Updates single selectionState (including focus) to selectedIndex. 
     * @param selectedIndex the new selectedIndex
     * 
     * @see #syncSingleSelectionState(int, boolean)
     */
    protected void syncSingleSelectionState(int selectedIndex) {
        syncSingleSelectionState(selectedIndex, true);
    }

    /**
     * Updates single selectionState to selectedIndex. Called whenever
     * selectedIndices are changed, either directly from the modifying methods
     * or on receiving changes from the backing list in itemsContentChanged.
     * <p>
     * 
     * This implementation 
     * <li> sets selectedIndex
     * <li> sets selectedItem to item at selectedIndex or null if -1
     * <li> focus to selectedIndex
     * 
     * @param selectedIndex the new selectedIndex
     * @param withFocus a boolean indicating whether focus should be synced to the index 
     *   as well
     */
    protected void syncSingleSelectionState(int selectedIndex, boolean withFocus) {
        setSelectedIndex(selectedIndex);
        if (selectedIndex > -1) {
            setSelectedItem(getModelItem(selectedIndex));
        } else {
            // PENDING JW: do better? can be uncontained item
            setSelectedItem(null);
        } 
        if (withFocus) {
            focus(selectedIndex);
        }
    }

    /**
     * Returns a boolean indicating whether the given index can be selected.
     * This implementations returns true if in valid range -1 < index < getItemCount.
     * 
     * @param index
     * @return
     */
    protected boolean isSelectable(int index) {
        return index > -1 && index < getItemCount();
    }

    /**
     * Selects an item that is not contained in the backing list and updates
     * single selection state as needed.<p>
     * 
     * This implementation clears selected index.
     * 
     * @param obj
     */
    protected void selectExternalItem(T obj) {
        setSelectedItem(obj);
        setSelectedIndex(-1);
    }

    /**
     * @return
     */
    protected int getCurrentIndex() {
        int current = getFocusedIndex();
        if (current < -1) current = getSelectedIndex();
        return current;
    }


    /**
     * Returns the number of items in the data model that underpins the control.
     * An example would be that a ListView selection model would likely return
     * <code>listView.getItems().size()</code>. The valid range of selectable
     * indices is between 0 and whatever is returned by this method.
     */
    protected int getItemCount() {
        return controller.getSourceSize();
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
        return controller.getSourceItem(index);
    };
    
    protected abstract FocusModel<T> getFocusModel();
    protected abstract void focus(int index);
    protected abstract int getFocusedIndex();
    

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AbstractSelectionModelBase.class.getName());
}
