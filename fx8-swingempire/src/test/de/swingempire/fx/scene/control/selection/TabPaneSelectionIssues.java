/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Testing TabPaneSelectionModel: incomplete, needs additional testing because it
 * should enforce exactly one selected tab at any time.
 * 
 * SelectionIssues evolved into a direction that's getting more and more
 * incompatible with this: with the list modification tests, we get 
 * errors due to items !instanceof Tab.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class TabPaneSelectionIssues extends SelectionIssues<TabPane, SingleSelectionModel> {

    
    /**
     *  Overridden to do nothing and ignore: always one tab selected
     */
    @Override @Ignore
    public void testSelectNextOnEmpty() {
    }

    /**
     *  Overridden to do nothing and ignore: always one tab selected
     */
    @Override @Ignore
    public void testSelectPreviousOnEmpty() {
    }

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
     * Overridden to ignore (sorting not supported)
     */
    @Override @Ignore
    public void testSelectedIndexAfterSort() {
    }

    

    @Override @Ignore @Test
    public void testSelectedOnSetItemAtSelectedFocused() {
        // TODO Auto-generated method stub
        super.testSelectedOnSetItemAtSelectedFocused();
    }

    @Override @Ignore @Test
    public void testFocusOnSetItemAtSelectedFocused() {
        // TODO Auto-generated method stub
        super.testFocusOnSetItemAtSelectedFocused();
    }

    @Override @Ignore @Test
    public void testAnchorOnSetItemAtSelectedFocused() {
        // TODO Auto-generated method stub
        super.testAnchorOnSetItemAtSelectedFocused();
    }

    @Override @Ignore @Test
    public void testSelectedOnInsertItemAbove() {
        // TODO Auto-generated method stub
        super.testSelectedOnInsertItemAbove();
    }

    @Override @Ignore @Test
    public void testFocusOnInsertItemAbove() {
        // TODO Auto-generated method stub
        super.testFocusOnInsertItemAbove();
    }

    @Override @Ignore @Test
    public void testFocusOnInsertItemAtSelected39042() {
        // TODO Auto-generated method stub
        super.testFocusOnInsertItemAtSelected39042();
    }

    @Override @Ignore @Test
    public void testSelectedOnInsertItemAtSelected39042() {
        // TODO Auto-generated method stub
        super.testSelectedOnInsertItemAtSelected39042();
    }

    @Override @Ignore @Test
    public void testAnchorOnInsertItemAtSelected39042() {
        // TODO Auto-generated method stub
        super.testAnchorOnInsertItemAtSelected39042();
    }

    @Override @Ignore @Test
    public void testFocusOnInsertItemAtSelected() {
        // TODO Auto-generated method stub
        super.testFocusOnInsertItemAtSelected();
    }

    @Override @Ignore @Test
    public void testSelectedOnInsertItemAtSelected() {
        // TODO Auto-generated method stub
        super.testSelectedOnInsertItemAtSelected();
    }

    @Override @Ignore @Test
    public void testAnchorOnInsertItemAtSelected() {
        // TODO Auto-generated method stub
        super.testAnchorOnInsertItemAtSelected();
    }

    @Override @Ignore @Test
    public void testAnchorOnInsertItemBelow() {
        // TODO Auto-generated method stub
        super.testAnchorOnInsertItemBelow();
    }

    @Override @Ignore @Test
    public void testAnchorOnInsertItemAbove() {
        // TODO Auto-generated method stub
        super.testAnchorOnInsertItemAbove();
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
    protected int getAnchorIndex(int index) {
        return index;
    }

    @Override
    protected FocusModel getFocusModel() {
        return null;
    }

    @Override
    protected void setSelectionModel(SingleSelectionModel model) {
        getView().setSelectionModel(model);
    }

    @Override
    protected void resetItems(ObservableList other) {
        // nothing to do, tabPane doesn't support setTabs
    }
}
