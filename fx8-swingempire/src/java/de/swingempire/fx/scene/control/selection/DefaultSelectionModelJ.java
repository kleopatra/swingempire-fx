/*
 * Created on 29.10.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class DefaultSelectionModelJ<T> implements SelectionModelJ<T> {

    private ListProperty<T> itemsList = new SimpleListProperty<>(this, "itemsList", null);
    private ReadOnlyIntegerWrapper selectedIndex = new ReadOnlyIntegerWrapper(this, "selectedIndex", -1);

    public DefaultSelectionModelJ() {
        this(null);
    }
    
    /**
     * @param items
     */
    public DefaultSelectionModelJ(ListProperty<T> items) {
        if (items != null) {
            itemsList.bind(items);
        }
    }

    public DefaultSelectionModelJ(ObservableList<T> items) {
        itemsList.set(items);
    }
    
    protected ListProperty<T> itemsListProperty() {
        return itemsList;
    }
    
    
    @Override
    public int getItemCount() {
        return itemsListProperty().size();
    }

    protected void setSelectedIndex(int value) { selectedIndex.set(value); }

    @Override
    public final ReadOnlyIntegerProperty selectedIndexProperty() {
        return selectedIndex.getReadOnlyProperty();
    }

    @Override
    public void clearAndSelect(int index) {
        if (!isSelectable(index)) return;
        setSelectedIndex(index);
    }

    @Override
    public void clearSelection(int index) {
        if (!isSelected(index)) return;
        clearSelection();
    }

    @Override
    public void clearSelection() {
        setSelectedIndex(-1);
    }

}
