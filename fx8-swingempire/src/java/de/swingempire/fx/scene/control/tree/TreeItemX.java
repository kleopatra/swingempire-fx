/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

/**
 * Extended to fire extended TreeModificationEvent.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeItemX<T> extends TreeItem<T> {

    /** flag to indicate whether or not we already replaced super's field */
    private boolean wasCheckedChildren;

    private ListChangeListener<TreeItem<T>> childrenListener = c -> childrenChanged(c) ;
    
    protected void childrenChanged(Change<? extends TreeItem<T>> c) {
        // first update internals of this treeItem
        updateChildren(c);
        // then fire an extended TreeModificationEvent that carries the complete change
        // (vs. one event for each sub-change as core does).
        c.reset();
        Event.fireEvent(this, new TreeModificationEventX<T>(
                childrenModificationEvent(), this, null, null, c));
    }

    /**
     * Internal on receiving a change notification from children.
     * 
     * @param c
     */
    private void updateChildren(ListChangeListener.Change<? extends TreeItem<T>> c) {
        invokeExpandedDescendentCountDirty();
        invokeSetLeaf(c.getList().isEmpty());
        while (c.next()) {
            final List<? extends TreeItem<T>> added = c.getAddedSubList();
            final List<? extends TreeItem<T>> removed = c.getRemoved();
            
            // update the relationships such that all added children point to
            // this node as the parent (and all removed children point to null)
            invokeUpdateChildrenParent(removed, null);
            invokeUpdateChildrenParent(added, this);
        }

        // fire an event up the parent hierarchy such that any listening
        // TreeViews (which only listen to their root node) can redraw
        
//        fireEvent(new TreeModificationEvent<T>(
//                CHILDREN_MODIFICATION_EVENT, this, added, removed, c));
    }

    @Override
    public ObservableList<TreeItem<T>> getChildren() {
        if (!wasCheckedChildren) checkChildren();
        return super.getChildren();
    }

    /**
     * Returns the number of expandedDescendants, will be updated
     * to account for any pending changes.
     * <p>
     * Note that this item itself is included dinto the count, that is the
     * minimal value is 1.
     * @return
     */
    public int getExpandedDescendentCount() {
        return invokeGetExpandedDescendentCount();
    }
    
    /**
     * Returns the current number of expandedDescendants. While the actual
     * count will be updated to account for any pending changes, it returns
     * the count _before_ the update (aka: previousExpandedDescendentCount.
     * <p>
     * Note that this item itself is included dinto the count, that is the
     * minimal value is 1.
     * 
     * <p>
     * 
     * PENDING: this is rather strange, but introduced by super. It listens
     * to itself for events of type itemCountChanged and sets a dirty flag. 
     * Next access to getCount will update the count, if either the flag
     * or the parameter is true. 
     * 
     * @return
     */
    public int getCurrentExpandedDescendentCount() {
        invokeGetExpandedDescendentCount();
        return invokePreviousExpandedDescendentCount();
    }

    
//----------- going dirty: reflective access of super

    /**
     * @return
     */
    private int invokePreviousExpandedDescendentCount() {
        Class clazz = TreeItem.class;
        try {
            Field field = clazz.getDeclaredField("previousExpandedDescendentCount");
            field.setAccessible(true);
            return (int) field.get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int invokeGetExpandedDescendentCount() {
        Class clazz = TreeItem.class;
        try {
            Method method = clazz.getDeclaredMethod("getExpandedDescendentCount", boolean.class);
            method.setAccessible(true);
            int count = (int) method.invoke(this, false);
            return count; //source.getExpandedDescendentCount(false);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Reflective call to super updateChildrenParent.
     * @param treeItems
     * @param newParent
     */
    protected void invokeUpdateChildrenParent(List<? extends TreeItem<T>> treeItems, final TreeItem<T> newParent) {
        Class<?> clazz = TreeItem.class;
        try {
            Method method = clazz.getDeclaredMethod("updateChildrenParent", List.class, TreeItem.class);
            method.setAccessible(true);
            method.invoke(this, treeItems, newParent);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Reflectiv setting super expendedDescendentCountDirty to true.
     * 
     */
    private void invokeExpandedDescendentCountDirty() {
        Class<?> clazz = TreeItem.class;
        try {
            Field field = clazz.getDeclaredField("expandedDescendentCountDirty");
            field.setAccessible(true);
            field.set(this, true);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reflective call to super setLeaf().
     * 
     * @param leaf
     */
    protected void invokeSetLeaf(boolean leaf) {
        Class<?> clazz = TreeItem.class;
        try {
            Method method = clazz.getDeclaredMethod("setLeaf", boolean.class);
            method.setAccessible(true);
            method.invoke(this, leaf);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Going dirty: children is package-private, is often addressed
     * directly, so we need to replace the field.
     */
    private void checkChildren() {
        wasCheckedChildren = true;
        Class<?> clazz = TreeItem.class;
        try {
            Field field = clazz.getDeclaredField("children");
            field.setAccessible(true);
            field.set(this, createChildren());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return
     */
    protected ObservableList<TreeItem<T>> createChildren() {
        ObservableList<TreeItem<T>> list = FXCollections.observableArrayList();
        list.addListener(childrenListener);
        return list;
    }

//--------------- constructors from super    
    public TreeItemX() {
        super();
    }

    /**
     * @param value
     * @param graphic
     */
    public TreeItemX(T value, Node graphic) {
        super(value, graphic);
    }

    /**
     * @param value
     */
    public TreeItemX(T value) {
        super(value);
    }

    
}
