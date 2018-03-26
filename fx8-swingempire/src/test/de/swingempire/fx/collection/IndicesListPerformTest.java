/*
 * Created on 21.04.2015
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.util.ListChangeReport;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

/**
 * Separate test to play with performance improvement in IndicesBase.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class IndicesListPerformTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    IndicesList<String> indicesList;
    ListChangeReport report;
    ObservableList<String> items;

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
        report.prettyPrint();
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
    public void testAddMultiple() {
        int[] indices = new int[] { 3, 5, 1};
        indicesList.addIndices(indices);
//        report.prettyPrint();
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
    @Test
    public void testInitial() {
        assertTrue(indicesList.isEmpty());
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


}
