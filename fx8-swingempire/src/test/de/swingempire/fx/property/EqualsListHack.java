/*
 * Created on 12.10.2014
 *
 */
package de.swingempire.fx.property;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Issue: RT-15793 - no notification from ObjectProperty<ObservableList> when
 * setting newList with newList.equals(oldList) && newList != oldList.
 * <p>
 * Without change to core api, there's no nice solution except adapting
 * a ListProperty that's bidi-binding itself to the objectProperty and
 * additionally registering an invalidationListener - on invalidation of
 * the bound objectProperty it'll explicitly sets its own value to that of
 * the objectProperty. Will not fire anything in itself, but rewires itself
 * to the new list, thus correctly firing notifications on modifications
 * to the new list (vs. nothing without, as its internal listChangeListener wasn't
 * rewired)
 * <p>
 * 
 * Keep this as a quick standalone check for fixed RT-38770 and its effect
 * on bidi-binding.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EqualsListHack {

    @Test
    public void testListChangeAndBound() {
        ObjectProperty<ObservableList> object = new SimpleObjectProperty<>();
        ListProperty listProperty = createListProperty(object);
        ObservableList list = createList(4);
        object.set(list);
        List listener = addListChangeListener(listProperty);
        object.set(createList(4));
        // setting an equals list doesn't fire but forces a rewire?
        // get notification on next list modification
        object.get().remove(0);
        assertEquals(1, listener.size());
    }
    
    
    @Test
    public void testListChangeNotification() {
        ListProperty object = new SimpleListProperty(createList(3));
        List listener = addListChangeListener(object);
        object.set(createList(3));
        assertEquals("ListProperty listChange notification ", 1, listener.size());
    }
    
    /**
     * Maybe same?
     * RT-38770 second listChangeListener not notified?
     */
    @Test
    public void testListChangeNotification2() {
        ListProperty object = new SimpleListProperty(createList(3));
        List listener = addListChangeListener(object);
        List listener2 = addListChangeListener(object);
        object.set(createList(3));
        assertEquals("ListProperty listChange notification ", 1, listener.size());
    }
    
    @Test
    public void testListNotification() {
        ListProperty object = new SimpleListProperty(createList(3));
        List listener = addChangeListener(object);
        object.set(createList(3));
        assertEquals("ListProperty change notification tests against equals", 1, listener.size());
    }
    
    /**
     * RT-38770: ListProperty notification incorrect for more than one 
     * changeListener
     */
    @Test
    public void testListNotification2() {
        ListProperty object = new SimpleListProperty(createList(3));
        List listener = addChangeListener(object);
        List listener2 = addChangeListener(object);
        object.set(createList(3));
        assertEquals("RT-3877 listProperty with more than one ChangeListener", 1, listener.size());
        assertEquals("RT-3877 listProperty with more than one ChangeListener", 1, listener2.size());
    }
    
    
    
    @Test
    public void testObjectNotification() {
        ObjectProperty object = new SimpleObjectProperty<ObservableList>(createList(3));
        List listener = addChangeListener(object);
        object.set(createList(3));
        assertEquals("ObjectProperty change notification tests against equals", 0, listener.size());
    }
    
    /**
     * @param object
     * @return
     */
    private List addChangeListener(Property object) {
        List listener = new ArrayList();
        ChangeListener l = (o, old, value) -> listener.add(value);
        object.addListener(l);
        return listener;
    }

    private List addListChangeListener(ObservableList list) {
        List listener = new ArrayList();
        ListChangeListener l = c -> listener.add(c);
        list.addListener(l);
        return listener;
    }

    /**
     * This makes a difference in ComboBoxX, but not in isolation like here.
     * Notification always missing, nevertheless selection updated and
     * listener rewired in comboBoxX? The error must be elsewhere as well ...
     * 
     * Wait for fix of 38770 (is in 8u40b9)
     *  
     * @param property
     * @return
     */
    private ListProperty createListProperty(ObjectProperty property) {

        ListProperty itemsList = new SimpleListProperty(this, "itemsList") {
            {
                bindBidirectional(property);
                // PENDING JW: here the hack is working - not in
                // BugPropertyAdapter: why not?
                // the other is an adapter, here we do it ourselves?
                InvalidationListener hack15793 = o -> {
                    ObservableList newItems = ((ObjectProperty<ObservableList>) o)
                            .get();
                    ObservableList oldItems = get();
                    boolean changedEquals = (newItems != null)
                            && (oldItems != null) && newItems.equals(oldItems);
                    if (changedEquals) {
                        set(newItems);
                    }
                };
                property.addListener(hack15793);
            }
        };
        return itemsList;
    }
    
    private ObservableList createList(int count){
        ObservableList list = FXCollections.observableArrayList();
        for (int i = 0; i < count; i++) {
            list.add(i + "-item");
            
        }
        return list;
    };

}
