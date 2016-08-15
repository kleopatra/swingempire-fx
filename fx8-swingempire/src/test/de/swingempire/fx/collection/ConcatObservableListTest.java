/*
 * Created on 02.06.2016
 *
 */
package de.swingempire.fx.collection;

import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import de.swingempire.fx.util.FXUtils;
import de.swingempire.fx.util.ListChangeReport;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ConcatObservableListTest {

    ObservableList sourceA;
    ObservableList sourceB;
    ConcatObservableList concat;
    
    @Test
    public void testNotificationB() {
        ListChangeReport report = new ListChangeReport(concat);
        sourceB.add(0, "B0");
        assertEquals(1, report.getEventCount());
        FXUtils.wasSingleAdded(report.getLastChange());
        Change c = report.getLastChange();
        c.next();
        assertEquals(2, c.getFrom());
        assertEquals("B0", c.getAddedSubList().get(0));
    }
    
    @Test
    public void testNotificationA() {
        LOG.info("concat: " + concat);
        ListChangeReport report = new ListChangeReport(concat);
        sourceA.add("A3");
        LOG.info("dummx: " + report.getLastChange());
        report.prettyPrint();
        assertEquals(1, report.getEventCount());
        FXUtils.wasSingleAdded(report.getLastChange());
        Change c = report.getLastChange();
        c.next();
        assertEquals(2, c.getFrom());
        assertEquals("A3", c.getAddedSubList().get(0));
    }
    
    @Test
    public void testSize() {
        assertEquals(sourceA.size() + sourceB.size(), concat.size());
    }
    
    @Before
    public void setup() {
        sourceA = FXCollections.observableArrayList("A1", "A2");
        sourceB = FXCollections.observableArrayList("B1", "B2", "B3");
        concat = new ConcatObservableList<>(sourceA, sourceB);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ConcatObservableListTest.class.getName());
}
