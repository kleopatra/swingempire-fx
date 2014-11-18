/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static org.junit.Assert.*;

import static org.junit.Assert.*;
import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;
import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;
import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;
import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;
import de.swingempire.fx.collection.IndexMappedList;
import de.swingempire.fx.collection.IndicesList;
import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.ListChangeReport;
import de.swingempire.fx.util.FXUtils.ChangeType;
import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

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
    public void testUpdate() {
        ObservableList<Person> base = Person.persons();
        ObservableList<Person> persons = FXCollections.observableList(base, p -> new Observable[] {p.firstNameProperty()});
        IndicesList<Person> indicesList = new IndicesList<>(persons);
        IndexMappedList<Person> indexedItems = new IndexMappedList<>(indicesList, base);
        int[] indices = new int[] {1, 3, 5};
        indicesList.addIndices(indices);
        ListChangeReport report = new ListChangeReport(indexedItems);
//        new PrintingListChangeListener("Set item at 3", indicesList);
        persons.get(3).setFirstName("newName");
        assertEquals(1, report.getEventCount());
    }
 
    @Test
    public void testItemRemovedBefore() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        
//        new PrintingListChangeListener("Items removed before", indexedItems);
        items.remove(0);
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indices[i] - 1;
        }
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, items.get(indices[i]), indexedItems.get(i));
        }
        assertEquals(1, report.getEventCount());
        assertEquals(1, getChangeCount(report.getLastChange(), ChangeType.REPLACED));
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
        assertTrue("singleReplaced ", wasSingleReplaced(report.getLastChange()));
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
        indexedItems = new IndexMappedList(indicesList, items);
        report = new ListChangeReport(indexedItems);
    }
    
    static final String[] DATA = {
        "9-item", "8-item", "7-item", "6-item", 
        "5-item", "4-item", "3-item", "2-item", "1-item"}; 

    protected ObservableList<String> createObservableList(boolean withData) {
        return withData ? FXCollections.observableArrayList(DATA)
                : FXCollections.observableArrayList();
    }


}
