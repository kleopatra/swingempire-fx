/*
 * Created on 29.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static de.swingempire.fx.util.VirtualFlowTestUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.util.AbstractEditReport;
import de.swingempire.fx.util.EditableControl;
import de.swingempire.fx.util.StageLoader;
import de.swingempire.fx.util.TableViewEditReport;
import de.swingempire.fx.util.OldTableViewEditReport;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.IndexedCell;
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
 * core tableView/cell test
 * 
 * moved cellSelection and extractor testing into this, not applicable in abstract layer.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(Parameterized.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TableCellTest extends AbstractCellTest<TableView, TableCell> {

    protected boolean cellSelectionEnabled; 
    
    @Parameters(name = "{index} - cell {0}")
    public static Collection selectionModes() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    /**
     * 
     */
    public TableCellTest(boolean cellSelection) {
        this.cellSelectionEnabled = cellSelection;
    }
    
    @Test
    public void testTableEditCommitCellSelection() {
        ETableView control = (ETableView) createEditableControl(true);
        TableColumn<TableColumn, String> column = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        assertEquals(cellSelectionEnabled, control.getSelectionModel().isCellSelectionEnabled());
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        // start edit on control
        control.edit(editIndex, column);
        AbstractEditReport report = createEditReport(control);
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
        ETableView control = (ETableView) createEditableControl(true);
        TableColumn<TableColumn, String> column = (TableColumn<TableColumn, String>) control.getColumns().get(0);
        new StageLoader(control);
        int editIndex = 1;
        IndexedCell cell =  getCell(control, editIndex, 0);
        // start edit on control
        control.edit(editIndex, column);;
        AbstractEditReport report = createEditReport(control);
        String editedValue = "edited";
        cell.commitEdit(editedValue);
        assertEquals("tableCell must fire a single event", 1, report.getEditEventSize());
    }

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
    

    @Override
    protected void assertValueAt(int index, Object editedValue,
            EditableControl<TableView, TableCell> control) {
        ETableView table = (ETableView) control;
        TableColumn column = table.getTargetColumn();
        assertEquals("editedValue must be committed", editedValue, 
                column.getCellObservableValue(index).getValue());
    }
    
    @Override
    protected void assertLastStartIndex(AbstractEditReport report, int index, Object first) {
        Optional<CellEditEvent> e = report.getLastEditStart();
        assertTrue(e.isPresent());
//        LOG.info("what do we get?" + report.getEditText(e.get()));
        assertNotNull("position on event must not be null", e.get().getTablePosition());
        assertEquals("index on start event", index, e.get().getTablePosition().getRow());
        assertEquals("column on start event", first, e.get().getTablePosition().getTableColumn());
        
    }
    
    @Override
    protected void assertLastCancelIndex(AbstractEditReport report, int index, Object first) {
        Optional<CellEditEvent> e = report.getLastEditCancel();
        assertTrue(e.isPresent());
//        LOG.info("what do we get?" + report.getEditText(e.get()));
        assertNotNull("position on event must not be null", e.get().getTablePosition());
        assertEquals("index on cancel event", index, e.get().getTablePosition().getRow());
        assertEquals("column on cancel event", first, e.get().getTablePosition().getTableColumn());
        
    }
    
    @Override
    protected void assertLastCommitIndex(AbstractEditReport report, int index, Object first, Object value) {
        Optional<CellEditEvent> e = report.getLastEditCommit();
        assertTrue(e.isPresent());
//        LOG.info("what do we get?" + report.getEditText(e.get()));
        assertNotNull("position on event must not be null", e.get().getTablePosition());
        assertEquals("index on commit event", index, e.get().getTablePosition().getRow());
        assertEquals("column on commit event", first, e.get().getTablePosition().getTableColumn());
        assertEquals("new value on commit event", value, e.get().getNewValue());
    }


    @Override
    protected IndexedCell getCellAt(
            EditableControl<TableView, TableCell> control, int editIndex) {
        return getCell(control.getControl(), editIndex, 0);
    }



    /**
     * Creates and returns an editable Table of TableColumns (as items ;)
     * configured with 3 items
     * and TextFieldTableCell as cellFactory on first column (which represents
     * the textProperty of a TableColumn, no extractor, cellSelectionEnabled
     * as defined by parameter.
     * 
     * @return
     */
    @Override
    protected EditableControl<TableView, TableCell> createEditableControl() {
        return createEditableControl(false);
    }
    
    /**
      * Creates and returns an editable Table of TableColumns (as items ;)
     * configured with 3 items
     * and TextFieldTableCell as cellFactory on first column (which represents
     * the textProperty of a TableColumn, extractor as requested cellSelectionEnabled
     * as defined by parameter.
     * 
     * 
     * @param withExtractor
     * @return
     */
    protected EditableControl<TableView, TableCell> createEditableControl(
            boolean withExtractor) {

        ObservableList<TableColumn> items = withExtractor
                ? FXCollections.observableArrayList(
                        e -> new Observable[] { e.textProperty() })
                : FXCollections.observableArrayList();
        items.addAll(new TableColumn("first"), new TableColumn("second"),
                new TableColumn("third"));
        ETableView table = new ETableView(items);
        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(cellSelectionEnabled);

        TableColumn<TableColumn, String> first = new TableColumn<>("Text");
        first.setCellFactory(createTextFieldCellFactory());
        first.setCellValueFactory(new PropertyValueFactory<>("text"));

        table.getColumns().addAll(first);
        return table;
    }

    @Override
    protected Callback createTextFieldCellFactory() {
        return e -> new TextFieldTableCell();
//                (Callback<ListView, ListCell>)TextFieldListCell.forListView();
    }

    @Override
    protected AbstractEditReport createEditReport(EditableControl control) {
        return new TableViewEditReport(control);
    }

    /**
     * A TableView decorated as EditableControl. Note that the table
     * must be instantiated with at least one column and all
     * column related edit api is passed to the target column.
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class ETableView extends TableView
         implements EditableControl<TableView, TableCell> {

        public ETableView() {
            super();
        }

        public ETableView(ObservableList items) {
            super(items);
        }

        @Override
        public void setCellFactory(Callback<TableView, TableCell> factory) {
            getTargetColumn().setCellFactory(factory);
        }

        @Override
        public TableColumn getTargetColumn() {
            return (TableColumn) getColumns().get(0);
        }

        
//        @Override
//        public Object getValueAt(int index) {
//            TableColumn column = getTargetColumn();
//            return column.getCellObservableValue(index);
//        }

        @Override
        public EventHandler getOnEditCommit() {
            return getTargetColumn().getOnEditCommit();
        }

        @Override
        public EventHandler getOnEditCancel() {
            return getTargetColumn().getOnEditCancel();
        }

        @Override
        public EventHandler getOnEditStart() {
            return getTargetColumn().getOnEditStart();
        }

        @Override
        public void setOnEditCommit(EventHandler handler) {
            getTargetColumn().setOnEditCommit(handler);
        }

        @Override
        public void setOnEditCancel(EventHandler handler) {
            getTargetColumn().setOnEditCancel(handler);
        }

        @Override
        public void setOnEditStart(EventHandler handler) {
            getTargetColumn().setOnEditStart(handler);
        }

        @Override
        public EventType editCommit() {
            return TableColumn.editCommitEvent();
        }

        @Override
        public EventType editCancel() {
            return TableColumn.editCancelEvent();
        }

        @Override
        public EventType editStart() {
            return TableColumn.editStartEvent();
        }

        @Override
        public EventType editAny() {
            return TableColumn.editAnyEvent();
        }

        @Override
        public TableView getControl() {
            return this;
        }

        @Override
        public int getEditingIndex() {
            TablePosition pos = getEditingCell();
            return pos != null ? pos.getRow() : -1;
        }

        @Override
        public void edit(int index) {
            TableColumn column = index < 0 ? null : getTargetColumn();
            edit(index, column);
        }

        @Override
        public <T extends Event> void addEditEventHandler(EventType<T> type,
                EventHandler<? super T> handler) {
            getTargetColumn().addEventHandler(type, handler);
        }
        
    }
}