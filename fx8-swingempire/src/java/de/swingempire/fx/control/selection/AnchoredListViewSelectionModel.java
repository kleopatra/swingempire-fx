/*
 * Created on 03.09.2014
 *
 */
package de.swingempire.fx.control.selection;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class AnchoredListViewSelectionModel<T> extends
        ListViewBitSetSelectionModel<T> implements AnchoredSelectionModel {

    /**
     * @param listView
     */
    public AnchoredListViewSelectionModel(ListView<T> listView) {
        super(listView);
    }

    private ReadOnlyIntegerWrapper anchorIndex = new ReadOnlyIntegerWrapper(this, "anchorIndex", -1);
    protected final void setAnchorIndex(int value) { anchorIndex.set(value); }

    @Override
    public int getAnchorIndex() {
        return anchorIndexProperty().get();
    }

    @Override
    public ReadOnlyIntegerProperty anchorIndexProperty() {
        return anchorIndex.getReadOnlyProperty();
    }

    @Override
    public void clearAndSelect(int row) {
        super.clearAndSelect(row);
        setAnchorIndex(row);
    }

    @Override
    public void select(int row) {
        boolean adjustAnchor = getSelectionMode() == SelectionMode.SINGLE || isEmpty();
        super.select(row);
        if (adjustAnchor) {
            setAnchorIndex(row);
        }
    }

    @Override
    public void clearSelection(int index) {
        boolean anchorCleared = isAnchor(index);
        super.clearSelection(index);
        if (isEmpty() || anchorCleared) {
            clearAnchor();
        }
    }

    /**
     * @param index
     * @return
     */
    protected boolean isAnchor(int index) {
        if (index == -1) return false;
        return index == getAnchorIndex();
    }

    /**
     * 
     */
    protected void clearAnchor() {
        setAnchorIndex(-1);
    }
    
    

}
