/*
 * Created on 20.09.2014
 *
 */
package de.swingempire.fx.property;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.util.Callback;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;



import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class PathPropertyTest {

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();
    /**
     * The model set to the views. It contains 5 string items, originally
     * in descending order. Invoking sort will revert the order.
     */
    protected ObservableList<String> items;
    protected ListView<String> view;
    
    @Test
    public void testRootValueChangeToNull() {
        Property<MultipleSelectionModel<String>> root = view.selectionModelProperty();
        root.getValue().select(items.get(2));
        Callback<MultipleSelectionModel<String>, ObservableValue<String>> factory = model -> {
            return model.selectedItemProperty();
        };
        ObservablePathAdapter<MultipleSelectionModel<String>, String> path = new ObservablePathAdapter<>(root, factory);
        view.setSelectionModel(null);
        assertEquals(null, path.get());
    }
    
    @Test
    public void testValueChange() {
        Property<MultipleSelectionModel<String>> root = view.selectionModelProperty();
        root.getValue().select(items.get(2));
        Callback<MultipleSelectionModel<String>, ObservableValue<String>> factory = model -> {
            return model.selectedItemProperty();
        };
        ObservablePathAdapter<MultipleSelectionModel<String>, String> path = new ObservablePathAdapter<>(root, factory);
        root.getValue().select(items.get(3));
        assertEquals(root, path.getRoot());
        assertEquals(root.getValue().getSelectedItem(), path.get());
    }
    
    @Test
    public void testInitialValue() {
        Property<MultipleSelectionModel<String>> root = view.selectionModelProperty();
        root.getValue().select(items.get(2));
        Callback<MultipleSelectionModel<String>, ObservableValue<String>> factory = model -> {
            return model.selectedItemProperty();
        };
        ObservablePathAdapter<MultipleSelectionModel<String>, String> path = new ObservablePathAdapter<>(root, factory);
        assertEquals(root, path.getRoot());
        assertEquals(root.getValue().getSelectedItem(), path.get());
    }
    
    @Test
    public void testInitial() {
        Property<MultipleSelectionModel<String>> root = view.selectionModelProperty();
        Callback<MultipleSelectionModel<String>, ObservableValue<String>> factory = model -> {
            return model.selectedItemProperty();
        };
        ObservablePathAdapter<MultipleSelectionModel<String>, String> path = new ObservablePathAdapter<>(root, factory);
        assertEquals(root, path.getRoot());
        assertEquals(root.getValue().getSelectedItem(), path.get());
    }
    
    @Before
    public void setup() {
        // JW: need more items for multipleSelection
        items = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        view = new ListView<String>(items);
    }

}
