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
    
    
//------------------ Table-related
    
    /**
     * Ignore tests that are failing if cellSelection enabled. 
     * This is used only temporarily to check test assumptions!
     */
    public static class IgnoreTableCellSelection implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    

    
    /**
     * Ignore tests related to uncontained selectedItem in selection. 
     * This is used only to keep the # of test failures low.
     */
    public static class IgnoreUncontained implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return false;
        }
        
    }
    
    
//------------------- Tree-related    
    /**
     * Ignore tests related to uncontained selectedItems in treeView selection. 
     * Seems unsupported, no idea how much sense it would make
     * 
     */
    public static class IgnoreTreeUncontained implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    /**
     * Ignore tests that need a second thought, due to tree specifics.
     * 
     * Open issues:
     * <li> handle single-replace in TreeIndicesList (might effect the rows
     * below if the replaced and/or the replacing TreeItem is expanded and
     * has children 
     * <li> sorting of treeItems
     * 
     */
    public static class IgnoreTreeDeferredIssue implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    /**
     * Open issue: selectedIndices fires two events on removeAt with strategy
     * to keep the selectedIndex constant if possible.<p>
     * 
     * The reason is understood: indicesList explicitly clears the index before
     * shifting left, selectionHelper re-selects the same. Unclear whether or
     * not this is important enough to trade against the clear separation of 
     * concerns (between single/multiple selection state) 
     */
    public static class IgnoreNotificationIndicesOnRemove implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    
    /**
     * Ignore focus-related tests in treeView selection: TreeFocusModel sets its
     * focus in a Platform.runLater, even wrapping the test in a runLater as well
     * is unreliable.
     * 
     */
    public static class IgnoreTreeFocus implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
    }
    
    /**
     * Ignore anchor-related tests in treeView selection: TreeFocusModel sets its
     * focus in a Platform.runLater, even wrapping the test in a runLater as well
     * is unreliable. Anchor might be tightly coupled?
     * 
     */
    public static class IgnoreTreeAnchor implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
    }
    
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
            return true;
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
     * Violating contract of selectFirst: with the violation
     * doc'ed as by-design, the issue is fixed
     * 
     * https://javafx-jira.kenai.com/browse/RT-26079
     */
    public static class IgnoreRT26079 implements IgnoreCondition {
        
        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    /**
     * Ignore failures due to selecting the separator.
     */
    public static class IgnoreSeparatorSelect implements IgnoreCondition {
    
        @Override
        public boolean isSatisfied() {
            return false;
        }
        
    }

    /**
     * Ignore tests that try to test state on filling the items
     * dynamically on showing the popup - hard to test.
     *  
     */
    public static class IgnoreDynamicItemsInPopup implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    /**
     * Ignore doc errors/ambiguities
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
     */
    public static class IgnoreExternalError implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    /**
     * Ignore tests that require a custom selectionModel (doesn't make sense
     * in adapters?)
     * 
     */
    public static class IgnoreSetSelectionModel implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return false;
        }
        
    }
    
    /**
     * Some tests are commented for various reason (f.i. adapted core tests
     * that use internal/off scope/unavailable classes). As a general 
     * procedure, we add a fail (to not forget) and a conditionalIgnore
     * so that the failures don't bloat the test results.
     */
    public static class IgnoreFailCommented implements IgnoreCondition {
    
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
