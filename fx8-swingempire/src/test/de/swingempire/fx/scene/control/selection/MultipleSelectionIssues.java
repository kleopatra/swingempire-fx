/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import fx.util.StageLoader;

import static junit.framework.TestCase.*;

/**
 * Tests behaviour of MultipleSelection api.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public abstract class MultipleSelectionIssues<V extends Control, T extends MultipleSelectionModel> {
    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();


    /**
     * The model set to the views. It contains 9 string items, originally
     * in descending order. Invoking sort will revert the order.
     */
    protected ObservableList items;
    protected V view;
    protected boolean multipleMode;

    
    /**
     * Test how the anchor behaves when clearing individual selected items.
     * 
     * Here we select a range by repeated selectNext.
     */
    @Test
    public void testAnchorOnSelectRangeWithNext() {
        if (!multipleMode) return;
        StageLoader loader = new StageLoader(getView());
        int first = 2;
        getSelectionModel().select(first);
        int last = 4;
        for (int i = first+ 1; i <= last; i++) {
            getSelectionModel().selectNext();
        }
        assertEquals(3, getSelectionModel().getSelectedIndices().size());
        assertEquals("anchor must be kept on first of range", first, getAnchorIndex());
    }
    
    /**
     * Here we select the range by repeated selectNext, anchor updating as expected.
     */
    @Test
    public void testAnchorOnClearSelectionInRangeWithNext() {
        if (!multipleMode) return;
        StageLoader loader = new StageLoader(getView());
        int first = 2;
        getSelectionModel().select(first);
        int last = 4;
        for (int i = first+ 1; i <= last; i++) {
            getSelectionModel().selectNext();
        }
        getSelectionModel().clearSelection();
        assertEquals(0, getSelectionModel().getSelectedIndices().size());
        assertEquals("anchor must be cleared", -1, getAnchorIndex());
    }
    
    /**
     * Here we select the range by repeated selectNext, anchor updating as expected.
     */
    @Test
    public void testAnchorOnClearSelectionAtInRangeWithNext() {
        if (!multipleMode) return;
        StageLoader loader = new StageLoader(getView());
        int first = 2;
        getSelectionModel().select(first);
        int last = 4;
        for (int i = first+ 1; i <= last; i++) {
            getSelectionModel().selectNext();
        }
        getSelectionModel().clearSelection(3);
        assertEquals(2, getSelectionModel().getSelectedIndices().size());
        assertEquals("anchor must be kept when clearing index in range", first, getAnchorIndex());
    }
    
    /**
     * Here we select the range by repeated selectNext, anchor updating as expected.
     */
    @Test
    public void testAnchorOnClearSelectionOfAnchorInRangeWithNext() {
        if (!multipleMode) return;
        StageLoader loader = new StageLoader(getView());
        int first = 2;
        getSelectionModel().select(first);
        int last = 4;
        for (int i = first+ 1; i <= last; i++) {
            getSelectionModel().selectNext();
        }
        getSelectionModel().clearSelection(first);
        assertEquals(2, getSelectionModel().getSelectedIndices().size());
        assertEquals("expected behavior when clearing anchor?", first, getAnchorIndex());
    }
    
    /**
     * Test how the anchor behaves when clearing individual selected items.
     * Here we select a range with the range method.
     * Fails because anchorOnSelectRangeAscending fails?
     */
    @Test
    public void testAnchorOnClearSelectionAtAfterRange() {
        if (!multipleMode) return;
        StageLoader loader = new StageLoader(getView());
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        getSelectionModel().clearSelection(3);
        assertEquals(2, getSelectionModel().getSelectedIndices().size());
        assertEquals("anchor must be kept when clearing index in range", start, getAnchorIndex());
    }
    
    @Test
    public void testAnchorOnSelectRangeAscending() {
        if (!multipleMode) return;
        StageLoader loader = new StageLoader(getView());
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        assertEquals(3, getSelectionModel().getSelectedIndices().size());
        assertEquals("anchor must be kept on first of range", start, getAnchorIndex());
    }
    
    @Test
    public void testAnchorOnSelectRangeDescending() {
        if (!multipleMode) return;
        StageLoader loader = new StageLoader(getView());
        int start = 5;
        int end = 2;
        getSelectionModel().selectRange(start, end);
        assertEquals(3, getSelectionModel().getSelectedIndices().size());
        assertEquals(start, getAnchorIndex());
    }
    
    @Test
    public void testFocusOnClearSelectionAtFocusRangeAscending() {
        if (!multipleMode) return;
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int last = end - 1;
        getSelectionModel().clearSelection(last);
        assertEquals(2, getSelectionModel().getSelectedIndices().size());
        assertEquals("focus must be unchanged on clearSelection at focus", last, getFocusIndex());
    }
    
    @Test
    public void testFocusOnClearSelectionAtRangeAscending() {
        if (!multipleMode) return;
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int last = end - 1;
        getSelectionModel().clearSelection(3);
        assertEquals(2, getSelectionModel().getSelectedIndices().size());
        assertEquals("focus must be unchanged on clearSelection in range", last, getFocusIndex());
    }
    
    @Test
    public void testFocusOnClearSelectionRangeAscending() {
        if (!multipleMode) return;
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        getSelectionModel().clearSelection();
        assertEquals(0, getSelectionModel().getSelectedIndices().size());
        assertEquals("focus must be cleared", -1, getFocusIndex());
    }
    
    @Test
    public void testFocusOnSelectRangeAscending() {
        if (!multipleMode) return;
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int last = end - 1;
        assertEquals(3, getSelectionModel().getSelectedIndices().size());
        assertEquals(last, getFocusIndex());
    }
    
    @Test
    public void testFocusOnSelectRangeDescending() {
        if (!multipleMode) return;
        int start = 5;
        int end = 2;
        getSelectionModel().selectRange(start, end);
        int last = end + 1;
        assertEquals(3, getSelectionModel().getSelectedIndices().size());
        assertEquals(last, getFocusIndex());
    }
    
    @Test
    public void testSelectedStartEndRangeAscending() {
        if (!multipleMode) return;
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        assertTrue("start index must be selected" + start, getSelectionModel().isSelected(start));
        assertFalse("end index must not be selected" + end, getSelectionModel().isSelected(end));
    }
    
    @Test
    public void testSelectedStartEndRangeDescending() {
        if (!multipleMode) return;
        int start = 5;
        int end = 2;
        getSelectionModel().selectRange(start, end);
        assertTrue("start index must be selected" + start, getSelectionModel().isSelected(start));
        assertFalse("end index must not be selected" + end, getSelectionModel().isSelected(end));
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

     public MultipleSelectionIssues(boolean multiple) {
        this.multipleMode = multiple;
    }

    /**
     * PENDING: why not parameterize directly on the mode?
     * @return
     */
    @Parameterized.Parameters
    public static Collection selectionModes() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    protected void checkMode(T model) {
       if (multipleMode && model.getSelectionMode() != SelectionMode.MULTIPLE) {
           model.setSelectionMode(SelectionMode.MULTIPLE);
       }
    }

    @Before
    public void setUp() throws Exception {
        // JW: need more items for multipleSelection
        items = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        view = createView(items);
    }
    
    protected abstract V createView(ObservableList items);
    
    protected abstract T getSelectionModel();

    protected V getView() {
        return view;
    }

    /**
     * Returns the index of the anchor value. Note that subclasses which store a
     * compound value need to override and extract the index.
     * 
     * @return
     */
    protected int getAnchorIndex() {
        Object anchor = getView().getProperties().get(SelectionIssues.ANCHOR_KEY);
        return anchor != null ? (int) anchor : -1;
    }


    /**
     * We expect views with MultipleSelectionModel to have a FocusModel as well.
     * 
     * @param index the default value for views that don't have a focusModel
     * @return 
     */
    protected int getFocusIndex() {
        return getFocusModel().getFocusedIndex();
    }
    
    protected abstract FocusModel getFocusModel();
    

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MultipleSelectionIssues.class.getName());
}
