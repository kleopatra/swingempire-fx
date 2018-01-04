/*
 * Created on 09.03.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

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
import de.swingempire.fx.util.AbstractEditReport;
import de.swingempire.fx.util.EditableControl;
import de.swingempire.fx.util.StageLoader;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.util.Callback;
/**
 * 
 * Base tests that are same/similar to all cell types. Initially copied all from 
 * CellTest, then deleted all tests that are not listCell
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class AbstractCellTest<C extends Control, I extends IndexedCell> {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    /**
     * Must not fire null events.
     */
    @Test (expected = NullPointerException.class)
    public void testNullEventFire() {
        EditableControl control = createEditableControl();
        control.fireEvent(null);
    }
    /**
     * Cell.startEdit doesn't switch into editing if empty. That's
     * the case for a cell without view.
     * 
     * But: core bug - NPE on all TextFieldXXCell (not with base XXCell!)
     */
    @Test
    public void testNullControlOnStartEdit() {
        I cell = createTextFieldCellFactory().call(null);
        cell.startEdit();
        assertFalse("cell without control must not be editing", cell.isEditing());
    }
    
    /**
     * Test cancel with null control
     */
    @Test
    public void testNullControlOnCancelEdit() {
        I cell = createTextFieldCellFactory().call(null);
        cell.cancelEdit();
    }
    
    /**
     * Test cancel with null control
     */
    @Test
    public void testNullControlOnCommitEdit() {
        I cell = createTextFieldCellFactory().call(null);
        cell.commitEdit("dummy");
    }
    
    /**
     * 
     */
    @Test
    public void testEditStartOnCellTwice() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell = getCellAt(control, editIndex);
        AbstractEditReport report = createEditReport(control);
        // start edit on control
        cell.startEdit();
        // start again -> nothing changed, no event
        cell.startEdit();
        // test editEvent
        assertEquals("second start on same must not fire event", 1, report.getEditEventSize());
    }
    
    @Test
    public void testEditStartOnControlTwice() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        AbstractEditReport report = createEditReport(control);
        // start edit on control
        control.edit(editIndex);
        // working as expected because index unchanged -> no change fired
        control.edit(editIndex);
        // test editEvent
        assertEquals("second start on same must not fire event", 1, report.getEditEventSize());
    }
    

//---------------- test editEvents and cell/control state   
    
    @Test
    public void testNoCommitNotEditing() {
        fail("tbd: not implemented - what should go in here");
    }
    
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
    public void testEditChangeEditIndexOnControlReversed() {
        assertChangeEdit(1,  0);
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
    public void testEditChangeEditIndexOnControl() {
        assertChangeEdit(0, 1);
    }
    
    protected void assertChangeEdit(int editIndex, int secondEditIndex) {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        // initial editing index
        IndexedCell editingCell = getCellAt(control, editIndex);
        IndexedCell secondEditingCell = getCellAt(control, secondEditIndex);
        // start edit on control with initial editIndex
        control.edit(editIndex);
        assertTrue(editingCell.isEditing());
        assertEquals(editIndex, editingCell.getIndex());
        AbstractEditReport report = createEditReport(control);
        // switch editing to second
        control.edit(secondEditIndex);
//        LOG.info("" + report.getAllEditEventTexts("edit(0) -> edit(1): "));
        // test cell state
        assertFalse(editingCell.isEditing());
        assertEquals(editIndex, editingCell.getIndex());
        assertTrue(secondEditingCell.isEditing());
        assertEquals(secondEditIndex, secondEditingCell.getIndex());
        // test editEvent
        assertLastStartIndex(report, secondEditIndex, control.getTargetColumn());
        assertLastCancelIndex(report, editIndex, control.getTargetColumn());
        
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
    public void testEditCommitOnCell() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell = getCellAt(control, editIndex);
        // start edit on control
        control.edit(editIndex);
        AbstractEditReport report = createEditReport(control);
        // commit value on cell
        String editedValue = "edited";
        cell.commitEdit(editedValue);
        // test control state
        assertEquals(-1, control.getEditingIndex());
        assertValueAt(editIndex, editedValue, control);
//        assertEquals(editedValue, control.getItems().get(editIndex));
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        assertLastCommitIndex(report, editIndex, control.getTargetColumn(), editedValue);
    }
    
    /**
     * Test that nothing changed by a cancel if not editing. 
     */
    @Test
    public void testEditCancelNotEditing() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell = getCellAt(control, editIndex);
        Object value = cell.getItem();
        AbstractEditReport report = createEditReport(control);
        // cancel edit on cell
        cell.cancelEdit();
        // test control state
        assertEquals(-1, control.getEditingIndex());
        assertValueAt(editIndex, value, control);
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        assertEquals(0, report.getEditEventSize());
    }
    
    /**
     * Test that nothing changed by a commit if not editing. 
     */
    @Test
    public void testEditCommitNotEditing() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell = getCellAt(control, editIndex);
        Object value = cell.getItem();
        AbstractEditReport report = createEditReport(control);
        // commit value on cell
        String editedValue = "edited";
        cell.commitEdit(editedValue);
        // test control state
        assertEquals(-1, control.getEditingIndex());
        assertValueAt(editIndex, value, control);
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        assertEquals(0, report.getEditEventSize());
    }
    
    protected abstract void assertValueAt(int index, Object editedValue, EditableControl<C, I> control);
    
    /**
     * Here: cancel the edit with cell.cancelEdit ->
     * the cancel index is correct
     * ListView: EditEvent on cancel has incorrect index
     * 
     * reported: https://bugs.openjdk.java.net/browse/JDK-8187226
     * 
     */
    @Test
    public void testEditCancelOnCell() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell = getCellAt(control, editIndex);
        // start edit on control
        control.edit(editIndex);
        AbstractEditReport report = createEditReport(control);
        // cancel edit on cell
        cell.cancelEdit();
        // test cell state
        assertEquals(-1, control.getEditingIndex());
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        assertLastCancelIndex(report, editIndex, control.getTargetColumn());
    }
    
    /**
     * Here: cancel the edit with list.edit(-1)
     * ListView: EditEvent on cancel has incorrect index
     * 
     * reported: https://bugs.openjdk.java.net/browse/JDK-8187226
     * 
     */
    @Test
    public void testEditCancelOnControl() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell = getCellAt(control, editIndex);
        // start edit on control
        control.edit(editIndex);
        AbstractEditReport report = createEditReport(control);
        // cancel edit on control
        control.edit(-1);
        // test cell state
        assertFalse(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        assertLastCancelIndex(report, editIndex, control.getTargetColumn());
    }
    
    /**
     * Incorrect index in editStart event when edit started on cell
     * 
     * reported as
     * https://bugs.openjdk.java.net/browse/JDK-8187432
     * 
     */
    @Test
    public void testEditStartOnCell() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell = getCellAt(control, editIndex);
        AbstractEditReport report = createEditReport(control);
        // start edit on control
        cell.startEdit();
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        assertLastStartIndex(report, editIndex, control.getTargetColumn());
//        Optional<EditEvent> e = report.getLastEditStart();
//        assertEquals("index on start event", editIndex, e.get().getIndex());
    }
    
    
    @Test
    public void testEditStartOnControl() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell = getCellAt(control, editIndex);
        AbstractEditReport report = createEditReport(control);
        // start edit on control
        control.edit(editIndex);
        // test cell state
        assertTrue(cell.isEditing());
        assertEquals(editIndex, cell.getIndex());
        // test editEvent
        assertEquals(1, report.getEditEventSize());
        assertLastStartIndex(report, editIndex, control.getTargetColumn());
    }
    
    protected abstract void assertLastCancelIndex(AbstractEditReport report, int index, Object column);
    protected abstract void assertLastStartIndex(AbstractEditReport report, int index, Object column);
    protected abstract void assertLastCommitIndex(AbstractEditReport report, int index, Object target, Object value);

    /**
     * Test update of editing location on control
     */
    @Test
    public void testEditCommitOnCellResetEditingIndex() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell = getCellAt(control, editIndex);
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
    public void testEditCancelOnCellResetEditingIndex() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell = getCellAt(control, editIndex);
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
    public void testEditStartOnCellHasEditingIndex() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell =  getCellAt(control, editIndex);
        // start edit on cell
        cell.startEdit();
        // test editing location
        assertEquals("editingIndex must be updated", editIndex, control.getEditingIndex());
    }

    /**
     * @param control
     * @param editIndex
     * @return
     */
    protected IndexedCell getCellAt(EditableControl<C,I> control, int editIndex) {
        return getCell(control.getControl(), editIndex);
    }

//----------------------- focus state
    

// ------------------ test default edit handlers
    /**
     * Test default edit handlers: expected none for start/cancel,
     * default that commits
     * 
     * Here: List
     */
    @Test
    public void testEditHandler() {
        EditableControl control = createEditableControl();
        new StageLoader(control.getControl());
        assertNull(control.getOnEditCancel());
        assertNull(control.getOnEditStart());
        assertNotNull("listView must have default commit handler", control.getOnEditCommit());
    }
    
    
//------------ infrastructure methods

   
    protected abstract AbstractEditReport createEditReport(EditableControl control);
    protected abstract EditableControl<C, I> createEditableControl();
    
    protected abstract Callback<C, I> createTextFieldCellFactory();
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AbstractCellTest.class.getName());
}
