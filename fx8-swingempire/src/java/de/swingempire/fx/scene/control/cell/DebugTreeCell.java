/*
 * Created on 12.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import de.swingempire.fx.scene.control.ControlUtils;
import de.swingempire.fx.util.FXUtils;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Custom TreeCell which overrides start/commit/cancelEdit and
 * takes over completely (c&p from super and reflective access to Cell methods).
 * This is (mostly) done to understand the editing mechanism. 
 * <p>
 * 
 * Bug fixes:
 * <ul>
 * <li> https://bugs.openjdk.java.net/browse/JDK-8187474 -
 *      in startEdit update editingItem of TreeView,
 * <li> https://bugs.openjdk.java.net/browse/JDK-8187309
 *      in commitEdit, don't change item value (that's the task of a commitHandler)
 * <li> not yet reported, but same problem as in ListCell, 
 *      skin cancels edit during a commit - happens on TreeView if
 *      commitEdit behaves correctly and leaves the data update to the handler
 *      see DebugListCell for details    
 * </ul>
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugTreeCell<T> extends TreeCell<T> implements CellDecorator<T> {

    private boolean ignoreCancel;

    /** 
     * {@inheritDoc} <p>
     * 
     * Basically, a c&p of super except:
     * 
     * <ul>
     * <li> update editingItem of TreeView, 
     *  fix https://bugs.openjdk.java.net/browse/JDK-8187474
     * </ul>
     */
    @Override 
    public void startEdit() {
        if (isEditing()) return;

        final TreeView<T> tree = getTreeView();
        if (! isEditable() || (tree != null && ! tree.isEditable())) {
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

    /** 
     * {@inheritDoc} 
     * Basically, a c&p of super except:
     * 
     * <ul>
     * <li> surround firing of editCommitEvent with ignoreCancel
     * </ul>
     * 
     * @see DebugListCell#commitEdit(Object)
     */
    @Override 
    public void commitEdit(T newValue) {
        if (! isEditing()) return;
        final TreeItem<T> treeItem = getTreeItem();
        final TreeView<T> tree = getTreeView();
        if (tree != null) {
            // experiment around commit-fires-cancel:
            // surround with ignore-cancel to not react if skin
            // cancels our edit due to data change triggered by this commit
            ignoreCancel = true;
            // Inform the TreeView of the edit being ready to be committed.
            tree.fireEvent(new TreeView.EditEvent<T>(tree,
                    TreeView.<T>editCommitEvent(),
                    treeItem,
                    getItem(),
                    newValue));
            ignoreCancel = false;
        }

        // inform parent classes of the commit, so that they can switch us
        // out of the editing state.
        // This MUST come before the updateItem call below, otherwise it will
        // call cancelEdit(), resulting in both commit and cancel events being
        // fired (as identified in RT-29650)
//        super.commitEdit(newValue);
        cellCommitEdit(newValue);

        // update the item within this cell, so that it represents the new value
        // JW: no, a cell must not change the data itself, but rely on a commitHandler
        // to commit them 
//        if (treeItem != null) {
//            treeItem.setValue(newValue);
//            updateTreeItem(treeItem);
//            updateItem(newValue, false);
//        }

        // PENDING JW: probably regrab tree's value? otherwise we
        // show incorrect data if handler rejects the edit
        // https://bugs.openjdk.java.net/browse/JDK-8187314  
        updateItem(newValue, false);
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

    /** 
     * {@inheritDoc} 
     * Basically, a c&p of super except:
     * 
     * <ul>
     * <li> do nothing if ignoreCancel
     * </ul>
     * 
     * @see DebugListCell#commitEdit(Object)
     */
    @Override public void cancelEdit() {
        if (ignoreCancel()) return;

        TreeView<T> tree = getTreeView();

//        super.cancelEdit();
        cellCancelEdit();

        if (tree != null) {
            // reset the editing index on the TreeView
//            if (updateEditingIndex) tree.edit(null);
            if (resetListEditingIndexInCancel()) {
                tree.edit(null);
            }

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

    protected boolean ignoreCancel() {
        return !isEditing() || ignoreCancel;
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
        return (boolean) FXUtils.invokeGetFieldValue(TreeCell.class, this, "updateEditingIndex");
    }

}
