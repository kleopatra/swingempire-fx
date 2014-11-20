/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.ListView;
import de.swingempire.fx.collection.IndexMappedList;
import de.swingempire.fx.collection.IndicesList;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class SimpleListSelectionModel<T> extends AbstractSelectionModelBase<T> {

    private ListView<T> listView;
    private ListProperty<T> itemsList;

    /**
     * Live with coupling to view for now, will be removed!
     * @param listView
     */
    public SimpleListSelectionModel(ListView<T> listView) {
        this.listView = listView;
        itemsList = new SimpleListProperty<>();
        itemsList.bind(listView.itemsProperty());
        indicesList = new IndicesList<>(itemsList);
        indexedItems = new IndexMappedList<>(indicesList);
        
        itemsList.addListener(weakItemsContentListener);
//        itemsList.addListener((ListChangeListener<T>) (c -> itemsChanged(c))); 
    }
    
//    /**
//     * IndicesList/IndexMappedItems have taken care of updating selectedIndices/-items,
//     * here we need to update selectedIndex/selectedItem.
//     *  
//     * @param c the change received from the backing items list
//     */
//    protected void itemsChanged(Change<? extends T> c) {
//        if (indicesList.size() != indexedItems.size()) 
//            throw new IllegalStateException("expected internal update to be done!");
//        int oldSelectedIndex = getSelectedIndex();
//        T oldSelectedItem = getSelectedItem();
//        if (oldSelectedIndex < 0) {
//            // no selected index, check selectedItem:
//            // it had not been part of the items before the change (if it had
//            // the selectedIndex wouldn't be < 0) but now might be
//            if (oldSelectedItem != null && itemsList.contains(oldSelectedItem)) {
//                int selectedIndex = itemsList.indexOf(oldSelectedItem);
//                select(selectedIndex);
//            }
//            
//            return;
//        }
//        // we had selectedItem/index pair that was part of the model
//        // first handle empty list
//        if (itemsList.isEmpty()) {
//            clearSelection();
//            return;
//        }
//        
//        // Note JW: isSelected tests whether the given index is part of selectedIndices
//        // (even doc'ed as such - invalidly in SelectionModel)
//        // vs. testing against -1 (which might or not be an option)
//
//        // oldSelectedItem still in selectedItems
//        if (indexedItems.contains(oldSelectedItem)) {
//            int indexedIndex = indexedItems.indexOf(oldSelectedItem);
//            int itemsIndex = indicesList.getSourceIndex(indexedIndex);
//            syncSingleSelectionState(itemsIndex);
//            return;
//        } else { // selectedItem removed, clean up
//            // no, need handle modifications at selectedIndex:
//            // indices/items simply remove - we might need to do better
//            // f.i. on set of single element or remove at selected 
//            // latter is RT-30931
////            syncSingleSelectionState(-1);
//        }
//        
//        // test if item at selectedIndex had been replaced
//        if (indicesList.contains(oldSelectedIndex)) {
//            int newSelectedIndex = -1;
//            while (c.next()) {
//                // PENDING JW: this is not good enough for multiple subchanges
//                if (c.wasReplaced() && c.getRemoved().contains(oldSelectedItem)) {
//                   newSelectedIndex = c.getFrom() + c.getRemoved().indexOf(oldSelectedItem);
//                   break;
//                }
//            }
//            if (newSelectedIndex > -1) {
//                select(newSelectedIndex);
//                return;
//            }
//        }
//        // index still valid
//        if (oldSelectedIndex < itemsList.size() 
//            && !indicesList.contains(oldSelectedIndex)) {
//            T newSelectedItem = itemsList.get(oldSelectedIndex);
//            String msg = "old/new/setSelectedItem: " + getSelectedItem() + "/" + newSelectedItem;
//            select(oldSelectedIndex);
////            LOG.info(msg + " / " + getSelectedItem());
//            
//        }
//    }

    @Override
    protected void focus(int index) {
        if (listView.getFocusModel() == null) return;
        listView.getFocusModel().focus(index);
    }
    
    @Override
    protected int getFocusedIndex() {
        if (listView.getFocusModel() == null) return -1;
        return listView.getFocusModel().getFocusedIndex();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SimpleListSelectionModel.class.getName());
}
