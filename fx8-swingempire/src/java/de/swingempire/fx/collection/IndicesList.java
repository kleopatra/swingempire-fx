/*
 * Created on 14.11.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.BitSet;
import java.util.logging.Logger;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

/**
 * Experimenting with BitSet backed List for selectedIndices of
 * MultipleSelectionModel. The basic idea is to let super handle the
 * tricky change notifications.<p>
 * 
 * Note that TransformList might not be the correct starter, it's just
 * convenient for now.<p>
 * 
 * Tentative Rules:
 * 
 * <li> sourceList.eventCount == this.eventCount
 * <li> added: values > getFrom increased by addedSize - updated or replaced?
 * <li> removed: indices in removedSize cleared, values > getFrom + removedSize 
 *      decreased by removedSize
 * <li> updated: pass-through (listeners might be interested in underlying items)     
 * <li> permutated: replaced on range (?)
 * <li> replaced: technically a mix of added/removed but THINK - 1:1 might be "set"
 *      in underlying model, the net effect on value here would be unchanged, 
 *      actual removal of value unwanted     
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class IndicesList<T> extends TransformationList<Integer, T> {

    private BitSet bitSet;
    Change<? extends T> sourceChange;
    
    /**
     * @param source
     */
    public IndicesList(ObservableList<? extends T> source) {
        super(source);
        bitSet = new BitSet();
    }

    /**
     * Adds the given indices. Does nothing if null or empty.
     * 
     * @param indices
     */
    public void addIndices(int... indices) {
        if (indices == null || indices.length == 0) return;
        sourceChange = null;
        beginChange();
        for (int i : indices) {
            if (bitSet.get(i)) continue;
            bitSet.set(i);
            int from = indexOf(i);
            nextAdd(from, from + 1);
        }
        endChange();
    }
    
    /**
     * Clears the given indices. Does nothing if null or empty.
     * @param indices position in sourceList
     */
    public void clearIndices(int... indices) {
        if (indices == null || indices.length == 0) return;
        sourceChange = null;
        beginChange();
        doClearIndices(indices);
        endChange();
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
     * Clears all indices.
     */
    public void clearAllIndices() {
        sourceChange = null;
        beginChange();
        for (int i = size() -1 ; i >= 0; i--) {
            int value = get(i);
            bitSet.clear(value);
            nextRemove(i, value);
        }
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
        if (getSource().isEmpty()) return;
        int[] indices = new int[getSource().size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        setIndices(indices);
    }
    
    @Override
    protected void sourceChanged(Change<? extends T> c) {
        beginChange();
        sourceChange = null;
        while (c.next()) {
            if (c.wasPermutated()) {
                permutate(c);
            } else if (c.wasUpdated()) {
                update(c);
            } else if (c.wasReplaced()) {
                replace(c);
            } else {
                addOrRemove(c);
            }
        }
        c.reset();
        sourceChange = c;
        endChange();
    }

    /**
     * 
     * Replaced are fired on
     * - single set(n, element) in the underlying list
     * - setAll/setItems when swapping out its contents
     * - technical result of multiple add/remove (f.i. when filtering or other transformations)  
     * 
     * PENDING JW: Need to think about specification - a replace of a single element
     * might be the result of a simple set(..), which may or may not require to
     * remove a the index at the set position. Any way to differentiate?
     * 
     * @param c
     */
    private void replace(Change<? extends T> c) {
        // need to replace even if unchanged, listeners to selectedItems
        // depend on it
        // handle special case of "real" replaced, often size == 1
        if (c.getAddedSize() == 1 && c.getAddedSize() == c.getRemovedSize()) {
            for (int i = bitSet.nextSetBit(c.getFrom()); i >= 0 && i < c.getTo(); i = bitSet.nextSetBit(i+1)) {
                int pos = indexOf(i);
                nextSet(pos, i);
            }
            return;
        }

        doRemoveIndices(c.getFrom(), c.getRemovedSize());
        int diff = c.getAddedSize() - c.getRemovedSize();
        if (diff < 0) {
            doShiftLeft(c.getFrom(), diff);
        } else {
            doShiftRight(c.getFrom(), diff);
        }
    }

    /**
     * Implements internal update for separate add/remove from backing list.
     * PENDING JW: think about set (aka: replace) 
     * 
     * @param c
     */
    private void addOrRemove(Change<? extends T> c) {
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

    private void remove(Change<? extends T> c) {
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

    private void doClearIndices(int... indices) {
        for (int i : indices) {
            if (!bitSet.get(i)) continue;
            int from = indexOf(i);
            bitSet.clear(i);
            nextRemove(from, i);
         }
    }

    private void doRemoveIndices(int from, int removedSize) {
        int[] removedIndices = new int[removedSize];
        int index = from;
        for (int i = 0; i < removedIndices.length; i++) {
            removedIndices[i] = index++;
        }
        // do step one by delegating to clearIndices
        doClearIndices(removedIndices);
    }

    private void add(Change<? extends T> c) {
        // added: values that are after the added index must be increased by addedSize
        int from = c.getFrom();
        int addedSize = c.getAddedSize();
        doShiftRight(from, addedSize);
    }

    private void doShiftRight(int from, int addedSize) {
        for (int i = bitSet.length(); (i = bitSet.previousSetBit(i-1)) >= from; ) {
            // operate on index i here
            int pos = indexOf(i);
            // operate on index i here
            bitSet.clear(i);
            bitSet.set(i + addedSize);
            if (pos != indexOf(i + addedSize)) {
                throw new RuntimeException ("wrongy! - learn to use bitset");
            }
            nextSet(pos, i);
        }
    }

    /**
     * Update changes are passed-through as are. No real change on the level 
     * of this list, but listeners might be interested.
     * 
     * @param c
     */
    private void update(Change<? extends T> c) {
        for (int i = bitSet.nextSetBit(c.getFrom()); i >= 0 && i < c.getTo(); i = bitSet.nextSetBit(i+1)) {
            int pos = indexOf(i);
            nextUpdate(pos);
        }
    }

    /**
     * PENDING: not yet implemented
     * @param c
     */
    private void permutate(Change<? extends T> c) {
        // change completely after
        if (bitSet.nextSetBit(c.getFrom()) < 0) return;
        if (true)
            throw new UnsupportedOperationException("permutation not yet implemented");
        int from = c.getFrom();
        int to = c.getTo();
        BitSet oldIndices = (BitSet) bitSet.clone();
        for (int oldIndex = from; oldIndex < to; oldIndex++) {
            if (!oldIndices.get(oldIndex)) continue;
            int newIndex = c.getPermutation(oldIndex);
//            if (newIndex == oldIndex) continue;
            int pos = indexOf(oldIndex);
            bitSet.clear(oldIndex);
            nextRemove(pos, oldIndex);
            bitSet.set(newIndex);
            int newPos = indexOf(newIndex);
            nextAdd(newPos, newIndex);
        }
    }

    /**
     * Returns the source index if given index is in valide range or -1
     * if out off range.
     * <p>
     * 
     * Note:
     * Source index means the index needed to get the value at our index
     * in the backing list:
     * 
     * <pre><code>
     * item = sourceList.get(transform.getSourceIndex(index));
     * // as we are the mapping it's the same as get
     * getSourceIndex(index) == get(index) 
     * </code></pre>
     * 
     *    
     */
    @Override
    public int getSourceIndex(int index) {
        return get(index);
    }

    /**
     * @return the value of this if index in valid range, or -1 if given
     * index out of range.
     */
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
    public Change<? extends T> getSourceChange() {
        return sourceChange;
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(IndicesList.class
            .getName());
}
