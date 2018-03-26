/*
 * Created on 18.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.scene.control.tree.TreeItemX.ExpandedDescendants;
import de.swingempire.fx.scene.control.tree.TreeModificationEventX;
import javafx.collections.ListChangeListener.Change;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.TreeView;

/**
 * This class helps a (Abstract)MultipleSelectionModel to keep its single selection state 
 * sync'ed to its multiple selection state when the source list changes. It assumes 
 * that multiple selection state is completely update before seeing the change.
 * 
 * <p>
 * 
 * This helper can handle a TreeView as source. Its root has to be of type TreeItemX.
 * 
 * <p>
 * PENDING JW
 * <li> weak eventHandler?
 * <li> DONE but not yet properly tested: listen to root changes
 * <li> DONE handle showRoot
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
    /**
     * The default listener installed on backing list to monitor list changes.
     * Implemented to call treeModified.
     */
    protected EventHandler<TreeModificationEvent<T>> modificationListener = e -> treeModified(e);
    private AbstractSelectionModelBase<TreeItem<T>> selectionModel;
    private TreeView<T> treeView;

    public TreeBasedSelectionHelper(AbstractSelectionModelBase<TreeItem<T>> selectionModel, TreeView<T> tree) {
        this.selectionModel = selectionModel;
        this.treeView = tree;
        tree.showRootProperty().addListener((source, old, value) -> {
            showRootChanged(value);
        });
        rootChanged(null, tree.getRoot());
        tree.rootProperty().addListener((s, old, value) -> rootChanged(old, value));
        
    }

    /**
     * @param old
     * @param value
     * @return
     */
    protected void rootChanged(TreeItem<T> old, TreeItem<T> root) {
        if (old != null) {
            old.removeEventHandler(TreeItem.treeNotificationEvent(), modificationListener);
        }
        if (root != null) {
            root.addEventHandler(TreeItem.treeNotificationEvent(), modificationListener);
        }
        updateIndicesOnRootChanged();
    }

    /**
     * 
     */
    protected void updateIndicesOnRootChanged() {
        selectionModel.clearSelection();
    }

    protected void showRootChanged(Boolean value) {
        int oldSelectedIndex = selectionModel.getSelectedIndex();
        if (oldSelectedIndex < 0) return;
        int diff = value ? 1 : -1;
        int toSelect = Math.max(0, oldSelectedIndex + diff);
        selectionModel.select(toSelect);
    }

    /**
     * This method is called when the listener on the tree's item received a TreeModificationEvent
     * from the tree's root. Implemented to dispatch the event or its list change (as appropriate)
     * to either treeItemChanged or childrenChanged, respectively. Does nothing if the sender
     * is not visible.
     * 
     * @param e the event received from the tree's root.
     * @throws IllegalStateException if the sender is not of type TreeItemX.
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
     * Called if the treeModification was caused by a change of a child list of 
     * a visible treeItem.
     * 
     * @param source the sending TreeItem
     * @param change the list change
     */
    protected void childrenChanged(TreeItemX<T> source,
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
     * Replaced or removed does make a difference if we want to keep the 
     * selectedIndex unchanged on setting the item!
     * <p>
     * We need to find the subtree it was contained in (now removed!) and 
     * select a sibling of the subtree or the source itself, if there are no
     * siblings.
     * 
     * @param source the treeItem sending the changed
     * @param change the list change
     */
    protected void selectedItemChanged(TreeItemX<T> source,
            Change<? extends TreeItem<T>> c) {
        TreeItem<T> oldSelectedItem = selectionModel.getSelectedItem();
        boolean found = false;
        c.reset();
        while(c.next()) {
            if (c.wasReplaced()) {
                for (int i = 0; i < c.getRemovedSize(); i++) {
                    if (isInSubtree(c.getRemoved().get(i))) {
                        selectedItemReplaced(source, c);
                        if (found) 
                            throw new IllegalStateException("old item found in more than one subchange " + c);
                        found = true;
                        break;
                    }
                }
                
            } else if (c.wasRemoved()) {
                for (int i = 0; i < c.getRemovedSize(); i++) {
                    if (isInSubtree(c.getRemoved().get(i))) {
                        if (found) 
                            throw new IllegalStateException("old item found in more than one subchange " + c);
                        found = true;
                        selectedItemRemoved(source, c);
                        break;
                    }
                }
            }
        }
    }

    protected void selectedItemReplaced(TreeItemX<T> source,
            Change<? extends TreeItem<T>> c) {
        int treeFrom = treeView.getRow(source) + 1 + c.getFrom();
        if (c.getRemovedSize() == 1 && c.getAddedSize() == 1) {
            // single replace (not entirely safe, could be a 
            // setAll with a single new element
            selectionModel.select(treeFrom);
        } 
//        else if (c.getAddedSize() == c.getList().size()) {
//            // all changed
//            selectionModel.clearSelection();
//        }
        
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
            throw new IllegalStateException("expected a raw removed but got: " + c);
            // PENDING: what to do? here we select parent.. arbitrary
//            newSelectedItem = source;
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
        ExpandedDescendants<TreeItem<T>> subtree = new ExpandedDescendants<TreeItem<T>>(treeItem);
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
        
        // similar short-cuts as for children modifications
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
    protected void collapsed(TreeModificationEvent<T> event) {
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
     * Called if focus hasn't been handled implicitly by synching selection.
     * 
     * PENDING: not yet implemented.
     * 
     * @param source the sender of the event
     * @param change the change of children's list
     */
    protected void updateFocus(TreeItemX<T> source,
            Change<? extends TreeItem<T>> change) {
        // TODO Auto-generated method stub
        
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeBasedSelectionHelper.class.getName());
}
