/*
 * Created on 05.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import java.util.Objects;

import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SingleSelectionModel;
import de.swingempire.fx.property.PathAdapter;

/**
 * A mulitpleSelectionModel which only supports singleSelectionMode.
 * Intended for use in ComboBox's ListView - should be controlled by
 * the combo's selectionModel (ideally, would even use the same
 * model but not possible due to hierarchy ...)
 * 
 * PENDING JW:
 * - enforce single mode
 * - return read-only lists of items/indices
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SingleMultipleSelectionModel<T> extends MultipleSelectionModel<T> {
    
    // PENDING JW: use read-only wrappers
    ObservableList<Integer> indices = FXCollections.observableArrayList();
    ObservableList<T> selectedItems = FXCollections.observableArrayList();

    SingleSelectionModel<T> controller;
    // use a path to the controller, we need to update internals if 
    // combo's selectionModel is changed
    PathAdapter<SingleSelectionModel<T>, T> selectedItemPath;
    PathAdapter<SingleSelectionModel<T>, Integer> selectedIndexPath;
    
    public SingleMultipleSelectionModel(ControllerProvider<T> provider) {
        this.controller = Objects.requireNonNull(controller, "controller must not be null");
        selectedItemPath = new PathAdapter<>(provider.selectionModelProperty(), 
                p -> p.selectedItemProperty());
        selectedItemPath.addListener((o, old, value) -> {selectedItemChanged(value);
        });
        
        selectedIndexPath  = new PathAdapter<SingleSelectionModel<T>, Integer>(
                provider.selectionModelProperty(), 
                p -> p.selectedIndexProperty().asObject());
        selectedIndexPath.addListener((o, old, value) -> selectedIndexChanged(value));
        
    }
    /**
     * @param value
     * @return
     */
    private void selectedIndexChanged(int value) {
        selectInternal(value);
    }
    /**
     * @param value
     * @return
     */
    private void selectInternal(int value) {
         setSelectedIndex(value);
         if (value >= 0) {
             indices.setAll(value);
         } else {
             indices.clear();
         }
    }
    /**
     * 
     */
    private void selectedItemChanged(T value) {
        selectInternal(value);
    }

    /**
     * @param value
     */
    private void selectInternal(T value) {
        setSelectedItem(value);
        if (value != null) {
           selectedItems.setAll(value);
        } else {
            selectedItems.clear();
        }
    }
    
    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return indices;
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return selectedItems;
    }

    /**
     * IMplemented to select only the last in the list
     */
    @Override
    public void selectIndices(int index, int... indices) {
        int last = index;
        if (indices != null && indices.length > 0) {
            for (int i = indices.length - 1; i >= 0; i--) {
                int row = indices[i];
                if (row >= 0 && row < getItemCount()) {
                    last = row;
                    break;
                }
            }
        }
        select(last);
    }

    /**
     * @return
     */
    private int getItemCount() {
        return 0;
    }

    /**
     * Implemented as no-op
     */
    @Override
    public void selectAll() {
    }

    @Override
    public void selectFirst() {
        if (hasController())
            getController().selectFirst();
    }

    /**
     * @return
     */
    private boolean hasController() {
        return getController() != null;
    }
    /**
     * @return
     */
    private SingleSelectionModel<T> getController() {
        Property<SingleSelectionModel<T>> controllerProperty = selectedItemPath.getRoot();
        return controllerProperty != null ? controllerProperty.getValue() : null;
    }
    @Override
    public void selectLast() {
        if (hasController()) {
            getController().selectLast();
        }
            
    }

    @Override
    public void clearAndSelect(int index) {
        if (hasController()) {
            getController().clearAndSelect(index);
        }
    }

    @Override
    public void select(int index) {
        if (hasController()) {
            getController().select(index);
        }
        
    }

    @Override
    public void select(T obj) {
        if (hasController()) {
            getController().select(obj);
        }
    }

    @Override
    public void clearSelection(int index) {
        if (hasController()) {
            getController().clearSelection(index);
        }
    }

    @Override
    public void clearSelection() {
        if (hasController()) {
            getController().clearSelection();
        }
    }

    @Override
    public boolean isSelected(int index) {
        if (hasController()) {
            return getController().isSelected(index);
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        if (hasController()) {
            return getController().isEmpty();
        }
        return true;
    }

    @Override
    public void selectPrevious() {
        if (hasController()) {
            getController().selectPrevious();
        }
    }

    @Override
    public void selectNext() {
        if (hasController()) {
            getController().selectNext();
        }
    }
    
    public static interface ControllerProvider<T> {
        
        public Property<SingleSelectionModel<T>> selectionModelProperty();
    }

}
