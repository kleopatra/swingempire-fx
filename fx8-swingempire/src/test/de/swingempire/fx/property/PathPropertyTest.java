/*
 * Created on 20.09.2014
 *
 */
package de.swingempire.fx.property;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
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

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.junit.JavaFXThreadingRule;
import static org.junit.Assert.*;

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
    
    private final ObservableList<Person> persons =
            FXCollections.observableArrayList(
                    new Person("Jacob", "Smith", "jacob.smith@example.com"),
                    new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
                    new Person("Ethan", "Williams", "ethan.williams@example.com"),
                    new Person("Emma", "Jones", "emma.jones@example.com"),
                    new Person("Michael", "Brown", "michael.brown@example.com"));

    /**
     * Test whether it's possible to use the path in a bidi binding, provided
     * the child property is a read/write property
     */
    @Test
    public void testPathBidi() {
        Person person = persons.get(0);
        ObjectProperty<Person> root = new SimpleObjectProperty<>(person);
        Callback<Person, ObservableValue<String>> factory = p -> p.emailProperty();
        PathAdapter<Person, String> path = new PathAdapter<Person, String>(root, factory);
        assertEquals(root.get().getEmail(), path.get());
        String email = "dummy";
        root.get().setEmail(email);
        assertEquals(email, path.get());
        String other = "other";
        path.set(other);
        assertEquals(other, root.get().getEmail());
    }

    @Test
    public void testPathNullRootValue() {
        ObjectProperty<Person> root = new SimpleObjectProperty<>();
        assertEquals("sanity", null, root.get());
        Callback<Person, ObservableValue<String>> factory = p -> p.emailProperty();
        PathAdapter<Person, String> path = new PathAdapter<>(root, factory);
    }
    @Test
    public void testRootValueChangeToNull() {
        Property<MultipleSelectionModel<String>> root = view.selectionModelProperty();
        root.getValue().select(items.get(2));
        Callback<MultipleSelectionModel<String>, ObservableValue<String>> factory = model -> {
            return model.selectedItemProperty();
        };
        PathAdapter<MultipleSelectionModel<String>, String> path = new PathAdapter<>(root, factory);
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
        PathAdapter<MultipleSelectionModel<String>, String> path = new PathAdapter<>(root, factory);
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
        PathAdapter<MultipleSelectionModel<String>, String> path = new PathAdapter<>(root, factory);
        assertEquals(root, path.getRoot());
        assertEquals(root.getValue().getSelectedItem(), path.get());
    }
    
    @Test
    public void testInitial() {
        Property<MultipleSelectionModel<String>> root = view.selectionModelProperty();
        Callback<MultipleSelectionModel<String>, ObservableValue<String>> factory = model -> {
            return model.selectedItemProperty();
        };
        PathAdapter<MultipleSelectionModel<String>, String> path = new PathAdapter<>(root, factory);
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
