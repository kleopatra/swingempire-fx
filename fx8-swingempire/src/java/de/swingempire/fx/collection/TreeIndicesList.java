/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.BitSet;
import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableListBase;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
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
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeIndicesList<T> extends ObservableListBase<Integer> {

    private BitSet bitSet;
    private ObjectProperty<TreeModificationEvent<T>> sourceChangeP = 
            new SimpleObjectProperty<>(this, "sourceChange");
    // needed for getRow because all utility methods are package private
    private TreeView<T> tree;
    // the tree's root item
    private TreeItemX<T> root;
    
    private EventHandler<TreeModificationEvent<T>> modificationListener = e -> treeModified(e);
    
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

//------------------- workhorses
    
    protected void treeModified(TreeModificationEvent<T> event) {
        if (!(event.getTreeItem() instanceof TreeItemX)) {
            throw new IllegalStateException("all treeItems must be of type TreeItemX but was " + event.getTreeItem());
        }
        TreeModificationEventX<T> ex = event instanceof TreeModificationEventX ? 
                (TreeModificationEventX<T>) event : null;
        beginChange();
        setSourceChange(null);
        if (ex != null && ex.getChange() != null) {
            childrenChanged(ex.getChange());
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
            throw new UnsupportedOperationException("TBD - expanded " + event);
        } else if (event.getEventType() == TreeItem.valueChangedEvent()) {
            throw new UnsupportedOperationException("TBD - valueChanged " + event);
        } else if (event.getEventType() == TreeItem.graphicChangedEvent()) {
            throw new UnsupportedOperationException("TBD - graphicChanged " + event);
        }
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
//                source.getPreviousExpandedDescendantCount() - 1;
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
     * PENDING JW: not good enough 
     * - need source TreeItem for adjusting from to pos in tree
     * - in implementing methods with change of count must use
     *   descendantCount vs bare added/removedSize
     * 
     * @param c
     */
    protected void childrenChanged(Change<? extends TreeItem<T>> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
//                permutated(c);
            } else if (c.wasUpdated()) {
//                updated(c);
            } else if (c.wasReplaced()) {
//                replaced(c);
            } else {
                addedOrRemoved(c);
            }
        }
    }
    
    /**
     * Implements internal update for separate add/remove from backing list.
     * PENDING JW: think about set (aka: replace) 
     * 
     * PENDING JW: need to use expandedDescendentCount vs. bare added/removedSize
     * @param c
     */
    private void addedOrRemoved(Change<? extends TreeItem<T>> c) {
        // change completely after
        if (bitSet.nextSetBit(c.getFrom()) < 0) return;

        if (c.wasAdded() && c.wasRemoved()) 
            throw new IllegalStateException("expected real add/remove but was: " + c);
        if (c.wasAdded()) {
            add(c);
        } else if (c.wasRemoved()) {
            remove(c);
        } else {
            throw new IllegalStateException("what have we got here? " + c);
        }
    }

    /**
     * PENDING JW: need to use expandedDescendentCount vs. bare addedSize
     * @param c
     */
    private void add(Change<? extends TreeItem<T>> c) {
        // added: values that are after the added index must be increased by addedSize
        int from = c.getFrom();
        int addedSize = c.getAddedSize();
        doShiftRight(from, addedSize);
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

    /**
     * PENDING JW: need to use expandedDescendentCount vs. bare removedSize
     * @param c
     */
    private void remove(Change<? extends TreeItem<T>> c) {
        // removed is two-step:
        // if any of the values that are mapped to indices, is removed remove the index
        // for all left over indices after the remove, decrease the value by removedSize (?)
        int removedSize = c.getRemovedSize();
        int from = c.getFrom();
        doRemoveIndices(from, removedSize);
        // step 2
        doShiftLeft(from, removedSize);
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
        if (index < 0 || index >= size()) return -1;

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
