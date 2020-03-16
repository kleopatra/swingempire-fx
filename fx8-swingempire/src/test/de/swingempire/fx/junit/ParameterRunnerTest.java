/*
 * Created on 16.03.2020
 *
 */
package de.swingempire.fx.junit;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.*;

/**
 * Quick check: new instance per run? Yes - for Parameterized it's specified:
 * "When running a parameterized test class, instances are created for the cross-product 
 * of the test methods and the test data elements".
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(Parameterized.class)
public class ParameterRunnerTest {

    private static int count;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {     
                 { 0, 0 }, 
                 { 1, 1 }, 
                 { 2, 1 }, 
                 { 3, 2 }, 
                 { 4, 3 }, 
                 { 5, 5 }, 
                 { 6, 8 }  
           });
    }

    int value;
    int first;
    int second;
    
    public ParameterRunnerTest(int first, int second) {
        this.first = first;
        this.second = second;
        count++;
    }
    
    /**
     * silly tests, just to see if we have the same instance for both.
     */
    @Test
    public void testSetupIncrements() {
        System.out.println("id " + this.hashCode() + " counter: " + count);
        assertEquals(first * second - first, value);
    }
    
    @Test
    public void testSetupIncrementsInvers() {
        System.out.println("id " + this.hashCode()+ " counter: " + count);
        assertEquals(first * second, value + first);
    }
    
    @Before
    public void setup() {
        value = first * second - first;
    }
    
    
}
