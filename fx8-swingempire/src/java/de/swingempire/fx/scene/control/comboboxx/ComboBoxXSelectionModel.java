/*
 * Created on 07.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import java.util.Objects;

import de.swingempire.fx.property.BugPropertyAdapters;
import de.swingempire.fx.scene.control.selection.ComboXSelectionIssues;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;



/**
 * SelectionModel to use with ComboBoxX.
 * <p>
 * 
 * PENDING JW:
 * <li> remove dependency from view, reduce coupling to its itemsProperty
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboBoxXSelectionModel<T> extends SingleSelectionModel<T> {

    protected final ListProperty<T> itemsList;
    
    /**
     * Instantiates a selectionModel coupled to the given items property.
     * Note that the items are adapted to a listProperty that keeps
     * itself synched to correct value of the property (see RT-15973).
     * 
     * @param items the items to select from, must not be null.
     * 
     * @see BugPropertyAdapters#listProperty(javafx.beans.property.Property)
     */
    public ComboBoxXSelectionModel(ObjectProperty<ObservableList<T>> items) {
        this(BugPropertyAdapters.listProperty(Objects.requireNonNull(items, "items property must not be null")));
    }
    
    /**
     * Instantiates a selectionModel coupled to th given itemsList property, must
     * not be null.
     * 
     * @param itemsList the items to select from, must not be null.
     */
    public ComboBoxXSelectionModel(ListProperty<T> itemsList) {
        this.itemsList = Objects.requireNonNull(itemsList, "itemsList property must not be null");
        
        final ListChangeListener<T> itemsContentObserver = c -> {
            itemsChanged(c);
        };
        itemsListProperty().addListener(itemsContentObserver);
    }

    protected ListProperty<T> itemsListProperty() {
        return itemsList;
    }

    /**
     * Updates selection state after change of items.
     * <p>
     * 
     * This implementation:
     *  <li> clears selection if the list has totally changed (being empty is
     *  a special case here) or a formerly contained selectedItem  has been removed
     *  <li> updates selectedItem if item at
     * selectedIndex was either replaced or updated 
     * <li> - updates selectedIndex to
     * new position of selectedItem
     * 
     * PENDING JW: further test if the conditions above are really met
     * PENDING JW: explore which combinations of all/added/removed/replaced/ are
     * to be expected
     * 
     * @param c
     */
    protected void itemsChanged(Change<? extends T> c) {
        // PENDING JW: looks fishy - but checking for wasRemoved here
        // introduced test failures
        if (wasAllChanged(c)) { // || wasRemoved(c, getSelectedItem())) {
            clearSelection();
            // PENDING JW: wrong thingy to do?
        } else if (wasReplaced(c, getSelectedIndex())
                || wasUpdated(c, getSelectedIndex())) {
            T newItem = getItems().get(getSelectedIndex());
            select(newItem);
        } else if (wasRemoved(c, getSelectedItem())) {
            clearSelection();
        } else { // selected item either still in list or wasn't before the
                 // change
            // update index
            int newIndex = getItems().indexOf(getSelectedItem());
            setSelectedIndex(newIndex);
        }
    }

    protected ObservableList<T> getItems() {
        return itemsListProperty().get();
    }

    // PENDING JW: copy of code at end of updateItemsObserver
    // find out what it was for
    // // when the items list totally changes, we should clear out
    // // the selection and focus
    // int newValueIndex = -1;
    // if (newList != null) {
    // T value = comboBox.getValue();
    // if (value != null) {
    // newValueIndex = newList.indexOf(value);
    // }
    // }
    // setSelectedIndex(newValueIndex);

    /**
     * Returns true if the whole list was changed.
     * 
     * @param c
     * @return
     */
    private boolean wasAllChanged(Change<? extends T> c) {
        // wouldn't have gotten a change if the list had
        // been empty before - actually, we get one
        // if setItems(emptyList) 
        if (isEmptyItems()) return true;
        c.reset();
        int count = c.getList().size();
        while(c.next()) {
            if (c.wasReplaced() || c.wasAdded()) {
                if (c.getFrom() != 0) return false;
                if (c.getAddedSize() == count) return true;
            }
        }
        return false;
    }

    /**
     * @param c
     * @param selectedItem
     * @return
     */
    protected boolean wasUpdated(Change<? extends T> c, int index) {
        if (index < 0)
            return false;
        c.reset();
        while (c.next()) {
            if (c.wasUpdated()) {
                if (index >= c.getFrom() && index < c.getTo()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 
     * PENDING JW: not doing what's intended ..
     * 
     * @param c the change
     * @param index the index in the _old_ list state
     * @return
     */
    protected boolean wasReplaced(Change<? extends T> c, int index) {
        if (index < 0)
            return false;
        c.reset();
        while (c.next()) {
            if (c.wasReplaced()) {
                if (index >= c.getFrom() && index < c.getTo()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the item was removed, false otherwise.
     * 
     * PENDING JW: backed out of a "real" remove (that is !wasAdded) due to test
     * failure in ComboBoxXSelectionIssue
     * 
     * @param c the ListChange received from ObservableList
     * @param item
     * @return
     * 
     * @see ComboXSelectionIssues#testSetItemsIfSelectedItemContained()
     */
    protected boolean wasRemoved(Change<? extends T> c, T item) {
        c.reset();
        while (c.next()) {
            // FXUtils.prettyPrint(c);
            if (item != null && c.getRemoved().contains(item)) {
                return true;
            }
            if (c.wasRemoved()) {
            }
        }
        return false;
    }

    /**
     * Returns true if the items list isn't empty, false otherwise.
     * 
     * @return
     */
    protected boolean isEmptyItems() {
        return getItemCount() == 0;
    }

    /**
     * Overridden to clear selectedIndex if selectedItem not contained in items.
     * Fixes broken class-invariant in super
     * 
     * if (!contains(selectedItem) assertEquals(-1, selectedIndex) if
     * (selectedIndex >= 0) assertEquals(selectedItem, items.get(selectedIndex))
     * 
     */
    @Override
    public void select(T obj) {
        super.select(obj);
        if (isExternalSelectedItem(obj)) {
            setSelectedIndex(-1);
        }
    }

    /**
     * Checks and returns whether item is an external selectedItem
     * 
     * PENDING JW: re-visit null/empty logic
     * 
     * @param item
     * @return
     */
    protected boolean isExternalSelectedItem(T item) {
        if (item == null)
            return false;
        if (getItemCount() == 0) {
            return true;
        }
        return !getItems().contains(item);
    }

    // API Implementation
    @Override
    protected T getModelItem(int index) {
        final ObservableList<T> items = getItems();
        if (items == null)
            return null;
        if (index < 0 || index >= items.size())
            return null;
        return items.get(index);
    }

    @Override
    protected int getItemCount() {
        final ObservableList<T> items = getItems();
        return items == null ? 0 : items.size();
    }
}