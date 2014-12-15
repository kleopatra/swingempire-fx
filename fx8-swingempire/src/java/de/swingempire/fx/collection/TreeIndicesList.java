/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableListBase;
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
 * @see IndicesList
 * @see IndicesBase
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeIndicesList<T> extends ObservableListBase<Integer> {

    private BitSet bitSet;
    // needed for getRow because all utility methods are package private
    private TreeView<T> tree;
    // the tree's root item
    private TreeItemX<T> root;
    
    private EventHandler<TreeModificationEvent<T>> modificationListener = e -> treeModified(e);

    private ObjectProperty<TreeModificationEvent<T>> sourceChangeP = 
            new SimpleObjectProperty<>(this, "sourceChange");
    // PENDING JW: wonky - this is the state when receiving a change from source
    // do better!
    List<Integer> oldIndices;

    /**
     * TreeView must treeItems of type TreeItemX. That's the only one firing an
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
    }
    
    /**
     * Sets the given indices. All previously set indices are
     * cleared.
     * 
     * Does nothing if null or empty.
     * 
     * @param indices positions in source list
     */
    public void setIndices(int... indices) {
        beginChange();
        clearAllIndices();
        addIndices(indices);
        endChange();
    }

    /**
     * Sets all indices. 
     * 
     * PENDING JW: notification on already set?
     * 
     * 
     */
    public void setAllIndices() {
//        if (getSource().isEmpty()) return;
        int[] indices = new int[tree.getExpandedItemCount()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        setIndices(indices);
    }

    /**
     * Adds the given indices. Does nothing if null or empty.
     * 
     * @param indices
     */
    public void addIndices(int... indices) {
        if (indices == null || indices.length == 0) return;
        setSourceChange(null);;
        beginChange();
        doAddIndices(indices);
        endChange();
    }

    /**
     * Clears the given indices. Does nothing if null or empty.
     * @param indices position in sourceList
     */
    public void clearIndices(int... indices) {
        if (indices == null || indices.length == 0) return;
        setSourceChange(null);
        beginChange();
        doClearIndices(indices);
        endChange();
    }

    /**
     * Clears all indices.
     */
    public void clearAllIndices() {
        setSourceChange(null);
        beginChange();
        for (int i = size() -1 ; i >= 0; i--) {
            int value = get(i);
            bitSet.clear(value);
            nextRemove(i, value);
        }
        endChange();
    }

//------------------- internal update on tree modification
    
    protected void treeModified(TreeModificationEvent<T> event) {
        if (!(event.getTreeItem() instanceof TreeItemX)) {
            throw new IllegalStateException("all treeItems must be of type TreeItemX but was " + event.getTreeItem());
        }
        TreeModificationEventX<T> ex = event instanceof TreeModificationEventX ? 
                (TreeModificationEventX<T>) event : null;
        beginChange();
        setSourceChange(null);
        // doooh .... need old state for the sake of IndexedItems
        oldIndices = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            oldIndices.add(get(i));
        }
        if (ex != null && ex.getChange() != null) {
            childrenChanged((TreeItemX<T>) event.getTreeItem(), ex.getChange());
        } else {
            treeItemModified(event);
        }
        setSourceChange(ex);
        endChange();
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
        // need to chech if visible because tree.getRow behaves unexpectedly
        if (!TreeItemX.isVisible(source)) return;
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
        // need to chech if visible because tree.getRow behaves unexpectedly
        if (!TreeItemX.isVisible(source)) return;
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
        int removedSize = getExpandedChildCount(source);
        // step one: remove the indices inside the range
        doRemoveIndices(from, removedSize);
        // step two: shift indices below the range
        doShiftLeft(from, removedSize);
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
     * Updates indices after receiving children have been added to the given
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
        doRemoveIndices(from, removedSize);
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
        if (removedSize == 1 && addedSize == 1) {
            // single replace nothing to do, assume
            return;
        }
//        if (c.getAddedSize() == 1 && c.getAddedSize() == c.getRemovedSize()) {
//            for (int i = bitSet.nextSetBit(c.getFrom()); i >= 0 && i < c.getTo(); i = bitSet.nextSetBit(i+1)) {
//                int pos = indexOf(i);
//                nextSet(pos, i);
//            }
//            return;
//        }

        doRemoveIndices(treeFrom, removedSize);
        int diff = addedSize - removedSize;
        if (diff < 0) {
            doShiftLeft(treeFrom, diff);
        } else {
            doShiftRight(treeFrom, diff);
        }
    }

    private void doShiftRight(int from, int addedSize) {
        // loop bitset from back to from
        for (int i = bitSet.length(); (i = bitSet.previousSetBit(i-1)) >= from; ) {
            // find position in this list
            int pos = indexOf(i);
            // clear old in bitset
            bitSet.clear(i);
            // set increased
            bitSet.set(i + addedSize);
            if (pos != indexOf(i + addedSize)) {
                throw new RuntimeException ("wrongy! - learn to use bitset");
            }
            nextSet(pos, i);
        }
    }

    private void doShiftLeft(int from, int removedSize) {
        for (int i = bitSet.nextSetBit(from); i >= 0; i = bitSet.nextSetBit(i+1)) {
            int pos = indexOf(i);
            bitSet.clear(i);
            int bitIndex = i - removedSize;
            if (bitIndex < 0) {
                // PENDING JW: really still needed? Should be removed in step 1?
//                LOG.info("Really remove again? " + i + "/" + bitIndex);
//                nextRemove(pos, i);
                throw new IllegalStateException("remove should have happened in first step at " 
                        + "bit: " + i + " value: " + pos );
            } else {
                bitSet.set(bitIndex);
                if (pos != indexOf(i - removedSize)) {
                    throw new IllegalStateException ("wrongy! - learn to use bitset");
                }
                nextSet(pos, i);
            }
        }
    }

    protected void doAddIndices(int... indices) {
        for (int i : indices) {
            if (bitSet.get(i)) continue;
            bitSet.set(i);
            int from = indexOf(i);
            nextAdd(from, from + 1);
        }
    }
    
    private void doClearIndices(int... indices) {
        for (int i : indices) {
            if (!bitSet.get(i)) continue;
            int from = indexOf(i);
            bitSet.clear(i);
            nextRemove(from, i);
         }
    }

    /**
     * Remove all indices in the given range. Indices are coordinates in 
     * backing list.
     * 
     * @param from
     * @param removedSize
     */
    private void doRemoveIndices(int from, int removedSize) {
        int[] removedIndices = new int[removedSize];
        int index = from;
        for (int i = 0; i < removedIndices.length; i++) {
            removedIndices[i] = index++;
        }
        // do step one by delegating to clearIndices
        doClearIndices(removedIndices);
    }


//------------------ list api    
    
    @Override
    public Integer get(int index) {
        // PENDING JW: it is wrong to use size of source list as upper boundary
        // get() defined only on _our_ size!
        if (index < 0 || index >= size()) // return -1;
            throw new IndexOutOfBoundsException("index must be not negative "
                    + "and less than size " + size() + ", but was: " + index);
        // PENDING JW: following lines simply copied from MultipleSelectionModelBase
        // we are looking for the nth bit set
        // double check needed because guard of valid range was incorrect
        // (checked against itemsSize instead of our size)
//        for (int pos = 0, val = bitSet.nextSetBit(0);
//             val >= 0 || pos == index;
//             pos++, val = bitSet.nextSetBit(val+1)) {
//            if (pos == index) return val;
//        }
        // this is functionally equivalent to the above, except for
        // throwing if we don't find the value - must succeed if the index
        // is valid
        int pos = 0;
        int val = bitSet.nextSetBit(0);
        while (pos < index) {
            pos++;
            val = bitSet.nextSetBit(val + 1);
        }
        if (val <0) {
            throw new IllegalStateException("wrongy! learn to use BitSet "
                    + "- must find set bit for valid index: " + index);
        }
        return val;
    }

    @Override
    public int size() {
        return bitSet.cardinality();
    }

    /**
     * Returns the last change from source that produced a remove, or null if last
     * change of this resulted from direct modification of indices.
     *  
     * For testing only, don't use outside of IndexMappedItems! 
     * @return
     */
    public TreeModificationEvent<T> getSourceChange() {
        return sourceChangeP.get();
    }
   
    public Property<TreeModificationEvent<T>> sourceChangeProperty() {
        return sourceChangeP;
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
    /**
     * Sets the sourceChange, resets if != null.
     * @param sc
     */
    protected void setSourceChange(TreeModificationEvent<T> sc) {
        // PENDING JW: really? there might be several listeners (theoretically)
        // with no responsibility to reset the change - such that each
        // interested party has to reset before usage anyway
//        if (sc != null) {
//            sc.reset();
//        }
        sourceChangeP.set(sc);
    }
    


}
