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
import static javafx.scene.control.ListView.*;
import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.util.ListViewEditReport;
import de.swingempire.fx.util.StageLoader;
import de.swingempire.fx.util.TableViewEditReport;
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

//----------- test editEvents and cell/control state on Table    
    
    /**
     * Testing cell/editEvent: start edit on cell.startEdit
     * 
     * EditingCell not updated? This might be fixed with Jon's patch
     * for commit-on-focuslost
     */
    @Test
    public void testTableEditStartOnCellEditingCell() {
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
    
    @Test
    public void testTableEditStartOnControl() {
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
        LOG.info("what do we get?" + report.getEditText(e.get()));
        assertEquals("index on start event", editIndex, e.get().getTablePosition().getRow());
        assertEquals("column on start event", first, e.get().getTablePosition().getTableColumn());
    }

//---------------- test editEvents and cell/control state on List    
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
    @Test
    public void testListEditCommitOnCell() {
        ListView<String> control = createEditableList();
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on control
        control.edit(editIndex);
        ListViewEditReport report = new ListViewEditReport(control);
        // cancel edit on control
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
    @Test
    public void testListEditCancelOnList() {
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
     * Testing cell/editEvent: start edit on cell.startEdit
     * 
     * event index incorrect: still -1
     */
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
    @Test
    public void testTreeEditHandler() {
        TreeView<String> control = createOneChildEditableTree();
        new StageLoader(control);
        assertNull(control.getOnEditCancel());
        assertNull(control.getOnEditStart());
        assertNotNull("treeView must have default commit handler", control.getOnEditCommit());
    }
//------------ infrastructure methods

    /**
     * Creates and returns an editable Table of TableColumns (as items ;)
     * configured with 2 items
     * and TextFieldTableCell as cellFactory on first column (which represents
     * the textProperty of a TableColumn
     * 
     * @return
     */
    protected TableView<TableColumn> createEditableTable() {
        TableView<TableColumn> table = new TableView<>(
                FXCollections.observableArrayList(new TableColumn("first"),
                        new TableColumn("second")));
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
     * Creates and returns an editable Tree configured with 1 child item,
     * hidden root
     * and TextFieldTreeCell as cellFactory
     * 
     * @return
     */
    protected TreeView<String> createOneChildEditableTree() {
        TreeItem<String> rootItem = new TreeItem<>("root");
        TreeItem<String> child = new TreeItem<>("child", new Label("X"));
        rootItem.getChildren().add(child);
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
