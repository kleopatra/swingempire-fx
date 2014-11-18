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
     * Administrative: ignore not yet implemented.
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
     * 
     */
    public static class IgnoreEqualsNotFire implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    /**
     * Used for reported bugs that are still open.
     * 
     */
     public static class IgnoreReported implements IgnoreCondition {
    
        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }


     
}
