/*
 * Created on 05.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;


import java.util.Objects;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SingleSelectionModel;
import de.swingempire.fx.property.PathAdapter;

/**
 * A mulitpleSelectionModel which only supports singleSelectionMode.
 * Intended for use in ComboBox's ListView - should be controlled by
 * the combo's selectionModel (ideally, would even use the same
 * model but not possible due to hierarchy ...)
 * <p>
 * 
 * All public singleSelection api is delegated to the controller, internal
 * state is updated when receiving change notifications from the controller.
 * Note that clients <b>must not</b> use internal modifying api (like calling
 * setSelectedIndex/Item) - otherwise the sync might break. But then, sane
 * clients will not have their hands in our bowels anyway, would they :-);
 * <p>
 *  
 * PENDING JW:
 * <li> enforce single mode
 * <li> return read-only lists of items/indices
 * <li> formally test control by focusModel
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SingleMultipleSelectionModel<T> extends MultipleSelectionModel<T> {
    
    // PENDING JW: use read-only wrappers
    private ObservableList<Integer> indices = FXCollections.observableArrayList();
    private ObservableList<T> selectedItems = FXCollections.observableArrayList();

    // use a path to the controller, we need to update internals if 
    // combo's selectionModel is changed
    private PathAdapter<SingleSelectionModel<T>, T> selectedItemPath;
    private PathAdapter<SingleSelectionModel<T>, Integer> selectedIndexPath;
    private Property<FocusModel<T>> focusModel;
    
    /**
     * Instantiates a MultipleSelectionModel that is the slave to the given
     * selectionModel and no focusModel.
     * 
     * @param selectionModel the selectionModel to sync itself to
     */
    public SingleMultipleSelectionModel(Property<SingleSelectionModel<T>> selectionModel) {
        this(selectionModel, null);
    }
    
    /**
     * Instantiates a MultipleSelectionModel that is the slave to the given
     * selection- and focusModel.
     * 
     * @param selectionModel the selectionModel to sync itself to
     * @param focusModel the focusModel to update on selection changes, may be null
     */
    public SingleMultipleSelectionModel(Property<SingleSelectionModel<T>> selectionModel, 
            Property<FocusModel<T>> focusModel) {
        Objects.requireNonNull(selectionModel, "selectionModel property must not be null");
        selectedItemPath = new PathAdapter<>(selectionModel, 
                p -> p.selectedItemProperty(), null);
        selectedItemPath.addListener((o, old, value) -> {selectedItemChanged(value);
        });
        
        selectedIndexPath  = new PathAdapter<SingleSelectionModel<T>, Integer>(
                selectionModel, 
                p -> p.selectedIndexProperty().asObject(), -1);
        selectedIndexPath.addListener((o, old, value) -> selectedIndexChanged(value));
        this.focusModel = focusModel;
        focus();
    }
    
    /** 
     * Callback invoked when receiving changes of the selectedIndex of the controlling
     * selectionModel.
     *  
     * @param value the new selected index
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
         focus();
    }

    private void focus() {
        if (focusModel == null || focusModel.getValue() == null) return;
        focusModel.getValue().focus(getSelectedIndex());
    }

    /** 
     * Callback invoked when receiving changes of the selectedItem of the controlling
     * selectionModel.
     *  
     * @param value the new selected item
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
     * 
     * PENDING: untested
     */
    @Override
    public void selectIndices(int index, int... indices) {
        int last = index;
        if (indices != null && indices.length > 0) {
            last = indices[indices.length - 1];
//            for (int i = indices.length - 1; i >= 0; i--) {
//                int row = indices[i];
//                if (row >= 0 && row < getItemCount()) {
//                    last = row;
//                    break;
//                }
//            }
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

//------------------------ delegate to controller
    
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
     * PENDING JW: public access to controller is a testing artefact, so far.
     * Needed/useful elsewhere?
     * 
     * @return
     */
    public SingleSelectionModel<T> getController() {
        ObservableValue<SingleSelectionModel<T>> controllerProperty = selectedItemPath.getRoot();
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

}
