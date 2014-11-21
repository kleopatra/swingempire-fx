/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;
import de.swingempire.fx.collection.IndexMappedList;
import de.swingempire.fx.collection.IndicesList;

/**
 * Concrete SelectionModel using indicesList/indexedItems for ListView.
 *   
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

    
    
    @Override
    protected FocusModel<T> getFocusModel() {
        return listView.getFocusModel();
    }

    @Override
    protected void itemsChanged(Change<? extends T> c) {
        super.itemsChanged(c);
        if (listView.getFocusModel() instanceof FocusModelSlave) {
//            updateFocus(c);
        }
    }

    protected void updateFocus(Change<? extends T> c) {
        c.reset();
        while (c.next()) {
            // looking at the first change
            int from = c.getFrom();
            if (getFocusedIndex() == -1 || from > getFocusedIndex()) {
                return;
            }
        }
        c.reset();
        boolean added = false;
        boolean removed = false;
        int addedSize = 0;
        int removedSize = 0;
        while (c.next()) {
            added |= c.wasAdded();
            removed |= c.wasRemoved();
            addedSize += c.getAddedSize();
            removedSize += c.getRemovedSize();
        }

        if (added && !removed) {
            focus(getFocusedIndex() + addedSize);
        } else if (!added && removed) {
            // fix of navigation issue on remove focus at 0
            focus(Math.max(0, getFocusedIndex() - removedSize));
        }
    }

    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SimpleListSelectionModel.class.getName());
}
