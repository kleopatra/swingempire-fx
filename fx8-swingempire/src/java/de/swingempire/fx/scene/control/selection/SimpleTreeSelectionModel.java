/*
 * Created on 18.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.scene.control.FocusModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class SimpleTreeSelectionModel<T> extends AbstractSelectionModelBase<TreeItem<T>> {

    private TreeView<T> treeView;
    private TreeBasedSelectionHelper helper;

    /**
     * 
     */
    public SimpleTreeSelectionModel(TreeView<T> treeView) {
       this.treeView = treeView;
       controller = new TreeSelectionController<>(treeView);
        // PENDING JW: this is brittle: need to register _after_ controller!
       helper = new TreeBasedSelectionHelper(this, treeView);
    }
    
    @Override
    protected FocusModel<TreeItem<T>> getFocusModel() {
        return treeView.getFocusModel();
    }

    @Override
    protected void focus(int index) {
        if (getFocusModel() == null) return;
        getFocusModel().focus(index);
    }

    @Override
    protected int getFocusedIndex() {
        if (getFocusModel() == null) return -1;
        return getFocusModel().getFocusedIndex();
    }

}
