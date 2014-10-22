/*
 * Created on 22.10.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import javafx.scene.Node;
import javafx.scene.control.TableCell;

/**
 * Though not abstract, TableCell simply shows nothing. Need to
 * subclass and implement updateItem.
 * 
 * C&P updateItem of default tableCell in TableColumn.
 * 
 * Trying to check for identity (vs. equality) when updating the item.
 * No luck and not needed? At least 22463 can be solved with a TableRow
 * that checks for identity.
 */
public class IdentityCheckingTableCell<S, T> extends TableCell<S, T> {
 
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
        LOG.info("item unchanged? " + (oldItem == getItem()));
        // weed out the obvious
        if (oldIndex != getIndex()) return;
        if (oldItem == null || getItem() == null) return;
        if (wasEmpty != isEmpty()) return;
        // here both old and new != null, check whether the item had changed
        if (oldItem != getItem()) return;
        // unchanged, check if it should have been changed
        S listItem = getTableView().getItems().get(getIndex());
        // update if not same
        if (oldItem != listItem) {
//            updateItem(listItem, isEmpty());
        }
    }


    
    @Override protected void updateItem(T item, boolean empty) {
        LOG.info(" in cell: " + item);
        if (item == getItem()) return;

        super.updateItem(item, empty);

        if (item == null) {
            super.setText(null);
            super.setGraphic(null);
        } else if (item instanceof Node) {
            super.setText(null);
            super.setGraphic((Node)item);
        } else {
            super.setText(item.toString());
            super.setGraphic(null);
        }
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(IdentityCheckingTableCell.class.getName()); 
}
