/*
 * Created on 14.12.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import javafx.collections.ObservableListBase;

/**
 * BitSet backed ObservableList. <p>
 * 
 * This is meant as an indexed view into a sequentially accessible backing data structure.
 * The IndicesBase 
 * provides the set/add/clearIndices and supporting infrastructure, the extending
 * classes would handle the updates triggered by notifications of the sequential
 * data structure.
 * <p>
 * Note: this is a kind-of transformation list, cannot use a Transform directly, though,
 * because the sequential "source" might be of type other than List.
 * 
 * <p>
 * 
 * Design/Implementation:
 * <li> IndicesBase is-a kind-of transfrom of a observable sequential indexable backing data structure
 * <li> IndicesBase is-an unmodifiable ObservableList of Integers
 * <li> IndicesBase has api to set/add/clear indices as valid by the backing structure
 * <li> concrete subclasses listen to changes in the backing data and update the indices as
 *     appropriate
 * 
 * @see IndicesList
 * @see IndexMappedList
 * @see TreeIndicesList
 * @see TreeIndexMappedList
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class IndicesBase<T> extends ObservableListBase<Integer> {

    protected BitSet bitSet;
    /**
     * Sets the given indices. All previously set indices are
     * cleared. Does nothing if null or empty.
     * 
     * @param indices positions in source list, must be valid.
     * @throws IndexOutOfBoundsException if any of the indices < 0
     *     or >= getSourceSize
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
        if (getSourceSize() == 0) return;
        int[] indices = new int[getSourceSize()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        setIndices(indices);
    }

    /**
     * Adds the given indices. Does nothing if null or empty.
     * 
     * @param indices positions in source list, must be valid.
     * @throws IndexOutOfBoundsException if any of the indices < 0
     *     or >= getSourceSize
     */
    public void addIndices(int... indices) {
        if (indices == null || indices.length == 0) return;
        beginChange();
        doAddIndices(indices);
        endChange();
    }

    /**
     * Clears the given indices. Does nothing if null or empty.
     * 
     * @param indices positions in source list, must be valid.
     * @throws IndexOutOfBoundsException if any of the indices < 0
     *     or >= getSourceSize
     */
    public void clearIndices(int... indices) {
        if (indices == null || indices.length == 0) return;
        beginChange();
        doClearIndices(indices);
        endChange();
    }

    /**
     * Clears all indices.
     */
    public void clearAllIndices() {
        beginChange();
        for (int i = size() -1 ; i >= 0; i--) {
            int value = get(i);
            bitSet.clear(value);
            nextRemove(i, value);
        }
        endChange();
    }

    /**
     * Adds the given indices. Does nothing empty, must not be null.
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} 
     * / {@code endChange()} block.
     * @param indices positions in source list, must be valid.
     * @throws IndexOutOfBoundsException if any of the indices < 0
     *     or >= getSourceSize
     * @throws NullPointerException if indices are null.    
     */
    protected void doAddIndices(int... indices) {
        for (int i : indices) {
            if (bitSet.get(i)) continue;
            bitSet.set(i);
            int from = indexOf(i);
            nextAdd(from, from + 1);
        }
    }

    /**
     * Clears the given indices. Does nothing empty, must not be null.
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} 
     * / {@code endChange()} block.
     * @param indices positions in source list, must be valid.
     * @return true if at least one of the indices had been cleared, false
     *    otherwise (== none had been set)
     * @throws IndexOutOfBoundsException if any of the indices < 0
     *     or >= getSourceSize
     * @throws NullPointerException if indices are null.    
     */
    protected boolean doClearIndices(int... indices) {
        boolean removed = false;
        for (int i : indices) {
            if (!bitSet.get(i))
                continue;
            int from = indexOf(i);
            bitSet.clear(i);
            nextRemove(from, i);
            removed = true;
        }
        return removed;
    }

    /**
     * Clears all indices in the given range. The index is a coordinates in 
     * backing list.
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} 
     * 
     * @param from
     * @param removedSize
     */
    protected boolean doClearIndices(int from, int removedSize) {
        int[] removedIndices = new int[removedSize];
        int index = from;
        for (int i = 0; i < removedIndices.length; i++) {
            removedIndices[i] = index++;
        }
        // do step one by delegating to clearIndices
        return doClearIndices(removedIndices);
    }

    /**
     * Shifts all bits above from by removedSize to the left. 
     * The index is a coordinates in 
     * backing list. The operation is
     * equivalent to decreasing the index values.<p>
     * 
     * This implementation assumes that there are no selected indices
     * in the range we are shifting up. That is, using code must first 
     * clear all indices in the range:
     * 
     * <pre><code>
     * doClearIndices(from, removedSize);
     * doShiftLeft(from, removedSize);
     * </code></pre>
     *  
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} 
     * @param from
     * @param removedSize
     */
    protected void doShiftLeft(int from, int removedSize) {
        for (int i = bitSet.nextSetBit(from); i >= 0; i = bitSet
                .nextSetBit(i + 1)) {
            int pos = indexOf(i);
            bitSet.clear(i);
            int bitIndex = i - removedSize;
            if (bitIndex < 0) {
                // PENDING JW: really still needed? Should be removed in step 1?
                // LOG.info("Really remove again? " + i + "/" + bitIndex);
                // nextRemove(pos, i);
                throw new IllegalStateException(
                        "remove should have happened in first step at "
                                + "bit: " + i + " value: " + pos);
            } else {
                bitSet.set(bitIndex);
                if (pos != indexOf(i - removedSize)) {
                    throw new IllegalStateException(
                            "wrongy! - learn to use bitset");
                }
                nextSet(pos, i);
            }
        }
    }

    /**
     * Shifts all bits above from by addedSize to the right. 
     * The index is a coordinates in 
     * backing list. The operation is
     * equivalent to increasing the index values by addedSize.
     *  
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} 
     * @param from
     * @param removedSize
     */
    protected void doShiftRight(int from, int addedSize) {
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
     * Returns the size of the backing data structure.
     * @return
     */
    protected abstract int getSourceSize();
    
//-------------------------- implementing List api
    
    /**
     * {@inheritDoc} <p>
     * access with off range index is programming error, better
     * throw
     * 
     * @return the value of this if index in valid range
     *  @throws IndexOutOfBoundsException if index off range
     */
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

}
