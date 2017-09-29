/*
 * Created on 29.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Optional;

import org.junit.Test;

import com.sun.xml.internal.ws.policy.AssertionValidationProcessor;

import static org.junit.Assert.*;

import de.swingempire.fx.scene.control.cell.AbstractCellTest.EditableControl;
import de.swingempire.fx.util.AbstractEditReport;
import de.swingempire.fx.util.StageLoader;
import de.swingempire.fx.util.TreeEditReport;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeView.EditEvent;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.control.skin.TreeCellSkin;
import javafx.util.Callback;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TrCellTest extends AbstractCellTest<TreeView, TreeCell> {

    @Test
    public void testCommitEditRespectHandler() {
        EditableControl<TreeView, TreeCell> control = createEditableControl();
        new StageLoader(control.getControl());
        int editIndex = 1;
        IndexedCell cell = getCellAt(control, editIndex);
        TreeItem<String> editItem = control.getControl().getTreeItem(editIndex);
        String oldValue = editItem.getValue();
        // do nothing
        control.setOnEditCommit(e -> new String("dummy"));
        // start edit on control
        control.edit(editIndex);
        AbstractEditReport report = createEditReport(control);
        String editedValue = "edited";
        // commit edit on cell
        cell.commitEdit(editedValue);
        // test data
        assertEquals("value must not be changed", oldValue, control.getControl().getTreeItem(editIndex).getValue());
        assertEquals(1, report.getEditEventSize());
    }
    
  //--------------------- old bugs, fixed in fx9    
    @Test
    public void testTreeCellSkin() {
        TreeCell cell = new TreeCell();
        cell.setSkin(new TreeCellSkin(cell));
    }

//----------------- implement super's assertions in terms of TreeView
    
    @Override
    protected void assertValueAt(int index, Object editedValue,
            EditableControl<TreeView, TreeCell> control) {
        TreeItem item = control.getControl().getTreeItem(index);
        assertEquals("value must be committed", editedValue, item.getValue());
    }

    @Override
    protected void assertLastCancelIndex(AbstractEditReport report, int index,
            Object column) {
        Optional<EditEvent> e = report.getLastEditCancel();
        assertTrue(e.isPresent());
        TreeItem item = e.get().getSource().getTreeItem(index);
        assertEquals(item, e.get().getTreeItem());
    }

    @Override
    protected void assertLastStartIndex(AbstractEditReport report, int index,
            Object column) {
        Optional<EditEvent> e = report.getLastEditStart();
        assertTrue(e.isPresent());
        TreeItem item = e.get().getSource().getTreeItem(index);
        assertEquals(item, e.get().getTreeItem());
    }

    @Override
    protected void assertLastCommitIndex(AbstractEditReport report, int index,
            Object target, Object value) {
        Optional<EditEvent> e = report.getLastEditCommit();
        assertTrue(e.isPresent());
        TreeItem item = e.get().getSource().getTreeItem(index);
        assertEquals(item, e.get().getTreeItem());
        
    }

    @Override
    protected AbstractEditReport createEditReport(EditableControl control) {
        return new TreeEditReport(control);
    }

    @Override
    protected EditableControl<TreeView, TreeCell> createEditableControl() {
        TreeItem rootItem = new TreeItem<>("root");
        rootItem.getChildren().addAll(
                new TreeItem<>("one"),
                new TreeItem<>("two"),
                new TreeItem<>("three")
                
                );
        ETreeView treeView = new ETreeView(rootItem);
        treeView.setShowRoot(false);
        treeView.setEditable(true);
        treeView.setCellFactory(createTextFieldCellFactory());
        return treeView;
    }

    @Override
    protected Callback<TreeView, TreeCell> createTextFieldCellFactory() {
        return e -> new TextFieldTreeCell();
    }

    public static class ETreeView extends TreeView 
         implements EditableControl<TreeView, TreeCell> {

        public ETreeView() {
            super();
        }

        public ETreeView(TreeItem root) {
            super(root);
        }

        @Override
        public <T extends Event> void addEditEventHandler(EventType<T> type,
                EventHandler<? super T> handler) {
            addEventHandler(type, handler);
            
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
        public EventType editAny() {
            return editAnyEvent();
        }

        @Override
        public TreeView getControl() {
            return this;
        }

        @Override
        public int getEditingIndex() {
            TreeItem item = getEditingItem();
            return getRow(item);
        }

        @Override
        public void edit(int index) {
            TreeItem item = getTreeItem(index);
            edit(item);
        }
        
    }
}
