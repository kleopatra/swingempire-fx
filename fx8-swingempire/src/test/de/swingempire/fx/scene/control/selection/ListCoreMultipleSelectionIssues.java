/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

/**
 * Testing MultipleSelection api in both modes.
 * 
 * Here we test unchanged core ListView with core selection/focusModel.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class ListCoreMultipleSelectionIssues extends AbstractListMultipleSelectionIssues<ListView> {

    /**
     * Trying to make test fail for 
     * https://bugs.openjdk.java.net/browse/JDK-8197985
     * 
     * The broken notification is from selectRange in selectedIndicesList:
     * it fires a change with the _value_ (that is the index in the underlying
     * items) when it should fire a change with the index in itself.
     */
    @Test
    public void testEventIndicesOnSelectRange() {
        if (!multipleMode) return;
        ObservableList<String> listitems = FXCollections.observableArrayList("zero", "one", "two");
        final ListView<String> lv = new ListView<>();
        lv.setItems(listitems);
        
        int selected = 1;
        lv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lv.getSelectionModel().select(selected);
        lv.getSelectionModel().getSelectedIndices().addListener((ListChangeListener<Integer>) ch -> {
            int size = ch.getList().size();
            assertEquals("added another selection index", 2, size);
            ch.next();
            LOG.info("dooo  + getting here?"  + ch.getList() + ch.getFrom() );
            assertTrue("added", ch.wasAdded());
            assertTrue("getFrom must be valid < " + size + " but was " + ch.getFrom(), ch.getFrom() < size);
        });
        int focus = lv.getFocusModel().getFocusedIndex();
        assertEquals("sanity - focus must be selected ", selected, focus);
        // select the next element
        lv.getSelectionModel().selectRange(selected, focus + 2);
    }
    
    /**
     * Trying to make test fail for 
     * https://bugs.openjdk.java.net/browse/JDK-8197985
     * 
     * The broken notification is from selectRange in selectedIndicesList:
     * it fires a change with the _value_ (that is the index in the underlying
     * items) when it should fire a change with the index in itself.
     */
    @Test
    public void testEventItemsOnExtendSelection() {
        if (!multipleMode) return;
        ObservableList<String> listitems = FXCollections.observableArrayList("zero", "one", "two");
        final ListView<String> lv = new ListView<>();
        lv.setItems(listitems);
        
        int selected = 1;
        lv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lv.getSelectionModel().select(selected);
        lv.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) ch -> {
            while (ch.next()) {
                if (ch.wasAdded()) {
                    System.out.println("+" + ch.getAddedSubList());
                }
            }
        });
        int focus = lv.getFocusModel().getFocusedIndex();
        assertEquals("focus must be selected ", selected, focus);
        // select the next element
        lv.getSelectionModel().selectRange(selected, focus + 2);
    }
    
    @Test
    public void testSelectedIndicesFireOnAddItem() {
        if (!multipleMode) return;
        ListView<String> stringListView = new ListView<>();
        stringListView.getItems().addAll("a", "b");
        
        MultipleSelectionModel<String> sm = stringListView.getSelectionModel();
        sm.setSelectionMode(SelectionMode.MULTIPLE);
        IntegerProperty counter = new SimpleIntegerProperty();
        sm.selectAll();
        
    }
    
    /**
     * Issue: ListView must not fire on re-select already selected.
     */
    @Test 
    public void testSelectedIndicesReselect() {
        if (!multipleMode) return;
        ListView<String> stringListView = new ListView<>();
        stringListView.getItems().addAll("a", "b");
        
        MultipleSelectionModel<String> sm = stringListView.getSelectionModel();
        sm.setSelectionMode(SelectionMode.MULTIPLE);
        IntegerProperty counter = new SimpleIntegerProperty();
        sm.selectAll();
        String selected = sm.getSelectedItem();
        int selectedIndex = sm.getSelectedIndex();
        List<String> selectedItems = new ArrayList(sm.getSelectedItems());
        List<Integer> selectedIndices = new ArrayList(sm.getSelectedIndices());
        sm.getSelectedIndices().addListener((ListChangeListener<Integer>) c -> {
            counter.set(counter.get() +1);
        });
        // add selectedIndex - changes nothing as it is already selected
        sm.select(selectedIndex);
        assertSame("sanity: state unchanged", selected, sm.getSelectedItems().get(1));
        assertEquals("sanity: state unchanged", 1, selectedIndex);
        assertEquals("sanity: state unchanged", selectedItems, sm.getSelectedItems());
        assertEquals("sanity: state unchanged", selectedIndices, sm.getSelectedIndices());
        assertEquals("must not fire if nothing changed", 0, counter.get());
    }
    
    @Test 
    public void testSelectedItemsReselect() {
        if (!multipleMode) return;
        ListView<String> stringListView = new ListView<>();
        stringListView.getItems().addAll("a", "b");
        
        MultipleSelectionModel<String> sm = stringListView.getSelectionModel();
        sm.setSelectionMode(SelectionMode.MULTIPLE);
        IntegerProperty counter = new SimpleIntegerProperty();
        sm.selectAll();
        String selected = sm.getSelectedItem();
        int selectedIndex = sm.getSelectedIndex();
        List<String> selectedItems = new ArrayList(sm.getSelectedItems());
        List<Integer> selectedIndices = new ArrayList(sm.getSelectedIndices());
        sm.getSelectedItems().addListener((ListChangeListener<String>) c -> {
            counter.set(counter.get() +1);
        });
        // add selectedIndex - changes nothing as it is already selected
        sm.select(selectedIndex);
        assertSame("sanity: state unchanged", selected, sm.getSelectedItems().get(1));
        assertEquals("sanity: state unchanged", 1, selectedIndex);
        assertEquals("sanity: state unchanged", selectedItems, sm.getSelectedItems());
        assertEquals("sanity: state unchanged", selectedIndices, sm.getSelectedIndices());
        assertEquals("must not fire if nothing changed", 0, counter.get());
    }
    
    /**
     * Here we test select(selectedIndex) twice: the second must not fire 
     * any event as no state is changed.
     */
    @Test 
    public void test_rt_37360_reselect() {
        ListView<String> stringListView = new ListView<>();
        stringListView.getItems().addAll("a", "b");
        
        MultipleSelectionModel<String> sm = stringListView.getSelectionModel();
        sm.setSelectionMode(SelectionMode.MULTIPLE);
        // PENDING JW: direct add doesn't work: 
        // expressions/bindings are defined on the
        // level of the ReadOnlyXX and return a new Binding (vs. 
        // modifying the current
        IntegerProperty adder = new SimpleIntegerProperty();
        IntegerProperty remover = new SimpleIntegerProperty();
        sm.getSelectedItems().addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    adder.set(adder.get() + c.getAddedSize());
                }
                if (c.wasRemoved()) {
                    remover.set(remover.get() + c.getRemovedSize());
                }
            }
        });
        
        assertEquals(0, sm.getSelectedItems().size());
        assertEquals(0, adder.get());
        assertEquals(0, remover.get());
        
        sm.select(0);
        assertEquals(1, sm.getSelectedItems().size());
        assertEquals(1, adder.get());
        assertEquals(0, remover.get());
        
        sm.select(1);
        assertEquals(2, sm.getSelectedItems().size());
        assertEquals(2, adder.get());
        assertEquals(0, remover.get());
        
        sm.select(1);
        assertEquals(2, sm.getSelectedItems().size());
        assertEquals(2, adder.get());
        assertEquals(0, remover.get());
    }
    
    @Test 
    public void test_rt_37360() {
        ListView<String> stringListView = new ListView<>();
        stringListView.getItems().addAll("a", "b");

        MultipleSelectionModel<String> sm = stringListView.getSelectionModel();
        sm.setSelectionMode(SelectionMode.MULTIPLE);
        // PENDING JW: direct add doesn't work: 
        // expressions/bindings are defined on the
        // level of the ReadOnlyXX and return a new Binding (vs. 
        // modifying the current
        IntegerProperty adder = new SimpleIntegerProperty();
        IntegerProperty remover = new SimpleIntegerProperty();
        sm.getSelectedItems().addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    adder.set(adder.get() + c.getAddedSize());
                }
                if (c.wasRemoved()) {
                    remover.set(remover.get() + c.getRemovedSize());
                }
            }
        });

        assertEquals(0, sm.getSelectedItems().size());
        assertEquals(0, adder.get());
        assertEquals(0, remover.get());

        sm.select(0);
        assertEquals(1, sm.getSelectedItems().size());
        assertEquals(1, adder.get());
        assertEquals(0, remover.get());

        sm.select(1);
        assertEquals(2, sm.getSelectedItems().size());
        assertEquals(2, adder.get());
        assertEquals(0, remover.get());

        sm.clearAndSelect(1);
        assertEquals(1, sm.getSelectedItems().size());
        assertEquals(2, adder.get());
        assertEquals(1, remover.get());
    }

    // Note: this is the exact copy of original test - brittle!!
    // can't be re-used, but easy to forget
    private int rt_37360_add_count = 0;
    private int rt_37360_remove_count = 0;
    @Test public void testOriginal_rt_37360() {
        ListView<String> stringListView = new ListView<>();
        stringListView.getItems().addAll("a", "b");

        MultipleSelectionModel<String> sm = stringListView.getSelectionModel();
        sm.setSelectionMode(SelectionMode.MULTIPLE);
        sm.getSelectedItems().addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    rt_37360_add_count += c.getAddedSize();
                }
                if (c.wasRemoved()) {
                    rt_37360_remove_count += c.getRemovedSize();
                }
            }
        });

        assertEquals(0, sm.getSelectedItems().size());
        assertEquals(0, rt_37360_add_count);
        assertEquals(0, rt_37360_remove_count);

        sm.select(0);
        assertEquals(1, sm.getSelectedItems().size());
        assertEquals(1, rt_37360_add_count);
        assertEquals(0, rt_37360_remove_count);

        sm.select(1);
        assertEquals(2, sm.getSelectedItems().size());
        assertEquals(2, rt_37360_add_count);
        assertEquals(0, rt_37360_remove_count);
        
        // re-select already selected
//        sm.select(1);
//        assertEquals(2, sm.getSelectedItems().size());
//        assertEquals(2, rt_37360_add_count);
//        assertEquals(0, rt_37360_remove_count);

        sm.clearAndSelect(1);
        assertEquals(1, sm.getSelectedItems().size());
        assertEquals(2, rt_37360_add_count);
        assertEquals(1, rt_37360_remove_count);
    }
    
    public ListCoreMultipleSelectionIssues(boolean multiple) {
        super(multiple);
    }
 
    @Override
    protected ListView createView(ObservableList items) {
        ListView table = new ListView(items);
        MultipleSelectionModel model = table.getSelectionModel();
        assertEquals("sanity: test setup assumes that initial mode is single", 
                SelectionMode.SINGLE, model.getSelectionMode());
        checkMode(model);
        // PENDING JW: this is crude ... think of doing it elsewhere
        // the problem is to keep super blissfully unaware of possible modes
        assertEquals(multipleMode, model.getSelectionMode() == SelectionMode.MULTIPLE);
        return table;
    }

    
    @Override
    public void setUp() throws Exception {
        super.setUp();
//        needsKey = true;
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ListCoreMultipleSelectionIssues.class
        .getName());


    @Override
    protected ListView createEmptyView() {
        return new ListView();
    }

}