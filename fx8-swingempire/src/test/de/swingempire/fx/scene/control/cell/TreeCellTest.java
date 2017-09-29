/*
 * Created on 09.03.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Optional;
import java.util.logging.Logger;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static de.swingempire.fx.util.VirtualFlowTestUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.util.StageLoader;
import de.swingempire.fx.util.TreeViewEditReport;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.control.skin.TreeCellSkin;
import javafx.util.Callback;
/**
 * Divers tests around all treeCell types. Initially copied all from 
 * CellTest, then deleted all tests that are not treeCell
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TreeCellTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    /**
     * Cell.startEdit doesn't switch into editing if empty. That's
     * the case for a cell without view.
     */
    @Test
    public void testNullControlOnStartEdit() {
        TreeCell cell = createTextFieldTreeCell().call(null);
        cell.startEdit();
        assertFalse("cell without control must not be editing", cell.isEditing());
    }
    
    @Test
    public void testNullControlOnCancelEdit() {
        TreeCell cell = createTextFieldTreeCell().call(null);
        cell.cancelEdit();
    }
    
    @Test
    public void testNullControlOnCommitEdit() {
        TreeCell cell = createTextFieldTreeCell().call(null);
        cell.commitEdit("dummy");
    }

    /**
     * Test editing state after start
     */
    @Test
    public void testTreeEditStartOnCellTwice() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex);
        TreeViewEditReport report = new TreeViewEditReport(control);
        // start edit on cell
        cell.startEdit();
        // start again -> nothing changed, no event
        cell.startEdit();
        // test editEvent
        assertEquals("second start on same must not fire event", 1, report.getEditEventSize());
    }

    @Test
    public void testTreeEditStartOnControlTwice() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int editIndex = 1;
        TreeItem editItem = control.getTreeItem(editIndex);
        TreeViewEditReport report = new TreeViewEditReport(control);
        // start edit on control
        control.edit(editItem);
        // start again -> nothing changed, no event
        control.edit(editItem);
        // test editEvent
        assertEquals("second start on same must not fire event", 1, report.getEditEventSize());
    }

//----------- test editEvents and cell/control state on Tree
    
    
    /**
     * Test notification/cell/list state with multiple edits
     * 
     * the index of cancel is always incorrect: a cancel is fired with index on the
     * new edit position.
     * Here the incorrect index is fired before the start event.
     * 
     * 
     * Here: 
     * edit(1)
     * edit(0)
     */
    @Test
    public void testTreeEditChangeEditOnControlReverse() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int initialEditIndex = 1;
        TreeItem initialEditItem = control.getTreeItem(initialEditIndex);
        IndexedCell initialEditingCell =  getCell(control, initialEditIndex);
        int secondEditIndex = 2;
        TreeItem secondEditItem = control.getTreeItem(secondEditIndex);
        IndexedCell secondEditingCell = getCell(control, secondEditIndex);
        // start edit on control with initial editIndex
        control.edit(initialEditItem);
        assertTrue(initialEditingCell.isEditing());
        assertEquals(initialEditIndex, initialEditingCell.getIndex());
        TreeViewEditReport report = new TreeViewEditReport(control);
        // switch editing to second
        control.edit(secondEditItem);
//        LOG.info("" + report.getAllEditEventTexts("edit(0) -> edit(1): "));
        // test cell state
        assertFalse(initialEditingCell.isEditing());
        assertEquals(initialEditIndex, initialEditingCell.getIndex());
        assertTrue(secondEditingCell.isEditing());
        assertEquals(secondEditIndex, secondEditingCell.getIndex());
        // test editEvent
        Optional<TreeView.EditEvent> start = report.getLastEditStart();
        assertTrue(start.isPresent());
        assertEquals("item on cancel event", secondEditItem, start.get().getTreeItem());
        Optional<TreeView.EditEvent> cancel = report.getLastEditCancel();
        assertTrue(cancel.isPresent());
        assertEquals("item on cancel event", initialEditItem, cancel.get().getTreeItem());
    }
    
    /**
     * Test notification/cell/list state with multiple edits
     * 
     * the index of cancel is always incorrect: a cancel is fired with index on the
     * new edit position.
     * Here the incorrect index is fired before the start event.
     * 
     * 
     * Here: 
     * edit(0)
     * edit(1)
     */
    @Test
    public void testTreeEditChangeEditOnTree() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int initialEditIndex = 0;
        TreeItem initialEditItem = control.getTreeItem(initialEditIndex);
        IndexedCell initialEditingCell =  getCell(control, initialEditIndex);
        int secondEditIndex = 1;
        TreeItem secondEditItem = control.getTreeItem(secondEditIndex);
        IndexedCell secondEditingCell = getCell(control, secondEditIndex);
        // start edit on control with initial editIndex
        control.edit(initialEditItem);
        assertTrue(initialEditingCell.isEditing());
        assertEquals(initialEditIndex, initialEditingCell.getIndex());
        TreeViewEditReport report = new TreeViewEditReport(control);
        // switch editing to second
        control.edit(secondEditItem);
//        LOG.info("" + report.getAllEditEventTexts("edit(0) -> edit(1): "));
        // test cell state
        assertFalse(initialEditingCell.isEditing());
        assertEquals(initialEditIndex, initialEditingCell.getIndex());
        assertTrue(secondEditingCell.isEditing());
        assertEquals(secondEditIndex, secondEditingCell.getIndex());
        // test editEvent
        Optional<TreeView.EditEvent> start = report.getLastEditStart();
        assertTrue(start.isPresent());
        assertEquals("item on cancel event", secondEditItem, start.get().getTreeItem());
        Optional<TreeView.EditEvent> cancel = report.getLastEditCancel();
        assertTrue(cancel.isPresent());
        assertEquals("item on cancel event", initialEditItem, cancel.get().getTreeItem());
    }
    
    /**
     * 
     * start edit on control
     * -> commit edit on cell with newValue (same with identical value)
     *
     * TreeCell: must not interfere with custom commit handler
     * 
     * reported:
     * https://bugs.openjdk.java.net/browse/JDK-8187309
     *
     * 
     * Note: can't commit on control, missing api
     */
    @Test
    public void testTreeEditRespectCommitHandler() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int editIndex = 1;
        TreeItem<String> editItem = control.getTreeItem(editIndex);
        String oldValue = editItem.getValue();
        IndexedCell cell =  getCell(control, editIndex);
        // do nothing
        control.setOnEditCommit(e -> new String("dummy"));
        // start edit on control
        control.edit(editItem);
        TreeViewEditReport report = new TreeViewEditReport(control);
        String editedValue = "edited";
        // commit edit on cell
        cell.commitEdit(editedValue);
        // test data
        assertEquals("value must not be changed", oldValue, control.getTreeItem(editIndex).getValue());
        assertEquals(1, report.getEditEventSize());
    }
    
    /**
     * 
     * start edit on list
     * -> commit edit on cell with newValue (same with identical value)
     * 
     * This passes in core - even though treeView has no default commit handler! -
     * due to https://bugs.openjdk.java.net/browse/JDK-8187309, cell fiddles with
     * data
     * 
     * Note: can't commit on control, missing api
     */
    @Test
    public void testTreeEditCommitOnCell() {
        TreeView<String> control = createEditableTree();
         new StageLoader(control);
        int editIndex = 1;
        TreeItem editItem = control.getTreeItem(editIndex);
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on control
        control.edit(editItem);
        TreeViewEditReport report = new TreeViewEditReport(control);
        String editedValue = "edited";
        // commit edit on cell
        cell.commitEdit(editedValue);
        // test data
        assertEquals("value must be updated", editedValue, control.getTreeItem(editIndex).getValue());
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        Optional<TreeView.EditEvent> commit = report.getLastEditCommit();
        assertTrue(commit.isPresent());
        assertEquals("index on start event", editItem, commit.get().getTreeItem());
        // test control state: editing location m
        assertEquals("editing location must be reset: ", null, control.getEditingItem());
        assertEquals("treeCell must fire a single event", 1, report.getEditEventSize());
    }
    
    
    
    /**
     * Here: cancel the edit with control.edit(-1, null)
     * 
     */
    @Test
    public void testTreeEditCancelOnCell() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int editIndex = 1;
        TreeItem editItem = control.getTreeItem(editIndex);
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on control
        control.edit(editItem);
        TreeViewEditReport report = new TreeViewEditReport(control);
        // cancel edit on cell
        cell.cancelEdit();
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<TreeView.EditEvent> cancel = report.getLastEditCancel();
        assertTrue(cancel.isPresent());
        assertEquals("index on cancel event", editItem, cancel.get().getTreeItem());
    }

    /**
     * Here: cancel the edit with control.edit(null)
     * 
     */
    @Test
    public void testTreeEditCancelOnControl() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int editIndex = 1;
        TreeItem editItem = control.getTreeItem(editIndex);
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on control
        control.edit(editItem);
        TreeViewEditReport report = new TreeViewEditReport(control);
        // cancel edit on control
        control.edit(null);
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<TreeView.EditEvent> cancel = report.getLastEditCancel();
        assertTrue(cancel.isPresent());
        assertEquals("index on cancel event", editItem, cancel.get().getTreeItem());
    }
    
    
    /**
     * Test editing state after start
     */
    @Test
    public void testTreeEditStartOnCell() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int editIndex = 1;
        TreeItem editItem = control.getTreeItem(editIndex);
        IndexedCell cell =  getCell(control, editIndex);
        TreeViewEditReport report = new TreeViewEditReport(control);
        // start edit on cell
        cell.startEdit();
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<TreeView.EditEvent> e = report.getLastEditStart();
        assertTrue(e.isPresent());
        assertEquals("index on start event", editItem, e.get().getTreeItem());
    }
    
    @Test
    public void testTreeEditStartOnControl() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int editIndex = 1;
        TreeItem editItem = control.getTreeItem(editIndex);
        IndexedCell cell =  getCell(control, editIndex);
        TreeViewEditReport report = new TreeViewEditReport(control);
        // start edit on control
        control.edit(editItem);
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<TreeView.EditEvent> e = report.getLastEditStart();
        assertTrue(e.isPresent());
        assertEquals("index on start event", editItem, e.get().getTreeItem());
    }
    

    /**
     * Test update of editing location on control.
     * Here: after commit edit with cell.commitEdit
     * 
     */
    @Test
    public void testTreeEditCommitOnCellResetEditingItem() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int editIndex = 1;
        TreeItem editItem = control.getTreeItem(editIndex);
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on control
        control.edit(editItem);
        String value = "edited";
        // cancel edit on cell
        cell.commitEdit(value);
        assertEquals("editing location must be reset: ", null, control.getEditingItem());
    }
    
    /**
     * Test update of editing location on control.
     * Here: after cancel edit with cell.cancelEdit
     * 
     */
    @Test
    public void testTreeEditCancelOnCellResetEditingItem() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int editIndex = 1;
        TreeItem editItem = control.getTreeItem(editIndex);
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on control
        control.edit(editItem);
        // cancel edit on cell
        cell.cancelEdit();
        assertEquals("editing location must be reset: ", null, control.getEditingItem());
    }

    /**
     * Test update of editing location on control.
     * Here: after start edit with cell.startEdit
     * 
     * This might be fixed with Jon's patch
     * for commit-on-focuslost
     */
    @Test
    public void testTreeEditStartOnCellHasEditingItem() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int editIndex = 1;
        TreeItem editingItem = control.getTreeItem(1);
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on cell
        cell.startEdit();
        // test control state
        assertEquals("editingCell on control ", editingItem, control.getEditingItem());
    }

    /**
     * Test default edit handlers: expected none for start/cancel,
     * default that commits
     * 
     * Here: Tree - fails, probably reason for cell taking over itself
     * (https://bugs.openjdk.java.net/browse/JDK-8187309)
     */
    @Test
    public void testTreeEditHandler() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        assertNull(control.getOnEditCancel());
        assertNull(control.getOnEditStart());
        assertNotNull("treeView must have default commit handler", control.getOnEditCommit());
    }
    
//------------ infrastructure methods

    /**
     * Creates and returns an editable Tree configured with 3 child item,
     * hidden root
     * and TextFieldTreeCell as cellFactory
     * 
     * @return
     */
    protected TreeView<String> createEditableTree() {
        TreeItem<String> rootItem = new TreeItem<>("root");
        rootItem.getChildren().addAll(
                new TreeItem<>("one"),
                new TreeItem<>("two"),
                new TreeItem<>("three")
                
                );
        TreeView<String> treeView = new TreeView<>(rootItem);
        treeView.setShowRoot(false);
        treeView.setEditable(true);
        treeView.setCellFactory(createTextFieldTreeCell());
        return treeView;
    }

    /**
     * @return
     */
    protected Callback<TreeView<String>, TreeCell<String>> createTextFieldTreeCell() {
        return TextFieldTreeCell.forTreeView();
    }
    
//--------------------- old bugs, fixed in fx9    
    @Test
    public void testTreeCellSkin() {
        TreeCell cell = new TreeCell();
        cell.setSkin(new TreeCellSkin(cell));
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeCellTest.class.getName());
}
