/*
 * Created on 03.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;

/**
 * All anchored** are based on 8u20.
 * 
 * Changes in 8u40b9:
 * 
 * ListView.ListBitSelectionModel
 * - selectFirstRowByDefault: listening to properties changes of key 
 *   "selectFirstRowByDefault" and updates private field accordingly

 * ListBitSelectionModel
 * - updateDefaultSelection/setAnchor in constructor of ListViewSelectionModel
 * - overridden clearAndSelect to set anchor
 * - extracted end of updateItems into updateDefaultSelection (which clears selection!) and
 *   calls select(newIndex)
 * 
 * ListViewBehavior
 * - new: checks for replaced in selectedIndices to update anchor
 * - new ListCellBehavior.setAnchor has third parameter
 * - new ListCellBehavior.hasNonDefaultAnchor called from behavior
 * 
 * ListViewSkin
 * - nothing except accessibility
 * 
 * ListCellBehavior
 * - no change
 * 
 * CellBehaviorBase
 * - added notion of defaultAnchor (whatever that is), key "isDefaultAnchor"
 * - default somehow used in set/get/removeAnchor
 * 
 * ControlUtils
 * - unchanged
 * 
 * MultipleSelectionModelBase
 * - select(T): added setSelectedIndex(-1) if item not in list
 * 
 * ListViewFocusModel
 * - focus(0) in constructor
 * - note: NO change in itemsContentListener, just changed to lambda
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ListViewAnchored<T> extends ListView<T> {

    /**
     * 
     */
    public ListViewAnchored() {
        this(FXCollections.<T>observableArrayList());
    }

    /**
     * @param items
     */
    public ListViewAnchored(ObservableList<T> items) {
        super(items);
        setSelectionModel(new AnchoredListViewSelectionModel<>(this));
        setFocusModel(new ListViewAFocusModel<>(this));
//        getProperties().put("selectOnFocusGain", Boolean.FALSE);
//        getProperties().put("selectFirstRowByDefault", Boolean.FALSE);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new de.swingempire.fx.scene.control.skin.ListViewASkin<>(this);
    }
    
}
