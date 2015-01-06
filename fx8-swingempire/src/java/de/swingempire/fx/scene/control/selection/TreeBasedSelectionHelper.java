/*
 * Created on 18.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.collections.ListChangeListener.Change;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.TreeView;
import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.scene.control.tree.TreeItemX.ExpandedDescendants;
import de.swingempire.fx.scene.control.tree.TreeModificationEventX;

/**
 * PENDING JW
 * <li> weak eventHandler?
 * <li> listen to root changes
 * <li> partly done: specify/implement behaviour of selection on collapsed node 
 *    selectedItem/Index is moved to the collapsing parent if somewhere under its
 *    subtree. 
 * <li> code cleanup: duplication of short-cuts for childrenChanged and treeModified
 *     extract 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeBasedSelectionHelper<T> {

    // PENDING JW: weakEventHandler?
    private EventHandler<TreeModificationEvent<T>> modificationListener = e -> treeModified(e);
    private AbstractSelectionModelBase<TreeItem<T>> selectionModel;
    private TreeView<T> treeView;

    public TreeBasedSelectionHelper(AbstractSelectionModelBase<TreeItem<T>> selectionModel, TreeView<T> tree) {
        this.selectionModel = selectionModel;
        this.treeView = tree;
        tree.getRoot().addEventHandler(TreeItem.treeNotificationEvent(), modificationListener);;
    }

    /**
     * @param e
     * @return
     */
    protected void treeModified(TreeModificationEvent<T> event) {
        if (!(event.getTreeItem() instanceof TreeItemX)) {
            throw new IllegalStateException("all treeItems must be of type TreeItemX but was " + event.getTreeItem());
        }
        TreeModificationEventX<T> ex = event instanceof TreeModificationEventX ? 
                (TreeModificationEventX<T>) event : null;
        TreeItemX<T> source = (TreeItemX<T>) event.getTreeItem(); 
        if (!TreeItemX.isVisible(source)) return;
        if (ex != null && ex.getChange() != null) {
            childrenChanged(source, ex.getChange());
        } else {
            treeItemModified(event);
        }
    }

    /**
     * @param source
     * @param change
     */
    private void childrenChanged(TreeItemX<T> source,
            Change<? extends TreeItem<T>> change) {
        int oldSelectedIndex = selectionModel.getSelectedIndex();
        TreeItem<T> oldSelectedItem = selectionModel.getSelectedItem();
        int oldFocus = selectionModel.getFocusedIndex();
        boolean sameFocus = oldFocus == oldSelectedIndex;
        
        // short-cut 1
        if (oldSelectedIndex < 0) {
            // no old selection, tree modifications couldn't have changed this
            // PENDING JW: handle uncontained selected (how probable is it?)
            // PENDING JW: handle focus
            if (!sameFocus) {
                updateFocus(source, change);
            }
            return;
        }
        
        // since here we had a selectedIndex/item pair that was contained in the list
        // oldSelectedIndex > -1 expected and oldSelectedItem != null
        // temporarily throw for sanity
        if (oldSelectedItem == null) 
            throw new IllegalStateException("selectedItem must not be null for index: " + oldSelectedIndex);
        if (oldSelectedIndex < 0) throw new IllegalStateException("expected positive selectedIndex");
        
        // short-cut 2: all empty (?)
        
        // short-cut 3: oldSelectedItem still in selectedItems
        // happens on all changes except a remove of the selected,
        // need to update index
        if (selectionModel.getSelectedItems().contains(oldSelectedItem)) {
            int indexInIndices = selectionModel.getSelectedItems().indexOf(oldSelectedItem);
            int sourceIndex = selectionModel.getSelectedIndices().get(indexInIndices);
            // PENDING JW: here's the only place where the listener
            // accesses private api - can do anything to remove?
            // wouldn't matter to use select, except for focus
            selectionModel.syncSingleSelectionState(sourceIndex, sameFocus);
            if (!sameFocus) {
               updateFocus(source, change); 
            }
//            select(sourceIndex);
            return;
        } 
        //-------------- end of short-cuts
        // selectedItem was removed: could have been a "true" remove
        // or a replace
        selectedItemChanged(source, change);

    }

    /**
     * This is called if the selectedItem had been contained in the backing
     * tree but no longer after the change. Could only be a removed, either
     * plain or as part of a replaced - doesn't make a difference (?).
     * <p>
     * We need to find the subtree it was contained in (now removed!) and 
     * select a sibling of the subtree or the source itself, if there are no
     * siblings.
     * 
     * @param source the treeItem sending the changed
     * @param change the list change
     */
    private void selectedItemChanged(TreeItemX<T> source,
            Change<? extends TreeItem<T>> c) {
        c.reset();
        while(c.next()) {
            if (c.wasRemoved()) {
                for (int i = 0; i < c.getRemovedSize(); i++) {
                    if (isInSubtree(c.getRemoved().get(i))) {
                        selectedItemRemoved(source, c);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 
     * @param source the parent of the change
     * @param c the change with its cursor placed on the removed 
     *     which contains the old selectedItem (either directly or 
     *     in a subtree
     */
    protected void selectedItemRemoved(TreeItemX<T> source,
            Change<? extends TreeItem<T>> c) {
        TreeItem<T> newSelectedItem;
        if (c.wasReplaced()) {
            // PENDING: what to do? here we select parent.. arbitrary
            newSelectedItem = source;
        } else if (c.wasRemoved()) {
            if (c.getList().isEmpty() ) {
                newSelectedItem = source;
            } else {
                int childIndex = Math.min(c.getFrom(), c.getList().size() - 1);
                newSelectedItem = c.getList().get(childIndex);
                
            }
        } else {
            throw new IllegalStateException("expected a remove, but was: " + c);
        }
        // JW: need special case source == root 
        //  it might not be visible in the tree
        if (newSelectedItem == treeView.getRoot() && !treeView.isShowRoot()) {
            newSelectedItem = null;
        }
        selectionModel.select(newSelectedItem);
    }

    /**
     * @param treeItem
     * @return
     */
    private boolean isInSubtree(TreeItem<T> treeItem) {
        TreeItem<T> oldSelectedItem = selectionModel.getSelectedItem();
        // short-cut for child itself
        if (oldSelectedItem == treeItem) return true;
        ExpandedDescendants<TreeItem> subtree = new ExpandedDescendants<TreeItem>(treeItem);
        while (subtree.hasNext()) {
            if (treeItem == subtree.next()) return true;
        }
        return false;
    }

    /**
     * Called if the modification is a modification of the item itself. Does
     * nothing for valueChanged/graphicChanged, updates selectedIndex/item
     * on collapsed/expanded. The sending item is guaranteed to be visible.
     * 
     * @param event
     */
    protected void treeItemModified(TreeModificationEvent<T> event) {
        if (event.getEventType() == TreeItem.childrenModificationEvent()) {
            throw new IllegalStateException("events of type childrenModified must be handled elsewhere " + event);
        }
        // nothing to do for valueChanged/graphicChanged ?
        if (event.getEventType() == TreeItem.valueChangedEvent() 
                || event.getEventType() == TreeItem.graphicChangedEvent()) return;
        
        TreeItemX<T> source = (TreeItemX<T>) event.getTreeItem();
        // getRow returns expected result only for visible items
        int from = treeView.getRow(source);
        // source hidden anyway or change completely after, nothing to do
        if (from < 0) {
            throw new IllegalStateException("weeded out hidden items before, "
                    + "something wrong if we get a negativ from " + event);
        }
        
        // similar short-cuts than for children modifications
        int oldSelectedIndex = selectionModel.getSelectedIndex();
        TreeItem<T> oldSelectedItem = selectionModel.getSelectedItem();
        int oldFocus = selectionModel.getFocusedIndex();
        boolean sameFocus = oldFocus == oldSelectedIndex;
        
        // short-cut 1
        if (oldSelectedIndex < 0) {
            // no old selection, tree modifications couldn't have changed this
            // PENDING JW: handle uncontained selected (how probable is it?)
            // PENDING JW: handle focus
            if (!sameFocus) {
//                updateFocus(source, change);
            }
            return;
        }
        
        // since here we had a selectedIndex/item pair that was contained in the list
        // oldSelectedIndex > -1 expected and oldSelectedItem != null
        // temporarily throw for sanity
        if (oldSelectedItem == null) 
            throw new IllegalStateException("selectedItem must not be null for index: " + oldSelectedIndex);
        if (oldSelectedIndex < 0) throw new IllegalStateException("expected positive selectedIndex");
        
        // short-cut 2: all empty (?)
        
        // short-cut 3: oldSelectedItem still in selectedItems
        // happens on all changes except a remove of the selected,
        // need to update index
        if (selectionModel.getSelectedItems().contains(oldSelectedItem)) {
            int indexInIndices = selectionModel.getSelectedItems().indexOf(oldSelectedItem);
            int sourceIndex = selectionModel.getSelectedIndices().get(indexInIndices);
            // PENDING JW: here's the only place where the listener
            // accesses private api - can do anything to remove?
            // wouldn't matter to use select, except for focus
            selectionModel.syncSingleSelectionState(sourceIndex, sameFocus);
            if (!sameFocus) {
//               updateFocus(source, change); 
            }
//            select(sourceIndex);
            return;
        } 
        
        // nothing to do, collapse/expand was below
        if (selectionModel.getSelectedIndex() <= from) return;
        
        if (event.wasCollapsed()) {
            collapsed(event);
        } else if (event.wasExpanded()) {
            throw new IllegalStateException("unexpected expanded - should have been handled in short-cuts " + event);
//            expanded(event);
        } 
    }


    /** 
     * Updates selectedIndex/item after a collapse. This is called only if
     * the selectedItem/Index was in a subtree of the sending treeItem.
     * Moves selection to the parent.
     * 
     * @param event
     */
    private void collapsed(TreeModificationEvent<T> event) {
        TreeItemX<T> source = (TreeItemX<T>) event.getTreeItem();
        // getRow returns expected result only for visible items
        int from = treeView.getRow(source);
        // source hidden anyway or change completely after, nothing to do
        if (from < 0) {
            throw new IllegalStateException("weeded out hidden items before, "
                    + "something wrong if we get a negativ from " + event);
        }
        selectionModel.select(from);
//        DebugUtils.printSelectionState(treeView);
        // nothing to do, expansion was below
//        if (selectionModel.getSelectedIndex() <= from) return;
        
    }

    /**
     * @param source
     * @param change
     */
    private void updateFocus(TreeItemX<T> source,
            Change<? extends TreeItem<T>> change) {
        // TODO Auto-generated method stub
        
    }

}
