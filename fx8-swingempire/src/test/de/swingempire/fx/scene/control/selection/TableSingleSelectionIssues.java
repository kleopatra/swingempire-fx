/*
 * Created on 06.11.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.swingempire.fx.util.FXUtils;
import de.swingempire.fx.util.ListChangeReport;
import static org.junit.Assert.*;

/**
 * Testing single selection api in TableViewSelectionModel, for both selection modes.
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class TableSingleSelectionIssues extends SingleSelectionIssues<TableView, MultipleSelectionModel> {

//-------------------- tests around RT-36911 - trying to spec expected behaviour    
    /**
     * This is a track-down of RT-36911: below is a failing test. The IOOB happens if 

     * - an index had been selected via clearAndSelect(int, TableColumn)
- the selectedIndex is re-selected via clearAndSelect(int) in a listener to selectedIndex/Item

while the setup is borderline (?, maybe crossing as it results in a change of selectedCells)
 pathologic, it may indicate a problem with selectedCells, either implemenation or specification.

- specification: couldn't find any doc on what to expect if !cellSelectionEnabled
- implementation: the exception is thrown in the internal listener to selectedCells

     * this is stripped-down RT-36911: 
     * clearAndSelect with column (called by behaviour when clicking inside a column)
     * clearAndSelect without column in listener
     * 
     * The no-op is as far as the selectedIndex is concerned, what's changed
     * implicitly is the column
     */
    @Test
    public void testClearAndSelectInItemListenerReselectWithNullColumn() {
        int index = 3;
        TableColumnBase column = (TableColumnBase) getView().getColumns().get(0);
        ChangeListener l = (source, old, value) -> {
            assertEquals("selectedIndex same makes clearAndSelect a no-op", 
                    index, getSelectedIndex());
            getSelectionModel().clearAndSelect(index);
        };
        getSelectionModel().selectedItemProperty().addListener(l);
        getSelectionModel().clearAndSelect(index, column);
    }
    
    /**
     * this is raw RT-36911, using select instead of clearAndSelect (all fine)
     */
    @Test
    public void testSelectInItemListenerReselectWithNullColumn() {
        int index = 3;
        TableColumnBase column = (TableColumnBase) getView().getColumns().get(0);
        ChangeListener l = (source, old, value) -> {
            assertEquals("selectedIndex same makes clearAndSelect a no-op", 
                    index, getSelectedIndex());
            getSelectionModel().select(index);
        };
        getSelectionModel().selectedItemProperty().addListener(l);
        getSelectionModel().select(index, column);
    }
    
    /**
     * Invers of raw RT-36911:
     * - clearAndSelect without column
     * - clearAndSelect with column
     */
    @Test
    public void testClearAndSelectInItemListenerReselectColumn() {
        int index = 3;
        TableColumnBase column = (TableColumnBase) getView().getColumns().get(0);
        ChangeListener l = (source, old, value) -> {
            assertEquals("selectedIndex same makes clearAndSelect a no-op", 
                    index, getSelectedIndex());
            getSelectionModel().clearAndSelect(index, column);
        };
        getSelectionModel().selectedItemProperty().addListener(l);
        getSelectionModel().clearAndSelect(index);
    }
    
    @Test
    public void testSelectInItemListenerReselectColumn() {
        int index = 3;
        TableColumnBase column = (TableColumnBase) getView().getColumns().get(0);
        ChangeListener l = (source, old, value) -> {
            assertEquals("selectedIndex same makes clearAndSelect a no-op", 
                    index, getSelectedIndex());
            getSelectionModel().select(index, column);
        };
        getSelectionModel().selectedItemProperty().addListener(l);
        getSelectionModel().select(index);
    }
    
    /**
     * Behaviour of selectedCells: clearAndSelect(int) vs. clearAndSelect(int, tableColumn)
     * sanity: isolated select(int, column) 
     * 
     */
    @Test
    public void testSelectedCellsClearAndSelectWithColumnIsSelected() {
        int index = 3;
        TableColumnBase column = (TableColumnBase) getView().getColumns().get(0);
        getSelectionModel().clearAndSelect(index, column);
        assertSelectedCells(index, column);
    }
    
    /**
     * Behaviour of selectedCells: select(int) vs. select(int, tableColumn)
     * sanity: isolated select(int) 
     */
    @Test
    public void testSelectedCellsClearAndSelectIsSelected() {
        int index = 3;
        TableColumnBase column = null;
        getSelectionModel().clearAndSelect(index);
        assertSelectedCells(index, column);
    }
    
    /**
     * Behaviour of selectedCells: select(int) vs. select(int, tableColumn)
     * first select(int), then select(int, column) - what to expect in cells?
     */
    @Test
    public void testSelectedCellsClearAndSelectTwiceIsSelected() {
        int index = 3;
        TableColumnBase column = (TableColumnBase) getView().getColumns().get(0);
        getSelectionModel().clearAndSelect(index);
        getSelectionModel().clearAndSelect(index, column);
        assertSelectedCells(index, null, column);
    }
    
    /**
     * Behaviour of selectedCells: select(int) vs. select(int, tableColumn)
     * sanity: isolated select(int, column) 
     * 
     */
    @Test
    public void testSelectedCellsSelectWithColumnIsSelected() {
        int index = 3;
        TableColumnBase column = (TableColumnBase) getView().getColumns().get(0);
        getSelectionModel().select(index, column);
        assertSelectedCells(index, column);
    }
    
    /**
     * Behaviour of selectedCells: select(int) vs. select(int, tableColumn)
     * sanity: isolated select(int) 
     */
    @Test
    public void testSelectedCellsSelectIsSelected() {
        int index = 3;
        TableColumnBase column = null;
        getSelectionModel().select(index);
        assertSelectedCells(index, column);
    }
    
    /**
     * Behaviour of selectedCells: select(int) vs. select(int, tableColumn)
     * first select(int), then select(int, column) - what to expect in cells?
     */
    @Test
    public void testSelectedCellsSelectTwiceIsSelected() {
        int index = 3;
        TableColumnBase column = (TableColumnBase) getView().getColumns().get(0);
        getSelectionModel().select(index);
        getSelectionModel().select(index, column);
        assertSelectedCells(index, null, column);
    }

    
    /**
     * Behaviour of selectedCells: select(int) vs. select(int, tableColumn)
     * select two columns via select(int, tableColumn) - what to expect in cells?
     */
    @Test
    public void testSelectedCellsSelectTwoColumns() {
        int index = 3;
        TableColumn first = (TableColumn) getView().getColumns().get(0);
        TableColumn second = new TableColumn("second");
        getView().getColumns().add(second);
        getSelectionModel().select(index, first);
        getSelectionModel().select(index, second);
        assertSelectedCells(index, first, second);
    }
    

    /**
     * Asserts isSelected and content of selected cells. Expects isSelected of
     * all combinations of index/column return true and selectedCells to contain
     * a (single?) position with the params as values. Must not be called if
     * cellSelectionEnabled.
     * 
     * @param index
     * @param columns
     */
    protected void assertSelectedCells(int index, TableColumnBase... columns) {
        if (getSelectionModel().isCellSelectionEnabled()) 
                fail("illegal precondition - cellSelection must be disabled");
        assertTrue("index must be selected " + index, getSelectionModel().isSelected(index));
        assertTrue("", getSelectionModel().isSelected(index, null));
        if (columns != null) {
            for (TableColumnBase tableColumnBase : columns) {
                assertTrue(getSelectionModel().isSelected(index,
                        tableColumnBase));
            }
        }
//        assertEquals(columns != null ? columns.length : 1, getSelectedCells().size());
        TablePosition pos = getSelectedCells().get(0);
        assertEquals(index, pos.getRow());
        TableColumnBase first = columns != null ? columns[0] : null;
        assertEquals(first, pos.getTableColumn());
    }
    /**
     * Behaviour of selectedCells: select(int) vs. select(int, tableColumn)
     */
    @Test
    public void testSelectedCellsNotification() {
        int index = 3;
        ListChangeReport report = new ListChangeReport(getSelectedCells());
        TableColumnBase column = (TableColumnBase) getView().getColumns().get(0);
        getSelectionModel().select(index, column);
        List oldCells = new ArrayList(getSelectedCells());
        assertEquals(1, report.getEventCount());
        report.clear();
        getSelectionModel().select(index);
        if (oldCells.equals(getSelectedCells())) {
//            LOG.info("selectedCells: " + multipleMode + "\n" + oldCells + " \n" + getSelectedCells());
            assertEquals("selectedCells unchanged, must not fire", 0, report.getEventCount());
        } else {
//            LOG.info("selectedCells: " + multipleMode + "\n" + oldCells + " \n" + getSelectedCells());
//            report.prettyPrint();
        }
    }
    @Test
    public void testSelectedCellsMoreColumnsNotification() {
        int index = 3;
        TableColumn first = (TableColumn) getView().getColumns().get(0);
        TableColumn second = new TableColumn("second");
        getView().getColumns().add(second);
        getSelectionModel().select(index, second);
        ListChangeReport report = new ListChangeReport(getSelectedCells());
        getSelectionModel().select(index, first);
        assertEquals(1, report.getEventCount());
//        report.prettyPrint();
        assertTrue("change must be single replaced but was: " + report.getLastChange(), 
                FXUtils.wasSingleReplaced(report.getLastChange()));
    }

    
    /**
     * Unspecified: behaviour of select(int)/select(int, tableColumn vs. clearAndSelect.
     * Whatever it is, would expect same for !cellSelectionEnabled. Trying to 
     * put into test and compare.
     */
    @Test
    public void testSelectedCellsSelectVsClearAndSelect() {
        int index = 3;
        TableColumnBase column = (TableColumnBase) getView().getColumns().get(0);
        // select
        getSelectionModel().select(index, column);
        List selectedCellsOnSelect = getSelectedCells();
        // reset
        getSelectionModel().clearSelection();
        getSelectionModel().clearAndSelect(index, column);
        assertEquals(selectedCellsOnSelect, getSelectedCells());
    }
    
    
//    @Test
//    public void testClearAndSelectInIndexListener() {
//        int index = 3;
//        ChangeListener l = (source, old, value) -> {
//            assertEquals("selectedIndex same makes clearAndSelect a no-op", 
//                    index, getSelectedIndex());
//            getSelectionModel().clearAndSelect(index);
//        };
//        getSelectionModel().selectedIndexProperty().addListener(l);
//        getSelectionModel().clearAndSelect(index, (TableColumnBase) getView().getColumns().get(0));
//    }
    
    /**
     * @param multiple
     */
    public TableSingleSelectionIssues(boolean multiple) {
        super(multiple);
    }

    @Override
    protected TableView createView(ObservableList items) {
        TableView table = new TableView(items);
        TableColumn column = new TableColumn("numberedItems");
        table.getColumns().add(column);
        MultipleSelectionModel model = table.getSelectionModel();
        assertEquals("sanity: test setup assumes that initial mode is single", 
                SelectionMode.SINGLE, model.getSelectionMode());
        checkMode(model);
        // PENDING JW: this is crude ... think of doing it elsewhere
        // the problem is to keep super blissfully unaware of possible modes
        assertEquals(multipleMode, model.getSelectionMode() == SelectionMode.MULTIPLE);
        return table;
    }

    protected ObservableList<TablePosition> getSelectedCells() {
        return getSelectionModel().getSelectedCells();
    }
    
    @Override
    protected TableViewSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }

    
    @Override
    protected FocusModel getFocusModel() {
        return getView().getFocusModel();
    }

    

    @Override
    protected int getAnchorIndex(int index) {
        TablePosition anchor = (TablePosition) getView().getProperties().get(ANCHOR_KEY);
        return anchor != null ? anchor.getRow() : -1;
    }



    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableSingleSelectionIssues.class.getName());



    @Override
    protected void setSelectionModel(MultipleSelectionModel model) {
        getView().setSelectionModel((TableViewSelectionModel) model);
    }

    @Override
    protected void resetItems(ObservableList other) {
        getView().setItems(other);
    }
}
