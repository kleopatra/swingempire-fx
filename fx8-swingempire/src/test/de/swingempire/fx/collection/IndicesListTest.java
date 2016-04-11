/*
 * Created on 14.11.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.sun.javafx.collections.SortHelper;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.ChangeReport;
import de.swingempire.fx.util.FXUtils.ChangeType;
import de.swingempire.fx.util.ListChangeReport;

import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class IndicesListTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    IndicesList<String> indicesList;
    ListChangeReport report;
    ObservableList<String> items;

    // ------------ test modifications of backing list

    /**
     * Sanity: quick check that using a ListProperty as source doesn't
     * interfere.
     */
    @Test
    public void testItemsIsListProperty() {
        ObjectProperty<ObservableList<String>> itemsProperty = new SimpleObjectProperty(
                items);
        ListProperty<String> listItems = new SimpleListProperty<>();
        listItems.bind(itemsProperty);
        IndicesBase indicesList = new IndicesList(listItems);
        ListChangeReport report = new ListChangeReport(indicesList);
        int[] indices = new int[] { 3, 5, 1 };
        indicesList.addIndices(indices);
        report.clear();
        // new PrintingListChangeListener("SetAll Same size", indicesList);
        ObservableList<String> other = createObservableList(true);
        // make it not equal
        other.set(0, "otherItem");
        items.setAll(other);

        assertEquals("all cleared", 0, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue(
                "expected single removed, but was " + report.getLastChange(),
                wasSingleRemoved(report.getLastChange()));
    }
    
    /**
     * Incorrect test assumption: updates to the items don't change the 
     * indices, no event to fire from the indices. They are immutable,
     * anyway.
     * 
     * The assumption had some meaning as long as IndexMappedList didn't
     * get the real change from the backing list - now it doesn't and
     * can fire on its own as needed.
     * 
     * @see IndexMappedListTest#testItemsUpdate()
     */
    @Test @Ignore
    public void testItemsUpdate() {
        ObservableList<Person> base = Person.persons();
        ObservableList<Person> persons = FXCollections.observableList(base, p -> new Observable[] {p.firstNameProperty()});
        IndicesBase<Person> indicesList = new IndicesList<>(persons);
        int[] indices = new int[] {1, 3, 5};
        indicesList.addIndices(indices);
        ListChangeReport report = new ListChangeReport(indicesList);
//        new PrintingListChangeListener("Set item at 3", indicesList);
        persons.get(3).setFirstName("newName");
        assertEquals(1, report.getEventCount());
    }
    
    public static class TestIndicesList<F> extends TransformationList<Integer, F> {

        SortHelper helper = new SortHelper();
        List<Integer> indices;
        /**
         * @param source
         */
        public TestIndicesList(ObservableList<? extends F> source, int... initial) {
            super(source);
            indices = new ArrayList();
            for (int i = 0; i < initial.length; i++) {
                indices.add(initial[i]);
            }
        }

        @Override
        protected void sourceChanged(Change<? extends F> c) {
            c.next();
            if (!c.wasPermutated()) throw new IllegalStateException("permutations accepted, only");
            beginChange();
            List<Integer> copy = new ArrayList(indices);
            for (int i = 0; i < copy.size(); i++) {
                int newValue = c.getPermutation(copy.get(i));
                indices.set(i, newValue);
            }
            int[] reverse = helper.sort(indices);
            for (int i = 0; i < indices.size(); i++) {
                nextSet(i, copy.get(reverse[i]));
            }
            endChange();
        }

        @Override
        public int getSourceIndex(int index) {
            return get(index);
        }

        @Override
        public Integer get(int index) {
            return indices.get(index);
        }

        @Override
        public int size() {
            return indices.size();
        }

    }
    
    @Test
    public void testItemsPermutateSimulation() {
        ObservableList<Integer> indices = new TestIndicesList(items, 1, 3, 5);
        String msg = "indices before/after: " + indices + " / "; 
        ListChangeReport report = new ListChangeReport(indices);
        ListChangeReport itemsReport = new ListChangeReport(items);
        items.sort(null);
//        itemsReport.prettyPrint();
//        report.prettyPrint();
//        LOG.info(msg + indices);
        
    }
    /**
     * 
     */
    @Test
    public void testItemsPermutate() {
        int[] indices = new int[] { 1, 3, 5};
        indicesList.addIndices(indices);
        report.clear();
        ListChangeReport itemsReport = new ListChangeReport(items);
        // permutation on items
        // [0->8, 1->7, 2->6, 3->5, 4->4, 5->3, 6->2, 7->1, 8->0]
        items.sort(null);
//        itemsReport.prettyPrint();
        int[] permutated = new int[] {3, 5, 7};
//        LOG.info("sortedItems" + itemsReport.getLastChange());
//        LOG.info("sorted: " + report.getLastChange());
//        report.prettyPrint();
        assertEquals(1, report.getEventCount());
        assertEquals(1, getChangeCount(report.getLastChange(), ChangeType.REPLACED));
        Change c = report.getLastChange();
        c.reset();
        c.next();
        assertEquals("added size same", permutated.length, c.getAddedSize());
        for (int i = 0; i < permutated.length; i++) {
            assertEquals("added at " + i, permutated[i], c.getAddedSubList().get(i));
        }
        assertEquals("removed size same", indices.length, c.getRemovedSize());
        for (int i = 0; i < indices.length; i++) {
            assertEquals("removed at" + i, indices[i], c.getRemoved().get(i));
        }
//        LOG.info("" + indicesList);
    }
    
    @Test
    public void testItemsClear() {
        int index = 0;
        indicesList.addIndices(index);
        report.clear();
//        new PrintingListChangeListener("clear", indicesList);
        items.clear();
        assertEquals(1, report.getEventCount());
        assertTrue(wasSingleRemoved(report.getLastChange()));
    }
    @Test
    public void testItemsSetAllLargerSize() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        
        ObservableList<String> other = createObservableList(true);
        other.add(1, "newItem");
//        new PrintingListChangeListener("SetAll", indicesList);
        items.setAll(other);
        
        assertEquals("all cleared", 0, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue("expected single removed, but was " + report.getLastChange(), 
                wasSingleRemoved(report.getLastChange()));
    }
    
    @Test
    public void testItemsSetAllSmallerSize() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        
        ObservableList<String> other = createObservableList(true);
        other.remove(1);
        items.setAll(other);
        
        assertEquals("all cleared", 0, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue("expected single removed, but was " + report.getLastChange(), 
                wasSingleRemoved(report.getLastChange()));
    }
    
    @Test
    public void testItemsSetAllSameSize() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
//        new PrintingListChangeListener("SetAll Same size", indicesList);
        ObservableList<String> other = createObservableList(true);
        // make it not equal
        other.set(0, "otherItem");
        items.setAll(other);
        
        assertEquals("all cleared", 0, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue("expected single removed, but was " + report.getLastChange(), 
                wasSingleRemoved(report.getLastChange()));
    }
    
    @Test
    public void testItemsSetAll() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        
        items.setAll("one", "two", "three");
//        LOG.info("setAll" + report.getLastChange());
        
        assertEquals("all cleared", 0, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue("expected single removed, but was " + report.getLastChange(), 
                wasSingleRemoved(report.getLastChange()));
        
    }
    
    @Test
    public void testItemsAddedBefore() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
//        new PrintingListChangeListener("Item added at 0", indicesList);
        items.add(0, "newItem");
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indices[i] + 1;
        }
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals(1, report.getEventCount());
        assertTrue("expected single replaced, but was " + report.getLastChange(), 
                wasSingleReplaced(report.getLastChange()));
    }
    
    @Test
    public void testItemsRemovedBeforeAndWithFirst() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
//        new PrintingListChangeListener("Items removed at 0/1", indicesList);
        items.removeAll(items.get(0), items.get(1));
        assertEquals(indices.length -1, indicesList.size());
        Arrays.sort(indices);
        indices = Arrays.copyOfRange(indices, 1, 3);
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indices[i] - 2;
        }
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals(1, report.getEventCount());
        assertTrue("expected single replaced, but was " + report.getLastChange(), 
                wasSingleReplaced(report.getLastChange()));
    }
    
    @Test
    public void testItemsRemoveRange() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        // items we expect to appear in removed
        List removedItems = new ArrayList();
        removedItems.add(3);
        removedItems.add(5);
        report.clear();
        // remove items at 3...5, inclusive
        items.remove(3, 6);
        assertEquals(1, indicesList.size());
//        report.prettyPrint();
        indices = new int[] {1};
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals(1, report.getEventCount());
        assertTrue("expected single replaced but was" + report.getLastChange(), 
                wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.next();
        assertEquals(2, c.getRemovedSize());
        assertEquals(removedItems, c.getRemoved());
        
    }
    /**
     * Remove items in between selected.
     */
    @Test
    public void testItemsRemovedBetweenReally() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        items.removeAll(items.get(2), items.get(4));
        assertEquals(indices.length, indicesList.size());
        indices = new int[] {1, 2, 3};
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals(1, report.getEventCount());
        assertTrue("expected single replaced but was" + report.getLastChange(), 
                wasSingleReplaced(report.getLastChange()));
        Change c = report.getLastChange();
        c.next();
        assertEquals(2, c.getAddedSize());
        assertEquals(c.getAddedSize(), c.getRemovedSize());
//        report.prettyPrint();
    }
    
    @Test
    public void testItemsRemovedSOAnswer() {
        int[] indices = new int[] { 2, 4, 5, 8};
        indicesList.addIndices(indices);
        report.clear();
//        new PrintingListChangeListener("Items removed at 3/5", indicesList);
        items.removeAll(items.get(3), items.get(5));
        indices = new int[] {2, 3, 6};
        assertEquals(indices.length, indicesList.size());
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals(1, report.getEventCount());
        assertTrue("expected single replaced but was" + report.getLastChange(), 
                wasSingleReplaced(report.getLastChange()));
    }
    
    @Test
    public void testItemsRemovedAtAndBetween() {
        int[] indices = new int[] { 1, 3, 5, 7};
        indicesList.addIndices(indices);
        report.clear();
//        new PrintingListChangeListener("Items removed at 2/4", indicesList);
        items.removeAll(items.get(3), items.get(6));
        indices = new int[] {1, 4, 5};
        assertEquals(indices.length, indicesList.size());
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals(1, report.getEventCount());
        assertTrue("expected single replaced but was" + report.getLastChange(), 
                wasSingleReplaced(report.getLastChange()));
    }
    
    @Test
    public void testItemsRemovedAtFirst() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        
        items.remove(1);
        assertEquals(indices.length -1, indicesList.size());
        Arrays.sort(indices);
        indices = Arrays.copyOfRange(indices, 1, 3);
        // note: since here, the indices are "shortend" at start!
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indices[i] - 1;
        }
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals(1, report.getEventCount());
        assertTrue("expected single replaced but was" + report.getLastChange(),
                wasSingleReplaced(report.getLastChange()));
    }
    
    @Test
    public void testItemsRemovedBefore() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        items.remove(0);
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indices[i] - 1;
        }
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals(1, report.getEventCount());
    }
    
    @Test
    public void testItemsReplacedBefore() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        
        items.set(0, "newItem");
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals("selectedIndices unchanged", 0, report.getEventCount());
    }
    
    /**
     * The spec is: keep the selected on-replace-item - this means no change
     * to the indicesList, no notification.
     */
    @Test
    public void testItemsReplacedAt() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        ListChangeReport itemsReport = new ListChangeReport(items);
        items.set(3, "newItem");
//        itemsReport.prettyPrint();
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals("selectedIndices unchanged", 0, report.getEventCount());
//        report.prettyPrint();
//        assertTrue("singleReplaced ", wasSingleReplaced(report.getLastChange()));
    }

    /**
     * Sanity: single remove at index removes indexed
     */
    @Test
    public void testItemRemovedAt() {
        indicesList.setIndices(3);
        items.remove(3);
        assertEquals(0, indicesList.size());
    }
    
    @Test
    public void testItemSetAt() {
        int index = 3;
        indicesList.setIndices(index);
        report.clear();
        items.set(index, "replaced-element-at-3");
        assertEquals(1, indicesList.size());
        assertEquals(index, indicesList.get(0).intValue());
        assertEquals(0, report.getEventCount());
    }
    
//---------- tests: check state of sourceChange
    
    /**
     * Try to let indexMappedItems handle their own cleanup on changes to items:
     * make sourceChange a property and test if we can force notification order.
     * 
     * Changed implementation: the sourceChange is != only from before endChange to
     * after endChange, so we get 2 notifications, the last with a null.
     */
    @Test 
    public void testSourceChangeProperty() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        ChangeReport cr = new ChangeReport(indicesList.sourceChangeProperty());
        items.add(0, "something");
        assertEquals(2, cr.getEventCount());
//        Change c = (Change) cr.getLastOldValue(0);
//        // PENDING JW: really? there might be several listeners (theoretically)
//        // with no responsibility to reset the change - such that each
//        // interested party has to reset before usage anyway
//        assertTrue("expect change reset", c.next());
    }
    
    /**
     * Since extraction of IndicesBase 
     * Changed behaviour of sourceChange: valid only from before endChange to after
     * endChange!
     */
    @Test @Ignore
    public void testSourceChangeNotificationSequence() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        List<Object> changes = new ArrayList();
        ChangeListener cpl = (source, old, value) -> changes.add(value);
        indicesList.sourceChangeProperty().addListener(cpl);
        ListChangeListener lcl = c -> changes.add(c);
        indicesList.addListener(lcl);
        ChangeReport cr = new ChangeReport(indicesList.sourceChangeProperty());
        items.add(0, "something");
        assertEquals(2, changes.size());
        assertEquals(cr.getLastNewValue(), changes.get(0));
        assertEquals(report.getLastChange(), changes.get(1));
    }
    
    @Test
    public void testChangeNullOnDirectSet() {
        indicesList.setIndices(0);
        assertEquals(null, indicesList.getSourceChange());
    }
    
    /**
     * direct modification -> backingList change -> direct modification must
     * reset sourceChange to null
     */
    @Test
    public void testChangeNullOnDirectSetAllAfterItemsModified() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        ListChangeReport itemsReport = new ListChangeReport(items);
//        new PrintingListChangeListener("Items removed before", indicesList);
        items.remove(0);
        assertEquals("sanity", 1, report.getEventCount());
        indicesList.setAllIndices();
        assertEquals(null, indicesList.getSourceChange());
    }
    
    @Test
    public void testChangeNullOnDirectClearAllAfterItemsModified() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        ListChangeReport itemsReport = new ListChangeReport(items);
//        new PrintingListChangeListener("Items removed before", indicesList);
        items.remove(0);
        indicesList.clearAllIndices();
        assertEquals(null, indicesList.getSourceChange());
    }
    
    @Test
    public void testChangeNullOnDirectSetAfterItemsModified() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        ListChangeReport itemsReport = new ListChangeReport(items);
//        new PrintingListChangeListener("Items removed before", indicesList);
        items.remove(0);
        indicesList.setIndices(6);
        assertEquals(null, indicesList.getSourceChange());
    }
    
    @Test
    public void testChangeNullOnDirectClearAfterItemsModified() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        ListChangeReport itemsReport = new ListChangeReport(items);
//        new PrintingListChangeListener("Items removed before", indicesList);
        items.remove(0);
        indicesList.clearIndices(1, 2, 3, 4);
        assertEquals(null, indicesList.getSourceChange());
    }
    
    @Test
    public void testChangeNullOnDirectAddAfterItemsModified() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        ListChangeReport itemsReport = new ListChangeReport(items);
//        new PrintingListChangeListener("Items removed before", indicesList);
        items.remove(0);
        indicesList.addIndices(6);
        assertEquals(null, indicesList.getSourceChange());
    }
    
    
    /**
     * Since extraction of IndicesBase 
     * Changed behaviour of sourceChange: valid only from before endChange to after
     * endChange!
     */
    @Test @Ignore
    public void testChangeOnAddItems() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        ListChangeReport itemsReport = new ListChangeReport(items);
//        new PrintingListChangeListener("Items removed before", indicesList);
        items.add(2, "newItems");
        assertEquals(itemsReport.getLastChange(), indicesList.getSourceChange());
    }
    
    /**
     * Since extraction of IndicesBase 
     * Changed behaviour of sourceChange: valid only from before endChange to after
     * endChange!
     */
    @Test @Ignore
    public void testChangeOnRemoveItems() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        ListChangeReport itemsReport = new ListChangeReport(items);
//        new PrintingListChangeListener("Items removed before", indicesList);
        items.remove(0);
        assertEquals(itemsReport.getLastChange(), indicesList.getSourceChange());
    }
    
    /**
     * Since extraction of IndicesBase 
     * Changed behaviour of sourceChange: valid only from before endChange to after
     * endChange!
     */
    @Test @Ignore
    public void testChangeOnSetItems() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        ListChangeReport itemsReport = new ListChangeReport(items);
//        new PrintingListChangeListener("Items removed before", indicesList);
        items.set(3, "newItem");
        assertEquals(itemsReport.getLastChange(), indicesList.getSourceChange());
    }
    
    /**
     * Since extraction of IndicesBase 
     * Changed behaviour of sourceChange: valid only from before endChange to after
     * endChange!
     */
    @Test @Ignore
    public void testChangeOnClearItems() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        ListChangeReport itemsReport = new ListChangeReport(items);
//        new PrintingListChangeListener("Items removed before", indicesList);
        items.clear();
        assertEquals(itemsReport.getLastChange(), indicesList.getSourceChange());
    }
    
    
//---------------- test direct set/clear    
    
    @Test
    public void testSetAllIndices() {
        indicesList.setAllIndices();
        assertEquals(items.size(), indicesList.size());
        assertEquals(1, report.getEventCount());
    }
    
    /**
     * Test notification on setAll if there are already set indices.
     * Changed implementation due to RT-39776 (performance!), 
     * test assumption here is now incorrect - we now get a replaced
     * of the whole range.
     * 
     * new issue coordinate:
     * https://bugs.openjdk.java.net/browse/JDK-8093204
     * 
     * PENDING JW: what's expected?
     */
    @Test
    public void testSetAllNotificationIfHasSet() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.setAllIndices();
        report.prettyPrint();
        assertEquals(items.size(), indicesList.size());
        assertEquals(1, report.getEventCount());
        Change c = report.getLastChange();
        assertEquals("PENDING: what IS the correct expectation?", 
                4, getChangeCount(c, ChangeType.ADDED));
    }
    
    /**
     * Plain observableList setAll (with some of them already in the list) 
     * - we get a replaced of the complete.
     */
    @Test
    public void testSetAllPlainList() {
       ObservableList plain = FXCollections.observableArrayList("5-item", "3-item" , "1-item" ); 
       ListChangeReport report = new ListChangeReport(plain);
       plain.setAll(items);
       report.prettyPrint();
       Change c = report.getLastChange();
       assertEquals("PENDING: what IS the correct expectation?", 
               4, getChangeCount(c, ChangeType.ADDED));
    }
    
    /**
     * Optimized contains (same as core in selectedIndicesSeq), need to 
     * test if still working.
     * 
     * https://javafx-jira.kenai.com/browse/RT-39776
     * 
     */
    @Test
    public void testContains() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        for (int i : indices) {
            assertTrue("index must be contained: " + i, indicesList.contains(i));
        }
    }
    
    /**
     * Optimized contains (same as core in selectedIndicesSeq), need to 
     * test if still working.
     * 
     * https://javafx-jira.kenai.com/browse/RT-39776
     * 
     */
    @Test
    public void testContainsNot() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        int[] notContained = new int[] {0, 7, 4, 2};
        for (int i : notContained) {
            assertFalse("index must not be contained: " + i, indicesList.contains(i));
        }
    }
    
    @Test
    public void testSetIndices() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        int[] setIndices = new int[] {2, 4, 6, 7};
        indicesList.setIndices(setIndices);
        assertEquals(setIndices.length, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue(wasSingleReplaced(report.getLastChange()));
        Change c = report.getLastChange();
        c.reset();
        c.next();
        Arrays.sort(indices);
        List base = new ArrayList();
        for (int i = 0; i < indices.length; i++) {
            base.add(indices[i]);
        }
        assertEquals(base, c.getRemoved());
    }
    
    @Test
    public void testClearAll() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.clearAllIndices();
        assertEquals(0, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue(wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
//        report.prettyPrint();
        c.reset();
        c.next();
        Arrays.sort(indices);
        List base = new ArrayList();
        for (int i = 0; i < indices.length; i++) {
            base.add(indices[i]);
        }
        assertEquals(base, c.getRemoved());
    }
    @Test
    public void testClearSomeIndicesNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        int[] clear = new int[] {5, 1};
//        new PrintingListChangeListener("clearSomeNotification", indicesList);
        indicesList.clearIndices(clear);
        assertEquals(1, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertEquals("must be 2 disjoint removes", 2, getChangeCount(report.getLastChange(), ChangeType.REMOVED));
    }
    @Test
    public void testClearIndicesNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.clearIndices(indices);
        assertEquals(0, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue("must be single remove", wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.reset();
        c.next();
        Arrays.sort(indices);
        List base = new ArrayList();
        for (int i = 0; i < indices.length; i++) {
            base.add(indices[i]);
        }
        assertEquals(base, c.getRemoved());
    }
    
    @Test
    public void testClearUnsetNoNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.clearIndices(2);
        assertEquals(0, report.getEventCount());
    }
    
    @Test
    public void testAddAlreadySetNoNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.addIndices(indices[0]);
        assertEquals(0, report.getEventCount());
    }
    
    @Test
    public void testSetAlreadySetNoNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.setIndices(indices);
        assertEquals(0, report.getEventCount());
    }
    
    @Test
    public void testAddMoreNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        int[] more = new int[] { 2, 7, 4};
        indicesList.addIndices(more);
        
        assertEquals(1, report.getEventCount());
        assertEquals(3, getChangeCount(report.getLastChange()));
    }
    
    @Test
    public void testAddMultiple() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        assertEquals(indices.length, indicesList.size());
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals(1, report.getEventCount());
        assertTrue("got a single added", wasSingleAdded(report.getLastChange()));
    }
    
    @Test
    public void testAddSingle() {
        int index = 3;
        indicesList.addIndices(index);
        assertEquals(1, indicesList.size());
        assertEquals(index, indicesList.get(0).intValue());
        assertEquals(1, report.getEventCount());
    }

    /**
     * Source index is same as get, kind of.
     */
    @Test
    public void testSourceIndexInRange() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        assertEquals(indices.length, indicesList.size());
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("sourceIndex " + i, indices[i], indicesList.getSourceIndex(i));
        }
        
    }
    /**
     * Test sourceIndex if index off range.
     * Changed implementation to throw IndexOOB 
     * (off range access is always a programming error)
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void testSourceIndexOffRange() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        assertEquals(indices.length, indicesList.size());
        assertEquals(-1, indicesList.getSourceIndex(-1));
        assertEquals(-1, indicesList.getSourceIndex(indices.length));
    }
    
    /**
     * Test get if index off range.
     * Changed implementation to throw IndexOOB 
     * (off range access is always a programming error)
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOffRange() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        assertEquals(indices.length, indicesList.size());
        assertEquals(-1, indicesList.get(-1).intValue());
        assertEquals(-1, indicesList.get(indices.length).intValue());
    }
    
    
    @Test
    public void testAddEmpty() {
        indicesList.addIndices();
        assertEquals(0, report.getEventCount());
    }
    
    @Test
    public void testInitial() {
        assertEquals(0, indicesList.size());
    }
    
    /**
     * Sanity: properties of BitSet.
     */
    @Test
    public void testBitSet() {
        BitSet bitSet = new BitSet();
        int index = 500;
        bitSet.set(index);
        assertTrue(bitSet.get(index));
        assertEquals(1, bitSet.cardinality());
        assertEquals(index + 1, bitSet.length());
        assertFalse(bitSet.get(index * 2));
    }
    
    @Before
    public void setup() {
        items = createObservableList(true);
        indicesList = new IndicesList<>(items);
        report = new ListChangeReport(indicesList);
    }
    
    static final String[] DATA = {
        "9-item", "8-item", "7-item", "6-item", 
        "5-item", "4-item", "3-item", "2-item", "1-item"}; 

    protected ObservableList<String> createObservableList(boolean withData) {
        return withData ? FXCollections.observableArrayList(DATA)
                : FXCollections.observableArrayList();
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(IndicesListTest.class.getName());
}
