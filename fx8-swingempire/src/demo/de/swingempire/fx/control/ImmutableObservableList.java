/*
 * Created on 14.04.2015
 *
 */
package de.swingempire.fx.control;

import java.util.List;

import javafx.collections.ObservableListBase;

public class ImmutableObservableList<E> extends ObservableListBase<E> {

    private List<E> backing;

    public ImmutableObservableList(List<E> backingList) {
        this.backing = backingList;
    }
    
    public void setBackingList(List<E> backingList) {
        beginChange();
        if (this.backing != null) {
            nextRemove(0, this.backing);
        }
        this.backing = backingList;
        if (backingList != null) {
            nextAdd(0, backingList.size());
        }
        endChange();
    }
    @Override
    public E get(int index) {
        if (backing == null) throw new IndexOutOfBoundsException();
        return backing.get(index);
    }

    @Override
    public int size() {
        return backing != null ? backing.size() : 0;
    }
    
}