/*
 * Created on 12.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import de.swingempire.fx.scene.control.ControlUtils;
import de.swingempire.fx.util.FXUtils;
import javafx.scene.control.ListCell;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugTreeCell<T> extends TreeCell<T> implements CellDecorator<T> {

    /** {@inheritDoc} */
    @Override public void startEdit() {
        if (isEditing()) return;

        final TreeView<T> tree = getTreeView();
        if (! isEditable() || (tree != null && ! tree.isEditable())) {
//            if (Logging.getControlsLogger().isLoggable(PlatformLogger.SEVERE)) {
//                Logging.getControlsLogger().severe(
//                    "Can not call TreeCell.startEdit() on this TreeCell, as it "
//                        + "is not allowed to enter its editing state (TreeCell: "
//                        + this + ", TreeView: " + tree + ").");
//            }
            return;
        }

        invokeUpdateItem(-1);

        // it makes sense to get the cell into its editing state before firing
        // the event to the TreeView below, so that's what we're doing here
        // by calling super.startEdit().
//        super.startEdit();
        cellStartEdit();
         // Inform the TreeView of the edit starting.
        if (tree != null) {
            tree.fireEvent(new TreeView.EditEvent<T>(tree,
                    TreeView.<T>editStartEvent(),
                    getTreeItem(),
                    getItem(),
                    null));

            // JW: missing update of tree"s editing cell when starting edit from cell
            tree.edit(getTreeItem());
            tree.requestFocus();
        }
    }

     /** {@inheritDoc} */
    @Override public void commitEdit(T newValue) {
        if (! isEditing()) return;
        final TreeItem<T> treeItem = getTreeItem();
        final TreeView<T> tree = getTreeView();
        if (tree != null) {
            // Inform the TreeView of the edit being ready to be committed.
            tree.fireEvent(new TreeView.EditEvent<T>(tree,
                    TreeView.<T>editCommitEvent(),
                    treeItem,
                    getItem(),
                    newValue));
        }

        // inform parent classes of the commit, so that they can switch us
        // out of the editing state.
        // This MUST come before the updateItem call below, otherwise it will
        // call cancelEdit(), resulting in both commit and cancel events being
        // fired (as identified in RT-29650)
//        super.commitEdit(newValue);
        cellCommitEdit(newValue);

        // update the item within this cell, so that it represents the new value
        if (treeItem != null) {
            treeItem.setValue(newValue);
            updateTreeItem(treeItem);
            updateItem(newValue, false);
        }

        if (tree != null) {
            // reset the editing item in the TreetView
            tree.edit(null);

            // request focus back onto the tree, only if the current focus
            // owner has the tree as a parent (otherwise the user might have
            // clicked out of the tree entirely and given focus to something else.
            // It would be rude of us to request it back again.
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(tree);
        }
    }

    /** {@inheritDoc} */
    @Override public void cancelEdit() {
        if (! isEditing()) return;

        TreeView<T> tree = getTreeView();

//        super.cancelEdit();
        cellCancelEdit();

        if (tree != null) {
            // reset the editing index on the TreeView
//            if (updateEditingIndex) tree.edit(null);
            if (resetListEditingIndexInCancel()) tree.edit(null);

            // request focus back onto the tree, only if the current focus
            // owner has the tree as a parent (otherwise the user might have
            // clicked out of the tree entirely and given focus to something else.
            // It would be rude of us to request it back again.
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(tree);

            tree.fireEvent(new TreeView.EditEvent<T>(tree,
                    TreeView.<T>editCancelEvent(),
                    getTreeItem(),
                    getItem(),
                    null));
        }
    }

//------------------- reflection acrobatics
    
    protected void invokeUpdateItem(int index) {
        FXUtils.invokeGetMethodValue(TreeCell.class, this, "updateItem", Integer.TYPE, index);
    }
    

    
    /**
     * Returns a flag indicating whether the list editingIndex should be 
     * reset in cancelEdit. Implemented to reflectively access super's
     * hidden field <code>updateEditingIndex</code>
     * @return
     */
    protected boolean resetListEditingIndexInCancel() {
        return (boolean) FXUtils.invokeGetFieldValue(ListCell.class, this, "updateEditingIndex");
    }

}
