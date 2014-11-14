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

import de.swingempire.fx.collection.SelectedIndicesList;
import de.swingempire.fx.util.ListChangeReport;

import static org.junit.Assert.*;
import static de.swingempire.fx.util.FXUtils.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class SelectedIndicesTest {

    SelectedIndicesList<String> indicesList;
    ListChangeReport report;
    ObservableList<String> items;
    
    @Test
    public void testItemAdded() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.selectIndices(indices);
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
    public void testItemRemoved() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.selectIndices(indices);
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
    public void testItemReplaced() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.selectIndices(indices);
        report.clear();

        items.set(0, "newItem");
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        LOG.info("" + report.getLastChange());
        assertEquals("selectedIndices unchanged", 0, report.getEventCount());
    }
    
    @Test
    public void testUnselect() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.selectIndices(indices);
        report.clear();
        indicesList.unselectIndices(indices);
        assertEquals(0, indicesList.size());
        assertEquals(1, report.getEventCount());
        assertTrue("must be single remove", wasSingleRemoved(report.getLastChange()));
    }
    
    @Test
    public void testUnselectNoNotificationOnUnselected() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.selectIndices(indices);
        report.clear();
        indicesList.unselectIndices(2);
        assertEquals(0, report.getEventCount());
    }
    
    @Test
    public void testSelectNoNotificationOnReselect() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.selectIndices(indices);
        report.clear();
        indicesList.selectIndices(indices[0]);
        assertEquals(0, report.getEventCount());
    }
    
    @Test
    public void testSelectMoreNotification() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.selectIndices(indices);
        report.clear();
        int[] more = new int[] { 2, 7, 4};
        indicesList.selectIndices(more);
        
        assertEquals(1, report.getEventCount());
        assertEquals(3, getChangeCount(report.getLastChange()));
    }
    
    @Test
    public void testMultipleSelect() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.selectIndices(indices);
        assertEquals(indices.length, indicesList.size());
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            assertEquals("expected value at " + i, indices[i], indicesList.get(i).intValue());
        }
        assertEquals(1, report.getEventCount());
        assertTrue("got a single added", wasSingleAdded(report.getLastChange()));
    }
    
    @Test
    public void testSingleSelect() {
        int index = 3;
        indicesList.selectIndices(index);
        assertEquals(1, indicesList.size());
        assertEquals(index, indicesList.get(0).intValue());
        assertEquals(1, report.getEventCount());
    }
    
    @Test
    public void testEmptySelect() {
        indicesList.selectIndices();
    }
    @Test
    public void testInitial() {
        assertEquals(0, indicesList.size());
    }
    
    @Before
    public void setup() {
        items = createObservableList(true);
        indicesList = new SelectedIndicesList<>(items);
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
            .getLogger(SelectedIndicesTest.class.getName());
}
