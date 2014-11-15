/*
 * Created on 14.11.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.BitSet;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

/**
 * Experimenting with BitSet backed List for selectedIndices of
 * MultipleSelectionModel. The basic idea is to let super handle the
 * tricky change notifications.<p>
 * 
 * Note that TransformList might not be the correct starter, it's just
 * convenient for now.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class IndicesList<T> extends TransformationList<Integer, T> {

    private BitSet bitSet;
    
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
     * @param indices
     */
    public void clearIndices(int... indices) {
        if (indices == null || indices.length == 0) return;
        beginChange();
        for (int i : indices) {
           if (!bitSet.get(i)) continue;
           int from = indexOf(i);
           bitSet.clear(i);
           nextRemove(from, i);
        }
        endChange();
    }
    
    /**
     * Sets the given indices. All previously set indices are
     * cleared.
     * 
     * Does nothing if null or empty.
     * 
     * @param indices
     */
    public void setIndices(int... indices) {
        
    }
    
    /**
     * Clears all indices.
     */
    public void clearAllIndices() {
        
    }
    
    /**
     * Sets all indices. 
     * 
     * PENDING JW: notification on already set?
     * 
     * 
     */
    public void setAllIndices() {
        
    }
    @Override
    protected void sourceChanged(Change<? extends T> c) {
        beginChange();
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
        endChange();
    }

    /**
     * PENDING JW: need to handle range replace with addedSize != removedSize
     * @param c
     */
    private void replace(Change<? extends T> c) {
        // need to replace even if unchanged, listeners to selectedItems
        // depend on it
        // handle special case of "real" replaced, often size == 1
        if (c.getAddedSize() == c.getRemovedSize()) {
            for (int i = bitSet.nextSetBit(c.getFrom()); i >= 0 && i < c.getTo(); i = bitSet.nextSetBit(i+1)) {
                int pos = indexOf(i);
                nextSet(pos, i);
            }
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
        // added
        for (int i = bitSet.length(); (i = bitSet.previousSetBit(i-1)) >= c.getFrom(); ) {
            // operate on index i here
            int pos = indexOf(i);
            // operate on index i here
            bitSet.clear(i);
            bitSet.set(i + c.getAddedSize());
            if (pos != indexOf(i + c.getAddedSize())) {
                throw new RuntimeException ("wrongy! - learn to use bitset");
            }
            nextSet(pos, i);
        }

        // removed
        for (int i = bitSet.nextSetBit(c.getFrom()); i >= 0; i = bitSet.nextSetBit(i+1)) {
            int pos = indexOf(i);
            bitSet.clear(i);
            bitSet.set(i - c.getRemovedSize());
            if (pos != indexOf(i - c.getRemovedSize())) {
                throw new RuntimeException ("wrongy! - learn to use bitset");
            }
            nextRemove(pos, i);
        }
      
    }

    /**
     * @param c
     */
    private void update(Change<? extends T> c) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param c
     */
    private void permutate(Change<? extends T> c) {
        // TODO Auto-generated method stub
        
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

}
