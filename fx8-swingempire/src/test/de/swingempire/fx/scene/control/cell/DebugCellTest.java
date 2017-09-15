/*
 * Created on 11.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static de.swingempire.fx.util.VirtualFlowTestUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.util.StageLoader;
import de.swingempire.fx.util.TreeViewEditReport;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.Callback;

/**
 * Use debugging cells instead of core cells.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DebugCellTest extends CellTest {
    
    
    /**
     * Overridden to comment
     * This passes in custom cell.
     * 
     */
    @Override
    public void testTreeEditRespectCommitHandler() {
        super.testTreeEditRespectCommitHandler();
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
    public void testTreeEditCommitOnCell() {
        super.testTreeEditCommitOnCell();
    }
    
    /**
     * Digging into failure due to 2 events fired: 
     * same reason as in ListView - skin cancels the edit on receiving tree modification
     */
    @Ignore
    @Test
    public void testTreeEditCommitOnCellEventCount() {
        TreeView<String> control = createEditableTree();
         new StageLoader(control);
        int editIndex = 1;
        TreeItem editItem = control.getTreeItem(editIndex);
        IndexedCell cell =  getCell(control, editIndex);
        // start edit on control
        control.edit(editItem);
        TreeViewEditReport report = new TreeViewEditReport(control);
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
     * Overridden to comment.
     * 
     * Fails with DebugListCell as well: fires 2 events just as core - 
     * can't do much about, its the cancel that comes from skin (cancels edit
     * on items change)
     */
    @Override
    public void testListEditCommitOnCell() {
        super.testListEditCommitOnCell();
    }

    /**
     * no-op here
     */
    @Override
    public void testListEditStartOnCellStandalone() {
        // TODO Auto-generated method stub
//        super.testListEditStartOnCellStandalone();
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
    protected TreeView<String> createEditableTree() {
        TreeView<String> treeView = super.createEditableTree();
        treeView.setOnEditCommit(e -> {
            TreeItem editItem = e.getTreeItem();
            editItem.setValue(e.getNewValue());
        });
        return treeView;
    }

    /**
     * @return
     */
    @Override
    protected Callback<TreeView<String>, TreeCell<String>> createTextFieldTreeCell() {
        return DebugTextFieldTreeCell.forTreeView();
    }

    @Override
    protected Callback<TableColumn<TableColumn, String>, TableCell<TableColumn, String>> createTextFieldTableCell() {
        return DebugTextFieldTableCell.forTableColumn();
    }

    @Override
    protected Callback<ListView<String>, ListCell<String>> createTextFieldListCell() {
        return DebugTextFieldListCell.forListView();
    }



    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DebugCellTest.class.getName());
}
