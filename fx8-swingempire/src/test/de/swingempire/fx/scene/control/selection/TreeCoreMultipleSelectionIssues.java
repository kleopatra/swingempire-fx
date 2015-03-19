/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class TreeCoreMultipleSelectionIssues extends AbstractTreeMultipleSelectionIssues {
 //   MultipleSelectionIssues<TreeView, MultipleSelectionModel> {

    
    /**
     * PENDING JW: report after download u17!
     * 
     * Standalone for bug report: selectedItem not cleared on removing all
     * children.
     * 
     * Looks like https://javafx-jira.kenai.com/browse/RT-34725 null element in
     * selectedItems, selectedIndex at 0
     */
    @Test
    public void testSelectedOnClearItemsReport() {
        ObservableList content = FXCollections.observableArrayList("9-item",
                "8-item", "7-item", "6-item", "5-item", "4-item", "3-item",
                "2-item", "1-item");
        TreeItem root = new TreeItem("root");
        content.stream().forEach(
                item -> root.getChildren().add(new TreeItem(item)));
        TreeView view = new TreeView(root);
        root.setExpanded(true);
        view.setShowRoot(false);
        int index = 3;
        view.getSelectionModel().select(index);
        assertEquals("sanity: ", content.get(index), ((TreeItem) root
                .getChildren().get(index)).getValue());
        root.getChildren().clear();
        assertEquals("itemCount must be ", 0, view.getExpandedItemCount());
        if (view.getSelectionModel().getSelectedIndex() > -1) {
            // uncomment to see RT-34725
//             LOG.info("selectedItems at 0: " +
//             view.getSelectionModel().getSelectedItems().get(0)
//             + "\n + selectedIndices at 0: " +
//             view.getSelectionModel().getSelectedIndices().get(0) );
        }
        assertEquals("selectedItems must be empty", 0, 
                view.getSelectionModel().getSelectedItems().size());
        assertEquals("selectedIndices must be empty", 0, 
                view.getSelectionModel().getSelectedIndices().size());
        assertEquals("selectedItem must be cleared", null, 
                view.getSelectionModel().getSelectedItem());
        assertEquals("selectedIndex must be cleared", -1, 
                view.getSelectionModel().getSelectedIndex());
    }

    public TreeCoreMultipleSelectionIssues(boolean multiple) {
        super(multiple);
    }

    @Override
    protected TreeView createView(ObservableList items) {
        TreeItem root = createItem("root");
        root.getChildren().setAll(items);
        TreeView table = new TreeView(root);
        root.setExpanded(true);
        table.setShowRoot(false);
        MultipleSelectionModel model = table.getSelectionModel();
        assertEquals("sanity: test setup assumes that initial mode is single", 
                SelectionMode.SINGLE, model.getSelectionMode());
        checkMode(model);
        // PENDING JW: this is crude ... think of doing it elsewhere
        // the problem is to keep super blissfully unaware of possible modes
        assertEquals(multipleMode, model.getSelectionMode() == SelectionMode.MULTIPLE);
        return table;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeCoreMultipleSelectionIssues.class.getName());

    @Override
    protected TreeView createEmptyView() {
        TreeView tree = new TreeView();
        tree.setShowRoot(false);
        return tree;
    }
    
}
