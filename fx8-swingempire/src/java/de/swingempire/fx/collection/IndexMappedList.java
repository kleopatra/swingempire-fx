/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.property.ListPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.transformation.TransformationList;

import com.sun.javafx.collections.SortHelper;

/**
 * Helper for selectedItems. Source contains the selectedIndices, backingList the items.
 * 
 * This is truely unmodifiable, changes mediated by changes to source and backing list.
 * <p>
 * Really needed to separate changes from "real" setting of indices in source from 
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
    // trying to support discontinous removes from backing list
    // this is meant to be the actual old index of c.getFrom
    private int accumulatedRemoved;
    
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
        accumulatedRemoved = 0;
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
                // PENDING JW: need to update accumulatedRemoved?
                // only if we have to expect a mixture of change types?
                // do we?
            } else if (c.wasAdded() || c.wasRemoved()){
                addedOrRemovedItems(c);
                accumulatedRemoved += c.getRemovedSize();
            } else {
                // we reach here if the backing source is-a ListProperty
                // and its empty list replaced by another empty list
                // https://javafx-jira.kenai.com/browse/RT-40213
                if (backingList instanceof ListPropertyBase && c.getAddedSize() == 0 && c.getRemovedSize() == 0) {
                    // probably fine
                    LOG.finer("got unknown change type from listProperty? " + c + "\n" + backingList);
                } else {
                    throw new IllegalStateException("unknown change type from " + c + "\n " + backingList );
                }
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
            // nothing to do: real additions to the backing list
            // don't change our state
        } else if (c.wasRemoved()){
            removedItems(c);
        } 
    }

    /**
     * This is called for a removed or replaced change in the backingList.
     * 
     * @param c
     */
    private void removedItems(Change<? extends T> c) {
        // from in coordinate of list before the remove
        int realFrom = c.getFrom() + accumulatedRemoved;
        // fromIndex is startIndex of our own change - that doesn't change
        // as a subChange is about a single interval!
        int fromIndex = findIndex(realFrom, c.getRemovedSize());
        if (fromIndex < 0) return;

        // here we loop over the indices of the removed items
        for (int index = 0; index < c.getRemovedSize(); index++) {
            int oldIndex = realFrom + index;
            // check if the old index was contained
            if (getIndicesList().oldIndices.contains(oldIndex)) {
                // add a remove if so
                T oldItem = c.getRemoved().get(index);
                nextRemove(fromIndex, oldItem);
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

    protected SortHelper sortHelper = new SortHelper();

    /**
     * @param c
     */
    private void permutatedItems(Change<? extends T> c) {
//        throw new IllegalStateException("not yet implemented");
        List<Integer> perm = new ArrayList<>(getIndicesList().oldIndices);
        int[] permA = new int[size()];
        for (int i = 0; i < getIndicesList().oldIndices.size(); i++) {
            perm.set(i, c.getPermutation(getIndicesList().oldIndices.get(i)));
        }
        int[] permutation = sortHelper.sort(perm);
        nextPermutation(0, perm.size(), permutation);
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
        throw new IllegalStateException("don't expect permutation changes from indices " + c);
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

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(IndexMappedList.class
            .getName());

}
