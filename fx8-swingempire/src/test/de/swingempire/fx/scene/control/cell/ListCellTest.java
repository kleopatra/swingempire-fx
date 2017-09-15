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
import com.sun.javafx.tk.Toolkit;

import static de.swingempire.fx.util.VirtualFlowTestUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.util.ListViewEditReport;
import de.swingempire.fx.util.StageLoader;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.skin.ListCellSkin;
import javafx.util.Callback;
/**
 * Divers tests around all listCell types. Initially copied all from 
 * CellTest, then deleted all tests that are not listCell
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ListCellTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();


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
//        LOG.info("" + report.getAllEditEventTexts("edit(1) -> edit(0): "));
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
    @Test
    public void testListEditChangeEditIndexOnControl() {
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
//        LOG.info("" + report.getAllEditEventTexts("edit(0) -> edit(1): "));
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
    
    /**
     * Test update of editing location on control
     */
    @Test
    public void testListEditCommitOnCellResetEditingIndex() {
        ListView<String> control = createEditableList();
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex);
        control.edit(editIndex);
        // cancel edit on control
        cell.commitEdit("edited");
        // test editing location
        assertEquals("editingIndex must be updated", -1, control.getEditingIndex());
    }
    
    /**
     * Test update of editing location on control
     */
    @Test
    public void testListEditCancelOnCellResetEditingIndex() {
        ListView<String> control = createEditableList();
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex);
        control.edit(editIndex);
        // cancel edit on control
        cell.cancelEdit();
        // test editing location
        assertEquals("editingIndex must be updated", -1, control.getEditingIndex());
    }
    
    /**
     * Test update of editing location on control
     */
    @Test
    public void testListEditStartOnListHasEditingIndex() {
        ListView<String> control = createEditableList();
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on control
        cell.startEdit();
        // test editing location
        assertEquals("editingIndex must be updated", editIndex, control.getEditingIndex());
    }

//----------------------- focus state
    
    /**
     * Experiments around focus
     */
    @Test
    public void testListEditStartFocus() {
        ListView<String> control = createEditableList();
        StageLoader sl = new StageLoader(control);
        sl.getStage().requestFocus();
        Toolkit.getToolkit().firePulse();
        assertEquals(sl.getStage().getScene().getFocusOwner(), control);
        int editIndex = 1;
        control.getFocusModel().focus(editIndex);
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on control
        control.edit(editIndex);
//        Toolkit.getToolkit().firePulse();
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        assertTrue("cell must be focused", cell.isFocused());
        assertEquals("textField must be focused", cell.getGraphic(), sl.getStage().getScene().getFocusOwner());
    }
    

// ------------------ test default edit handlers
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
    
    
//------------ infrastructure methods

    /**
     * Creates and returns an editable List configured with 4 items
     * and TextFieldListCell as cellFactory
     * 
     */
    protected ListView<String> createEditableList() {
        ListView<String> control = new ListView<>(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        control.setEditable(true);
        control.setCellFactory(createTextFieldListCell());
        return control;
    }

    /**
     * @return
     */
    protected Callback<ListView<String>, ListCell<String>> createTextFieldListCell() {
        return TextFieldListCell.forListView();
    }
    
//--------------------- old bugs, fixed in fx9    
    
    @Test
    public void testListCellSkinInit() {
        ListCell cell = new ListCell();
        cell.setSkin(new ListCellSkin(cell));
    }
    

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ListCellTest.class.getName());
}
