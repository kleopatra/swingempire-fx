/*
 * Created on 30.09.2014
 *
 */
package de.swingempire.fx.junit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreCondition;

import static org.junit.Assert.*;

/**
 * Try to dig why conditionalIgnor makes test hang.
 * 
 * Does not work with subclassing the test. Decision for now:
 * defer to future, add code inside tests.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class DummyTestConditionalIgnore {

//    @ClassRule
//    public static TestRule classRule = new JavaFXThreadingRule();

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();
    protected int passing;
    protected int failing;
    public class NoSeparatorSupport implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return !supportsSeparators();
        }
        
    }
    
    @Test
    public void testWithoutIgnore() {
        assertEquals("failing test", failing, 1);
    }
    
    @Test
    @ConditionalIgnore(condition = NoSeparatorSupport.class)
    public void testWithIgnore() {
        assertEquals("failing test", failing, 1);
    }
    

    @Test
    public void testAnotherTest() {
        assertEquals("passing", passing, 1);
    }
    @Test
    public void testYetAnotherTest() {
        assertEquals("failing", failing, 1);
    }
    
    @Before
    public void setUp() {
        passing = 1;
        failing = 5;
    }
    /**
     * @return
     */
    public boolean supportsSeparators() {
        return false;
    }


}
