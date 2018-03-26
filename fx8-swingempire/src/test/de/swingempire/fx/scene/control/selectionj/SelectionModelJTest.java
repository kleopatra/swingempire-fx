/*
 * Created on 29.10.2014
 *
 */
package de.swingempire.fx.scene.control.selectionj;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.scene.control.selection.DefaultSelectionModelJ;
import de.swingempire.fx.scene.control.selection.SelectionModelJ;
import de.swingempire.fx.util.StageLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SelectionModelJTest<V extends Control, T extends SelectionModelJ> {

    public final static String ANCHOR_KEY = "anchor";
    
    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    /**
     * The model set to the views. It contains 5 string items, originally
     * in descending order. Invoking sort will revert the order.
     */
    protected ObservableList items;
    protected V view;

    protected SelectionModelJ model;

//------------ test default
    
    @Test
    public void testClearAndSelect() {
        int index = 2;
        model.select(index + 1);
        model.clearAndSelect(index);
        assertEquals(index, model.getSelectedIndex());
    }
    
    @Test
    public void testClearAndSelectUnselectable() {
        int index = 2;
        model.select(index);
        model.clearAndSelect(items.size());
        assertEquals(index, model.getSelectedIndex());
    }
    
    @Test
    public void testClearSelectedAt() {
        int index = 2;
        model.select(index);
        model.clearSelection(index);
        assertTrue(model.isEmpty());
    }
    
    @Test
    public void testNotClearSelectedAt() {
        int index = 2;
        model.select(index);
        model.clearSelection(index + 1);
        assertEquals(index, model.getSelectedIndex());
    }
    
    @Test
    public void testSelectNext() {
        int index = 2;
        model.select(index);
        model.selectNext();
        assertEquals(index + 1, model.getSelectedIndex());
    }
    
    @Test
    public void testSelectNextIfUnselectable() {
        int unselectable = 2;
        SelectionModelJ model = createSelectionModelUnselectable(unselectable);
        model.select(unselectable - 1);
        model.selectNext();
        assertEquals(unselectable +1, model.getSelectedIndex());
        
    }
    @Test
    public void testClearAndSelectNextIfUnselectable() {
        int unselectable = 2;
        SelectionModelJ model = createSelectionModelUnselectable(unselectable);
        model.select(unselectable - 1);
        model.clearAndSelectNext();
        assertEquals(unselectable +1, model.getSelectedIndex());
        
    }
    @Test
    public void testSelectPrevious() {
        int index = 2;
        model.select(index);
        model.selectPrevious();
        assertEquals(index -1, model.getSelectedIndex());
    }
    
    @Test
    public void testSelectPreviousIfUnselectable() {
        int unselectable = 2;
        SelectionModelJ model = createSelectionModelUnselectable(unselectable);
        model.select(unselectable + 1);
        model.selectPrevious();
        assertEquals(unselectable -1, model.getSelectedIndex());
    }
    
    @Test
    public void testClearAndSelectPreviousIfUnselectable() {
        int unselectable = 2;
        SelectionModelJ model = createSelectionModelUnselectable(unselectable);
        model.select(unselectable + 1);
        model.clearAndSelectPrevious();
        assertEquals(unselectable -1, model.getSelectedIndex());
    }

    /**
     * Creates and returns a SelectionModel with unselectable index.
     * @param unselectable
     * @return
     */
    protected SelectionModelJ createSelectionModelUnselectable(int unselectable) {
        SelectionModelJ model = new DefaultSelectionModelJ(items) {

            @Override
            public boolean isSelectable(int index) {
                if (index == unselectable) return false;
                return super.isSelectable(index);
            }
            
        };
        return model;
    }
    @Test
    public void testSelectNextAtLast() {
        int index = items.size() - 1;
        model.select(index);
        model.selectNext();
        assertEquals("selection must not be changed", index, model.getSelectedIndex());
    }
    
    @Test
    public void testSelectPreviousAtFirst() {
        int index = 0;
        model.select(index);
        model.selectPrevious();
        assertEquals("selection must be unchanged", index, model.getSelectedIndex());
    }
    
    @Test
    public void testSelectFirst() {
        model.selectFirst();
        assertEquals(0, model.getSelectedIndex());
    }

    @Test
    public void testSelectFirstIfUnselectable() {
        int unselectable = 0;
        SelectionModelJ model = createSelectionModelUnselectable(unselectable);
        model.selectFirst();
        assertEquals(unselectable +1, model.getSelectedIndex());
        
    }
    
    @Test
    public void testClearAndSelectFirstIfUnselectable() {
        int unselectable = 0;
        SelectionModelJ model = createSelectionModelUnselectable(unselectable);
        model.clearAndSelectFirst();
        assertEquals(unselectable +1, model.getSelectedIndex());

    }
    
    @Test
    public void testSelectLast() {
        model.selectLast();
        assertEquals(items.size()- 1, model.getSelectedIndex());
    }
    
    @Test
    public void testSelectLastIfUnselectable() {
        int unselectable = items.size() - 1;
        SelectionModelJ model = createSelectionModelUnselectable(unselectable);
        model.selectLast();
        assertEquals(unselectable -1, model.getSelectedIndex());
        
    }
    @Test
    public void testClearSelectLastIfUnselectable() {
        int unselectable = items.size() - 1;
        SelectionModelJ model = createSelectionModelUnselectable(unselectable);
        model.clearAndSelectLast();
        assertEquals(unselectable -1, model.getSelectedIndex());
        
    }
    @Test
    public void testSelectNextIfEmpty() {
        model.selectNext();
        assertEquals(0, model.getSelectedIndex());
    }
    
    @Test
    public void testSelectPreviousIfEmpty() {
        model.selectPrevious();
        assertEquals(items.size() - 1, model.getSelectedIndex());
    }
//------------ test implementation    
    
    @Test
    public void testNotSelectUnselectableIndexNegative() {
        int index = 0;
        model.select(index);
        model.select(-1);
        assertEquals(index, model.getSelectedIndex());
    }
    
    @Test
    public void testNotSelectUnselectableIndexAboveRange() {
        int index = 0;
        model.select(index);
        model.select(items.size());
        assertEquals(index, model.getSelectedIndex());
    }
    
    @Test
    public void testSelectSelectableIndex() {
        model.select(0);
        assertEquals(0, model.getSelectedIndex());
    }
    
    @Test
    public void testInitialNotEmptyItems() {
        assertNotNull(model.selectedIndexProperty());
        assertTrue(model.isEmpty());
        assertEquals(-1, model.getSelectedIndex());
        assertEquals(items.size(), model.getItemCount());
        assertFalse(model.isSelectable(-1));
        assertFalse(model.isSelectable(items.size()));
        assertTrue(model.isSelectable(0));
    }
    
    @Test
    public void testInitialEmptyItems() {
        SelectionModelJ model = new DefaultSelectionModelJ<>();
        assertNotNull(model.selectedIndexProperty());
        assertTrue(model.isEmpty());
        assertEquals(-1, model.getSelectedIndex());
        assertEquals(0, model.getItemCount());
        assertFalse(model.isSelectable(-1));
        assertFalse(model.isSelectable(0));
    }
    
    /**
     * The stageLoader used to force skin creation. It's an artefact of fx
     * instantiation process, not meant to be really used.
     * Note that it's the responsibility of the test method itself (not the setup)
     * to init if needed.
     */
    protected StageLoader loader;

    protected void initSkin() {
        if (getView() == null) return;
        loader = new StageLoader(getView());
        // doesn't make a difference: still spurious RejectedExecutionException ..
        // triggered by task PaintRenderJob
//        PlatformImpl.runAndWait(() -> loader = new StageLoader(getView()));
    }

    protected V getView() {
        return view;
    }
    
    @Before
    public void setUp() throws Exception {
        // JW: need more items for multipleSelection
        items = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
//        view = createView(items);
        model = new DefaultSelectionModelJ(items);
    }

    
}
