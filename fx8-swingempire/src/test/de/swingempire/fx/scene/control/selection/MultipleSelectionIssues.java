/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import static junit.framework.TestCase.assertFalse;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;

import de.swingempire.fx.junit.JavaFXThreadingRule;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public abstract class MultipleSelectionIssues<V extends Control, T extends MultipleSelectionModel> {
    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();


    /**
     * The model set to the views. It contains 5 string items, originally
     * in descending order. Invoking sort will revert the order.
     */
    protected ObservableList items;
    protected V view;

    
        protected boolean multipleMode;
    
    public MultipleSelectionIssues(boolean multiple) {
        this.multipleMode = multiple;
    }

    @Parameterized.Parameters
    public static Collection primeNumbers() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    protected void checkMode(T model) {
       if (multipleMode && model.getSelectionMode() != SelectionMode.MULTIPLE) {
           model.setSelectionMode(SelectionMode.MULTIPLE);
       }
    }

    @Test
    public void testSelectedIndicesAfterSort() {
        int first = 0;
        int last = items.size() -1;
        getSelectionModel().select(first);
        FXCollections.sort(items);
        assertEquals(1, getSelectionModel().getSelectedIndices().size());
        assertEquals(last, getSelectionModel().getSelectedIndices().get(0));
    }


    @Test
    public void testSelectedIndexIsLastSelected() {
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
            assertEquals("selectedIndex is last selected", 
                    i, getSelectionModel().getSelectedIndex());
        }
    }

    /**
     * Test api doc: reselect an already selected index makes it
     * the selectedIndex.
     */
    @Test
    public void testIndicesSelectedIndexIsUpdatedAfterSelectAgain() {
        int[] indices = new int[] {2,3};
        int lastSelected = indices[0];
        // select all
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        // re-select first
        getSelectionModel().select(lastSelected);
        assertEquals(lastSelected, getSelectionModel().getSelectedIndex());
    }
    
    /**
     * Issue: selectedIndex not updated on unselect if there are
     * other indices selected. 
     * 
     * Might be intended behaviour, by the doc of selectedIndexProperty:
     * in multiple selectionMode
     * "the selected index will always represent the last selection made"
     * 
     * Leads to the unintuitive (I would say: illegal) 
     * state where the selectedIndex is unselected.
     * 
     * The (my) confusion might stem from selectionModel mixing "lead" and 
     * "selectedIndex" - a lead might be unselected. Problem is, that
     * unselecting the "selectedIndex" (aka: lead) doesn't send a 
     * notification to listeners, so they can't update their visuals.
     * 
     */
    @Test
    public void testIndicesSelectedIndexIsUpdatedAfterUnselect() {
        int[] indices = new int[] {2,3};
        int lastSelected = indices[indices.length - 1];
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        // clear the last selected
        getSelectionModel().clearSelection(lastSelected);
        int selectedIndex = getSelectionModel().getSelectedIndex();
        assertEquals("selected index must be .. selected", selectedIndex >= 0, 
                getSelectionModel().isSelected(selectedIndex));
        // JW: what exactly happens is unspecified, my expectation would be to
        // move it to one of the still selected 
        assertEquals("selected index must be updated to another selected", 
                indices[0], selectedIndex);
    }

    

    /**
     * Sanity test: select several indices, unselect them one-by-one -
     * selection must be empty and unselected removed from selected
     * indices
     */
    @Test
    public void testIndicesUnselectAll() {
        int[] indices = new int[] {2,3};
        int size = indices.length;
        for (int index : indices) {
            getSelectionModel().select(index);
        }
        assertEquals(size, getSelectionModel().getSelectedIndices().size());
        for (int index : indices) {
            getSelectionModel().clearSelection(index);
            assertFalse("cleared index must be unselected", getSelectionModel().isSelected(index));
            assertFalse("cleared index must not be contained in indices", 
                    getSelectionModel().getSelectedIndices().contains(index));
            assertEquals("size of indices must be decreased by one", 
                    --size, getSelectionModel().getSelectedIndices().size());
        }
        assertTrue(getSelectionModel().isEmpty());
    }
    
    @Test
    public void testSelectedIndices() {
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        assertEquals("sanity: same size", indices.length, 
                getSelectionModel().getSelectedIndices().size());;
        for (int i : indices) {
            assertTrue("index must be selected", 
                    getSelectionModel().isSelected(i));
            assertTrue("index must be contained in selectedIndices", 
                    getSelectionModel().getSelectedIndices().contains(i));
        }
        
    }
    
        
    /** 
     * Test select(-1) must do nothing - this differs from the single selection
     * case in that we start of with multiple selected indices
     */
    @Test
    public void testSelectMinusOneIndex() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        getSelectionModel().select(-1);
        assertEquals(indices[1], getSelectionModel().getSelectedIndex());
    }
    
    /** 
     * Test select(-1) must do nothing - this differs from the single selection
     * case in that we start of with multiple selected indices
     */
    @Test
    public void testSelectMinusOneItem() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        getSelectionModel().select(-1);
        assertEquals(items.get(indices[1]), getSelectionModel().getSelectedItem());
    }
    

    /** 
     * Test select(null) is unspecified - this differs from the single selection
     * case in that we start of with multiple selected indices
     */
    @Test
    public void testSelectNullIndex() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        getSelectionModel().select(null);
        assertEquals(indices[1], getSelectionModel().getSelectedIndex());
    }
    
    /** 
     * Test select(null) is unspecified - this differs from the single selection
     * case in that we start of with multiple selected indices
     */
    @Test
    public void testSelectNullItem() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        getSelectionModel().select(null);
        Object item = getSelectionModel().getSelectedItem();
        getSelectionModel().select(null);
        assertEquals(item, getSelectionModel().getSelectedItem());
    }

     @Before
    public void setUp() throws Exception {
//        JFXPanel panel = new JFXPanel();

        items = FXCollections.observableArrayList(
                "5-item", "4-item", "3-item", "2-item", "1-item");
        view = createView(items);
    }
    
    protected abstract V createView(ObservableList items);
    
    protected abstract T getSelectionModel();

    protected V getView() {
        return view;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MultipleSelectionIssues.class.getName());
}
