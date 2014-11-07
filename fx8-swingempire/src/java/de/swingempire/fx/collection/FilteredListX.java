/*
 * Created on 04.11.2014
 *
 */
package de.swingempire.fx.collection;

/*
 * Copyright (c) 2011, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

import com.sun.javafx.collections.NonIterableChange.GenericAddRemoveChange;
import com.sun.javafx.collections.SortHelper;

import de.swingempire.fx.scene.control.selection.TableViewFilterSelectionRT_39289;

/**
 * C&P core with modifications to fix (not completely tested!):
 * - to support finer-grained notification in re-filter
 *   https://javafx-jira.kenai.com/browse/RT-39291 
 * - cope with null predicates
 *   https://javafx-jira.kenai.com/browse/RT-39290
 * - help TableView to keep selection state
 *   https://javafx-jira.kenai.com/browse/RT-39289  
 *   
 * PENDING JW:
 * - think about implications of raw null here: might be that we want
 *   a silent transformation of null into a predicate? Then could use
 *   in ands without further check?
 * 
 * Changed:
 * - implemented refilter to delegate to updateFilter(fromSource, toSource) with
 *   (0, source.size())
 * - PENDING JW: performance? doing lots of array copies
 * - supports null predicate
 * - removed null check in constructor and predicateProperty  
 * - replaced direct access to predicate by include in update. Note: couldn't
 *   come up with a failing test for addRemove, look into core tests to
 *   understand when that's needed. 
 * - using null in constructor without ALWAYS_TRUE makes test
 *   with multiple removes fail - why? 
 *   Reason: Doesn't initialize because replacing a null with a null
 *   doesn't trigger properties invalidation
 *   
 * Wraps an ObservableList and filters it's content using the provided Predicate.
 * All changes in the ObservableList are propagated immediately
 * to the FilteredList.
 *
 * @see TransformationList
 * @since JavaFX 8.0
 * 
 */
@SuppressWarnings("unchecked")
public final class FilteredListX<E> extends TransformationList<E, E>{

    private int[] filtered;
    private int size;

    private SortHelper helper;
    @SuppressWarnings("rawtypes")
    private static final Predicate ALWAYS_TRUE = t -> true;

    /**
     * Constructs a new FilteredList wrapper around the source list.
     * The provided predicate will match the elements in the source list that will be visible.
     * @param source the source list
     * @param predicate the predicate to match the elements. May be null to indicate
     * including all items of the source list
     */
    public FilteredListX(@NamedArg("source") ObservableList<E> source, @NamedArg("predicate") Predicate<? super E> predicate) {
        super(source);
        filtered = new int[source.size() * 3 / 2  + 1];
        setPredicate(predicate);
        // Note JW: the initial value of predicateProperty is null, 
        // refiltering triggered in its invalidated
        // to not initializing the filter if the predicate is null initially
        if (predicate == null) refilter();
    }

    /**
     * Constructs a new FilteredList wrapper around the source list with null predicate.
     * @param source the source list
     */
    public FilteredListX(@NamedArg("source") ObservableList<E> source) {
        this(source, null); //ALWAYS_TRUE);
    }

    /**
     * The predicate that will match the elements that will be in this FilteredList.
     * Elements not matching the predicate will be filtered-out. A null predicate
     * is equivalent to all matching.
     */
    private final ObjectProperty<Predicate<? super E>> predicate = new SimpleObjectProperty<Predicate<? super E>>(this, "predicate") {

        @Override
        protected void invalidated() {
            refilter();
        }

    };

    public final ObjectProperty<Predicate<? super E>> predicateProperty() {
        return predicate;
    }

    public final Predicate<? super E> getPredicate() {
        return predicate.get();
    }

    public final void setPredicate(Predicate<? super E> predicate) {
        this.predicate.set(predicate);
    }

    @Override
    protected void sourceChanged(Change<? extends E> c) {
        beginChange();
        while (c.next()) {
            if (c.wasPermutated()) {
                permutate(c);
            } else if (c.wasUpdated()) {
                update(c);
            } else {
                addRemove(c);
            }
        }
        endChange();
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public E get(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return getSource().get(filtered[index]);
    }

    @Override
    public int getSourceIndex(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return filtered[index];
    }

    private SortHelper getSortHelper() {
        if (helper == null) {
            helper = new SortHelper();
        }
        return helper;
    }

    private int findPosition(int p) {
        if (filtered.length == 0) {
            return 0;
        }
        if (p == 0) {
            return 0;
        }
        int pos = Arrays.binarySearch(filtered, 0, size, p);
        if (pos < 0 ) {
            pos = ~pos;
        }
        return pos;
    }


    @SuppressWarnings("unchecked")
    private void ensureSize(int size) {
        if (filtered.length < size) {
            int[] replacement = new int[size * 3/2 + 1];
            System.arraycopy(filtered, 0, replacement, 0, this.size);
            filtered = replacement;
        }
    }

    private void updateIndexes(int from, int delta) {
        for (int i = from; i < size; ++i) {
            filtered[i] += delta;
        }
    }

    private void permutate(Change<? extends E> c) {
        int from = findPosition(c.getFrom());
        int to = findPosition(c.getTo());

        if (to > from) {
            for (int i = from; i < to; ++i) {
                filtered[i] = c.getPermutation(filtered[i]);
            }

            int[] perm = getSortHelper().sort(filtered, from, to);
            nextPermutation(from, to, perm);
        }
    }

    private void update(Change<? extends E> c) {
        int from = findPosition(c.getFrom());
        int to = findPosition(c.getTo());

        // NOTE: this is sub-optimal, as we may mark some Nodes as "updated" even though
        // they will be removed from the list in the next step
        for (int i = from; i < to; ++i) {
            nextUpdate(i);
        }

        updateFilter(c.getFrom(), c.getTo());
    }

    private void addRemove(Change<? extends E> c) {
        ensureSize(getSource().size());
        final int from = findPosition(c.getFrom());
        final int to = findPosition(c.getFrom() + c.getRemovedSize());

        // Mark the nodes that are going to be removed
        for (int i = from; i < to; ++i) {
            nextRemove(from, c.getRemoved().get(filtered[i] - c.getFrom()));
        }

        // Update indexes of the sublist following the last element that was removed
        updateIndexes(to, c.getAddedSize() - c.getRemovedSize());

        // Replace as many removed elements as possible
        int fpos = from;
        int pos = c.getFrom();

        ListIterator<? extends E> it = getSource().listIterator(pos);
        for (; fpos < to && it.nextIndex() < c.getTo();) {
            if (include(it.next())) {
                filtered[fpos] = it.previousIndex();
                nextAdd(fpos, fpos + 1);
                ++fpos;
            }
        }

        if (fpos < to) {
            // If there were more removed elements than added
            System.arraycopy(filtered, to, filtered, fpos, size - to);
            size -= to - fpos;
        } else {
            // Add the remaining elements
            while (it.nextIndex() < c.getTo()) {
                if (include(it.next())) {
                    System.arraycopy(filtered, fpos, filtered, fpos + 1, size - fpos);
                    filtered[fpos] = it.previousIndex();
                    nextAdd(fpos, fpos + 1);
                    ++fpos;
                    ++size;
                }
                ++pos;
            }
        }
    }

    private void updateFilter(int sourceFrom, int sourceTo) {
        beginChange();
        // Fast path for single element update
        if (sourceFrom == sourceTo - 1) {
            int pos = findPosition(sourceFrom);
            final E sourceFromElement = getSource().get(sourceFrom);
            if (filtered[pos] == sourceFrom) {
                if (!include(sourceFromElement)) {
                    nextRemove(pos, sourceFromElement);
                    System.arraycopy(filtered, pos + 1, filtered, pos, size - pos - 1);
                    --size;
                }
            } else {
                ensureSize(getSource().size());
                if (include(sourceFromElement)) {
                    nextAdd(pos, pos + 1);
                    System.arraycopy(filtered, pos, filtered, pos + 1, size - pos);
                    filtered[pos] = sourceFrom;
                    ++size;
                }
            }
        } else {
            ensureSize(getSource().size());
            int filterFrom = findPosition(sourceFrom);
            int filterTo = findPosition(sourceTo);

            int i = filterFrom; // The index that traverses filtered[] array

            if (i == 0) {
                // Look at the beginning
                final int jTo = size == 0 ? sourceTo : filtered[0];
                final ListIterator<? extends E> it = getSource().listIterator(sourceFrom);
                for (; it.nextIndex() < jTo;) {
                    E el = it.next();
                    if (include(el)) {
                        nextAdd(i, i + 1);
                        System.arraycopy(filtered, i, filtered, i + 1, size - i);
                        filtered[i] = it.previousIndex();
                        size++;
                        filterTo++;
                        i++;
                    }
                }
            }


            // Now traverse the rest of the list. We first check the item in the filtered
            // array, if it still matches the filter
            ListIterator<? extends E> it = getSource().listIterator(filtered[i]);
            for (; i < filterTo; ++i) {
                advanceTo(it, filtered[i]);
                final E el = it.next();
                if (!include(el)) {
                    nextRemove(i, el);
                    System.arraycopy(filtered, i + 1, filtered, i, size - i - 1);
                    size--;
                    filterTo--;
                    i--;
                }
                final int jTo = (i == filterTo - 1 ? sourceTo : filtered[i + 1]);
                // Then we look at the elements that are between the current element in filtered[] array
                // and it's successor
                while (it.nextIndex() < jTo) {
                    final E midEl = it.next();
                    if (include(midEl)) {
                        nextAdd(i + 1, i + 2);
                        System.arraycopy(filtered, i + 1, filtered, i + 2, size - i - 1);
                        filtered[i + 1] = it.previousIndex();
                        size++;
                        filterTo++;
                        i++;
                    }
                }
            }

        }
        endChange();
    }

    /**
     * Returns whether the item should be included, queries the predicate if
     * not null or returns true if predicate is null.
     * @param el
     * @return
     */
    protected boolean include(final E el) {
        return getPredicate() != null ? getPredicate().test(el) : true;
    }

    private static void advanceTo(ListIterator<?> it, int index) {
        while(it.nextIndex() < index) {
            it.next();
        }
    }

    @SuppressWarnings("unchecked")
    private void refilter() {
        ensureSize(getSource().size());
        refilterByUpdate();
//        refilterByReplace();
    }

    /**
     * Implemented to delegate to updateFilter.
     */
    protected void refilterByUpdate() {
        // PENDING JW: need the begin/endChange?
        // updateFilter does it anyway
        beginChange();
        updateFilter(0, getSource().size());
        endChange();
    }

    /**
     * Extracted refilter from core, not used.
     * Changed to not directly access pred, but delegate to include.
     */
    protected void refilterByReplace() {
        List<E> removed = null;
        if (hasListeners()) {
            removed = new ArrayList<>(this);
        }
        size = 0;
        int i = 0;
        for (Iterator<? extends E> it = getSource().iterator();it.hasNext(); ) {
            final E next = it.next();
            if (include(next)) {
                filtered[size++] = i;
            }
            ++i;
        }
        if (hasListeners()) {
            fireChange(new GenericAddRemoveChange<>(0, size, removed, this));
        }
    }

}
