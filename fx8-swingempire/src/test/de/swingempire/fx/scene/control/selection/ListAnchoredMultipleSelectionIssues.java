/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.swingempire.fx.control.selection.AnchoredSelectionModel;
import de.swingempire.fx.control.selection.ListViewABehavior;
import de.swingempire.fx.control.selection.ListViewAnchored;
import de.swingempire.fx.control.selection.ListViewAnchoredSkin;
import fx.util.StageLoader;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class ListAnchoredMultipleSelectionIssues extends MultipleSelectionIssues<ListView, MultipleSelectionModel> {

//---------- direct tests of action methods in behaviour: access reflectively  
// PENDING JW: move up - we can do the same for core ListViewBehavior    

    /**
     * The stageLoader used to force skin creation. It's an artefact of fx
     * instantiation process, not meant to be really used.
     * Note that it's the responsibility of the test method itself (not the setup)
     * to init if needed.
     */
    private StageLoader loader;

    @Test
    public void testBehaviorAlsoSelectNextRow() throws Exception {
        initSkin();
        String next = "alsoSelectNextRow";
        int index = 2;
        getSelectionModel().select(index);
        invokeBehavior(next);
        assertEquals(index + 1, getSelectionModel().getSelectedIndex());
        assertEquals(index + 1, getFocusIndex());
        if (multipleMode) {
            assertEquals(index, getAnchorIndex());
            assertEquals(2, getSelectionModel().getSelectedIndices().size());
        } else {
            assertEquals("anchor must be updated to next in single mode", 
                    index + 1, getAnchorIndex());
            assertEquals(1, getSelectionModel().getSelectedIndices().size());
            
        }
    }
    
    @Test
    public void testBehaviorAlsoSelectPreviousRow() throws Exception {
        initSkin();
        String previous = "alsoSelectPreviousRow";
        int index = 2;
        getSelectionModel().select(index);
        invokeBehavior(previous);
        assertEquals(index - 1, getSelectionModel().getSelectedIndex());
        assertEquals(index - 1, getFocusIndex());
        if (multipleMode) {
            assertEquals("anchor must be unchanged in multiple mode", index, getAnchorIndex());
            assertEquals(2, getSelectionModel().getSelectedIndices().size());
        } else {
            assertEquals("anchor must be updated to previous in single mode", 
                    index - 1, getAnchorIndex());
            assertEquals(1, getSelectionModel().getSelectedIndices().size());
        }
    }
    
    
    @Test
    public void testBehaviorSelectNextRow() throws Exception {
        initSkin();
        String next = "selectNextRow";
        int index = 2;
        getSelectionModel().select(index);
        invokeBehavior(next);
        assertEquals(index + 1, getSelectionModel().getSelectedIndex());
        assertEquals(index + 1, getAnchorIndex());
        assertEquals(index + 1, getFocusIndex());
        assertEquals(1, getSelectionModel().getSelectedIndices().size());
    }
    
    @Test
    public void testBehaviorSelectPreviousRow() throws Exception {
        initSkin();
        String previous = "selectPreviousRow";
        int index = 2;
        getSelectionModel().select(index);
        invokeBehavior(previous);
        assertEquals(index - 1, getSelectionModel().getSelectedIndex());
        assertEquals(index - 1, getAnchorIndex());
        assertEquals(index - 1, getFocusIndex());
        assertEquals(1, getSelectionModel().getSelectedIndices().size());
    }

    @Test
    public void testBehaviorSelectFirstRow() throws Exception {
        initSkin();
        String first = "selectFirstRow";
        int index = 2;
        getSelectionModel().select(index);
        invokeBehavior(first);
        assertEquals(0, getSelectionModel().getSelectedIndex());
        assertEquals(0, getFocusIndex());
        assertEquals(0, getAnchorIndex());
        assertEquals(1, getSelectionModel().getSelectedIndices().size());
    }
    
    @Test
    public void testBehaviorSelectLastRow() throws Exception {
        initSkin();
        String first = "selectLastRow";
        int index = 2;
        getSelectionModel().select(index);
        invokeBehavior(first);
        assertEquals(items.size() - 1, getSelectionModel().getSelectedIndex());
        assertEquals(items.size() - 1, getFocusIndex());
        assertEquals(items.size() - 1, getAnchorIndex());
        assertEquals(1, getSelectionModel().getSelectedIndices().size());
    }
    
    
    @Test
    public void testBehaviorSelectAllToFirstRow() throws Exception {
        initSkin();
        String first = "selectAllToFirstRow";
        int index = 2;
        getSelectionModel().select(index);
        invokeBehavior(first);
        assertEquals(0, getSelectionModel().getSelectedIndex());
        assertEquals(0, getFocusIndex());
        if (multipleMode) {
            assertEquals(index, getAnchorIndex());
            assertEquals(index + 1, getSelectionModel().getSelectedIndices().size());
        } else {
            assertEquals(0, getAnchorIndex());
            assertEquals(1, getSelectionModel().getSelectedIndices().size());
        }
    }

    @Test
    public void testBehaviorSelectAllToLastRow() throws Exception {
        initSkin();
        String first = "selectAllToLastRow";
        int index = 2;
        getSelectionModel().select(index);
        invokeBehavior(first);
      
        assertEquals(items.size() - 1, getSelectionModel().getSelectedIndex());
        assertEquals(items.size() - 1, getFocusIndex());
        if (multipleMode) {
            assertEquals(index, getAnchorIndex());
            assertEquals(items.size() - index, getSelectionModel().getSelectedIndices().size());
        } else {
            assertEquals(items.size() - 1, getAnchorIndex());
            assertEquals(1, getSelectionModel().getSelectedIndices().size());
        } 
    }
    
    
    /**
     * Loads the view into a StageLoader to enforce skin creation.
     * asserts empty selection.
     */
    protected void initSkin() {
        loader = new StageLoader(getView());
        assertEquals("sanity: initially unselected", -1, getSelectionModel().getSelectedIndex());
    }


    /**
     * Refectively invokes action method on behavior
     * @param next
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    protected void invokeBehavior(String next)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        ListViewAnchoredSkin skin = (ListViewAnchoredSkin) getView().getSkin();
        ListViewABehavior behavior = (ListViewABehavior) skin.getBehavior();
        Method selectNextRow = behavior.getClass().getDeclaredMethod(next);
        selectNextRow.setAccessible(true);
        selectNextRow.invoke(behavior);
    }
    
    
    /**
     * AnchoredSelectionModel allows to anchor the focus. Raw testing.
     */
    @Test
    public void testAnchorToFocus() {
        int focus = 2;
        getFocusModel().focus(focus);
        getAnchoredSelectionModel().anchor();
        assertEquals(focus, getAnchorIndex());
    }
    
    public ListAnchoredMultipleSelectionIssues(boolean multiple) {
        super(multiple);
    }
 
    @Override
    protected ListViewAnchored createView(ObservableList items) {
        ListViewAnchored table = new ListViewAnchored(items);
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
    protected  MultipleSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }
    
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ListAnchoredMultipleSelectionIssues.class
        .getName());

    @Override
    protected FocusModel getFocusModel() {
        return getView().getFocusModel();
    }
    
    protected AnchoredSelectionModel getAnchoredSelectionModel() {
        return (AnchoredSelectionModel) getSelectionModel();
    }
    @Override
    protected int getAnchorIndex() {
        return getAnchoredSelectionModel().getAnchorIndex();
    }



}