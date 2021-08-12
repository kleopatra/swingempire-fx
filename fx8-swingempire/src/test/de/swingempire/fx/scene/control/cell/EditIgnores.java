/*
 * Created on 12.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import com.codeaffine.test.ConditionalIgnoreRule.IgnoreCondition;

/**
 * Contains ignores around editing tests of virtual controls and their cells.
 * 
 * These are for temporary to focus on a single type of virtual control .. too
 * many failures in core
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class EditIgnores {

    /**
     * Standalone test variants are created for bug reports. Typically,
     * are covered (should be) in local infrastructure, so it's safe 
     * to ignore after reporting.
     * 
     */
    public static class IgnoreStandalone implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return false;
        }
        
    }
    /**
     */
    public static class IgnoreTreeEdit implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return false;
        }
        
    }
    /**
     */
    public static class IgnoreTableEdit implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return false;
        }
        
    }
    /**
     */
    public static class IgnoreListEdit implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return false;
        }
        
    }
    
    private EditIgnores() {}
}
