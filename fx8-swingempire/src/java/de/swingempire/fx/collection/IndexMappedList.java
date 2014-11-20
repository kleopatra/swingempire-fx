/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
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
 * @author Jeanette Winzenburg, Berlin
 */
public class IndexMappedList<T> extends TransformationList<T, Integer> {

    private List<? extends T> backingList;

    /**
     * @param source
     */
    public IndexMappedList(IndicesList<T> source) {
        super(source);
        this.backingList = source.getSource();
    }

    @Override
    protected void sourceChanged(Change<? extends Integer> c) {
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
     * @param c
     */
    private void addOrRemove(Change<? extends Integer> c) {
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
     * @param c
     */
    private void add(Change<? extends Integer> c) {
        nextAdd(c.getFrom(), c.getTo());
    }
    
    /**
     * PENDING JW: here's a problem - the removed has the indices as values, but the
     * backing list is already removed (that is, access via backingList.get(removedIndex) is 
     * invalid. Might need to keep a copy instead of direct access?
     * 
     * @param c
     */
    private void remove(Change<? extends Integer> c) {
        List<? extends Integer> indices = c.getRemoved();
        List<T> items = new ArrayList<>();
        if (getIndicesList().sourceChange == null) {
            // change resulted from direct modification of indices
            // no change in backingList, so we can access its items 
            // directly
            for (Integer index : indices) {
                items.add(backingList.get(index));
            }
        } else { 
            for (int i = 0 ; i < c.getRemovedSize(); i++) {
                int removedSourceIndex = c.getRemoved().get(i);
                // find change in source that covers removedSource
                Change<? extends T> sourceChange = getIndicesList().sourceChange;
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
    private void replace(Change<? extends Integer> c) {
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
    private void update(Change<? extends Integer> c) {
        nextUpdate(c.getFrom());
    }

    /**
     * @param c
     */
    private void permutate(Change<? extends Integer> c) {
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
