/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils.ChangeType;
import de.swingempire.fx.util.ListChangeReport;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class IndexMappedListTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    private IndicesList<String> indicesList;
    private ObservableList<String> items;

    private IndexMappedList<String> indexedItems;
    private ListChangeReport report;

    
//----------------- test change in indexMapped after change in items    

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
        Object[] permItems = new Object[] {items.get(3), items.get(5), items.get(7)}; 
        LOG.info("sortedItems" + itemsReport.getLastChange());
//        LOG.info("sorted: " + report.getLastChange());
        for (int i = 0; i < permutated.length; i++) {
            assertEquals("permutated at" + i, permItems[i], indexedItems.get(i));
        }
//        report.prettyPrint();
        assertEquals(1, report.getEventCount());
        assertEquals(1, getChangeCount(report.getLastChange(), ChangeType.PERMUTATED));
        Change c = report.getLastChange();
        c.reset();
        c.next();
        assertEquals("permutated all", permutated.length, c.getTo() - c.getFrom());
//        LOG.info("" + indicesList);
    }

    @Test
    public void testItemsUpdate() {
        ObservableList<Person> base = Person.persons();
        ObservableList<Person> persons = FXCollections.observableList(base, p -> new Observable[] {p.firstNameProperty()});
        IndicesList<Person> indicesList = new IndicesList<>(persons);
        IndexMappedList<Person> indexedItems = new IndexMappedList<>(indicesList);
        int[] indices = new int[] {1, 3, 5};
        indicesList.addIndices(indices);
        ListChangeReport report = new ListChangeReport(indexedItems);
//        new PrintingListChangeListener("Set item at 3", indicesList);
        persons.get(3).setFirstName("newName");
        assertEquals(1, report.getEventCount());
    }

    @Test
    public void testItemsClear() {
        int index = 0;
        indicesList.addIndices(index);
        Object item = items.get(index);
        
        report.clear();
//        new PrintingListChangeListener("clear", indexedItems);
        items.clear();
        assertEquals(1, report.getEventCount());
        assertTrue(wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.next();
        assertEquals(1, c.getRemovedSize());
        assertEquals(item, c.getRemoved().get(0));
    }
    
    @Test
    public void testItemsSetAllSameSize() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        ObservableList<String> other = createObservableList(true);
        // make it not equal
        other.set(0, "otherItem");
        items.setAll(other);
//        report.prettyPrint();
        assertEquals("all cleared", 0, indexedItems.size());
        assertEquals(1, report.getEventCount());
        assertTrue("expected single removed, but was " + report.getLastChange(), 
                wasSingleRemoved(report.getLastChange()));
    }

    @Test
    public void testItemsRemoveSingleSelected() {
        int index = 0;
        indicesList.addIndices(index);
        Object selectedItem = items.get(indicesList.get(index));
        report.clear();
        items.remove(0);
//        report.prettyPrint();
        assertEquals(1, report.getEventCount());
        assertTrue(wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.next();
        assertEquals(selectedItem, c.getRemoved().get(0));
    }

    /**
     * Trying to test remove of a range where and old item is removed but
     * the new index of another is accidentally the old index.
     */
    @Test
    public void testItemsRemoveRange() {
        int[] indices = new int[] {1, 3, 8};
        Object removedIndexed = items.get(3);
        indicesList.setIndices(indices);
        report.clear();
        // removing such that old 8 -> new 3
        items.remove(2, 7);
//        report.prettyPrint();
        int[] expected = new int[] {1, 3};
        for (int i = 0; i < expected.length; i++) {
            assertTrue(indicesList.contains(expected[i]));
        }
        assertEquals("single event", 1, report.getEventCount());
        assertTrue("expected single remove but was: " + report.getLastChange(), wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.next();
        assertEquals("removed size", 1, c.getRemovedSize());
        assertEquals("removed indexedItem", removedIndexed, c.getRemoved().get(0));
    }
    /**
     * items are unchanged if removed above ... only the indices are replaced
     * by new values! At the very least (if we really want to pass-on the replaced),
     * addedSubList.equals(removed)
     */
    @Test
    public void testItemsRemovedBefore() {
        assertAddRemoveBefore(true);
    }

    /**
     * Nothing changed in indexedItems!.
     * 
     */
    @Test
    public void testItemAddedBefore() {
        assertAddRemoveBefore(false);
    }

    protected void assertAddRemoveBefore(boolean remove) {
            int[] indices = new int[] { 3, 5, 1};
            indicesList.addIndices(indices);
    //        report.prettyPrint();
            report.clear();
            if (remove) {
                items.remove(0);
                for (int i = 0; i < indices.length; i++) {
                    indices[i] = indices[i] - 1;
                }
            } else {
                items.add(0, "something");
                for (int i = 0; i < indices.length; i++) {
                    indices[i] = indices[i] + 1;
                }
            }
    //        report.prettyPrint();
            Arrays.sort(indices);
            for (int i = 0; i < indices.length; i++) {
                assertEquals("expected value at " + i, items.get(indices[i]), indexedItems.get(i));
            }
            if (report.getEventCount() > 0) {
                assertEquals(1, getChangeCount(report.getLastChange(), ChangeType.REPLACED));
                Change c = report.getLastChange();
                c.next();
                assertEquals(c.getAddedSize(), c.getRemovedSize());
                assertEquals("added/removed must be equal", c.getAddedSubList(), c.getRemoved());
                fail("fail anyway: we should not get notification if selectedItems didn't change");
            }
        }

    @Test
    public void testItemsRemoveRangeStart() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        // items we expect to appear in removed
        List removedItems = new ArrayList();
        removedItems.add(items.get(1));
        removedItems.add(items.get(3));
        report.clear();
        // remove items at 1...3, inclusive
        items.remove(1, 4);
        assertEquals(1, indexedItems.size());
//        report.prettyPrint();
        indices = new int[] {2};
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, items.get(indices[i]), indexedItems.get(i));
        }
        assertEquals(1, report.getEventCount());
        assertTrue("expected single removed but was" + report.getLastChange(), 
                wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.next();
        assertEquals(2, c.getRemovedSize());
        assertEquals(removedItems, c.getRemoved());
        
    }
    
    @Test
    public void testItemsRemoveRangeEnd() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        // items we expect to appear in removed
        List removedItems = new ArrayList();
        removedItems.add(items.get(3));
        removedItems.add(items.get(5));
        report.clear();
        // remove items at 3...5, inclusive
        items.remove(3, 6);
        assertEquals(1, indexedItems.size());
//        report.prettyPrint();
        indices = new int[] {1};
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, items.get(indices[i]), indexedItems.get(i));
        }
        assertEquals(1, report.getEventCount());
        assertTrue("expected single removed but was" + report.getLastChange(), 
                wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.next();
        assertEquals(2, c.getRemovedSize());
        assertEquals(removedItems, c.getRemoved());
        
    }

    
    @Test
    public void testItemsRemovedBetweenReally() {
        int[] indices = new int[] { 3, 5, 1 };
        indicesList.addIndices(indices);
        report.clear();
        items.removeAll(items.get(2), items.get(4));
        assertEquals(indices.length, indexedItems.size());
        indices = new int[] { 1, 2, 3 };
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, items.get(indices[i]),
                    indexedItems.get(i));
        }
        if (report.getEventCount() > 0) {
//             report.prettyPrint();
            assertEquals(1, report.getEventCount());
            assertTrue("expected single replaced but was" + report.getLastChange(),
                    wasSingleReplaced(report.getLastChange()));
            Change c = report.getLastChange();
            c.next();
            assertEquals(2, c.getAddedSize());
            assertEquals(c.getAddedSize(), c.getRemovedSize());
            assertEquals("added/removed must be equal", c.getAddedSubList(), c.getRemoved());
            fail("fail anyway: we should not get notification if selectedItems didn't change");
        } 
    }

    /**
     * Single real remove of selected item, expect a change fired from 
     * indexedItems.
     */
    @Test
    public void testItemsRemovedAtLast() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        Arrays.sort(indices);
        report.clear();
        Object removedItem = items.get(indices[2]);
//        LOG.info("indexed before" + indexedItems);
        items.remove(removedItem);
        assertEquals(indices.length -1, indexedItems.size());
        // expected indices when removing the middle
        indices = new int[] {1, 3};
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, items.get(indices[i]), indexedItems.get(i));
        }
        assertEquals(1, report.getEventCount());
        assertTrue("expected single removed but was" + report.getLastChange(),
                wasSingleRemoved(report.getLastChange()));
//        report.prettyPrint();
        Change c = report.getLastChange();
        c.next();
        assertEquals(1, c.getRemovedSize());
        assertEquals(removedItem, c.getRemoved().get(0));
    }
    
    /**
     * Single real remove of selected item, expect a change fired from 
     * indexedItems.
     */
    @Test
    public void testItemsRemovedAtMiddle() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        Arrays.sort(indices);
        report.clear();
        Object removedItem = items.get(indices[1]);
//        LOG.info("indexed before" + indexedItems);
        items.remove(removedItem);
        assertEquals(indices.length -1, indexedItems.size());
        // expected indices when removing the middle
        indices = new int[] {1, 4};
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, items.get(indices[i]), indexedItems.get(i));
        }
        assertEquals(1, report.getEventCount());
        assertTrue("expected single removed but was" + report.getLastChange(),
                wasSingleRemoved(report.getLastChange()));
//        report.prettyPrint();
        Change c = report.getLastChange();
        c.next();
        assertEquals(1, c.getRemovedSize());
        assertEquals(removedItem, c.getRemoved().get(0));
    }
    
    /**
     * Single real remove of selected item, expect a change fired from 
     * indexedItems.
     */
    @Test
    public void testItemsRemovedAtFirst() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        Arrays.sort(indices);
        report.clear();
        Object removedItem = items.get(indices[0]);
//        LOG.info("indexed before" + indexedItems);
        items.remove(removedItem);
        assertEquals(indices.length -1, indexedItems.size());
        indices = Arrays.copyOfRange(indices, 1, 3);
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indices[i] - 1;
        }
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, items.get(indices[i]), indexedItems.get(i));
        }
        assertEquals(1, report.getEventCount());
//        report.prettyPrint();
        assertTrue("expected single removed but was" + report.getLastChange(),
                wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.next();
        assertEquals(1, c.getRemovedSize());
        assertEquals(removedItem, c.getRemoved().get(0));
    }

    @Test
    public void testItemReplacedAt() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        items.set(3, "newItem");
        Arrays.sort(indices);
        assertEquals("newItem", indexedItems.get(1));
        assertEquals("selectedIndices unchanged", 1, report.getEventCount());
//        report.prettyPrint();
        assertTrue("singleReplaced ", wasSingleReplaced(report.getLastChange()));
        Change c = report.getLastChange();
        c.next();
        assertEquals("newItem", c.getAddedSubList().get(0));
    }
 
    /**
     * Trying to find out why force-failed treeIndexMappedListTest - use same
     * test here
     * 
     * @see TreeIndexMappedListTest#testSetCollapsedChildAt()
     * @see TreeIndexMappedListTest#testSetExpandedChildAt()
     * 
     */
    @Test
    public void testSetItemAtIndexed() {
        int index = 3;
        indicesList.setIndices(index); 
        report.clear();
        String element = "replaced-element-at-3";
        items.set(index, element);
        assertEquals(element, indexedItems.get(0));
        assertEquals("indices unchanged", index, indicesList.get(0).intValue());
        assertTrue("expected singleReplaced but was: " + report.getLastChange(), 
                wasSingleReplaced(report.getLastChange()));
        
    }
    
//------------------- test change in indexMapped after direct setting of indicesList    

    @Test
    public void testSetIndices() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        int[] setIndices = new int[] {2, 4, 6, 7};
        indicesList.setIndices(setIndices);
        assertEquals(setIndices.length, indexedItems.size());
        assertEquals(1, report.getEventCount());
        assertTrue(wasSingleReplaced(report.getLastChange()));
        Change c = report.getLastChange();
        c.reset();
        c.next();
        Arrays.sort(indices);
        List base = new ArrayList();
        for (int i = 0; i < indices.length; i++) {
            base.add(items.get(indices[i]));
        }
        assertEquals(base, c.getRemoved());
        
    }

    @Test
    public void testClearAll() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.clearAllIndices();
        assertEquals(0, indexedItems.size());
        assertEquals(1, report.getEventCount());
        assertTrue(wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.reset();
        c.next();
        Arrays.sort(indices);
        List base = new ArrayList();
        for (int i = 0; i < indices.length; i++) {
            base.add(items.get(indices[i]));
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
        assertEquals(1, indexedItems.size());
        assertEquals(1, report.getEventCount());
        assertEquals("must be 2 disjoint removes", 2, getChangeCount(report.getLastChange(), ChangeType.REMOVED));
    }

    @Test
    public void testClearIndicesNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.clearIndices(indices);
        assertEquals(0, indexedItems.size());
        assertEquals(1, report.getEventCount());
        assertTrue("must be single remove", wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.reset();
        c.next();
        Arrays.sort(indices);
        List base = new ArrayList();
        for (int i = 0; i < indices.length; i++) {
            base.add(items.get(indices[i]));
        }
        assertEquals(base, c.getRemoved());
    }

    @Test
    public void testClearIndexNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        Arrays.sort(indices);
        indicesList.clearIndices(indices[0]);
        assertEquals(indices.length - 1, indexedItems.size());
        assertEquals(1, report.getEventCount());
        assertTrue("must be single remove", wasSingleRemoved(report.getLastChange()));
        Change c = report.getLastChange();
        c.reset();
        c.next();
        List base = new ArrayList();
        base.add(items.get(indices[0]));
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
    public void testAddAlreadySetNoNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.addIndices(indices[0]);
        assertEquals(0, report.getEventCount());
    }
    
   
    @Test
    public void testSetMultiple() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        assertEquals(indices.length, indicesList.size());
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, items.get(indices[i]), indexedItems.get(i));
        }
        assertEquals(1, report.getEventCount());
        assertTrue("got a single added", wasSingleAdded(report.getLastChange()));
//        LOG.info("" + indexedItems);

    }
    
    @Test
    public void testSetIndex() {
        int index = 3;
        indicesList.setIndices(index);
        assertEquals(1, report.getEventCount());
        assertTrue(wasSingleAdded(report.getLastChange()));
        Change c = report.getLastChange();
        c.reset();
        c.next();
        assertSame(items.get(index), c.getAddedSubList().get(0));
    }
    
    @Test
    public void testGet() {
        int index = 3;
        indicesList.setIndices(index);
        assertEquals(1, indexedItems.size());
        assertEquals(0, indexedItems.getSourceIndex(0));
        assertSame(items.get(index), indexedItems.get(0));
    }
    
    @Test
    public void testInitial() {
        assertSame(indicesList, indexedItems.getSource());
        assertEquals(0, indexedItems.size());
    }

    @Before
    public void setup() {
        items = createObservableList(true);
        indicesList = new IndicesList<>(items);
        indexedItems = new IndexMappedList(indicesList);
        report = new ListChangeReport(indexedItems);
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
            .getLogger(IndexMappedListTest.class.getName());
}
