/*
 * Created on 16.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreBehavior;
import de.swingempire.fx.util.ListChangeReport;
import static org.junit.Assert.*;

/**
 * This adds bowel-testing of ListViewBehavior (instead of life behavior testing
 * with f.i. JemmyFx) - might break with any update version!
 * 
 * NOTE: some core behavior methods are not semantically separated, that is they depend 
 * on a low-level key state flag. No key, no flag here, so they will fail ...  
 *  
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public abstract class AbstractListMultipleSelectionIssues<V extends ListView> 
    extends MultipleSelectionIssues<V, MultipleSelectionModel>{

    /**
     * selectedItems fires incorrect list change on items.clear
     * 
     * Introduced while fixing 38884
     * 
     * Stand-alone test for report.
     * 
     * Reported: https://javafx-jira.kenai.com/browse/RT-39553 
     * 
     * @see #testNoSuchElementOnClear_38884()
     * @see #testNoSuchElementOnClear_38884Unfixed()
     */
    @Test
    public void testRegressionIncorrectRemovedSizeOnClear_38884() {
        ObservableList items = FXCollections.observableArrayList(1, 2, 3, 4, 5);
        // note: commented line for really stand-alone
//        ListView view = new ListView(items);
        ListView view = createView(items);
        MultipleSelectionModel sm = view.getSelectionModel();
        sm.select(0);
        Object item = sm.getSelectedItem();
        int selectedItemsSize = sm.getSelectedItems().size();
        assertEquals("sanity: size of selectedItems ", 1, selectedItemsSize);
        List<Change> changes = new ArrayList();
        ListChangeListener l = c -> changes.add(c);
        sm.getSelectedItems().addListener(l);
        items.clear();
        Change c = changes.get(0);
        c.next();
        assertEquals("removed size", selectedItemsSize, c.getRemovedSize());
        // next is passing with fix, failing without
        assertEquals("removed item", item, c.getRemoved().get(0));
    }
    
    /**
     * Hack for core ListViewBehavior incomplete separation of 
     * low- vs. semantic level. Doesn't make a difference, though?.
     */
    protected boolean needsKey;

    protected void assertBehaviourFocusMove(String method, int intialSelected,
            int expectedFocus) throws Exception {
        initSkin();
        getSelectionModel().select(intialSelected);
        invokeBehavior(method);
        assertEquals(method + " focus must be moved", expectedFocus, getFocusIndex());
        assertFalse(method + " focus must not be selected", getSelectionModel().isSelected(expectedFocus));
        assertEquals(method + " selection must be unchanged", intialSelected, getSelectedIndex());
        assertEquals(method + " anchor must be unchanged", intialSelected, getAnchorIndex());
    }
    
    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorFocusNextRowIfBefore() throws Exception {
        assertBehaviourFocusMove("focusNextRow", -1, 0);
    }
    
    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorFocusNextRow() throws Exception {
        assertBehaviourFocusMove("focusNextRow", 2, 3);
    }
    
    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorFocusPreviousRow() throws Exception {
        assertBehaviourFocusMove("focusPreviousRow", 2, 1);
    }
    
    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorFocusFirstRow() throws Exception {
        assertBehaviourFocusMove("focusFirstRow", 2, 0);
    }
    
    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorFocusLastRow() throws Exception {
        assertBehaviourFocusMove("focusLastRow", 2, items.size() - 1);
    }
    
    protected void assertBehaviorSelectionMoved(String method, int index,
            int focus) throws Exception {
        assertBehaviorSelectionMovedWithModification(method, index, focus, null);
    }

    /**
     * Steps tested:
     * 
     * select -> run (if not null) -> invokeBehavior(method) -> test selection/focus state
     * 
     * 
     * @param method the name of the method do invoke in behaviour
     * @param selectedIndex the index to select
     * @param expectedIndex the expected index after running and invoking
     * @param run the action to run after selecting, may be null to 
     *    do nothing
     * @throws Exception
     */
    protected void assertBehaviorSelectionMovedWithModification(String method,
            int selectedIndex, int expectedIndex, Runnable run) throws Exception {
        initSkin();
        getSelectionModel().select(selectedIndex);
        if (run != null) run.run();
        invokeBehavior(method);
        assertEquals(method + " selected index must be moved", expectedIndex, getSelectedIndex());
        assertEquals(method + " focus must same as selected index", expectedIndex, getFocusIndex());
        assertEquals(method + " selection size must be one", 1, getSelectedIndices().size());
        assertEquals(method + " anchor must be same as focus", expectedIndex, getAnchorIndex());
    }

    /**
     * Navigation disabled if first is selected/focused and removed
     * https://javafx-jira.kenai.com/browse/RT-38785
     * 
     * (fixed for TableView, not for ListView 8u40b12)
     * 
     * Two issues: 
     * - after remove, focus on -1 instead of 0 ) that is, != selected
     * - navigation disabled
     * 
     * @see #testFocusFirstRemovedItem() 
     */
    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorSelectNextRowIfFirstRemoved() throws Exception {
        // select first
        int index = 0;
        // remove first:
        Runnable r = () -> getView().getItems().remove(index );
        // selectNext: 
        assertBehaviorSelectionMovedWithModification("selectNextRow", index, 1, r);
    }
    
    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorSelectNextRowIfFirst() throws Exception {
        assertBehaviorSelectionMoved("selectNextRow", -1, 0);
    }
    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorSelectNextRow() throws Exception {
        int index = 2;
        assertBehaviorSelectionMoved("selectNextRow", index, index + 1);
    }

    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorSelectPreviousRow() throws Exception {
        int index = 2;
        assertBehaviorSelectionMoved("selectPreviousRow", index, index - 1);
    }

    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorSelectFirstRow() throws Exception {
        assertBehaviorSelectionMoved("selectFirstRow", 2, 0);
    }

    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorSelectLastRow() throws Exception {
        assertBehaviorSelectionMoved("selectLastRow", 2, items.size() - 1);
    }

    /**
     * Sets up the selection model with one index selected initially and asserts state 
     * for after invoking a method that extends the selection.
     * 
     * @param method the name of the method to test
     * @param index the index to select initially
     * @param focus the expected selected index/focus after invoking the method
     * @param size the size of the selectedIndices
     * @throws Exception bunch of exceptions if reflective method lookup/invocation fails
     */
    protected void assertBehaviourSelectionExtended(String method, int index,
            int focus, int size) throws Exception{
        initSkin();
        getSelectionModel().select(index);
        invokeKey("isShiftDown");
        invokeBehavior(method);
        assertEquals(method + " selected index must be moved", focus, getSelectedIndex());
        assertEquals(method + " focus must be moved", focus, getFocusIndex());
        if (multipleMode) {
            assertEquals(method + " selection size must be changed in multiple mode", size, getSelectedIndices().size());
            assertEquals(method + " anchor must be unchanged in multiple mode", index, getAnchorIndex());
        } else {
            assertEquals(method + " selection size must be 1 in single mode", 1, getSelectedIndices().size());
            assertEquals(method + " anchor must be updated to focus in single mode", 
                    focus, getAnchorIndex());
        }
    }
 
    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorAlsoSelectNextRow() throws Exception {
        int index = 2;
        int focus = index + 1;
        int size = focus - index + 1;
        assertBehaviourSelectionExtended("alsoSelectNextRow", index, focus, size);
    }

    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorAlsoSelectPreviousRow() throws Exception {
        int index = 2;
        int focus = index - 1;
        int size = index - focus + 1;
        assertBehaviourSelectionExtended("alsoSelectPreviousRow", index, focus, size);
    }

    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorSelectAllToFirstRow() throws Exception {
        int index = 2;
        int focus = 0;
        int size = index - focus + 1;
        assertBehaviourSelectionExtended("selectAllToFirstRow", index, focus, size);
    }

    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorSelectAllToLastRow() throws Exception {
        int index = 2;
        int focus = items.size() - 1;
        int size = focus - index + 1;
        assertBehaviourSelectionExtended("selectAllToLastRow", index, focus, size);;
    }
    
    /**
     * 
     * @param anchorFocus
     * @throws Exception
     */
    protected void assertBehaviorSelectAllToFocus(boolean anchorFocus)
            throws Exception {
        initSkin();
        String method = "selectAllToFocus";
        int index = 2;
        getSelectionModel().select(index);
        int focus = 4;
        int size = focus - index + 1;
        getFocusModel().focus(focus);
        invokeBehavior(method, anchorFocus);
        assertEquals(focus, getSelectedIndex());
        assertEquals("sanity: focus unchanged", focus, getFocusIndex());
        if (multipleMode) {
            int expectedAnchor = anchorFocus ? focus : index;
            assertEquals(expectedAnchor, getAnchorIndex());
            assertEquals(size, getSelectedIndices().size());
        } else {
            assertEquals(focus, getAnchorIndex());
            assertEquals(1, getSelectedIndices().size());
        }
    }


    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorSelectAllToFocusFalse() throws Exception {
        assertBehaviorSelectAllToFocus(false); 
    }
    
    @Test
    @ConditionalIgnore (condition = IgnoreBehavior.class)
    public void testBehaviorSelectAllToFocusTrue() throws Exception {
        assertBehaviorSelectAllToFocus(true); 
    }

    /**
     * Refectively invokes action method on behavior
     * 
     * @param name method name
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    protected void invokeBehavior(String name) throws NoSuchMethodException,
        IllegalAccessException, InvocationTargetException {
        BehaviorSkinBase skin = (BehaviorSkinBase) getView().getSkin();
        BehaviorBase behavior = skin.getBehavior();
        Method method = behavior.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(behavior);
    }
    
    /**
     * Refectively invokes action method on behavior
     * 
     * NOTE to self: boolean.class != Boolean.class - getDeclaredMethod doesn't
     * do any primitve/Object type conversion.
     * 
     * @param name method name
     * @param param boolean parameter
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    protected void invokeBehavior(String name, boolean param) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        BehaviorSkinBase skin = (BehaviorSkinBase) getView().getSkin();
        BehaviorBase behavior = skin.getBehavior();
        Class<? extends BehaviorBase> behaviorClass = behavior.getClass();
        Method method = behaviorClass.getDeclaredMethod(name, boolean.class);
        method.setAccessible(true);
        method.invoke(behavior, param);
    }

    protected void invokeKey(String name) throws Exception {
        if (!needsKey) return;
        BehaviorSkinBase skin = (BehaviorSkinBase) getView().getSkin();
        BehaviorBase behavior = skin.getBehavior();
        Class<? extends BehaviorBase> behaviorClass = behavior.getClass();
        Field field = behaviorClass.getDeclaredField(name);
        field.setAccessible(true);
        field.set(behavior, true);
    }
    
    
//------------ regression testing
    
    /**
     * The main driving force here is to experiment with AnchoredSelectionModel
     * we'll live with the regression here, since it will be solved correctly
     * in core.
     */
    @Test
    public void testRT15793() {
        ListView<String> view = createEmptyView();
        int notified = 0;
//        view.itemsProperty().addListener(o -> {LOG.info("notified");});
//        view.itemsProperty().addListener((o, old, value) -> {LOG.info("notified");});
        ObservableList<String> emptyList = FXCollections.observableArrayList();
        // listView is instantiated with an empty list, so following assumption 
        // is incorrect
//        assertEquals(null, view.getItems());
        view.setItems(emptyList);
        emptyList.add("something");
        view.getSelectionModel().select(0);
        assertEquals(0, view.getSelectionModel().getSelectedIndex());
        emptyList.remove(0);
        assertEquals(-1, view.getSelectionModel().getSelectedIndex());
    }
    
    protected abstract ListView createEmptyView();

    /**
     * Implemented to call list.setItems.
     */
    @Override
    protected void setItems(ObservableList other) {
        getView().setItems(other);
    }

    @Override
    protected MultipleSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }

    @Override
    protected FocusModel getFocusModel() {
        return getView().getFocusModel();
    }

    /**
     * @param multiple
     */
    public AbstractListMultipleSelectionIssues(boolean multiple) {
        super(multiple);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AbstractListMultipleSelectionIssues.class.getName());
}
