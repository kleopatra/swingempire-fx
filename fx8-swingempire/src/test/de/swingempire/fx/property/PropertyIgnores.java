/*
 * Created on 31.10.2014
 *
 */
package de.swingempire.fx.property;

import com.codeaffine.test.ConditionalIgnoreRule.IgnoreCondition;

/**
 * Ignores for Property/Observable related tests
 * @author Jeanette Winzenburg, Berlin
 */
public class PropertyIgnores {

    /**
     * Ignore tests around TreeView.getRow() 
     * Reported as https://javafx-jira.kenai.com/browse/RT-39661
     * 
     * Tests still failing? Dig!
     */
    public static class IgnoreTreeGetRow implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    /**
     * Administrative: ignore not yet implemented.
     * 
     * - sorting of tree
     */
    public static class IgnoreNotYetImplemented implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    /**
     * Object property doesn't fire change if newVale.equals(oldValue), 
     * That's by design (impl?), nothing we can do about
     * <p>
     * Any test failing due to this is incorrect! Remove
     * 
     */
    public static class IgnoreEqualsNotFire implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return false;
        }
        
    }
    
    /**
     * Used for reported bugs that are still open.
     * 
     */
    public static class IgnoreReported implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return false;
        }
        
    }
     
}
