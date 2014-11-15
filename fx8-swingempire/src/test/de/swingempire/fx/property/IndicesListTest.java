/*
 * Created on 14.11.2014
 *
 */
package de.swingempire.fx.property;

import java.util.Arrays;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.collection.IndicesList;
import de.swingempire.fx.util.ListChangeReport;

import static org.junit.Assert.*;
import static de.swingempire.fx.util.FXUtils.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class IndicesListTest {

    IndicesList<String> indicesList;
    ListChangeReport report;
    ObservableList<String> items;

//------------ test modifications of backing list
    
    @Test
    public void testItemAddedBefore() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        
        items.add(0, "newItem");
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indices[i] + 1;
        }
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals(1, report.getEventCount());
    }
    
    @Test
    public void testItemRemovedBefore() {
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
    public void testItemReplacedBefore() {
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
    
    @Test
    public void testItemReplacedAt() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();

        items.set(3, "newItem");
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals("selectedIndices unchanged", 1, report.getEventCount());
    }
 
//---------------- test direct set/clear    
    @Test
    public void testClearSomeNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        int[] clear = new int[] {5, 1};
        indicesList.clearIndices(clear);
        assertEquals(1, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertEquals("must be single remove", 2, getChangeCount(report.getLastChange()));
    }
    @Test
    public void testClearNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        report.clear();
        indicesList.clearIndices(indices);
        assertEquals(0, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue("must be single remove", wasSingleRemoved(report.getLastChange()));
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
     */
    @Test
    public void testSourceIndexOffRange() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
        assertEquals(indices.length, indicesList.size());
        assertEquals(-1, indicesList.getSourceIndex(-1));
        assertEquals(-1, indicesList.getSourceIndex(indices.length));
    }
    
    /**
     * Test get if index off range.
     */
    @Test
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
