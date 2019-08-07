/*
 * Created on 07.08.2019
 *
 */
package de.swingempire.fx;

import com.codeaffine.test.ConditionalIgnoreRule.IgnoreCondition;

/**
 * Contains Ignores that are likely to apply across all test packages.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class GlobalIgnores {

    /**
     * Ignores tests of dead ends (that is unlikely to be made passing, ever)
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class IgnoreWontfix implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    /**
     * Ignores tests used for debugging (logging).
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class IgnoreDebug implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    /**
     * Ignores TBD failures. 
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class IgnoreTBD implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    /**
     * Ignores failures to core fx issues.
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class IgnoreSpecUnclear implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    /**
     * Ignores failures to core fx issues.
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class IgnoreCore implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    /**
     * Ignore test failure that shouldn't fail (because the tested property behaves
     * as expected) - could be due to fx threading?  
     */
    public static class IgnoreFailureNotUnderstood implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
    }

}
