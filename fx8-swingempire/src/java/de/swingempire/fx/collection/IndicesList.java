/*
 * Created on 14.11.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Logger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.transformation.TransformationList;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.WeakListChangeListener;

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
 * @see IndicesBase
 * @see TreeIndicesList
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class IndicesList<T> extends IndicesBase<T> {
//    extends ObservableListBase<Integer> {
//    TransformationList<Integer, T> {

    //    private Change<? extends T> sourceChange;
    private ObjectProperty<Change<? extends T>> sourceChangeP = new SimpleObjectProperty<>(this, "sourceChange");
    /**
     * Contains the source list of this transformation list.
     * This is never null and should be used to directly access source list content
     */
    private ObservableList<T> source;
    /**
     * This field contains the result of expression "source instanceof {@link javafx.collections.ObservableList}".
     * If this is true, it is possible to do transforms online.
     */
    private ListChangeListener<T> sourceListener;

    /**
     * @param source
     */
    public IndicesList(ObservableList<T> source) {
        bitSet = new BitSet();
        this.source = source;
        source.addListener(new WeakListChangeListener<>(getListener()));
    }

    public ObservableList<T> getSource() {
        return source;
    }
    
    private ListChangeListener<T> getListener() {
        if (sourceListener == null) {
            sourceListener = c -> {
                sourceChanged(c);
            };
        }
        return sourceListener;
    }

    //    @Override
    protected void sourceChanged(Change<? extends T> c) {
        beginChange();
        // doooh .... need old state for the sake of IndexedItems
        oldIndices = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            oldIndices.add(get(i));
        }
        setSourceChange(null); 
        while (c.next()) {
            if (c.wasPermutated()) {
                permutated(c);
            } else if (c.wasUpdated()) {
                updated(c);
            } else if (c.wasReplaced()) {
                replaced(c);
            } else {
                addedOrRemoved(c);
            }
        }
//        c.reset();
        setSourceChange(c);
        endChange();
        resetSourceChange();
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
    private void replaced(Change<? extends T> c) {
        // PENDING JW: no longer true, might decide to do nothing for the
        // special case?
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
    private void addedOrRemoved(Change<? extends T> c) {
        // change completely after
        if (bitSet.nextSetBit(c.getFrom()) < 0) return;

        if (c.wasAdded() && c.wasRemoved()) 
            throw new IllegalStateException("expected real add/remove but was: " + c);
        if (c.wasAdded()) {
            added(c);
        } else if (c.wasRemoved()) {
            removed(c);
        } else {
            throw new IllegalStateException("what have we got here? " + c);
        }
    }

    private void removed(Change<? extends T> c) {
        // removed is two-step:
        // if any of the values that are mapped to indices, is removed remove the index
        // for all left over indices after the remove, decrease the value by removedSize (?)
        int removedSize = c.getRemovedSize();
        int from = c.getFrom();
        doRemoveIndices(from, removedSize);
        // step 2
        doShiftLeft(from, removedSize);
    }

    private void added(Change<? extends T> c) {
        // added: values that are after the added index must be increased by addedSize
        int from = c.getFrom();
        int addedSize = c.getAddedSize();
        doShiftRight(from, addedSize);
    }

    /**
     * Update changes are passed-through as are. No real change on the level 
     * of this list, but listeners might be interested.
     * 
     * @param c
     */
    private void updated(Change<? extends T> c) {
        for (int i = bitSet.nextSetBit(c.getFrom()); i >= 0 && i < c.getTo(); i = bitSet.nextSetBit(i+1)) {
            int pos = indexOf(i);
            nextUpdate(pos);
        }
    }

    /**
     * A permutation in the backing list is a replaced on the indices (nearly always: one
     * example for a permutation here as well would be if all indices are selected)
     * @param c
     */
    private void permutated(Change<? extends T> c) {
        // change completely after
        if (bitSet.nextSetBit(c.getFrom()) < 0) return;
        int from = c.getFrom();
        int to = c.getTo();
        BitSet copy = (BitSet) bitSet.clone();
        // argghh .. second parameter is the _size_
        doRemoveIndices(from, to - from);
        int addSize = 0;
        for (int i = from; i < to; i++) {
            if (copy.get(i)) addSize++;
        }
        int[] permutated = new int[addSize];
        int current = 0;
        for (int oldIndex = from; oldIndex < to; oldIndex++) {
            if (copy.get(oldIndex)) {
                permutated[current++] = c.getPermutation(oldIndex);
            }
        }
        doAddIndices(permutated);
    }

    /**
     * {@inheritDoc} <p>
     * Returns the source index if given index is in valid.
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
//    @Override
    public int getSourceIndex(int index) {
        return get(index);
    }
    @Override
    protected int getSourceSize() {
        return getSource().size();
    }
    
//    @Override
    protected void resetSourceChange() {
        setSourceChange(null);
    }

    /**
     * Returns the last change from source that produced a remove, or null if last
     * change of this resulted from direct modification of indices.
     *  
     * For testing only, don't use outside of IndexMappedItems! 
     * @return
     */
    public Change<? extends T> getSourceChange() {
        return sourceChangeP.get();
    }
   
    public Property<Change<? extends T>> sourceChangeProperty() {
        return sourceChangeP;
    }
    
    /**
     * Sets the sourceChange, resets if != null.
     * @param sc
     */
    protected void setSourceChange(Change<? extends T> sc) {
        // PENDING JW: really? there might be several listeners (theoretically)
        // with no responsibility to reset the change - such that each
        // interested party has to reset before usage anyway
        if (sc != null) {
            sc.reset();
        }
        sourceChangeP.set(sc);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(IndicesList.class
            .getName());

}
