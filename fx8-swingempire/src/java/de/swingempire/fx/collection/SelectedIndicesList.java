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
public class SelectedIndicesList<T> extends TransformationList<Integer, T> {

    private BitSet bitSet;
    
    /**
     * @param source
     */
    public SelectedIndicesList(ObservableList<? extends T> source) {
        super(source);
        bitSet = new BitSet();
    }

    public void selectIndices(int... indices) {
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
    
    public void unselectIndices(int... indices) {
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
        // single set, nothing to do
        if (c.getAddedSize() == 1 && c.getAddedSize() == c.getRemovedSize()) return;
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
     * PENDING JW:
     * We are not really a transform list ...
     */
    @Override
    public int getSourceIndex(int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Integer get(int index) {
        if (index < 0 || index >= getSource().size()) return -1;

        for (int pos = 0, val = bitSet.nextSetBit(0);
             val >= 0 || pos == index;
             pos++, val = bitSet.nextSetBit(val+1)) {
            if (pos == index) return val;
        }

        return -1;
    }

    @Override
    public int size() {
        return bitSet.cardinality();
    }

}
