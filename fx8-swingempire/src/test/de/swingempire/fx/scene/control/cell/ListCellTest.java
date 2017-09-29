/*
 * Created on 29.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Optional;

import org.junit.Test;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;
import com.sun.javafx.tk.Toolkit;

import static de.swingempire.fx.util.VirtualFlowTestUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.scene.control.cell.EditIgnores.IgnoreStandalone;
import de.swingempire.fx.util.AbstractEditReport;
import de.swingempire.fx.util.ListEditReport;
import de.swingempire.fx.util.StageLoader;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.skin.ListCellSkin;
import javafx.util.Callback;

/**
 * core listView/cell test
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ListCellTest extends AbstractCellTest<ListView, ListCell> {

    @ConditionalIgnore (condition = IgnoreStandalone.class)
    @Test
    public void standaloneListEditStartOnBaseCellTwiceStandalone() {
        ListView<String> control = new ListView<>(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        control.setEditable(true);
//        control.setCellFactory(TextFieldListCell.forListView());
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell = getCell(control, editIndex);
        SimpleIntegerProperty counter = new SimpleIntegerProperty(0);
        control.addEventHandler(ListView.editStartEvent(), e -> counter.set(counter.get() + 1));
        
        // start edit on control
        cell.startEdit();
        // start again -> nothing changed, no event
        cell.startEdit();
        // test editEvent
        assertEquals("second start on same must not fire event", 1, counter.get());
    }
    
    /**
     * Must not fire editStartEvent if editing
     * reported: https://bugs.openjdk.java.net/browse/JDK-8188027
     */
    @ConditionalIgnore (condition = IgnoreStandalone.class)
    @Test
    public void standaloneListEditStartOnCellTwice() {
        ListView<String> control = new ListView<>(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        control.setEditable(true);
        control.setCellFactory(TextFieldListCell.forListView());
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell = getCell(control, editIndex);
        SimpleIntegerProperty counter = new SimpleIntegerProperty(0);
        control.addEventHandler(ListView.editStartEvent(), e -> counter.set(counter.get() + 1));
       
        // start edit on control
        cell.startEdit();
        // start again -> nothing changed, no event
        cell.startEdit();
        // test editEvent
        assertEquals("second start on same must not fire event", 1, counter.get());
    }
    
    /**
     * NPE on all TextFieldXXCell (not with base XXCell!)
     * reported: https://bugs.openjdk.java.net/browse/JDK-8188026
     */
    @ConditionalIgnore (condition = IgnoreStandalone.class)
    @Test
    public void standaloneTextFieldCellNullControlOnStartEdit() {
        ListCell cell = TextFieldListCell.forListView().call(null);
        cell.startEdit();
    }

    /**
     * NPE on all TextFieldXXCell (not with base XXCell!)
     */
    @ConditionalIgnore (condition = IgnoreStandalone.class)
    @Test
    public void standaloneBaseNullControlOnStartEdit() {
        ListCell cell = new ListCell();
        cell.startEdit();
    }

    /**
     * Incorrect index in editStart event when edit started on cell
     * 
     * reported as
     * https://bugs.openjdk.java.net/browse/JDK-8187432
     */
    @ConditionalIgnore (condition = IgnoreStandalone.class)
    @Test
    public void standaloneListEditStartOnCell() {
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

    /**
     * Experiments around focus
     */
    @Test
    public void testListEditStartFocus() {
        ListView<String> control = (ListView<String>) createEditableControl().getControl();
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
    

    @Override
    protected void assertValueAt(int index, Object editedValue, EditableControl<ListView, ListCell> control) {
        assertEquals(editedValue, control.getControl().getItems().get(index));
    };
    
    @Override
    protected void assertLastStartIndex(AbstractEditReport report, int index, Object target) {
        Optional<EditEvent> e = report.getLastEditStart();
        assertTrue(e.isPresent());
        assertEquals("index on start event", index, e.get().getIndex());
    }
    
    @Override
    protected void assertLastCancelIndex(AbstractEditReport report, int index, Object target) {
        Optional<EditEvent> e = report.getLastEditCancel();
        assertTrue(e.isPresent());
        assertEquals("index on cancel event", index, e.get().getIndex());
    }
    
    @Override
    protected void assertLastCommitIndex(AbstractEditReport report, int index, Object target, Object value) {
        Optional<EditEvent> commit = report.getLastEditCommit();
        assertTrue(commit.isPresent());
        assertEquals("index on commit event", index, commit.get().getIndex());
        assertEquals("newValue on commit event", value, commit.get().getNewValue());
        assertEquals("commit must fire a single event", 1, report.getEditEventSize());
    }

    //--------------------- old bugs, fixed in fx9    
    @Test
    public void testListCellSkinInit() {
        ListCell cell = new ListCell();
        cell.setSkin(new ListCellSkin(cell));
    }
    

    /**
     * Creates and returns an editable List configured with 4 items
     * and TextFieldListCell as cellFactory
     * 
     */
    @Override
    protected EditableControl createEditableControl() {
        EListView control = new EListView(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        control.setEditable(true);
        control.setCellFactory(createTextFieldCellFactory());
        return control;
    }

    @Override
    protected Callback<ListView, ListCell> createTextFieldCellFactory() {
        return e -> new TextFieldListCell();
//                (Callback<ListView, ListCell>)TextFieldListCell.forListView();
    }

    @Override
    protected AbstractEditReport createEditReport(EditableControl control) {
        return new ListEditReport(control);
    }

    public static class EListView extends ListView 
        implements EditableControl<ListView, ListCell> {

        
        @Override
        public ListView getControl() {
            return this;
        }

        public EListView() {
            super();
        }

        public EListView(ObservableList arg0) {
            super(arg0);
        }

        @Override
        public EventType editAny() {
            return editAnyEvent();
        }

        @Override
        public EventType editCommit() {
            return editCommitEvent();
        }

        @Override
        public EventType editCancel() {
            return editCancelEvent();
        }

        @Override
        public EventType editStart() {
            return editStartEvent();
        }

        @Override
        public <T extends Event> void addEditEventHandler(EventType<T> type,
                EventHandler<? super T> handler) {
            addEventHandler(type, handler);
        }
        
        
    }

}
