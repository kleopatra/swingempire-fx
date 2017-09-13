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
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;

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
     * Creates and returns an editable Tree configured with 3 child item,
     * hidden root
     * and DebugTextFieldTreeCell as cellFactory
     * 
     * Note: we also install a custom commit handler because super doesn't 
     * have any installed! 
     * 
     * @return
     */
    @Override
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
        treeView.setCellFactory(DebugTextFieldTreeCell.forTreeView());
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
    protected TableView<TableColumn> createEditableTable(boolean withExtractor) {
        ObservableList<TableColumn> items = withExtractor ? 
        FXCollections.observableArrayList(e -> new Observable[] {e.textProperty()}) : FXCollections.observableArrayList();
        items.addAll(new TableColumn("first"), new TableColumn("second"));
        TableView<TableColumn> table = new TableView<>(
                items);
        table.setEditable(true);

        TableColumn<TableColumn, String> first = new TableColumn<>("Text");
        first.setCellFactory(DebugTextFieldTableCell.forTableColumn());
        first.setCellValueFactory(new PropertyValueFactory<>("text"));

        table.getColumns().addAll(first);
        return table;
    }
    
    
    
    /**
     * Creates and returns an editable List configured with 4 items
     * and DebugTextFieldListCell as cellFactory
     * 
     */
    @Override
    protected ListView<String> createEditableList() {
        ListView<String> control = new ListView<>(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        control.setEditable(true);
        control.setCellFactory(DebugTextFieldListCell.forListView());
        return control;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DebugCellTest.class.getName());
}
