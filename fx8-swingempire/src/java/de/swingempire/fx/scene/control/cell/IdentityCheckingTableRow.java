/*
 * Created on 22.10.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import javafx.scene.control.Skin;
import javafx.scene.control.TableRow;


/**
 * 
 * Extended TableRow that updates its item if equal but not same.
 * Needs custom skin to update cells on invalidation of the 
 * item property.<p>
 * 
 * Looks ugly, as we have to let super doing its job and then
 * re-check the state. No way to hook anywhere else into super 
 * because all is private. <p>
 * 
 * Super might support a configuration option to check against
 * identity vs. against equality.<p>
 * 
 * Note: with the fix of  
 * https://javafx-jira.kenai.com/browse/RT-39094
 * super has a method <code>isItemChanged(old, new)</code> that can be overridden
 * instead of the ugly hook here. The custom skin is still needed, until
 * fix of https://javafx-jira.kenai.com/browse/RT-39321.
 * 
 * 
 * Note that this is _not_ formally tested! Any execution paths calling
 * <code>updateItem(int)</code> other than through 
 * <code>indexedCell.updateIndex(int)</code> are not handled.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class IdentityCheckingTableRow<T>  extends TableRow<T> {

    @Override
    public void updateIndex(int i) {
        int oldIndex = getIndex();
        T oldItem = getItem();
        boolean wasEmpty = isEmpty();
        super.updateIndex(i);
        updateItemIfNeeded(oldIndex, oldItem, wasEmpty);
        
    }

    /**
     * Here we try to guess whether super updateIndex didn't update the item if
     * it is equal to the old.
     * 
     * Strictly speaking, an implementation detail.
     * 
     * @param oldIndex cell's index before update
     * @param oldItem cell's item before update
     * @param wasEmpty cell's empty before update
     */
    protected void updateItemIfNeeded(int oldIndex, T oldItem, boolean wasEmpty) {
        // weed out the obvious
        if (oldIndex != getIndex()) return;
        if (oldItem == null || getItem() == null) return;
        if (wasEmpty != isEmpty()) return;
        // here both old and new != null, check whether the item had changed
        if (oldItem != getItem()) return;
        // unchanged, check if it should have been changed
        T listItem = getTableView().getItems().get(getIndex());
        // update if not same
        if (oldItem != listItem) {
            // doesn't help much because itemProperty doesn't fire
            // so we need the help of the skin: it must listen
            // to invalidation and force an update if 
            // its super wouldn't get a changeEvent
            updateItem(listItem, isEmpty());
        }
    }

    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new TableRowSkinX<>(this);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(IdentityCheckingListCell.class.getName());

}
