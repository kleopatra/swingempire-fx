/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Locale;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.TableView.TableViewSelectionModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import de.swingempire.fx.property.PropertyIgnores.IgnoreReported;
import de.swingempire.fx.util.ListChangeReport;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class TableMultipleSelectionIssues 
    extends MultipleSelectionIssues<TableView, TableViewSelectionModel> {


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

    
    @Override
    protected TableViewSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }

    /**
     * 
     */
    @Override
    protected TableView createView(ObservableList items) {
        TableView table = new TableView(items);
        TableColumn column = new TableColumn("numberedItems");
        table.getColumns().add(column);
        TableViewSelectionModel model = table.getSelectionModel();
        assertEquals("sanity: test setup assumes that initial mode is single", 
                SelectionMode.SINGLE, model.getSelectionMode());
        checkMode(model);
        // PENDING JW: this is crude ... think of doing it elsewhere
        // the problem is to keep super blissfully unaware of possible modes
        assertEquals(multipleMode, model.getSelectionMode() == SelectionMode.MULTIPLE);
        return table;
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

    @Override
    protected void setAllItems(ObservableList other) {
        getView().getItems().setAll(other);
    }
    
    @Override
    protected void setItems(ObservableList other) {
        getView().setItems(other);
    }

    public TableMultipleSelectionIssues(boolean multiple) {
        super(multiple);
    }
    
}
