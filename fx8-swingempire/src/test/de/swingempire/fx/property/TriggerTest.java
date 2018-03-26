/*
 * Created on 24.05.2014
 *
 */
package de.swingempire.fx.property;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import de.swingempire.fx.util.ChangeReport;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class TriggerTest {

    private Trigger triggerValue;
    private ChangeReport report;

    @Test
    public void testTriggerCommitTwice() {
        triggerValue.triggerCommit();
        report.clear();
        triggerValue.triggerCommit();
        assertEquals("value must be true after commit", Boolean.TRUE, triggerValue.getValue());
        // JW: it's 3 including the first change from null to true
        assertEquals("change event count", 2, report.getEventCount());
    }
    
    @Test
    public void testTriggerFlushTwice() {
        triggerValue.triggerFlush();
        report.clear();
        triggerValue.triggerFlush();
        assertEquals("value must be true after flush", Boolean.FALSE, triggerValue.getValue());
        // JW: it's 3 including the first change from null to true
        assertEquals("change event count", 2, report.getEventCount());
    }
    @Test
    public void testTriggerCommit() {
        triggerValue.triggerCommit();
        assertEquals("value must be true after commit", Boolean.TRUE, triggerValue.getValue());
        assertEquals("change event count", 1, report.getEventCount());
    }
    
    @Test
    public void testTriggerFlush() {
        triggerValue.triggerFlush();
        assertEquals("value must be true after flush", Boolean.FALSE, triggerValue.getValue());
        assertEquals("change event count", 1, report.getEventCount());
    
    }
    
    @Test
    public void testTriggerValueInitial() {
        assertNull(triggerValue.getValue());
    }
    
    @Before
    public void setup() {
        triggerValue = new Trigger();
        report = new ChangeReport(triggerValue);
    }
}
