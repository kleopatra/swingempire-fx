/*
 * Created on 25.05.2014
 *
 */
package de.swingempire.fx.property;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class BufferTest {

    private BufferedObjectProperty buffer;
    private Trigger trigger;
    private StringProperty property;
    private String propertyValue;

    /**
     * BooleanProperty converts a null to false, and logs an info with NPE ...
     * The test passes, though, due to the automatic conversion
     */
    @Test @Ignore
    public void testBooleanNull() {
        BooleanProperty simple = new SimpleBooleanProperty();
        simple.set(true);
        // wrong assumption, the initial value is false (primitive in BooleanPropertyBase)
        // assertEquals(null, simple.get());
        ObjectProperty property = new SimpleObjectProperty();
        simple.bindBidirectional(property);
        assertEquals(false, simple.get());
        simple.set(true);
        assertEquals(true, property.getValue());
    }
    @Test
    public void testName() {
        String name = "myName";
        BufferedObjectProperty buffer = new BufferedObjectProperty(name, trigger);
        assertEquals(name, buffer.getName());
    }
    
    @Test
    public void testFlush() {
        buffer.setSubject(property);
        String value = "value";
        buffer.set(value);
        trigger.triggerFlush();
        assertEquals(propertyValue, property.get());
        assertTrue(buffer.isBound());
        assertFalse(buffer.isBuffering());
    }
    @Test
    public void testCommit() {
        buffer.setSubject(property);
        String value = "value";
        buffer.set(value);
        trigger.triggerCommit();
        assertEquals(value, property.get());
        assertTrue(buffer.isBound());
        assertFalse(buffer.isBuffering());
    }
    
    /**
     * Can't disable super's unbind without giving up all the nice 
     * automatics .. aborted trying, strong doc warning instead.
     */
    @Test @Ignore
    public void testPreventUnbind() {
        buffer.setSubject(property);
        buffer.unbind();
        assertTrue("buffer must still be bound after external unbind", buffer.isBound());
    }
    
    @Test (expected = IllegalStateException.class)
    public void testBindDifferentObservable() {
        buffer.setSubject(property);
        buffer.bind(new SimpleStringProperty("other"));
    }
    
    @Test
    public void testStartBuffering() {
        buffer.setSubject(property);
        String value = "value";
        buffer.set(value);
        assertEquals(value, buffer.get());
        assertEquals("buffered property must be unchanged ", propertyValue, property.get());
        assertTrue(buffer.isBuffering());
    }
    
    @Test
    public void testSubjectNull() {
        buffer.setSubject(property);
        buffer.setSubject(null);
    }
    
    @Test
    public void testSubject() {
        buffer.setSubject(property);
        assertTrue(buffer.isBound());
        assertEquals(property.get(), buffer.get());
        // JW: goodies doesn't touch the buffering state
        // JW: doesn't really test anything here, didn't buffer before
        assertFalse(buffer.isBuffering());
    }
    
    @Test (expected = NullPointerException.class)
    public void testSetTriggerNull() {
        buffer.setTrigger(null);
    }
    @Test
    public void testSetTrigger() {
        Trigger other = new Trigger();
        buffer.setTrigger(other);
        assertSame(other, buffer.getTrigger());
    }
    
    @Test
    public void testInitial() {
        assertFalse(buffer.isBuffering());
        assertSame(trigger, buffer.getTrigger());
    }
    
    @Test
    public void testInitialBoolean() {
        BufferedObjectProperty<Boolean> b = new BufferedObjectProperty<Boolean>("myname", trigger, false);
        assertFalse("must not be buffering initially", b.isBuffering());
        BooleanProperty wrapper = BooleanProperty.booleanProperty(b);
        assertFalse("must not be buffering after wrapping inot booleanProperty ", b.isBuffering());
    }
    
    @Before
    public void setup() {
        trigger = new Trigger();
        buffer = new BufferedObjectProperty(trigger);
        propertyValue = "dummy";
        property = new SimpleStringProperty(propertyValue);
    }
}
