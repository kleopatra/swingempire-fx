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
 * @author Jeanette Winzenburg, Berlin
 */
public class IndexMappedList<T> extends TransformationList<T, Integer> {

    private List<T> backingList;

    /**
     * @param source
     */
    public IndexMappedList(ObservableList<? extends Integer> source, List<T> backingList) {
        super(source);
        this.backingList = Objects.requireNonNull(backingList, "backingList must not be null");
        // TODO Auto-generated constructor stub
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
     * @param c
     */
    private void remove(Change<? extends Integer> c) {
        List<? extends Integer> indices = c.getRemoved();
        List<T> items = new ArrayList<>();
        for (Integer index : indices) {
            items.add(backingList.get(index));
        }
        nextRemove(c.getFrom(), items);
    }

    /**
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
        throw new UnsupportedOperationException("TBD: implement permutation changes");
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
