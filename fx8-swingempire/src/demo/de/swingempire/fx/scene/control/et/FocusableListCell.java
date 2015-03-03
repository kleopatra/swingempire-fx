/*
 * Created on 03.03.2015
 *
 */
package de.swingempire.fx.scene.control.et;

import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;

import com.sun.javafx.scene.control.skin.ListCellSkin;

/**
 * Trying to make a cell the focusOwner. This would allow the
 * keyboard-activated contextMenu to work out off the box.
 * Need to make the cell focusTraversable and its skin consume
 * mouseEvents (weird that the latter has ?)
 * 
 * Side-effects:
 * 
 * - no selection highlight (greyed out)
 * - no focus indicator on cell
 * - no focus indicator on listView
 * 
 * The first two can be solved by adding the focused/selected styles to the cell
 * directly (vs. to a cell that's a child of a focused listview)
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class FocusableListCell<T> extends ListCell<T> {

    private ContextMenu menu;

    private ChangeListener<Number> focusedIndexListener;
    public FocusableListCell() {
        this(null);
    }
    
    public FocusableListCell(ContextMenu menu) {
        this.menu = menu;
//        setFocusTraversable(true);
        focusedIndexListener = (source, oldIndex, index) -> focusedIndexChanged((Integer)oldIndex, (Integer)index);
        
        listViewProperty().addListener((source, old, value) -> updateFocusListener(old, value));
        
    }

    /**
     * @param oldIndex
     * @param index
     * @return
     */
    private void focusedIndexChanged(int oldIndex, int index) {
//        LOG.info("new focused/my index: " + index + " / " + getIndex());
        if (index > -1 && getIndex() == index) requestFocus();
    }

    /**
     * PENDING only install implemented, focusmodel must be immutable
     * @param old
     * @param value
     * @return
     */
    private void updateFocusListener(ListView<T> old, ListView<T> value) {
        if (old != null) {
            ReadOnlyIntegerProperty focusedIndexProperty = old.getFocusModel().focusedIndexProperty();
            focusedIndexProperty.removeListener(focusedIndexListener);
        }
        if (value != null) {
            value.getFocusModel().focusedIndexProperty().addListener(focusedIndexListener);
        }
    }

    // boiler-plate: copy of default implementation
    @Override 
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty) {
            setContextMenu(null);
            setText(null);
            setGraphic(null);
        } else {
//            if (HAS_CELL_CONTEXT)
                setContextMenu(menu);
            setText(item == null ? "null" : item.toString());
            setGraphic(null);
        }
    }

    
    @Override
    protected Skin<?> createDefaultSkin() {
//        return super.createDefaultSkin();
        return new FocusableListCellSkin<>(this);
    }


    private static class FocusableListCellSkin<T> extends ListCellSkin<T> {

        /**
         * @param control
         */
        public FocusableListCellSkin(ListCell<T> control) {
            super(control);
            consumeMouseEvents(true);
        }
        
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(FocusableListCell.class
            .getName());
}
