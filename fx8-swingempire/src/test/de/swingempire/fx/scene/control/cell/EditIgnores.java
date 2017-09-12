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
            return true;
        }
        
    }
    /**
     */
    public static class IgnoreListEdit implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    private EditIgnores() {}
}
