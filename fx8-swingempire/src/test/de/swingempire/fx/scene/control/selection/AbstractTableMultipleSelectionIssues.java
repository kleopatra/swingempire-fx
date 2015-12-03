/*
 * Created on 19.11.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.TableView.TableViewSelectionModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import de.swingempire.fx.property.PropertyIgnores.IgnoreReported;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreTableCellSelection;
import de.swingempire.fx.util.ListChangeReport;

import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

/**
 * Contains TableView specifics, particularly cell selection.
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public abstract class AbstractTableMultipleSelectionIssues<V extends TableView> extends
        MultipleSelectionIssues<V, TableViewSelectionModel> {

//------------- test failures when cellSelection is on 
// PENDING: check for assumptions, probably need to be adjusted
    
    @Override
    @Test
//    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testClearSelectionAtInvalidIndex() {
        int start = 2;
        int end = 6;
        getSelectionModel().selectRange(start, end);
        int index = end - 1;
        int selectionSize = getSelectedIndices().size();
        getSelectionModel().clearSelection(items.size());
        assertTrue("index must still be selected " + index, getSelectionModel().isSelected(index));
        assertEquals("selectedIndex must be unchanged", 
                index, getSelectedIndex());
        assertEquals(selectionSize, getSelectedIndices().size());
        assertEquals(selectionSize, getSelectedItems().size());
    }

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testIsSelectedOnInsertAbove() {
        int index = 3;
        getSelectionModel().select(index);
        assertTrue("index must be selected: ", getSelectionModel().isSelected(index));
        addItem(0, createItem("added at 0"));
        int expected = index + 1;
        assertEquals(expected, getSelectedIndex());
        assertFalse("old index must not be selected " + index, getSelectionModel().isSelected(index));
    }

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testClearSelectionAtUnselectedIndex() {
        int start = 2;
        int end = 6;
        getSelectionModel().selectRange(start, end);
        int index = end - 1;
        int selectionSize = getSelectedIndices().size();
        getSelectionModel().clearSelection(end);
        assertTrue("index must still be selected " + index, getSelectionModel().isSelected(index));
        assertEquals("selectedIndex must be unchanged", 
                index, getSelectedIndex());
        assertEquals(selectionSize, getSelectedIndices().size());
        assertEquals(selectionSize, getSelectedItems().size());
    }

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testAnchorOnSelectRangeDescending() {
        if (!multipleMode) return;
        initSkin();
        int start = 5;
        int end = 2;
        getSelectionModel().selectRange(start, end);
        assertEquals(3, getSelectedIndices().size());
        assertEquals(start, getAnchorIndex());
    }
    
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testIndicesUnselectAll() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        int size = indices.length;
        for (int index : indices) {
            getSelectionModel().select(index);
        }
        assertEquals(size, getSelectedIndices().size());
        for (int index : indices) {
            getSelectionModel().clearSelection(index);
            assertFalse("cleared index must be unselected", getSelectionModel().isSelected(index));
            assertFalse("cleared index must not be contained in indices", 
                    getSelectedIndices().contains(index));
            assertEquals("size of indices must be decreased by one", 
                    --size, getSelectedIndices().size());
        }
        assertTrue(getSelectionModel().isEmpty());
    }
    
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testAlsoSelectNextDescending() {
        if (!multipleMode) return;
        initSkin();
        prepareAlsoSelectDescending();
        
        int anchor = getAnchorIndex();
        int oldFocus = getFocusedIndex();
        int newFocus = oldFocus + 1;
        getSelectionModel().clearSelection();
        // not included boundary is new - 1 for descending
        getSelectionModel().selectRange(anchor, newFocus - 1);
        assertEquals("focus must be last of range", newFocus, getFocusedIndex());
        assertEquals("selected must be focus", newFocus, getSelectedIndex());
        assertEquals("anchor must be unchanged", anchor, getAnchorIndex());
        assertEquals("size must be old selection till focus", anchor - newFocus + 1, 
                getSelectedIndices().size());
        
    }

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testSelectedStartEndRangeDescending() {
        if (!multipleMode) return;
        int start = 5;
        int end = 2;
        getSelectionModel().selectRange(start, end);
        assertTrue("start index must be selected" + start, getSelectionModel().isSelected(start));
        assertFalse("end index must not be selected" + end, getSelectionModel().isSelected(end));
    }

    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testSelectedStartEndRangeAscending() {
        if (!multipleMode) return;
        int start = 2;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        assertTrue("start index must be selected" + start, getSelectionModel().isSelected(start));
        assertFalse("end index must not be selected" + end, getSelectionModel().isSelected(end));
    }
    
    /**
     * Here we select the range by repeated selectNext, anchor updating as expected.
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testAnchorOnClearSelectionAtInRangeWithNext() {
        if (!multipleMode) return;
        initSkin();
        int first = 2;
        getSelectionModel().select(first);
        int last = 4;
        for (int i = first+ 1; i <= last; i++) {
            getSelectionModel().selectNext();
        }
        getSelectionModel().clearSelection(3);
        assertEquals(2, getSelectedIndices().size());
        assertEquals("anchor must be kept when clearing index in range", first, getAnchorIndex());
    }
    
    /**
     * Regression testing RT-37360: fired both removed and added
     * for single-select
     * 
     * PENDING JW: still issues
     * - core: incorrect change event
     * - simple: clearAndSelect not yet implemented (to not fire if selected)
     * 
     * This is the test for 37360: clearAndSelect an already selected
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testEventsMultipleToSingleClearAndSelectSelectedIndices() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int selected = getSelectedIndex();
        ListChangeReport report = new ListChangeReport(getSelectedIndices());
        getSelectionModel().clearAndSelect(selected);
        assertEquals("event on clearAndSelect already selected", 1, report.getEventCount());
        Change c = report.getLastChange();
        // hadbeen wasSingleReplaced - 
        // that's an incorrect assumption: we had 2 selected, with clearAndSelect the last
        // we should get a single removed
        // fails in core due an invalid change event
        // fails in simple due to clearAndSelect not yet really implemented
        assertTrue("must be single removed but was " + c, wasSingleRemoved(c));
    }
    
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testSelectedIndices() {
        if (!multipleMode) return;
        int[] indices = new int[] {2,3};
        for (int i : indices) {
            getSelectionModel().select(i);
        }
        assertEquals("sanity: same size", indices.length, 
                getSelectedIndices().size());;
        for (int i : indices) {
            assertTrue("index must be selected", 
                    getSelectionModel().isSelected(i));
            assertTrue("index must be contained in selectedIndices", 
                    getSelectedIndices().contains(i));
        }
        
    }
    
    /**
     * Descending == anchor > focus
     * Previous == newFocus = focus - 1
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testAlsoSelectPreviousDescending() {
        if (!multipleMode) return;
        initSkin();
        prepareAlsoSelectDescending();
        
        int anchor = getAnchorIndex();
        int oldFocus = getFocusedIndex();
        int newFocus = oldFocus - 1;
        getSelectionModel().clearSelection();
        // not included boundary is new - 1 for descending
        getSelectionModel().selectRange(anchor, newFocus - 1);
        assertEquals("focus must be last of range", newFocus, getFocusedIndex());
        assertEquals("selected must be focus", newFocus, getSelectedIndex());
        assertEquals("anchor must be unchanged", anchor, getAnchorIndex());
        assertEquals("size must be old selection till focus", anchor - newFocus + 1, 
                getSelectedIndices().size());
    }
    
    /**
     * Regression testing RT-37360: fired both removed and added
     * for single-select
     * 
     * This is the test for 37360: clearAndSelect an already selected
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreTableCellSelection.class)
    public void testEventsMultipleToSingleClearAndSelectSelectedItems() {
        if (!multipleMode) return;
        int start = 3;
        int end = 5;
        getSelectionModel().selectRange(start, end);
        int selected = getSelectedIndex();
        ListChangeReport report = new ListChangeReport(getSelectedItems());
        getSelectionModel().clearAndSelect(selected);
        assertEquals("event on clearAndSelect already selected", 1, report.getEventCount());
        Change c = report.getLastChange();
        // incorrect assumption: we had 2 selected, with clearAndSelect the last
        // we should get a single removed
//        report.prettyPrint();
        assertTrue("must be single removed but was " + c, wasSingleRemoved(c));
    }
    
    

    
//----------------- pulled up from old tableCore    
    /**
     * Test selectedItem/s state for discontinous remove items
     * https://javafx-jira.kenai.com/browse/RT-39636
     */
    @Test
    @ConditionalIgnore(condition = IgnoreReported.class)
    public void testSelectedItemsOnDiscontinousRemovedItemsReport() {
        ObservableList items = FXCollections.observableArrayList(Locale.getAvailableLocales());
        TableView view = new TableView(items);
        int last = items.size() - 1;
        view.getSelectionModel().select(last);
        Object selectedItem = view.getSelectionModel().getSelectedItem();
        //ListChangeReport report = new ListChangeReport(view.getSelectionModel().getSelectedItems());
        items.removeAll(items.get(2), items.get(5));
        assertEquals("selectedItem", selectedItem, view.getSelectionModel().getSelectedItem());
        assertEquals("selected in items after removing", selectedItem, 
                view.getSelectionModel().getSelectedItems().get(0));
        //assertEquals("no event of selectedItems on remove above " + report.getLastChange(), 
        //        0, report.getEventCount());
    }
    
    /**
     * Test selectedIndex/ices state for discontinous remove items
     * https://javafx-jira.kenai.com/browse/RT-39636
     */
    @Test
    @ConditionalIgnore(condition = IgnoreReported.class)
    public void testSelectedIndicesOnDiscontinousRemovedItemsReport() {
        ObservableList items = FXCollections.observableArrayList(Locale.getAvailableLocales());
        TableView view = new TableView(items);
        int last = items.size() - 1;
        view.getSelectionModel().select(last);
        //ListChangeReport report = new ListChangeReport(view.getSelectionModel().getSelectedIndices());
        items.removeAll(items.get(2), items.get(5));
        int expected = last - 2;
        assertEquals("selectedindex", expected, view.getSelectionModel().getSelectedIndex());
        assertEquals("selected in indices after remove above", expected, 
                view.getSelectionModel().getSelectedIndices().get(0));
        //assertEquals("single event of selectedIndices on remove above " + report.getLastChange(), 
        //        1, report.getEventCount());
    }

    /**
     * Reported for tableView, not for others.
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreReported.class)
    public void testSelectedIndicesOnDiscontinousRemovedItems() {
        super.testSelectedIndicesOnDiscontinousRemovedItems();
    }
    
    /**
     * Reported for tableView, not for others.
     */
    @Override
    @Test
    @ConditionalIgnore(condition = IgnoreReported.class)
    public void testSelectedItemsOnDiscontinousRemovedItems() {
        super.testSelectedItemsOnDiscontinousRemovedItems();
    }

    @Test
    public void testTablePosition() {
        TableView table = getView();
        TablePosition pos = new TablePosition(table, 0, null);
        assertEquals(pos, new TablePosition(table, 0, null));
    }
    
    @Test
    public void testSanity() {
        TableView table = getView();
        assertEquals(1, table.getColumns().size());
        assertEquals(items, table.getItems());
    }


    protected abstract V createEmptyView();

    /**
     * 
     */
    @Override
    protected V createView(ObservableList items) {
        V table = createEmptyView();
        table.setItems(items);
        TableViewSelectionModel model = table.getSelectionModel();
        assertEquals("sanity: test setup assumes that initial mode is single", 
                SelectionMode.SINGLE, model.getSelectionMode());
        checkMode(model);
        // PENDING JW: this is crude ... think of doing it elsewhere
        // the problem is to keep super blissfully unaware of possible modes
        assertEquals(multipleMode, model.getSelectionMode() == SelectionMode.MULTIPLE);
        checkCellSelection(model);
        assertEquals("cellSelection", cellSelection, model.isCellSelectionEnabled());
        return table;
    }
    
    /**
     * @param model
     */
    private void checkCellSelection(TableViewSelectionModel model) {
        // PENDING JW: why so complicated? copied in analogy to
        // multi mode ...
        if (cellSelection && !model.isCellSelectionEnabled()) {
            model.setCellSelectionEnabled(true);
        }
    }

    @Override
    protected void setAllItems(ObservableList other) {
        getView().getItems().setAll(other);
    }

    @Override
    protected void setItems(ObservableList other) {
        getView().setItems(other);
    }

    @Override
    protected TableViewSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }

    @Override
    protected TableViewFocusModel getFocusModel() {
        return getView().getFocusModel();
    }

    @Override
    protected int getAnchorIndex() {
        Object anchor = getView().getProperties().get(SelectionIssues.ANCHOR_KEY);
        if (anchor instanceof TablePosition) {
            return ((TablePosition) anchor).getRow();
        }
        return -1;
    }


    boolean cellSelection;
    
//    public AbstractTableMultipleSelectionIssues(boolean multiple) {
//        this(multiple, false);
//    }
    /**
     * @param multiple
     */
    public AbstractTableMultipleSelectionIssues(boolean multiple, boolean cellSelection) {
        super(multiple);
        this.cellSelection = cellSelection;
    }
 
    /**
     * Added parameter for cell selection.
     * @return
     */
    @Parameters(name = "{index} - multiple {0}, cell {1}")
    public static Collection selectionModes() {
        List params = Arrays.asList(new Object[][] { 
//              { false, false }, 
              { false, true}, 
//              { true, false }, 
//              { true, true},  
         });
         return params;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AbstractTableMultipleSelectionIssues.class.getName());
}
