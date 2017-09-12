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
import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import static de.swingempire.fx.util.VirtualFlowTestUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.scene.control.cell.EditIgnores.IgnoreListEdit;
import de.swingempire.fx.scene.control.cell.EditIgnores.IgnoreTableEdit;
import de.swingempire.fx.scene.control.cell.EditIgnores.IgnoreTreeEdit;
import de.swingempire.fx.util.ListViewEditReport;
import de.swingempire.fx.util.StageLoader;
import de.swingempire.fx.util.TableViewEditReport;
import de.swingempire.fx.util.TreeViewEditReport;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.control.skin.ListCellSkin;
import javafx.scene.control.skin.TreeCellSkin;
import javafx.scene.control.skin.TreeTableRowSkin;
/**
 * Divers tests around all cell types.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CellTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

//----------- test editEvents and cell/control state on Tree
    
    
//    /**
//     * Test notification/cell/list state with multiple edits
//     * 
//     * the index of cancel is always incorrect: a cancel is fired with index on the
//     * new edit position.
//     * Here the incorrect index is fired before the start event.
//     * 
//     * 
//     * Here: 
//     * edit(0)
//     * edit(1)
//     */
//    @ConditionalIgnore (condition = IgnoreTableEdit.class)
//    @Test
//    public void testTableEditChangeEditOnTableReversed() {
//        TableView<TableColumn> control = createEditableTable();
//        TableColumn<TableColumn, String> column = (TableColumn<TableColumn, String>) control.getColumns().get(0);
//        new StageLoader(control);
//        int initialEditIndex = 1;
//        IndexedCell initialEditingCell =  getCell(control, initialEditIndex, 0);
//        int secondEditIndex = 0;
//        IndexedCell secondEditingCell = getCell(control, secondEditIndex, 0);
//        // start edit on control with initial editIndex
//        control.edit(initialEditIndex, column);
//        assertTrue(initialEditingCell.isEditing());
//        assertEquals(initialEditIndex, initialEditingCell.getIndex());
//        TableViewEditReport report = new TableViewEditReport(control);
//        // switch editing to second
//        control.edit(secondEditIndex, column);
//        LOG.info("" + report.getAllEditEventTexts("edit(0) -> edit(1): "));
//        // test control state
//        TablePosition editingPos = control.getEditingCell();
//        assertNotNull("editingPos must not be null", editingPos);
//        assertEquals(secondEditIndex, editingPos.getRow());
//        // test cell state
//        assertFalse(initialEditingCell.isEditing());
//        assertEquals(initialEditIndex, initialEditingCell.getIndex());
//        assertTrue(secondEditingCell.isEditing());
//        assertEquals(secondEditIndex, secondEditingCell.getIndex());
//        // test editEvent
//        Optional<CellEditEvent> start = report.getLastEditStart();
//        assertTrue(start.isPresent());
//        assertNotNull("pos on startEvent must not be null", start.get().getTablePosition());
//        assertEquals("start on changed edited", secondEditIndex, start.get().getTablePosition().getRow());
//        Optional<CellEditEvent> cancel = report.getLastEditCancel();
//        assertTrue(cancel.isPresent());
//        assertNotNull("pos on cancelEvent must not be null", start.get().getTablePosition());
//        assertEquals("cancel on initially edited", initialEditIndex, cancel.get().getTablePosition().getRow());
//    }
//    
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
    @ConditionalIgnore (condition = IgnoreTreeEdit.class)
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
        LOG.info("" + report.getAllEditEventTexts("edit(0) -> edit(1): "));
        // test control state
        TreeItem editingPos = control.getEditingItem();
        assertNotNull("editingPos must not be null", editingPos);
        assertEquals(secondEditItem, editingPos);
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
     * start edit on list
     * -> commit edit on cell with newValue (same with identical value)
     * 
     * Note: can't commit on control, missing api
     */
    @ConditionalIgnore (condition = IgnoreTreeEdit.class)
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
        assertEquals("column text must be updated", editedValue, control.getTreeItem(editIndex).getValue());
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        Optional<TreeView.EditEvent> commit = report.getLastEditCommit();
        assertTrue(commit.isPresent());
        assertEquals("index on start event", editItem, commit.get().getTreeItem());
        // test control state: editing location m
        assertEquals("editing location must be reset: ", null, control.getEditingItem());
        assertEquals(1, report.getEditEventSize());
    }
    
    /**
     * Here: cancel the edit with control.edit(-1, null)
     * 
     */
    @ConditionalIgnore (condition = IgnoreTreeEdit.class)
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
        // test control state: editing location m
        assertEquals("editing location must be updated: ", null, control.getEditingItem());
    }
    
    /**
     * Here: cancel the edit with control.edit(-1, null)
     * 
     */
    @ConditionalIgnore (condition = IgnoreTreeEdit.class)
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
        // test control state: editing location m
        assertEquals("editing location must be updated: ", null, control.getEditingItem());
    }
    
    
    /**
     * Testing cell/editEvent: start edit on cell.startEdit
     * 
     * EditingCell not updated? This might be fixed with Jon's patch
     * for commit-on-focuslost
     */
    @ConditionalIgnore (condition = IgnoreTreeEdit.class)
    @Test
    public void testTreeEditStartOnCellHasEditingItem() {
        TreeView<String> control = createEditableTree();
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on cell
        cell.startEdit();
        // test control state
        assertNotNull("editingCell on control must not be null", control.getEditingItem());
    }
    /**
     * Test editing state after start
     */
    @ConditionalIgnore (condition = IgnoreTreeEdit.class)
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
//        LOG.info("what do we get?" + report.getEditText(e.get()));
//        assertNotNull(e.get().getTablePosition());
        assertEquals("index on start event", editItem, e.get().getTreeItem());
        // test control state: editing location m
        assertEquals("editing location must be updated: ", editItem, control.getEditingItem());
    }
    
    @ConditionalIgnore (condition = IgnoreTreeEdit.class)
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
//        LOG.info("what do we get?" + report.getEditText(e.get()));
//        assertNotNull(e.get().getTablePosition());
        assertEquals("index on start event", editItem, e.get().getTreeItem());
    }
    

    
//----------- test editEvents and cell/control state on Table  
    
    
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
    @ConditionalIgnore (condition = IgnoreTableEdit.class)
    @Test
    public void testTableEditChangeEditOnTableReversed() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn<TableColumn, String> column = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        new StageLoader(control);
        int initialEditIndex = 1;
        IndexedCell initialEditingCell =  getCell(control, initialEditIndex, 0);
        int secondEditIndex = 0;
        IndexedCell secondEditingCell = getCell(control, secondEditIndex, 0);
        // start edit on control with initial editIndex
        control.edit(initialEditIndex, column);
        assertTrue(initialEditingCell.isEditing());
        assertEquals(initialEditIndex, initialEditingCell.getIndex());
        TableViewEditReport report = new TableViewEditReport(control);
        // switch editing to second
        control.edit(secondEditIndex, column);
        LOG.info("" + report.getAllEditEventTexts("edit(0) -> edit(1): "));
        // test control state
        TablePosition editingPos = control.getEditingCell();
        assertNotNull("editingPos must not be null", editingPos);
        assertEquals(secondEditIndex, editingPos.getRow());
        // test cell state
        assertFalse(initialEditingCell.isEditing());
        assertEquals(initialEditIndex, initialEditingCell.getIndex());
        assertTrue(secondEditingCell.isEditing());
        assertEquals(secondEditIndex, secondEditingCell.getIndex());
        // test editEvent
        Optional<CellEditEvent> start = report.getLastEditStart();
        assertTrue(start.isPresent());
        assertNotNull("pos on startEvent must not be null", start.get().getTablePosition());
        assertEquals("start on changed edited", secondEditIndex, start.get().getTablePosition().getRow());
        Optional<CellEditEvent> cancel = report.getLastEditCancel();
        assertTrue(cancel.isPresent());
        assertNotNull("pos on cancelEvent must not be null", start.get().getTablePosition());
        assertEquals("cancel on initially edited", initialEditIndex, cancel.get().getTablePosition().getRow());
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
    @ConditionalIgnore (condition = IgnoreTableEdit.class)
    @Test
    public void testTableEditChangeEditOnTable() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn<TableColumn, String> column = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        new StageLoader(control);
        int initialEditIndex = 0;
        IndexedCell initialEditingCell =  getCell(control, initialEditIndex, 0);
        int secondEditIndex = 1;
        IndexedCell secondEditingCell = getCell(control, secondEditIndex, 0);
        // start edit on control with initial editIndex
        control.edit(initialEditIndex, column);
        assertTrue(initialEditingCell.isEditing());
        assertEquals(initialEditIndex, initialEditingCell.getIndex());
        TableViewEditReport report = new TableViewEditReport(control);
        // switch editing to second
        control.edit(secondEditIndex, column);
        LOG.info("" + report.getAllEditEventTexts("edit(0) -> edit(1): "));
        // test control state
        TablePosition editingPos = control.getEditingCell();
        assertNotNull("editingPos must not be null", editingPos);
        assertEquals(secondEditIndex, editingPos.getRow());
        // test cell state
        assertFalse(initialEditingCell.isEditing());
        assertEquals(initialEditIndex, initialEditingCell.getIndex());
        assertTrue(secondEditingCell.isEditing());
        assertEquals(secondEditIndex, secondEditingCell.getIndex());
        // test editEvent
        Optional<CellEditEvent> start = report.getLastEditStart();
        assertTrue(start.isPresent());
        assertNotNull("pos on startEvent must not be null", start.get().getTablePosition());
        assertEquals("start on changed edited", secondEditIndex, start.get().getTablePosition().getRow());
        Optional<CellEditEvent> cancel = report.getLastEditCancel();
        assertTrue(cancel.isPresent());
        assertNotNull("pos on cancelEvent must not be null", start.get().getTablePosition());
        assertEquals("cancel on initially edited", initialEditIndex, cancel.get().getTablePosition().getRow());
    }

    /**
     * 
     * start edit on list
     * -> commit edit on cell with newValue (same with identical value)
     * 
     * Note: can't commit on control, missing api
     */
    @ConditionalIgnore (condition = IgnoreTableEdit.class)
    @Test
    public void testTableEditCommitOnCell() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn<TableColumn, String> column = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        // start edit on control
        control.edit(editIndex, column);;
        TableViewEditReport report = new TableViewEditReport(control);
        String editedValue = "edited";
        cell.commitEdit(editedValue);
        // test data
        assertEquals("column text must be updated", editedValue, control.getItems().get(editIndex).getText());
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        Optional<CellEditEvent> commit = report.getLastEditCommit();
        assertTrue(commit.isPresent());
        assertNotNull("tablePosition must not be null", commit.get().getTablePosition());
        assertEquals("index on cancel event", editIndex,
                commit.get().getTablePosition().getRow());
        assertEquals("tableColumn must be colum", column, commit.get().getTableColumn());
        assertEquals(1, report.getEditEventSize());
    }
    
    /**
     * Here: cancel the edit with cell.cancelEdit ->
     * the cancel index is correct
     * ListView: EditEvent on cancel has incorrect index
     * 
     * reported: https://bugs.openjdk.java.net/browse/JDK-8187226
     * 
     */
    @ConditionalIgnore (condition = IgnoreTableEdit.class)
    @Test
    public void testTableEditCancelOnCell() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn<TableColumn, String> column = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        // start edit on control
        control.edit(editIndex, column);;
        TableViewEditReport report = new TableViewEditReport(control);
        // cancel edit on control
        control.edit(-1, null);
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<CellEditEvent> cancel = report.getLastEditCancel();
        assertTrue(cancel.isPresent());
        assertNotNull("tablePosition must not be null", cancel.get().getTablePosition());
        assertEquals("index on cancel event", editIndex,
                cancel.get().getTablePosition().getRow());
    }
    

    /**
     * Here: cancel the edit with control.edit(-1, null)
     * 
     */
    @ConditionalIgnore (condition = IgnoreTableEdit.class)
    @Test
    public void testTableEditCancelOnTable() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn column = control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        // start edit on control
        control.edit(editIndex, column);;
        TableViewEditReport report = new TableViewEditReport(control);
        // cancel edit on control
        control.edit(-1, null);
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<CellEditEvent> cancel = report.getLastEditCancel();
        assertTrue(cancel.isPresent());
        assertNotNull("tablePosition must not be null", cancel.get().getTablePosition());
        assertEquals("index on cancel event", editIndex,
                cancel.get().getTablePosition().getRow());
    }
    

    /**
     * Testing cell/editEvent: start edit on cell.startEdit
     * 
     * EditingCell not updated? This might be fixed with Jon's patch
     * for commit-on-focuslost
     */
    @ConditionalIgnore (condition = IgnoreTableEdit.class)
    @Test
    public void testTableEditStartOnCellHasEditingCell() {
        TableView<TableColumn> control = createEditableTable();
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        // start edit on cell
        cell.startEdit();
        // test control state
        assertNotNull("editingCell on control must not be null", control.getEditingCell());
    }
    
    /**
     * This test blows because table.editingCell isn't updated ... why not?
     */
    @ConditionalIgnore (condition = IgnoreTableEdit.class)
    @Test
    public void testTableEditStartOnCell() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn<TableColumn, String> first = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        TableViewEditReport report = new TableViewEditReport(control);
        // start edit on cell
        cell.startEdit();
        // test control state
        TablePosition pos = control.getEditingCell();
        assertNotNull("editingCell must not be null", pos);
        assertEquals(editIndex, pos.getRow());
        assertSame(first, pos.getTableColumn());
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<CellEditEvent> e = report.getLastEditStart();
        assertTrue(e.isPresent());
        assertEquals("index on start event", editIndex, e.get().getTablePosition().getRow());
        assertEquals("column on start event", first, e.get().getTablePosition().getTableColumn());
    }
    
    @ConditionalIgnore (condition = IgnoreTableEdit.class)
    @Test
    public void testTableEditStartOnTable() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn<TableColumn, String> first = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        TableViewEditReport report = new TableViewEditReport(control);
        // start edit on control
        control.edit(editIndex, first);
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<CellEditEvent> e = report.getLastEditStart();
        assertTrue(e.isPresent());
//        LOG.info("what do we get?" + report.getEditText(e.get()));
//        assertNotNull(e.get().getTablePosition());
        assertEquals("index on start event", editIndex, e.get().getTablePosition().getRow());
        assertEquals("column on start event", first, e.get().getTablePosition().getTableColumn());
    }

//---------------- test editEvents and cell/control state on List    
    
    
    /**
     * Test notification/cell/list state with multiple edits
     * 
     * the index of cancel is always incorrect: a cancel is fired with index on the
     * new edit position.
     * Here the incorrect index is fired before the start event.
     * 
     * Here: 
     * edit(1)
     * edit(0)
     * 
     */
    @ConditionalIgnore (condition = IgnoreListEdit.class)
    @Test
    public void testListEditChangeEditIndexOnListReversed() {
        ListView<String> control = createEditableList();
        new StageLoader(control);
        // initial edit index
        int initialEditIndex = 1;
        IndexedCell initialEditingCell =  getCell(control, initialEditIndex);
        int secondEditIndex = 0;
        IndexedCell secondEditingCell = getCell(control, secondEditIndex);
        // start edit on control with initial editIndex
        control.edit(initialEditIndex);
        assertTrue(initialEditingCell.isEditing());
        assertEquals(initialEditIndex, initialEditingCell.getIndex());
        ListViewEditReport report = new ListViewEditReport(control);
        // switch editing to second
        control.edit(secondEditIndex);
        LOG.info("" + report.getAllEditEventTexts("edit(1) -> edit(0): "));
        // test control state
        assertEquals(secondEditIndex, control.getEditingIndex());
        // test cell state
        assertFalse(initialEditingCell.isEditing());
        assertEquals(initialEditIndex, initialEditingCell.getIndex());
        assertTrue(secondEditingCell.isEditing());
        assertEquals(secondEditIndex, secondEditingCell.getIndex());
        // test editEvent
        assertEquals("change edit must fire ", 2, report.getEditEventSize());
        
        Optional<EditEvent> start = report.getLastEditStart();
        assertTrue(start.isPresent());
        assertEquals("start on changed edited", secondEditIndex, start.get().getIndex());
        Optional<EditEvent> cancel = report.getLastEditCancel();
        assertTrue(cancel.isPresent());
        assertEquals("cancel on initially edited", initialEditIndex, cancel.get().getIndex());
    }
    
    /**
     * Test notification/cell/table state with multiple edits
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
    @ConditionalIgnore (condition = IgnoreListEdit.class)
    @Test
    public void testListEditChangeEditIndexOnList() {
        ListView<String> control = createEditableList();
        new StageLoader(control);
        // initial edit index
        int initialEditIndex = 0;
        IndexedCell initialEditingCell =  getCell(control, initialEditIndex);
        int secondEditIndex = 1;
        IndexedCell secondEditingCell = getCell(control, secondEditIndex);
        // start edit on control with initial editIndex
        control.edit(initialEditIndex);
        assertTrue(initialEditingCell.isEditing());
        assertEquals(initialEditIndex, initialEditingCell.getIndex());
        ListViewEditReport report = new ListViewEditReport(control);
        // switch editing to second
        control.edit(secondEditIndex);
        LOG.info("" + report.getAllEditEventTexts("edit(0) -> edit(1): "));
        // test control state
        assertEquals(secondEditIndex, control.getEditingIndex());
        // test cell state
        assertFalse(initialEditingCell.isEditing());
        assertEquals(initialEditIndex, initialEditingCell.getIndex());
        assertTrue(secondEditingCell.isEditing());
        assertEquals(secondEditIndex, secondEditingCell.getIndex());
        // test editEvent
        Optional<EditEvent> start = report.getLastEditStart();
        assertTrue(start.isPresent());
        assertEquals("start on changed edited", secondEditIndex, start.get().getIndex());
        Optional<EditEvent> cancel = report.getLastEditCancel();
        assertTrue(cancel.isPresent());
        assertEquals("cancel on initially edited", initialEditIndex, cancel.get().getIndex());
    }
    

    
    
    /**
     * start edit on list
     * -> commit edit on cell with newValue (same with identical value)
     * 
     * Here we see 
     * ListView: receives both editCommit (expected) and 
     * editCancel (unexpected) when edit committed
     * 
     * reported:
     * https://bugs.openjdk.java.net/browse/JDK-8187307
     *
     * Note: can't commit on control, missing api
     */
    @ConditionalIgnore (condition = IgnoreListEdit.class)
    @Test
    public void testListEditCommitOnCell() {
        ListView<String> control = createEditableList();
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on control
        control.edit(editIndex);
        ListViewEditReport report = new ListViewEditReport(control);
        // commit value on cell
        String editedValue = "edited";
        cell.commitEdit(editedValue);
        // test control state
        assertEquals(-1, control.getEditingIndex());
        assertEquals(editedValue, control.getItems().get(editIndex));
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        Optional<EditEvent> commit = report.getLastEditCommit();
        assertTrue(commit.isPresent());
        assertEquals("index on commit event", editIndex, commit.get().getIndex());
        assertEquals("newValue on commit event", editedValue, commit.get().getNewValue());
        assertEquals("commit must fire a single event", 1, report.getEditEventSize());
    }
    
    /**
     * Here: cancel the edit with cell.cancelEdit ->
     * the cancel index is correct
     * ListView: EditEvent on cancel has incorrect index
     * 
     * reported: https://bugs.openjdk.java.net/browse/JDK-8187226
     * 
     */
    @ConditionalIgnore (condition = IgnoreListEdit.class)
    @Test
    public void testListEditCancelOnCell() {
        ListView<String> control = createEditableList();
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on control
        control.edit(editIndex);
        ListViewEditReport report = new ListViewEditReport(control);
        // cancel edit on cell
        cell.cancelEdit();
        // test cell state
        assertEquals(-1, control.getEditingIndex());
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<EditEvent> cancel = report.getLastEditCancel();
        assertTrue(cancel.isPresent());
        assertEquals("index on cancel event", editIndex, cancel.get().getIndex());
    }
    
    /**
     * Here: cancel the edit with list.edit(-1)
     * ListView: EditEvent on cancel has incorrect index
     * 
     * reported: https://bugs.openjdk.java.net/browse/JDK-8187226
     * 
     */
    @ConditionalIgnore (condition = IgnoreListEdit.class)
    @Test
    public void testListEditCancelOnControl() {
        ListView<String> control = createEditableList();
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell = getCell(control, editIndex);
        // start edit on control
        control.edit(editIndex);
        ListViewEditReport report = new ListViewEditReport(control);
        // cancel edit on control
        control.edit(-1);
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<EditEvent> cancel = report.getLastEditCancel();
        assertTrue(cancel.isPresent());
        assertEquals("index on cancel event", editIndex,
                cancel.get().getIndex());
    }
    
    /**
     * Incorrect index in editStart event when edit started on cell
     * 
     * reported as
     * https://bugs.openjdk.java.net/browse/JDK-8187432
     * 
     * Ignore stand-alone only, otherwise can't show fix in custom ListCell
     */
    @ConditionalIgnore (condition = IgnoreListEdit.class)
    @Test
    public void testListEditStartOnCell() {
        ListView<String> control = createEditableList();
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell = getCell(control, editIndex);
        ListViewEditReport report = new ListViewEditReport(control);
        // start edit on control
        cell.startEdit();
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<EditEvent> e = report.getLastEditStart();
        assertEquals("index on start event", editIndex, e.get().getIndex());
    }
    
    /**
     * Incorrect index in editStart event when edit started on cell
     * 
     * reported as
     * https://bugs.openjdk.java.net/browse/JDK-8187432
     */
    @ConditionalIgnore (condition = IgnoreListEdit.class)
    @Test
    public void testListEditStartOnCellStandalone() {
        ListView<String> control = new ListView<>(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        control.setEditable(true);
        control.setCellFactory(TextFieldListCell.forListView());
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell = getCell(control, editIndex);
        ObjectProperty<ListView.EditEvent> editEvent = new SimpleObjectProperty<>();
        control.addEventHandler(ListView.editStartEvent(), e -> editEvent.set(e));
        // start edit on cell
        cell.startEdit();
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertNotNull(editEvent.get());
        assertEquals("type is startEdit", ListView.editStartEvent(), editEvent.get().getEventType());
        assertEquals("index on start event", editIndex, editEvent.get().getIndex());
    }
    
    @ConditionalIgnore (condition = IgnoreListEdit.class)
    @Test
    public void testListEditStartOnList() {
        ListView<String> control = createEditableList();
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex);
        ListViewEditReport report = new ListViewEditReport(control);
        // start edit on control
        control.edit(editIndex);
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<EditEvent> e = report.getLastEditStart();
        assertTrue(e.isPresent());
        assertEquals("index on start event", editIndex, e.get().getIndex());
    }

// ------------------ test default edit handlers
    /**
     * Test default edit handlers: expected none for start/cancel,
     * default that commits
     * 
     * Here: Table
     */
    @ConditionalIgnore (condition = IgnoreTableEdit.class)
    @Test
    public void testTableEditHandler() {
        TableView<TableColumn> table = createEditableTable();
        new StageLoader(table);
        TableColumn control = table.getColumns().get(0);
        assertNull(control.getOnEditCancel());
        assertNull(control.getOnEditStart());
        assertNotNull("listView must have default commit handler", control.getOnEditCommit());
    }
    
    /**
     * Test default edit handlers: expected none for start/cancel,
     * default that commits
     * 
     * Here: List
     */
    @ConditionalIgnore (condition = IgnoreListEdit.class)
    @Test
    public void testListEditHandler() {
        ListView<String> control = createEditableList();
        new StageLoader(control);
        assertNull(control.getOnEditCancel());
        assertNull(control.getOnEditStart());
        assertNotNull("listView must have default commit handler", control.getOnEditCommit());
    }
    
    /**
     * Test default edit handlers: expected none for start/cancel,
     * default that commits
     * 
     * Here: Tree - fails, due to 
     * TreeViewCell: must not interfere with custom commit handler
     * 
     * reported:
     * https://bugs.openjdk.java.net/browse/JDK-8187309
     */
    @ConditionalIgnore (condition = IgnoreTreeEdit.class)
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
     * Creates and returns an editable Table of TableColumns (as items ;)
     * configured with 3 items
     * and TextFieldTableCell as cellFactory on first column (which represents
     * the textProperty of a TableColumn
     * 
     * @return
     */
    protected TableView<TableColumn> createEditableTable() {
        TableView<TableColumn> table = new TableView<>(
                FXCollections.observableArrayList(new TableColumn("first"),
                        new TableColumn("second"),
                        new TableColumn("third")
                ));
        table.setEditable(true);

        TableColumn<TableColumn, String> first = new TableColumn<>("Text");
        first.setCellFactory(TextFieldTableCell.forTableColumn());
        first.setCellValueFactory(new PropertyValueFactory<>("text"));

        table.getColumns().addAll(first);
        return table;

    }
    
    
    /**
     * Creates and returns an editable List configured with 4 items
     * and TextFieldListCell as cellFactory
     * 
     */
    protected ListView<String> createEditableList() {
        ListView<String> control = new ListView<>(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        control.setEditable(true);
        control.setCellFactory(TextFieldListCell.forListView());
        return control;
    }
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
        treeView.setCellFactory(TextFieldTreeCell.forTreeView());
        return treeView;
    }
    
//--------------------- old bugs, fixed in fx9    
    /**
     * Test about treeTableRowSkin: registers
     * a listener on the treeTableView treeColumn in constructor 
     * - throws if not yet bound to a treeTable
     * 
     * reported:
     * https://bugs.openjdk.java.net/browse/JDK-8151524
     */
    @Test
    public void testTreeTableRowSkinInit() {
        TreeTableRow row = new TreeTableRow();
        row.setSkin(new TreeTableRowSkin(row));
    }
    
    @Test
    public void testListCellSkinInit() {
        ListCell cell = new ListCell();
        cell.setSkin(new ListCellSkin(cell));
    }
    
    @Test
    public void testTreeCellSkin() {
        TreeCell cell = new TreeCell();
        cell.setSkin(new TreeCellSkin(cell));
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(CellTest.class.getName());
}
