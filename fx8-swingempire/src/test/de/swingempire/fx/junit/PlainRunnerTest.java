/*
 * Created on 16.03.2020
 *
 */
package de.swingempire.fx.junit;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Quick check: new instance per run?
 * @author Jeanette Winzenburg, Berlin
 */
public class PlainRunnerTest {

    private static final int MAX = 3;
    private static int count;
    
    int value = MAX;
    
    public PlainRunnerTest() {
        count++;
    }
    
    /**
     * silly tests, just to see if we have the same instance for both.
     */
    @Test
    public void testSetupIncrements() {
        System.out.println("id " + this.hashCode() + " counter: " + count);
        assertEquals(MAX + 1, value);
    }
    
    @Test
    public void testSetupIncrementsInvers() {
        System.out.println("id " + this.hashCode()+ " counter: " + count);
        assertEquals(MAX, value -1);
    }
    
    @Before
    public void setup() {
        value = MAX + 1;
    }
    
    
}
