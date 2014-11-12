/*
 * Created on 04.10.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import com.codeaffine.test.ConditionalIgnoreRule.IgnoreCondition;

/**
 * Rules for ignoring certain test in selectionIssues.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SelectionIgnores {
    
    

    /**
     * Working with static class that doesn't require access to running
     * test class
     */
    public static class IgnoreRT26079 implements IgnoreCondition {
    
        @Override
        public boolean isSatisfied() {
            return false;
        }
        
    }

    /**
     * Ignore tests that try to test state on filling the items
     * dynamically on showing the popup - hard to test.
     *  
     * @author Jeanette Winzenburg, Berlin
     */
    public static class IgnoreDynamicItems implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    /**
     * Ignore doc errors/ambiguities
     * @author Jeanette Winzenburg, Berlin
     */
    public static class IgnoreDocErrors implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return false;
        }
        
    }
    
    /**
     * Occasionally, a test fails with an error that doesn't show up in the
     * list but only on the console. Not understood, might be a 
     * threading issue?
     *  
     * @author Jeanette Winzenburg, Berlin
     */
    public static class IgnoreExternalError implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    /**
     * Ignore tests that require a custom selectionModel (doesn't make sense
     * in adapters)
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class IgnoreSetSelectionModel implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
        
        
    }
    
    // KEEP to not forget: nested classes in tests are not working
    // in classes that extend the defining
    // not working as we need to access the running test class
//    public class NoSeparatorSupport implements IgnoreCondition {
//
//        @Override
//        public boolean isSatisfied() {
//            return !supportsSeparators();
//        }
//        
//    }
//

    private SelectionIgnores() {};
}
