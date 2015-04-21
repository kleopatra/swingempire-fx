/*
 * Created on 14.12.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.ObservableListBase;

/**
 * BitSet backed ObservableList. <p>
 * 
 * This is meant as an indexed view into a sequentially accessible backing data structure.
 * The IndicesBase 
 * provides the set/add/clearIndices and supporting infrastructure, the extending
 * classes would handle the updates triggered by notifications of the sequential
 * data structure. Implemented to fire as few changes as possible, particularly not
 * firing an added change if an index is already set.
 * <p>
 * Note: this is-a kind-of transformation list, cannot use a TransformList directly, though,
 * because the sequential "source" might be of type other than List, f.i. a TreeView
 * 
 * <p>
 * 
 * Design/Implementation:
 * <li> IndicesBase is-a kind-of transfrom of a observable sequential indexable backing data structure
 * <li> IndicesBase is-an unmodifiable ObservableList of Integers
 * <li> IndicesBase has api to set/add/clear indices as valid by the backing structure
 * <li> concrete subclasses listen to changes in the backing data and update the indices as
 *      appropriate
 * <li> implementations (at least that's the idea so far) should be deaf and dumb to its clients: 
 *      in particular they should _not_ know anything about the selectionModel that it is used by.
 *      It only manages the transform, semantics of "special" indices (like the selectedIndex)
 *      don't belong here, they should be implemented in the collaborator that manages single
 *      selection state.   
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
    
    //-------- fields for performance experiments: currently unused
    private static boolean PERFORM = false;
    private boolean valid;
    private int firstSetIndex;
    private int lastSetIndex;
    private int lastGetListIndex;
    private int lastGetBitSetIndex;
    //-------- end fields for performance experiments
    
    /**
     * Sets the given indices. All previously set indices that are not
     * in the given list are
     * cleared. Does nothing if null or empty.<p>
     * 
     * Fixed: don't remove indices that are about to be added
     * again - add some logic to find the latter and call clear only 
     * on the rest (vs. clearAll as currently done here)
     * 
     * @param indices positions in source list, must be valid.
     * @throws IndexOutOfBoundsException if any of the indices < 0
     *     or >= getSourceSize
     */
    public void setIndices(int... indices) {
        if (indices == null || indices.length == 0) return;
        beginChange();
//        clearAllIndices();
//        addIndices(indices);
        doClearAllIndicesExcept(indices);
        doAddIndices(indices);
        endChange();
    }

    /**
     * Sets all indices. 
     * <p>
     * PENDING JW: notification on already set? Delegates to 
     * setIndices which handles the correct notification .. but:
     * very slow! Alternative implementation: clears all and block-sets
     * all. Perfomant, fires a replaced. Acceptable?
     * 
     */
    public void setAllIndices() {
        if (getSourceSize() == 0) return;
        // performance optimized verstion: clear all then select all
        beginChange();
        clearAllIndices();
        bitSet.set(0, getSourceSize(), true);
        nextAdd(0, getSourceSize());
        endChange();
        // code below delegates to setIndices, handles correct notification
        // but is very slow.
//        int[] indices = new int[getSourceSize()];
//        for (int i = 0; i < indices.length; i++) {
//            indices[i] = i;
//        }
//        setIndices(indices);
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
//            int value = get(i);
            // performance op: don't go through get
            // bitSet lenght is updated automatically, 
            // last set at length - 1
            int value = bitSet.length() - 1;
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
     * Clears all indices that are not contained in the given array.
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} 
     * / {@code endChange()} block.
     * 
     * PENDING JW: this is ... rough implementation
     * @param indices the indices that should not be cleared.
     */
    protected void doClearAllIndicesExcept(int... indices) {
        List<Integer> toSet = Arrays.stream(indices).boxed().collect(Collectors.toList());
        
        doClearIndices(stream()
                .filter(p -> !toSet.contains(p))
                .mapToInt(Integer::intValue)
                .toArray()
                );
    }

    /**
     * Clears all indices in the given range. The index is a coordinates in 
     * backing list.
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} 
     * 
     * @param from
     * @param removedSize
     */
    protected boolean doClearIndicesInRange(int from, int removedSize) {
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
     * doClearIndicesInRange(from, removedSize);
     * doShiftLeft(from, removedSize);
     * </code></pre>
     * 
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} 
     * 
     * <p> PENDING JW: check 0 range!
     *  
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
     * Shifts all bits above and equal from by addedSize to the right. 
     * The index is a coordinate in 
     * backing list. The operation is
     * equivalent to increasing the index values by addedSize.
     * 
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} 
     *  
     * <p> PENDING JW: check 0 range! 
     * 
     * @param from index in backing list
     * @param addedSize the size to add to each index value of the bitSet
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
     * Access with off range index is programming error, better
     * throw.
     * <p>
     * 
     * Note: core (as of 8u60b11) still accepts indices outside our own size,
     * shouldn't 
     * <p>
     * PENDING JW: optimize (?) for RT-39776 - performance issue on access.
     * 
     * @return the value of this if index in valid range
     *  @throws IndexOutOfBoundsException if index off range
     */
    @Override
    public Integer get(int listIndex) {
        // PENDING JW: it is wrong to use size of source list as upper boundary
        // get() defined only on _our_ size!
        if (listIndex < 0 || listIndex >= size()) // return -1;
            throw new IndexOutOfBoundsException("index must be not negative "
                    + "and less than size " + size() + ", but was: " + listIndex);
        // PENDING JW: following line is for experimenting with performance
        // removed again: we access get during change/notification
        // at that time access state is inherently invalid - don't!
        if (PERFORM) {
            return doGet(listIndex);
        }    
        int pos = 0;
        int val = bitSet.nextSetBit(0);
        while (pos < listIndex) {
            pos++;
            val = bitSet.nextSetBit(val + 1);
        }
        if (val <0) {
            throw new IllegalStateException("wrongy! learn to use BitSet "
                    + "- must find set bit for valid index: " + listIndex);
        }
        return val;
    }

    /**
     * Performance optimized (sic! or not) version of get. Not used.
     * 
     * @param listIndex must be in valid 
     * @return
     */
    private Integer doGet(int listIndex) {
        validate();
        if (listIndex == 0) {
            lastGetListIndex = 0;
            lastGetBitSetIndex = firstSetIndex;
        } else if (listIndex == size() - 1) {
            lastGetListIndex = listIndex;
            lastGetBitSetIndex = lastSetIndex;
        } else if (listIndex == lastGetListIndex) {
            // nothing to do, querying the same index as last time
        } else {
            int pos = lastGetListIndex;
            int value = lastGetBitSetIndex;
            if (listIndex > lastGetListIndex) {
                // move forward from last
                while (pos < listIndex) {
                    pos++;
                    value = bitSet.nextSetBit(value + 1);
                }
            } else { // listIndex <lastGetBitSetIndex
                // move backwards from last
                while (pos > listIndex) {
                    pos--;
                    value = bitSet.previousSetBit(value - 1);
                }
            }
            lastGetListIndex = pos;
            lastGetBitSetIndex = value;
            if (value <0) {
                throw new IllegalStateException("wrongy! learn to use BitSet "
                        + "- must find set bit for valid index: " + listIndex);
            }
        }
        return lastGetBitSetIndex;
    }

    
    /**
     * 
     */
    private void validate() {
        if (valid) return;
        int size = size();
        if (size == 0) {
            firstSetIndex = -1;
            lastSetIndex = -1;
            lastGetListIndex = -1;
            lastGetBitSetIndex = -1;
        } else {
            lastSetIndex = bitSet.length() - 1;
            if (size == 1) {
                firstSetIndex = lastSetIndex;
            } else {
                firstSetIndex = bitSet.nextSetBit(0);
            }
            lastGetListIndex = 0;
            lastGetBitSetIndex = firstSetIndex;
        }
        valid = true;
    }

    @Override 
    public boolean contains(Object o) {
        if (o instanceof Number) {
            Number n = (Number) o;
            int index = n.intValue();

            return index >= 0 && index < bitSet.length() &&
                    bitSet.get(index);
        }

        return false;
    }

    
    
    @Override
    public int size() {
        return bitSet.cardinality();
    }

}
