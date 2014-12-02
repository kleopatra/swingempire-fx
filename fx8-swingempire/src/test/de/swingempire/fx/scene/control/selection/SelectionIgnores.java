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
     * Ignore notification issues due to correlated properties.
     * https://javafx-jira.kenai.com/browse/RT-39552
     * http://stackoverflow.com/q/27186755/203657):
     * 
     */
    public static class IgnoreCorrelated implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    /**
     * Condition to ignore tests which involve anchor.Used only while adding a new
     * SelectionModel - should be enable again when a evolution round is complete.
     * 
     */
    public static class IgnoreAnchor implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    /**
     * Condition to ignore tests which involve focus.Used only while adding a new
     * SelectionModel - should be enable again when a evolution round is complete.
     * 
     * hmm .. not helpful, focus is interwoven with selectedIndex in most tests
     */
    public static class IgnoreFocus implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return false;
        }
        
    }
    /** 
     * Ignore tests for behaviour classes. Used only while adding a new
     * SelectionModel - should be enable again when a evolution round is complete.
     */
    public static class IgnoreBehavior implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }

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
            return true;
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

}
