/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.transformation.TransformationList;

/**
 * Helper for selectedItems. Source contains the selectedIndices, backingList the items.
 * 
 * This is truely unmodifiable, changes mediated by changes to source only.
 * 
 * PENDING JW:
 * - removed are firing incorrect removed items (due to direct access of the backing
 *   list, where the removed is no longer contained). reaching the boundary of chained
 *   transformation lists? Or doing something wrong in the chain? Can the intermediate
 *   (here IndicesList) somehow pass-on the removed items from the backing list? 
 *   
 * Really need to separate changes from "real" setting of indices in source from 
 * changes that were induced by backingList changes. Without, all changes envolving
 * a remove are plain incorrect!
 * <p>
 * 
 * Trying to separate out by 
 * <li> not reacting to source changes if its sourceChange != null
 * <li> listen to change of sourceChangeProperty and handle changes in
 *    backingList - note that the assumption here is that indicesList
 *    has updated itself completely before firing
 *    fires null -> change notification 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class IndexMappedList<T> extends TransformationList<T, Integer> {

    private List<? extends T> backingList;
    private ChangeListener<Change<? extends T>> sourceChangeListener;
    private WeakChangeListener<Change<? extends T>> weakSourceChangeListener;
    
    /**
     * @param source
     */
    public IndexMappedList(IndicesList<T> source) {
        super(source);
        this.backingList = source.getSource();
        sourceChangeListener = (p, old, value) -> backingListChanged(value);
        weakSourceChangeListener = new WeakChangeListener<>(sourceChangeListener);
        source.sourceChangeProperty().addListener(weakSourceChangeListener);
    }

    
    protected void backingListChanged(Change<? extends T> c) {
        // nothing to do
        if (c == null) return;
        c.reset();
        beginChange();
        while (c.next()) {
            if (c.wasPermutated()) {
                permutatedItems(c);
            } else if (c.wasUpdated()) {
                updatedItems(c);
            } else if (c.wasReplaced()) {
                replacedItems(c);
            } else {
                addedOrRemovedItems(c);
            }
        }
        endChange();
    }
    
    
    
    /**
     * @param c
     */
    private void addedOrRemovedItems(Change<? extends T> c) {
        if (c.wasAdded() && c.wasRemoved()) 
            throw new IllegalStateException("expected real add/remove but was: " + c);
        if (c.wasAdded()) {
            addedItems(c);
        } else if (c.wasRemoved()){
            removedItems(c);
        } else {
            throw new IllegalStateException("shouldn't be here: " + c);
        }
    }


    /**
     * Called on real additions to the backing list.
     * 
     * Implemented to do nothing: real addition to the backinglist don't change the
     * state of this.
     * 
     * @param c
     */
    private void addedItems(Change<? extends T> c) {
        // no-op
    }

    /**
     * This is called for a removed or replaced change in the backingList.
     * 
     * @param c
     */
    private void removedItems(Change<? extends T> c) {
        // fromIndex is startIndex of our own change - that doesn't change
        // as a subChange is about a single interval!
        int fromIndex = findIndex(c.getFrom(), c.getRemovedSize());
        if (fromIndex < 0) return;
        for (int i = c.getFrom(); i < c.getFrom() + c.getRemovedSize(); i++) {
            // have to fire if the item had been selected before and
            // no longer is now - how to detect the "before" part? we 
            // have no real state, only indirectly accessed
            int oldIndex = getIndicesList().oldIndices.indexOf(i);
            if(oldIndex > -1) {
                // was selected, check if still is
                int index = getIndicesList().indexOf(i);
                if (index < 0) {
                    T oldItem = c.getRemoved().get(i - c.getFrom());
                    nextRemove(fromIndex, oldItem);
                }
            }
        }
    }

    /**
     * Returns the first index in indicesList _old_ state. The result is
     * in our own coordinate system, start is in backingList's coordinates.
     *  
     * @param start the backinglist's index to start looking for a match
     * @param size the end of the interval, in backinglists counting.
     * 
     * @return a the first index of old indices coordinates that matches 
     *    a coordinate in the range, or -1 for no match.
     */
    protected Integer findIndex(int start, int size) {
        for (int i = start; i < start + size; i++) {
            int index = getIndicesList().oldIndices.indexOf(i);
            if (index > - 1) return index;
        }
        return -1;
    }

    /**
     * @param c
     */
    private void replacedItems(Change<? extends T> c) {
        // need to replace even if unchanged, listeners to selectedItems
        // depend on it
        // handle special case of "real" replaced, often size == 1
        if (c.getAddedSize() == 1 && c.getAddedSize() == c.getRemovedSize()) {
            for (int i = c.getFrom(); i < c.getTo(); i++) {
                int index = getIndicesList().indexOf(i);
                if (index > -1) {
                    nextSet(index, c.getRemoved().get(i - c.getFrom()));
                }
            } 
            return;
        }
        // here we most (?) likely received a setAll/setItems
        // that results in a removed on the indicesList, independent on the 
        // actual relative sizes of removed/added: all (?) old indices are invalid
        // PENDING JW: when can we get a replaced on a subrange?
        if (size() == 0) {
            removedItems(c);
        } else {
            throw new IllegalStateException("unexpected replaced: " + c);
        }
    }


    /**
     * Called when list change in backing list is of type update.
     * @param c
     */
    private void updatedItems(Change<? extends T> c) {
        for (int i = c.getFrom(); i < c.getTo(); i++) {
            int index = getIndicesList().indexOf(i);
            if (index > -1) {
                nextUpdate(index);
            }
        } 
    }


    /**
     * @param c
     */
    private void permutatedItems(Change<? extends T> c) {
        // TODO Auto-generated method stub
        
    }


    /**
     * Implemented to react to changes in indicesList only if they originated
     * from directly modifying its indices. Indirect changes (by backingList)
     * are handled on receiving a sourceChanged from indicesList.
     */
    @Override
    protected void sourceChanged(Change<? extends Integer> c) {
        if (getIndicesList().getSourceChange() != null) return;
        beginChange();
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
        endChange();
    }

    /**
     * Called on real added/removed changes to the indices.
     * @param c
     */
    private void addedOrRemoved(Change<? extends Integer> c) {
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

    /**
     * Called on real adds to the indices.
     * @param c
     */
    private void added(Change<? extends Integer> c) {
        nextAdd(c.getFrom(), c.getTo());
    }
    
    /**
     * Called on real removes on indices.
     * <p>
     * @param c
     */
    private void removed(Change<? extends Integer> c) {
        List<? extends Integer> indices = c.getRemoved();
        List<T> items = new ArrayList<>();
        if (getIndicesList().getSourceChange() == null) {
            // change resulted from direct modification of indices
            // no change in backingList, so we can access its items 
            // directly
            for (Integer index : indices) {
                items.add(backingList.get(index));
            }
        } else { 
            if (true)
                throw new IllegalStateException("shouldn't be here - separated out indirect changes");
            for (int i = 0 ; i < c.getRemovedSize(); i++) {
                int removedSourceIndex = c.getRemoved().get(i);
                // find change in source that covers removedSource
                Change<? extends T> sourceChange = getIndicesList().getSourceChange();
                sourceChange.reset();
                int accumulatedRemovedSize = 0;
                while (sourceChange.next()) {
                    if (!sourceChange.wasRemoved()) continue;
                    int fromSource = sourceChange.getFrom() + accumulatedRemovedSize;
                    if (removedSourceIndex >= fromSource && removedSourceIndex < fromSource + sourceChange.getRemovedSize()) {
                        // hit - PENDING JW: need to accumulate if we have multiple removes!
                        int indexInRemoved = removedSourceIndex - fromSource;
                        items.add(sourceChange.getRemoved().get(indexInRemoved));
                        break;
                    }
                    accumulatedRemovedSize += sourceChange.getRemovedSize();
                }
            }
        }
        nextRemove(c.getFrom(), items);
    }

    
    /**
     * @return
     */
    private IndicesList<T> getIndicesList() {
        return (IndicesList<T>) getSource();
    }

    /**
     * PENDING JW: all removes are carying incorrect removes - they access the 
     * backingList which has them already removed!
     * 
     * @param c
     */
    private void replaced(Change<? extends Integer> c) {
        List<? extends Integer> indices = c.getRemoved();
        List<T> items = new ArrayList<>();
        for (Integer index : indices) {
            items.add(backingList.get(index));
        }
        nextReplace(c.getFrom(), c.getTo(), items);
    }

    /**
     * @param c
     */
    private void updated(Change<? extends Integer> c) {
        nextUpdate(c.getFrom());
    }

    /**
     * @param c
     */
    private void permutated(Change<? extends Integer> c) {
        // TODO Auto-generated method stub
//        throw new UnsupportedOperationException("TBD: implement permutation changes");
    }

    @Override
    public int getSourceIndex(int index) {
        return index;
    }

    @Override
    public T get(int index) {
        int realIndex = getSource().get(index);
        return backingList.get(realIndex);
    }

    @Override
    public int size() {
        return getSource().size();
    }


}
