/*
 * Created on 29.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import static de.swingempire.fx.util.VirtualFlowTestUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.util.EditableControl;
import de.swingempire.fx.util.OldTreeViewEditReport;
import de.swingempire.fx.util.StageLoader;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeView.EditEvent;
import javafx.util.Callback;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DebugTreeCellTest extends TreeCellTest {

    /**
     * Overridden to comment
     * This passes in custom cell.
     * 
     */
    @Override
    public void testCommitEditRespectHandler() {
        super.testCommitEditRespectHandler();
    }

    /**
     * Overridden to comment
     * 
     * This fails in custom cell because TreeView has no default commit handler-
     * so adding a custom commit handler in createEditableTree
     * 
     * Still fails, because receiving two events .. passes with "fix" of
     * ignoreCancel in DebugXX
     * 
     * @see #testTreeEditCommitOnCellEventCount()
     */
    @Override
    public void testEditCommitOnCell() {
        super.testEditCommitOnCell();
    }
    
    /**
     * Digging into failure due to 2 events fired: 
     * same reason as in ListView - skin cancels the edit on receiving tree modification
     */
    @Ignore
    @Test
    public void testTreeEditCommitOnCellEventCount() {
        ETreeView control = (ETreeView) createEditableControl();
         new StageLoader(control);
        int editIndex = 1;
        TreeItem editItem = control.getTreeItem(editIndex);
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on control
        control.edit(editItem);
        OldTreeViewEditReport report = new OldTreeViewEditReport(control);
        String editedValue = "edited";
        control.addEventHandler(TreeView.editCancelEvent(), e -> {
            new RuntimeException("who is calling?\n").printStackTrace();
        });
        // commit edit on cell
        cell.commitEdit(editedValue);
//        LOG.info(report.getAllEditEventTexts("after treecell commit with DebugTreeCell"));
        // test editEvent
        Optional<TreeView.EditEvent> commit = report.getLastEditCommit();
        assertTrue(commit.isPresent());
        assertEquals("index on commit event", editItem, commit.get().getTreeItem());
        assertEquals(1, report.getEditEventSize());
    }


  
    /**
     * {@inheritDoc} <p>
     * 
     * Overridden to install a custom default commit handler (core TreeView 
     * does not have one)
     * 
     * @return
     */
    @Override
    protected EditableControl<TreeView, TreeCell> createEditableControl() {
        EditableControl treeView = super.createEditableControl();
        treeView.setOnEditCommit(t -> {
            EditEvent e = (EditEvent) t;
            TreeItem editItem = e.getTreeItem();
            editItem.setValue(e.getNewValue());
        });
        return treeView;
    }

    @Override
    protected Callback createTextFieldCellFactory() {
        return e -> new DebugTextFieldTreeCell();
    }

}
