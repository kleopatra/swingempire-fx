/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;

/**
 * Concrete SelectionModel using indicesList/indexedItems for ListView.
 *   
 * Note: this can extend AbstractSelectionModelBase/-React as needed - functionally
 * equivalent except for handling correlated selectedIndex/Item in the latter.
 *   
 * @author Jeanette Winzenburg, Berlin
 */
public class SimpleListSelectionModel<T> 
    extends AbstractSelectionModelBase<T> {
//    extends AbstractSelectionModelReact<T> {

    private ListView<T> listView;
    private ListProperty<T> itemsList;
    private ListBasedSelectionHelper<T> helper;
    
    /**
     * Live with coupling to view for now, will be removed!
     * @param listView
     */
    public SimpleListSelectionModel(ListView<T> listView) {
        this.listView = listView;
        itemsList = new SimpleListProperty<>();
        itemsList.bind(listView.itemsProperty());
        controller = new MultipleSelectionController<>(itemsList);
        // PENDING JW: this is brittle: need to register _after_ controller!
        helper = new ListBasedSelectionHelper<>(this, itemsList);
    }
    
    @Override
    protected void focus(int index) {
        if (getFocusModel() == null) return;
        getFocusModel().focus(index);
    }
    
    @Override
    protected int getFocusedIndex() {
        if (getFocusModel() == null) return -1;
        return getFocusModel().getFocusedIndex();
    }
    
    @Override
    protected FocusModel<T> getFocusModel() {
        return listView.getFocusModel();
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SimpleListSelectionModel.class.getName());
}
