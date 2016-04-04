/*
 * Created on 01.04.2016
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.List;
import java.util.logging.Logger;

import de.swingempire.fx.scene.control.skin.patch.TableRowSkin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

/**
 * Issue: selection not working on filtered/scrolled list
 * https://bugs.openjdk.java.net/browse/JDK-8152665
 * <p>
 * Playing with suggested patch: 
 * - listen to indexProperty just as super and update cell index
 * 
 * No success: brings back 
 * https://bugs.openjdk.java.net/browse/JDK-8115269
 * 
 * might be due to super doing its old stuff ..
 */
public class IndexUpdatingTableRowSkin<T> extends TableRowSkin<T> {

    public IndexUpdatingTableRowSkin(TableRow<T> control) {
        super(control, null);
//        control.itemProperty().addListener(o -> onItemInvalidated());
////        unregisterChangeListener(control.indexProperty());
//        registerChangeListener(control.indexProperty(), e -> onIndexChanged());
    }

    /**
     * @return
     */
    private void onItemInvalidated() {
//        synchIndices();
    }

    /**
     * Implemented to check if all cell's have the same index as 
     * this row and update them if not.
     * @return
     */
    private void onIndexChanged() {
//        if (getSkinnable().isEmpty()) //return;
//        synchIndices();
    }

    /**
     * 
     */
    private void synchIndices() {
        setUpdateCells(true);
        getSkinnable().requestLayout();
        
        List<TableCell<T, ?>> cells = getCells();
        boolean updatedSelf = false;
        for (TableCell<T, ?> tableCell : cells) {
            if (tableCell.getIndex() != getSkinnable().getIndex()) {
                LOG.info("myindex/myItem/cell index: " + getSkinnable().getIndex() 
                        + " / " + getSkinnable().getItem()
                        + " / " + tableCell.getIndex() + tableCell.isEmpty());
                if (!updatedSelf) {
                    updatedSelf = true;
                    updateSelfItem();
                }
                tableCell.updateIndex(getSkinnable().getIndex());
            }
        }
    }
 
    /**
     * 
     */
    private void updateSelfItem() {
        if (!validIndex()) return;;
        T currentItem = getSkinnable().getItem();
        T tableItem = getSkinnable().getTableView().getItems().get(getSkinnable().getIndex());
        if (currentItem != tableItem) {
            getSkinnable().setItem(tableItem);
        }
    }

    /**
     * 
     */
    private boolean validIndex() {
        return getSkinnable().getIndex() >= 0 
                && getSkinnable().getTableView() != null
                && getSkinnable().getTableView().getItems().size() < getSkinnable().getIndex();
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(IndexUpdatingTableRowSkin.class.getName());
}