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
import de.swingempire.fx.util.TableViewEditReport;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.skin.TreeTableRowSkin;
import javafx.util.Callback;
/**
 * Now moved into AbstractCellTest - verified complete coverage.
 * 
 * Divers tests around all tableCell types. Initially copied all from 
 * CellTest, then deleted all tests that are not tableCell
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class OldTableCellTest {

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
        TableCell cell = createTextFieldTableCell().call(null);
        cell.startEdit();
        assertFalse("cell without control must not be editing", cell.isEditing());
    }
    
    @Test
    public void testNullControlOnCancelEdit() {
        TableCell cell = createTextFieldTableCell().call(null);
        cell.cancelEdit();
    }
    
    @Test
    public void testNullControlOnCommitEdit() {
        TableCell cell = createTextFieldTableCell().call(null);
        cell.commitEdit("dummy");
    }

    /**
     */
    @Test
    public void testTableEditStartOnCellTwice() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn<TableColumn, String> first = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        TableViewEditReport report = new TableViewEditReport(control);
        // start edit on cell
        cell.startEdit();
        // start again -> nothing changed, no event
        cell.startEdit();
        // test editEvent
        assertEquals("second start on same must not fire event", 1, report.getEditEventSize());
    }
    
    @Test
    public void testTableEditStartOnControlTwice() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn<TableColumn, String> first = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        TableViewEditReport report = new TableViewEditReport(control);
        // start edit on cell
        control.edit(editIndex, first);
        // start again -> nothing changed, no event
        control.edit(editIndex, first);
        // test editEvent
        assertEquals("second start on same must not fire event", 1, report.getEditEventSize());
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
    @Test
    public void testTableEditChangeEditOnControlReversed() {
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
//        LOG.info("" + report.getAllEditEventTexts("edit(0) -> edit(1): "));
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
    @Test
    public void testTableEditChangeEditOnControl() {
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
//        LOG.info("" + report.getAllEditEventTexts("edit(0) -> edit(1): "));
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

    @Test
    public void testTableEditCommitCellSelection() {
        TableView<TableColumn> control = createEditableTable(true);
        TableColumn<TableColumn, String> column = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        control.getSelectionModel().setCellSelectionEnabled(true);
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        // start edit on control
        control.edit(editIndex, column);;
        TableViewEditReport report = new TableViewEditReport(control);
        String editedValue = "edited";
        cell.commitEdit(editedValue);
        assertEquals("tableCell must fire a single event", 1, report.getEditEventSize());
        
    }


    /**
     * Focus on event count: fires incorrect cancel if items has extractor
     * on edited column.
     */
    @Test
    public void testTableEditCommitOnCellEventCount() {
        TableView<TableColumn> control = createEditableTable(true);
        TableColumn<TableColumn, String> column = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        // start edit on control
        control.edit(editIndex, column);;
        TableViewEditReport report = new TableViewEditReport(control);
        String editedValue = "edited";
        cell.commitEdit(editedValue);
        assertEquals("tableCell must fire a single event", 1, report.getEditEventSize());
    }
    
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
        assertEquals("tableCell must fire a single event", 1, report.getEditEventSize());
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
    public void testTableEditCancelOnCell() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn<TableColumn, String> column = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        // start edit on control
        control.edit(editIndex, column);
        TableViewEditReport report = new TableViewEditReport(control);
        // cancel edit on control
        cell.cancelEdit();
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
    @Test
    public void testTableEditCancelOnControl() {
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
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        Optional<CellEditEvent> e = report.getLastEditStart();
        assertTrue(e.isPresent());
        assertNotNull("position on start event must not be null", e.get().getTablePosition());
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
//        LOG.info("what do we get?" + report.getEditText(e.get()));
        assertNotNull("position on event must not be null", e.get().getTablePosition());
        assertEquals("index on start event", editIndex, e.get().getTablePosition().getRow());
        assertEquals("column on start event", first, e.get().getTablePosition().getTableColumn());
    }

    /**
     * Test update of editing location on control.
     * Here: after commit edit with cell.cancelEdit
     * 
     */
    @Test
    public void testTableEditCommitOnCellResetEditingCell() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn column = control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        control.edit(editIndex, column);
        IndexedCell cell =  getCell(control, editIndex, 0);
        // cancel edit on cell
        cell.commitEdit("edited");
        // test control state
        assertEquals("editingCell on control be updated", null, control.getEditingCell());
    }
    /**
     * Test update of editing location on control.
     * Here: after cancel edit with cell.cancelEdit
     * 
     */
    @Test
    public void testTableEditCancelOnCellResetEditingCell() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn column = control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        control.edit(editIndex, column);
        IndexedCell cell =  getCell(control, editIndex, 0);
        // cancel edit on cell
        cell.cancelEdit();
        // test control state
        assertEquals("editingCell on control be updated", null, control.getEditingCell());
    }
    /**
     * Test update of editing location on control.
     * Here: after start edit with cell.startEdit
     * 
     * 
     * This might be fixed with Jon's patch
     * for commit-on-focuslost
     */
    @Test
    public void testTableEditStartOnCellHasEditingCell() {
        TableView<TableColumn> control = createEditableTable();
        TableColumn column = control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        TablePosition expected = new TablePosition(control, editIndex, column);
        IndexedCell cell =  getCell(control, editIndex, 0);
        // start edit on cell
        cell.startEdit();
        // test control state
        assertEquals("editingCell on control be updated", expected, control.getEditingCell());
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
    
//------------ infrastructure methods

    /**
     * Creates and returns an editable Table of TableColumns (as items ;)
     * configured with 3 items
     * and TextFieldTableCell as cellFactory on first column (which represents
     * the textProperty of a TableColumn, no extractor
     * 
     * @return
     */
    protected TableView<TableColumn> createEditableTable() {
        return createEditableTable(false);
    }
    
    /**
     * Creates and returns an editable Table of TableColumns (as items ;)
     * configured with 3 items
     * and TextFieldTableCell as cellFactory on first column (which represents
     * the textProperty of a TableColumn, extractor as requested 
     * 
     * @param withExtractor flag to indicate whether or not install extractor on 
     *   editing column
     * @return
     */
    protected TableView<TableColumn> createEditableTable(boolean withExtractor) {
        ObservableList<TableColumn> items = withExtractor ? 
            FXCollections.observableArrayList(e -> new Observable[] {e.textProperty()})
            : FXCollections.observableArrayList();
        items.addAll(new TableColumn("first"), new TableColumn("second"), new TableColumn("third"));
        TableView<TableColumn> table = new TableView<>(
                items);
        table.setEditable(true);

        TableColumn<TableColumn, String> first = new TableColumn<>("Text");
        first.setCellFactory(createTextFieldTableCell());
        first.setCellValueFactory(new PropertyValueFactory<>("text"));

        table.getColumns().addAll(first);
        return table;
    }

    /**
     * @return
     */
    protected Callback<TableColumn<TableColumn, String>, TableCell<TableColumn, String>> createTextFieldTableCell() {
        return TextFieldTableCell.forTableColumn();
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
    

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(OldTableCellTest.class.getName());
}
