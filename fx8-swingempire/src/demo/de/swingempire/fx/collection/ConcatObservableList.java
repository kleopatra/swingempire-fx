/*
 * Created on 02.06.2016
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ConcatObservableList<E> extends ObservableListBase<E> {

    List<List<E>> lists;
    ListChangeListener l = c -> this.sourceChanged(c);
    public ConcatObservableList(ObservableList<E>... lists) {
        this.lists = new ArrayList<>();
        for (ObservableList<E> observableList : lists) {
            this.lists.add(observableList);
            observableList.addListener(l);
        }
    }
    
    protected void sourceChanged(Change c) {
        beginChange();
        while (c.next()) {
            if (c.wasPermutated()) {
                // TODO - permutations must be handled first
            } else if (c.wasUpdated()) {
                // TODO 
            } else if (c.wasReplaced()) {
                // TODO -weed out to simplified handling of add/remove
            } else if (c.wasAdded()){
                added(c);
            } else if (c.wasRemoved()) {
                // TODO - similar to added
            } else {
                // TODO;
                // think about possible errors/conditions if source is ListProperty
            }
        }
        endChange();
    }

    /**
     * Note: this method must be called within a begin/endChange block
     * @param c a single sub-change as received by one of the backing lists
     */
    protected void added(Change c) {
        int offset = getOffset(c.getList());
        nextAdd(offset + c.getFrom(), offset + c.getTo());
    }

    /**
     * Returns the offset of the given list's first index in coordinates
     * of this list.
     * @param list
     * @return
     */
    protected int getOffset(ObservableList<E> list) {
        int offset = 0;
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i) == list) {
                break;
            }
            offset += lists.get(i).size();
        }
        return offset;
    }

    @Override
    public E get(int index) {
        if (index < 0 || index >= size()) 
            throw new IndexOutOfBoundsException("invaliid index: " + index);
        int offset = 0;
        for(List<E> list : lists) {
            offset += list.size();
            if (index < offset) {
                int listIndex = offset - index;
                return list.get(listIndex);
            }
        }
        throw new IllegalStateException("unexpected end-of-lists reached - no element for " + index);
    }

    @Override
    public int size() {
        int size = 0;
        for(List<E> list : lists) {
            size += list.size();
        }
        return size;
    }

}
