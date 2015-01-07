/*
 * Created on 06.01.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * Experiment: defining leafness such that subclasses of TreeItem can
 * customize state depending on either children list state or value state
 * (or anything in between?)
 *  
 * @author Jeanette Winzenburg, Berlin
 */
public interface Leafness<T> {
    
    /**
     * The semantic leaf property. Base implementations will implement
     * it as low-level leaf (== !hasChildren), extending implmentations
     * can take allowsChildren/askAllowsChildren into account to
     * differentiate f.i. an empty folder node from a file. <p>
     * 
     * In Swing, isLeaf == !hasChildren and defined in TreeNode.
     * @return
     * 
     * @see javafx.scene.control.TreeItem#isLeaf()
     * @see javafx.scene.control.TreeItem#leafProperty()
     */
    boolean isLeaf();
    
    /**
     * Returns true if leaf should depend on allowsChildren.
     * The default value is false.
     * 
     * In Swing, this is a mutable property of DefaultTreeModel.
     * @return
     */
    default boolean isAskAllowsChildren() {
        return false;
    }
    
    /**
     * Returns true if this can have children, false if not.
     * The default implementation returns true.
     * 
     * In Swing, this is a mutable property of TreeNode.
     * @return
     */
    default boolean isAllowsChildren() {
        return true;
    }
    
    /**
     * Returns true if the children list is not empty.
     * @return
     */
    default boolean hasChildren() {
        return !getChildren().isEmpty();
    }
    
    /**
     * Returns the list of children. Guaranteed to be not null, but may be
     * empty or not modifiable.
     * 
     * @return
     */
    ObservableList<TreeItem<T>> getChildren();
    
}
