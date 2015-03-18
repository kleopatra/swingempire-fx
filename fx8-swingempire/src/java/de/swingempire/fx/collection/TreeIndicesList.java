/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.TreeView;
import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.scene.control.tree.TreeModificationEventX;

/**
 * Trying a tree-backed indicesList. The indices peek into the visible
 * rows of a tree. Listening to root item TreeModificationEvent and
 * updates its own indices as appropriate.
 * 
 * <p> 
 * 
 * PENDING JW: 
 * <li> root changes 
 * <li> changes to showRoot property not handled     
 * <li> weakEventHandler?
 * <li> core (OS) behaviour is to select the parent if a child has been selected
 *      when the branch is collapsed - implement here? More than the usual 
 *      bitSet-only behaviour, but feels natural enough? But can't without
 *      knowing whether to add (multiple mode) or set (single mode) the index
 * 
 * @see IndicesList
 * @see TreeIndexMappedList
 * @see IndicesBase
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeIndicesList<T> extends IndicesBase<T> {

    private TreeView<T> tree;
    // the tree's root item
    private TreeItemX<T> root;
    
    private EventHandler<TreeModificationEvent<T>> modificationListener = e -> treeModified(e);

    private ObjectProperty<TreeModificationEvent<T>> treeModificationP = 
            new SimpleObjectProperty<>(this, "treeModification");

    /**
     * The state of this before handling changes received from our backing structure.
     * We seem to need this because the using indexMappedList receives the
     * change from the backing structure after this has updated itself.
     * Wheezy ... need to do better
     */
    protected List<Integer> oldIndices;


    /**
     * TreeView must have treeItems of type TreeItemX. That's the only one firing an
     * extended TreeModificationEventX: without access to the children's change, 
     * this class can't do its job properly. Here we check if the root has the
     * correct type. Can throw an IllegalStateException later if not receiving
     * the expected event type.
     * 
     * @param tree
     */
    public TreeIndicesList(TreeView<T> tree) {
        this.tree = Objects.requireNonNull(tree, "tree must not be null");
        if (!(tree.getRoot() instanceof TreeItemX)) {
            throw new IllegalArgumentException("expected extended TreeItemX but was:" + tree.getRoot() );
        }
        this.root = (TreeItemX<T>) tree.getRoot();
        bitSet = new BitSet();
        root.addEventHandler(TreeItem.treeNotificationEvent(), modificationListener);
        tree.showRootProperty().addListener((source, old, value) -> {
            showRootChanged(value);
        });
    }

    private Property<Boolean> showRoot = new SimpleObjectProperty<Boolean>(this, "showRoot");
    
    protected void setShowRoot(Boolean showing) {
        showRootProperty().setValue(showing);
    }
    
    public Boolean getShowRoot() {
        return showRootProperty().getValue();
    }
    public Property<Boolean> showRootProperty() {
        return showRoot;
    }
    /**
     * Called when the tree's showRoot property changed. 
     * Not good enough in itself: at least the selectionHelper must be
     * implemented to cope! 
     *  
     * @param value
     */
    protected void showRootChanged(Boolean value) {
        beginChange();
        if (value) {
            doShiftRight(0, 1);
        } else {
            // JW: keep it simple - this class shouldn't
            // know anything about singleSelection state
            // so we treat a removed selected root the same
            // way as any other removed selected node
            // that is remove from our list and let 
            // more appropriate collaborators reselect if needed
            doClearIndicesInRange(0, 1);
            doShiftLeft(0, 1);
        }
        setShowRoot(value);
        endChange();
        setShowRoot(null);
    }
    
    
    
//------------------- internal update on tree modification

    /**
     * Implementation notes: 
     * 
     * Does nothing if the sending treeItem isn't visible.
     * 
     * Delegates the event either to childrenChanged or treeItemModified for
     * further processing. 
     * 
     * Guarantees to reset treeModification: either null if event not handled,
     * or the event as received.
     * 
     * 
     * @param event
     * @throws IllegalStateException if source of event not of type TreeItemX. 
     */
    protected void treeModified(TreeModificationEvent<T> event) {
        if (!(event.getTreeItem() instanceof TreeItemX)) {
            throw new IllegalStateException("all treeItems must be of type TreeItemX but was " + event.getTreeItem());
        }
        TreeModificationEventX<T> ex = event instanceof TreeModificationEventX ? 
                (TreeModificationEventX<T>) event : null;
        TreeItemX<T> source = (TreeItemX<T>) event.getTreeItem(); 
        if (!TreeItemX.isVisible(source)) return;
        beginChange();
        // doooh .... need old state for the sake of IndexedItems
        oldIndices = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            oldIndices.add(get(i));
        }
        if (ex != null && ex.getChange() != null) {
            childrenChanged(source, ex.getChange());
        } else {
            treeItemModified(event);
        }
        setTreeModification(event);
        endChange();
        setTreeModification(null);
    }
    
    /**
     * Called to handle modifications events other than childrenModified.
     * @param event
     */
    protected void treeItemModified(TreeModificationEvent<T> event) {
        if (event.getEventType() == TreeItem.childrenModificationEvent()) {
            throw new IllegalStateException("events of type childrenModified must be handled elsewhere " + event);
        }
        if (event.wasCollapsed()) {
            collapsed(event);
        } else if (event.wasExpanded()) {
            expanded(event);
        } else if (event.getEventType() == TreeItem.valueChangedEvent()) {
            // nothing to do for indices, the index is unchanged
            // throw new UnsupportedOperationException("TBD - valueChanged " + event);
        } else if (event.getEventType() == TreeItem.graphicChangedEvent()) {
            // nothing to do for indices, the index is unchanged
            // throw new UnsupportedOperationException("TBD - valueChanged " + event);
        }
    }

    /**
     * @param event
     */
    protected void expanded(TreeModificationEvent<T> event) {
        TreeItemX<T> source = (TreeItemX<T>) event.getTreeItem();
        // getRow returns expected result only for visible items
        int from = tree.getRow(source);
        // source hidden anyway or change completely after, nothing to do
        if (from < 0) {
            throw new IllegalStateException("weeded out hidden items before, "
                    + "something wrong if we get a negativ from " + event);
        }
        if (from < 0 || bitSet.nextSetBit(from) < 0) return;
        // account for the item itself
        from++;
        // current expanded count was added 
        int addedSize = source.getExpandedDescendantCount() - 1;
        // increase indices
        doShiftRight(from, addedSize);
    }

    /**
     * Called for events of type branchCollapsed.
     * @param event
     */
    protected void collapsed(TreeModificationEvent<T> event) {
        TreeItemX<T> source = (TreeItemX<T>) event.getTreeItem();
        // getRow returns expected result only for visible items
        int from = tree.getRow(source);
        // source hidden anyway or change completely after, nothing to do
        if (from < 0) {
            throw new IllegalStateException("weeded out hidden items before, "
                    + "something wrong if we get a negativ from " + event);
        }
        if (bitSet.nextSetBit(from) < 0) return;
        int row = from;
        // account for the item itself
        from++;
        int removedSize = getExpandedChildCount(source);
        // step one: remove the indices inside the range
        // PENDING JW: decrease removed by one? maybe not, as we query the
        // children (vs. the source), such that the source itself is not 
        // counted
        boolean hadSetBits = doClearIndicesInRange(from, removedSize);
        // step two: shift indices below the range
        doShiftLeft(from, removedSize);
//        if (hadSetBits) doAddIndices(row);
    }

    /**
     * Returns the sum of the expandedDescandantCount of the item's children.
     * We loop its children
     * and sum up their expandedItemCount.
     * Called after receiving a collapsed from parent. 
     * @param parent
     * @return
     */
    protected int getExpandedChildCount(TreeItemX<T> parent) {
        int result = 0;
        for (TreeItem<T> child : parent.getChildren()) {
            result += ((TreeItemX<T>) child).getExpandedDescendantCount();
        }
        return result;
    }

    /**
     * Note: 
     * <li> need source TreeItem for adjusting from to pos in tree
     * <li> in implementing methods with change of count must use
     *   descendantCount vs bare added/removedSize
     * 
     * @param c
     */
    protected void childrenChanged(TreeItemX<T> source, Change<? extends TreeItem<T>> c) {
        if (!source.isExpanded() || !TreeItemX.isVisible(source)) return;
        while (c.next()) {
            if (c.wasPermutated()) {
                throw new UnsupportedOperationException("TBD - permutation " + c);
//                permutated(c);
            } else if (c.wasUpdated()) {
                // don't expect a modification on the TreeItem itself, should
                // be notified via TreeModification.value/graphicChanged?
                throw new UnsupportedOperationException("unexpected update on children of treeItem: " 
                        + source + " / " + c);
//                updated(c);
            } else if (c.wasReplaced()) {
                replaced(source, c);
            } else {
                addedOrRemoved(source, c);
            }
        }
    }
    
    /**
     * Implements internal update for separate add/remove from backing list.
     * This method is called if we got a treeModification with change != null
     * and source is both visible and expanded.
     * 
     * PENDING JW: think about set (aka: replace) 
     * 
     * PENDING JW: need to use expandedDescendentCount vs. bare added/removedSize
     * @param c
     */
    private void addedOrRemoved(TreeItemX<T> source, Change<? extends TreeItem<T>> c) {
        int from = tree.getRow(source) + 1 + c.getFrom();
        // change completely after return
        if (bitSet.nextSetBit(from) < 0) return;

        if (c.wasAdded() && c.wasRemoved()) 
            throw new IllegalStateException("expected real add/remove but was: " + c);
        if (c.wasAdded()) {
            added(source, c);
        } else if (c.wasRemoved()) {
            removed(source, c);
        } else {
            throw new IllegalStateException("what have we got here? " + c);
        }
    }

    /**
     * Updates indices after receiving children have been added to the given
     * treeItem. The source is expanded and visible.
     * 
     * @param source 
     * @param c
     */
    private void added(TreeItemX<T> source, Change<? extends TreeItem<T>> c) {
        int from = tree.getRow(source) + 1 + c.getFrom();
        // added: values that are after the added index must be increased by addedSize
        // need to calculate the added size from the expandedCount of all
        // added children
        int addedSize = 0;
        for (TreeItem<T> item : c.getAddedSubList()) {
            addedSize += ((TreeItemX<T>) item).getExpandedDescendantCount();
        }
        doShiftRight(from, addedSize);
    }

    /**
     * Updates indices after receiving children have been removed from the given
     * treeItem. The source is expanded and visible.
     * 
     * @param source 
     * @param c
     */
    private void removed(TreeItemX<T> source, Change<? extends TreeItem<T>> c) {
        int from = tree.getRow(source) + 1 + c.getFrom();
        // removed is two-step:
        // if any of the values that are mapped to indices, is removed remove the index
        // for all left over indices after the remove, decrease the value by removedSize (?)
        int removedSize = 0;
        for (TreeItem<T> item : c.getRemoved()) {
            removedSize += ((TreeItemX<T>) item).getExpandedDescendantCount();
        }
        doClearIndicesInRange(from, removedSize);
        // step 2
        doShiftLeft(from, removedSize);
    }

    private void replaced(TreeItemX<T> source, Change<? extends TreeItem<T>> c) {
        int treeFrom = tree.getRow(source) + 1 + c.getFrom();
        int removedSize = 0;
        for (TreeItem<T> item : c.getRemoved()) {
            removedSize += ((TreeItemX<T>) item).getExpandedDescendantCount();
        }
        int addedSize = 0;
        for (TreeItem<T> item : c.getAddedSubList()) {
            addedSize += ((TreeItemX<T>) item).getExpandedDescendantCount();
        }
        // handle special case of "real" replaced, often size == 1
        // PENDING JW: no special handling of single replaced for a tree?
        // might effect a complete subtree 
//        if (removedSize == 1 && addedSize == 1) {
//            // single replace nothing to do, assume
//            return;
//        }
//        if (c.getAddedSize() == 1 && c.getAddedSize() == c.getRemovedSize()) {
//            for (int i = bitSet.nextSetBit(c.getFrom()); i >= 0 && i < c.getTo(); i = bitSet.nextSetBit(i+1)) {
//                int pos = indexOf(i);
//                nextSet(pos, i);
//            }
//            return;
//        }

        doClearIndicesInRange(treeFrom, removedSize);
        int diff = addedSize - removedSize;
        if (diff < 0) {
            doShiftLeft(treeFrom, diff);
        } else {
            doShiftRight(treeFrom, diff);
        }
    }

    /**
     * Returns the last treeModication from souroe. Guaranteed to be null
     * if the firing treeItem isn't visible. 
     *  
     * For testing only, don't use outside of IndexMappedItems! 
     * @return
     */
    public TreeModificationEvent<T> getTreeModification() {
        return treeModificationP.get();
    }
   
    public Property<TreeModificationEvent<T>> treeModificationProperty() {
        return treeModificationP;
    }
    
    /**
     * Sets the sourceChange, resets if != null.
     * @param sc<
     */
    protected void setTreeModification(TreeModificationEvent<T> sc) {
        treeModificationP.set(sc);
    }

    /**
     * Testing only: the selectedIndices at the moment at receiving a list change
     * from items.
     * 
     * @return
     */
    public List<Integer> getOldIndices() {
        return Collections.unmodifiableList(oldIndices);
    }

    /**
     * PENDING JW:
     * Temporary exposure - can we get away without? This is what the 
     * TreeIndexMappedList needs to update itself.
     * 
     * @return
     */
    public TreeView<T> getSource() {
        return tree;
    }

    @Override
    protected int getSourceSize() {
        return getSource().getExpandedItemCount();
    }

}
