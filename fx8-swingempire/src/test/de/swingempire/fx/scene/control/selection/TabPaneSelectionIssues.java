/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;
import static org.junit.Assert.*;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class TabPaneSelectionIssues extends SelectionIssues<TabPane, SingleSelectionModel> {

    
    /**
     * Overridden for TabSelection specifics: initially first tab selected
     */
    @Override
    public void testInitialSelection() {
        assertEquals(0, getSelectionModel().getSelectedIndex());
        assertEquals(items.get(0), getSelectionModel().getSelectedItem());
    }

    
    @Override
    public void testSelectUncontainedIfEmptySelection() {
        Tab item = new Tab("uncontained");
        getSelectionModel().select(item);
        assertEquals("sanity: the item is selected", item, getSelectionModel().getSelectedItem());
        assertFalse("selection must not be empty", getSelectionModel().isEmpty());
    }


    @Override
    public void testSelectUncontainedIfNotEmptySelection() {
        int index = 2;
        getSelectionModel().select(index);
        Tab item = new Tab("uncontained");
        getSelectionModel().select(item);
        assertEquals(index, getSelectionModel().getSelectedIndex());
        assertEquals("selectedItem must be model item at selectedIndex", 
                items.get(index), getSelectionModel().getSelectedItem());
        // this is what passes, but is inconsistent with the doc of getSelectedItem
        assertEquals("uncontained item must be selected item", item, getSelectionModel().getSelectedItem());
    }

    /**
     * Overridden to ignore (sorting not expected)
     */
    @Override @Ignore
    public void testSelectedIndexAfterSort() {
    }


    @Override
    protected SingleSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        ObservableList<String> titles = FXCollections.observableArrayList(
                "5-item", "4-item", "3-item", "2-item", "1-item");
        view = createView(titles);
        items = view.getTabs();
    }

    @Override
    protected TabPane createView(ObservableList items) {
        TabPane pane = new TabPane();
        for (Object object : items) {
            Tab tab = new Tab(object.toString());
            pane.getTabs().add(tab);
        }
        return pane;
    }


    @Override
    protected FocusModel getFocusModel() {
        return null;
    }
}
