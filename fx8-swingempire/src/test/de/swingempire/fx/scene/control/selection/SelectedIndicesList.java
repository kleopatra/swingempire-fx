/*
 * Created on 02.03.2020
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;

/**
 * c&p from MultipleSelectionModelBase, version near-fx14
 */
public abstract class SelectedIndicesList extends ReadOnlyUnbackedObservableList<Integer> {
    private final BitSet bitset;

    private int lastGetIndex = -1;
    private int lastGetValue = -1;

    // Fix for RT-20945 (and numerous other issues!)
    private int atomicityCount = 0;

//    @Override
//    public void callObservers(Change<Integer> c) {
//        throw new RuntimeException("callObservers unavailable");
//    }

    public SelectedIndicesList() {
        this(new BitSet());
    }

    public SelectedIndicesList(BitSet bitset) {
        this.bitset = bitset;
    }

    boolean isAtomic() {
        return atomicityCount > 0;
    }
    void startAtomic() {
        atomicityCount++;
    }
    void stopAtomic() {
        atomicityCount = Math.max(0, atomicityCount - 1);
    }

    // Returns the selected index at the given index.
    // e.g. if our selectedIndices are [1,3,5], then an index of 2 will return 5 here.
    @Override public Integer get(int index) {
        final int itemCount = size();
        if (index < 0 || index >= itemCount)  {
            throw new IndexOutOfBoundsException(index + " >= " + itemCount);
        }

        if (index == (lastGetIndex + 1) && lastGetValue < itemCount) {
            // we're iterating forward in order, short circuit for
            // performance reasons (RT-39776)
            lastGetIndex++;
            lastGetValue = bitset.nextSetBit(lastGetValue + 1);
            return lastGetValue;
        } else if (index == (lastGetIndex - 1) && lastGetValue > 0) {
            // we're iterating backward in order, short circuit for
            // performance reasons (RT-39776)
            lastGetIndex--;
            lastGetValue = bitset.previousSetBit(lastGetValue - 1);
            return lastGetValue;
        } else {
            for (lastGetIndex = 0, lastGetValue = bitset.nextSetBit(0);
                 lastGetValue >= 0 || lastGetIndex == index;
                 lastGetIndex++, lastGetValue = bitset.nextSetBit(lastGetValue + 1)) {
                if (lastGetIndex == index) {
                    return lastGetValue;
                }
            }
        }

        return -1;
    }

    public void set(int index) {
        if (!isValidIndex(index) || isSelected(index)) {
            return;
        }

        _beginChange();
        bitset.set(index);
        int indicesIndex = indexOf(index);
        _nextAdd(indicesIndex, indicesIndex + 1);
        _endChange();
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < getItemCount();
    }

    /**
     * @return
     */
    protected abstract int getItemCount();

    public void set(int index, boolean isSet) {
        if (isSet) {
            set(index);
        } else {
            clear(index);
        }
    }

    public void set(int index, int end, boolean isSet) {
        _beginChange();
        if (isSet) {
            bitset.set(index, end, isSet);
            int indicesIndex = indexOf(index);
            int span = end - index;
            _nextAdd(indicesIndex, indicesIndex + span);
        } else {
            // TODO handle remove
            bitset.set(index, end, isSet);
        }
        _endChange();
    }

    public void set(int index, int... indices) {
        if (indices == null || indices.length == 0) {
            set(index);
        } else {
            // we reduce down to the minimal number of changes possible
            // by finding all contiguous indices, of all indices that are
            // not already selected, and which are in the valid range
            startAtomic();
            List<Integer> sortedNewIndices =
                    IntStream.concat(IntStream.of(index), IntStream.of(indices))
                    .distinct()
                    .filter(this::isValidIndex)
                    .filter(this::isNotSelected)
                    .sorted()
                    .boxed()
                    .peek(this::set) // we also set here, but it's atomic!
                    .collect(Collectors.toList());
            stopAtomic();

            final int size = sortedNewIndices.size();
            if (size == 0) {
                // no-op
            } else if (size == 1) {
                _beginChange();
                int _index = sortedNewIndices.get(0);
                int indicesIndex = indexOf(_index);
                _nextAdd(indicesIndex, indicesIndex + 1);
                _endChange();
            } else {
                _beginChange();
                int pos = 0;
                int start = 0;
                int end = 0;

                // starting from pos, we keep going until the value is
                // not the next value
                int startValue = sortedNewIndices.get(pos++);
                start = indexOf(startValue);
                end = start + 1;
                int endValue = startValue;
                while (pos < size) {
                    int previousEndValue = endValue;
                    endValue = sortedNewIndices.get(pos++);
                    ++end;
                    if (previousEndValue != (endValue - 1)) {
                        _nextAdd(start, end);
                        start = end;
                        continue;
                    }

                    // special case for when we get to the point where the loop is about to end
                    // and we have uncommitted changes to fire.
                    if (pos == size) {
                        _nextAdd(start, start + pos);
                    }
                }

                _endChange();
            }
        }
    }

    public void clear() {
        _beginChange();
        List<Integer> removed = bitset.stream().boxed().collect(Collectors.toList());
        bitset.clear();
        _nextRemove(0, removed);
        _endChange();
    }

    public void clear(int index) {
        if (!bitset.get(index)) return;

        _beginChange();
        bitset.clear(index);
        _nextRemove(index, index);
        _endChange();
    }

//    public void clearAndSelect(int index) {
//        if (index < 0 || index >= getItemCount()) {
//            clearSelection();
//            return;
//        }
//
//        final boolean wasSelected = isSelected(index);
//
//        // RT-33558 if this method has been called with a given row, and that
//        // row is the only selected row currently, then this method becomes a no-op.
//        if (wasSelected && getSelectedIndices().size() == 1) {
//            // before we return, we double-check that the selected item
//            // is equal to the item in the given index
//            if (getSelectedItem() == getModelItem(index)) {
//                return;
//            }
//        }
//
//        List<Integer> removed = bitset.stream().boxed().collect(Collectors.toList());
//        boolean isSelected = removed.contains(index);
//        if (isSelected) {
//            removed.remove((Object)index);
//        }
//
//        if (removed.isEmpty()) {
//            set(index);
//        }
//
//        bitset.clear();
//        bitset.set(index);
//        _beginChange();
//        if (isSelected) {
//            _nextRemove(0, removed);
//        } else {
//            _nextAdd(0, 1);
//            _nextRemove(0, removed);
//        }
//        _endChange();
//    }

    public boolean isSelected(int index) {
        return bitset.get(index);
    }

    public boolean isNotSelected(int index) {
        return !isSelected(index);
    }

    /** Returns number of true bits in BitSet */
    @Override public int size() {
        return bitset.cardinality();
    }

    /** Returns the number of bits reserved in the BitSet */
    public int bitsetSize() {
        return bitset.size();
    }

    @Override public int indexOf(Object obj) {
        reset();
        return super.indexOf(obj);
    }

    @Override public boolean contains(Object o) {
        if (o instanceof Number) {
            Number n = (Number) o;
            int index = n.intValue();

            return index >= 0 && index < bitset.length() &&
                    bitset.get(index);
        }

        return false;
    }

    public void reset() {
        this.lastGetIndex = -1;
        this.lastGetValue = -1;
    }

    @Override public void _beginChange() {
        if (!isAtomic()) {
            super._beginChange();
        }
    }

    @Override public void _endChange() {
        if (!isAtomic()) {
            super._endChange();
        }
    }

    @Override public final void _nextUpdate(int pos) {
        if (!isAtomic()) {
            nextUpdate(pos);
        }
    }

    @Override public final void _nextSet(int idx, Integer old) {
        if (!isAtomic()) {
            nextSet(idx, old);
        }
    }

    @Override public final void _nextReplace(int from, int to, List<? extends Integer> removed) {
        if (!isAtomic()) {
            nextReplace(from, to, removed);
        }
    }

    @Override public final void _nextRemove(int idx, List<? extends Integer> removed) {
        if (!isAtomic()) {
            nextRemove(idx, removed);
        }
    }

    @Override public final void _nextRemove(int idx, Integer removed) {
        if (!isAtomic()) {
            nextRemove(idx, removed);
        }
    }

    @Override public final void _nextPermutation(int from, int to, int[] perm) {
        if (!isAtomic()) {
            nextPermutation(from, to, perm);
        }
    }

    @Override public final void _nextAdd(int from, int to) {
        if (!isAtomic()) {
            nextAdd(from, to);
        }
    }
}
