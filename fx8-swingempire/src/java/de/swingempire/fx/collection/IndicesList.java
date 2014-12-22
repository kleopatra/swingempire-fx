/*
 * Created on 14.11.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

/**
 * An implementation of IndicesBase that is backed by a ObservableList. 
 * Must update internal state when receiving change notification from 
 * the backing structure. 
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
 * <p>
 * 
 * Note: this has a property sourceChange that is set to the change received from the
 * backing structure after this has been updated but before the our own change has been
 * broadcasted. It is reset again after nofication of our listeners. So intimately
 * coupled users like IndexMappedList can differentiate changes due to direct modification of
 * the indices (sourceChange == null) from those that were  induced by changes 
 * in the backing list (sourceChange != null).
 * <p>
 * 
 * PENDING JW: check if/how ReactFx might help instead of the brittle sourceChange.
 * 
 * @see IndicesBase
 * @see IndexMappedList
 * @see TreeIndicesList
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class IndicesList<T> extends IndicesBase<T> {

    private ObjectProperty<Change<? extends T>> sourceChangeP = new SimpleObjectProperty<>(this, "sourceChange");
    /**
     * Contains the backing list for which we store indices.
     * This is never null and should be used to directly access source list content
     */
    private ObservableList<T> source;
    private ListChangeListener<T> sourceListener;

    /**
     * The state of this before handling changes received from our backing structure.
     * We seem to need this because the using indexMappedList receives the
     * change from the backing structure after this has updated itself.
     * Wheezy ... need to do better
     */
    protected List<Integer> oldIndices;


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

    /**
     * Implemented to update itself on changes to the backing list. This handles
     * permutated, replaced and real added/removed. Does nothing on updated. <p>
     * 
     * Stores its own old state in oldIndices before changing anything (brittle!) and
     * routes the received change as-is via its sourceChange property after updating
     * itself but before notifying listeners (brittle again ..).
     * 
     * @param c
     */
    protected void sourceChanged(Change<? extends T> c) {
        beginChange();
        // doooh .... need old state for the sake of IndexedItems
        oldIndices = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            oldIndices.add(get(i));
        }
        while (c.next()) {
            if (c.wasPermutated()) {
                permutated(c);
            } else if (c.wasUpdated()) {
                // no-op, the state of this list isn't changed on updates of backing list
            } else if (c.wasReplaced()) {
                replaced(c);
            } else {
                addedOrRemoved(c);
            }
        }
        setSourceChange(c);
        endChange();
        setSourceChange(null); 
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

        doClearIndices(c.getFrom(), c.getRemovedSize());
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
        doClearIndices(from, removedSize);
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
        doClearIndices(from, to - from);
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

    /**
     * Testing only: the selectedIndices at the moment at receiving a list change
     * from items.
     * 
     * @return
     */
    public List<Integer> getOldIndices() {
        return Collections.unmodifiableList(oldIndices);
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(IndicesList.class
            .getName());

}
