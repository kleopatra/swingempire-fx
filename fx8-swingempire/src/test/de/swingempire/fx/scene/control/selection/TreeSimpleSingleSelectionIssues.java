/*
 * Created on 06.11.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeView;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.swingempire.fx.scene.control.tree.TreeItemX;
import static org.junit.Assert.*;

/**
 * Testing single selection api in TreeViewSelectionModel, for both selection modes.
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class TreeSimpleSingleSelectionIssues extends AbstractTreeSingleSelectionIssues {


    /**
     * @param multiple
     */
    public TreeSimpleSingleSelectionIssues(boolean multiple) {
        super(multiple);
    }

    
    @Override
    public void setUp() throws Exception {
        // JW: need more items for multipleSelection
        ObservableList content = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        items = createItems(content);
        view = createView(items);
        // complete override, need to handle focus here as well
        if (getFocusModel() != null) {
            getFocusModel().focus(-1);
        }
    }


    @Override
    protected TreeView createView(ObservableList items) {
        TreeItemX root = new TreeItemX("root");
        root.getChildren().setAll(items);
        TreeView table = new TreeView(root);
        table.setSelectionModel(new SimpleTreeSelectionModel<>(table));
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

    @Override
    protected TreeItemX createItem(Object item) {
        return new TreeItemX(item);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeSimpleSingleSelectionIssues.class.getName());
}
