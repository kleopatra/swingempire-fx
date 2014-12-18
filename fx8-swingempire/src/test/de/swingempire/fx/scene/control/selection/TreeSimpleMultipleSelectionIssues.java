/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.swingempire.fx.scene.control.tree.TreeItemX;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class TreeSimpleMultipleSelectionIssues extends AbstractTreeMultipleSelectionIssues {

//extends MultipleSelectionIssues<TreeView, MultipleSelectionModel> {

    public TreeSimpleMultipleSelectionIssues(boolean multiple) {
        super(multiple);
    }

    @Override
    protected TreeView createView(ObservableList items) {
        TreeItem root = createItem("root");
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

    

    

//    @Override
//    protected void setSelectionModel(MultipleSelectionModel model) {
//        getView().setSelectionModel(model);
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeSimpleMultipleSelectionIssues.class.getName());
    
}
