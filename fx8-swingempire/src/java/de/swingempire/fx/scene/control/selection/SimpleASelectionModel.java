/*
 * Created on 03.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;


/**
 * Subclass of SimpleListSelectionModel that implements AnchoredSelectionModel.<p>
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SimpleASelectionModel<T> extends
//        ListViewBitSetSelectionModel<T> implements AnchoredSelectionModel {
        SimpleListSelectionModel<T> implements AnchoredSelectionModel {
    /**
     * @param listView
     */
    public SimpleASelectionModel(ListView<T> listView) {
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
    public void anchor() {
        int focus = getFocusedIndex();
        setAnchorIndex(focus);
    }

    //-------------------- overrides of selectionModel base methods    
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

    /**
     * Note: "Keeps the anchor unchanged" - not so easy or unwanted? See super.
     * 
     * PENDING JW:
     * Here we clear if anchor unselected. That's different from AnchoredListSelectionModel:
     * there we don't due to interference by super.
     */
    @Override
    public void clearSelection(int index) {
        boolean anchorCleared = isAnchor(index);
        super.clearSelection(index);
        if (isEmpty() || anchorCleared) {
            clearAnchor();
        }
    }
    
    @Override
    public void clearSelection() {
        super.clearSelection();
        clearAnchor();
    }

//------------------ TODO (?) overrides of selectionModel navigational methods   
    
    
//------------------ TODO overrides of MultipleSelectionModel base methods
    
    @Override
    public void selectIndices(int row, int... rows) {
        boolean wasEmpty = isEmpty();
        boolean adjustAnchor = getSelectionMode() == SelectionMode.SINGLE || isEmpty();
        super.selectIndices(row, rows);
        if (getSelectionMode() == SelectionMode.SINGLE) {
            // in singleSelectionMode move anchor to focus
            anchor();
        } else if (wasEmpty) { // multipleSelection and wasEmpty
            // transition from empty to not empty, anchor first selected
            setAnchorIndex(row);
        }
    }
    
    @Override
    public void selectRange(int start, int end) {
        super.selectRange(start, end);
    }
    
    
// helpers
    
    /**
     * Override intention was to update anchor along with selectedIndex - 
     * got more failing tests, though. Hmm ... double adjust?
     * Maybe could remove anchor handling from methods above?
     * 
     */
    @Override
    protected void syncSingleSelectionState(int selectedIndex) {
        super.syncSingleSelectionState(selectedIndex);
//        setAnchorIndex(selectedIndex);
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
    
    
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SimpleASelectionModel.class.getName());
}
