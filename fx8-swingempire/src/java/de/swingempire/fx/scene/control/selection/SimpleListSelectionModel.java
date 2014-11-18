/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.control.ListView;
import de.swingempire.fx.collection.IndexMappedList;
import de.swingempire.fx.collection.IndicesList;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class SimpleListSelectionModel<T> extends AbstractSelectionModelBase<T> {

    private ListView<T> listView;
    private ListProperty<T> itemsList;
    //    public SimpleListSelectionModel(ObjectProperty<T> itemsPropery) {
//        
//    }
//    
    /**
     * Live with coupling to view for now, will be removed!
     * @param listView
     */
    public SimpleListSelectionModel(ListView<T> listView) {
        this.listView = listView;
        itemsList = new SimpleListProperty<>();
        itemsList.bind(listView.itemsProperty());
        indicesList = new IndicesList<>(itemsList);
        indexedItems = new IndexMappedList<>(indicesList, itemsList);
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
}
