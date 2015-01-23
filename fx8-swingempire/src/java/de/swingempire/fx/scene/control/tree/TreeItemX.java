/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

/**
 * Extended to fire extended TreeModificationEvent that exposes the 
 * change received from its children list as-is.
 * 
 * https://javafx-jira.kenai.com/browse/RT-39644
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeItemX<T> extends TreeItem<T> implements Leafness<T> {

    /** flag to indicate whether or not we already replaced super's field */
    private boolean wasCheckedChildren;

    private ListChangeListener<TreeItem<T>> childrenListener = c -> childrenChanged(c) ;

    /**
     * Beware: dirty!
     * This is an alias to super's private children field. It may be null
     * if either not yet accessed or reflection failed.
     */
    private ObservableList<TreeItem<T>> aliasChildren;

//-------------------------- support extended TreeModificationEvent
    
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
        updateLeaf(c.getList());
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

    /**
     * Overridden to sneak in our own listener to the children.
     */
    @Override
    public ObservableList<TreeItem<T>> getChildren() {
        if (!wasCheckedChildren) checkChildren();
        return super.getChildren();
    }

    /**
     * Returns true if the value identified by path is currently viewable,
     * which means it is either the root or all of its parents are expanded.
     * Otherwise, this method returns false.
     * 
     * @param treeItem
     * @return
     */
    public static boolean isVisible(TreeItem<?> treeItem) {
        if (treeItem == null) return false;
        if (treeItem.getParent() == null) {
            return true;
        } else if (!treeItem.getParent().isExpanded()) {
            return false;
        }
        return isVisible(treeItem.getParent());
        
    }
    /**
     * Returns the number of expandedDescendants, will be updated
     * to account for any pending changes.
     * <p>
     * Note that this item itself is included into the count, that is the
     * minimal value is 1.
     * @return
     */
    public int getExpandedDescendantCount() {
        return invokeGetExpandedDescendantCount();
    }
    
    /**
     * Returns the current number of expandedDescendants. While the actual
     * count will be updated to account for any pending changes, it returns
     * the count _before_ the update (aka: previousExpandedDescendentCount.
     * <p>
     * Note that this item itself is included into the count, that is the
     * minimal value is 1.
     * 
     * <p>
     * 
     * PENDING: this is rather strange, but introduced by super. It listens
     * to itself for events of type itemCountChanged and sets a dirty flag. 
     * Next access to getCount will update the count, if either the flag
     * or the parameter is true. 
     * 
     * <p>
     * DONT USE THIS! Make private asap.
     * 
     * <p>
     * Not really good to rely on stale state ... the general process is to
     * fire only if internal state is updated (or as done here, each access
     * will update internal state before returning the value).   
     * <p>
     * Clients interested in the old value should
     * loop through the children, call getExpandedDescendentCount on each
     * and sum them up (it the item was collapsed) or assume a 1 if visible
     * and expanded. 
     * 
     * @return
     * 
     * @see #getExpandedDescendantCount()
     */
    private int getPreviousExpandedDescendantCount() {
        invokeGetExpandedDescendantCount();
        return invokePreviousExpandedDescendantCount();
    }

//-------------------------- support leafness
    
    /**
     * This is called from the listener to children.
     * Extracted to support semantic leafs.
     * 
     * @param list
     */
    protected void updateLeaf(ObservableList<? extends TreeItem<T>> list) {
        invokeSetLeaf(list.isEmpty());
    }
    
    
//----------- going dirty: reflective access of super

    /**
     * NOTE the typo "descendents" vs. "descendants" - reflective access
     * will break if fixed...
     * @return
     */
    private int invokePreviousExpandedDescendantCount() {
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

    /**
     * NOTE the typo in super "descendent" vs. "descendant" - reflective access
     * will break if fixed...
     * 
     * @return
     */
    private int invokeGetExpandedDescendantCount() {
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
    protected void checkChildren() {
        wasCheckedChildren = true;
        Class<?> clazz = TreeItem.class;
        try {
            Field field = clazz.getDeclaredField("children");
            field.setAccessible(true);
            ObservableList<TreeItem<T>> replaceChildren = createChildren();
            field.set(this, replaceChildren);
            aliasChildren = replaceChildren;
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

//------------- enum across visible child nodes
 
    
    /**
     * Implementation of a preorder traversal of a subtree with nodes of type TreeNode.
     * <p>
     * PENDING JW: change to Iterator.
     */
    public static class PreorderTreeItemEnumeration<M extends TreeItem> implements Enumeration<M> {
        protected Deque<Enumeration<M>> stack;
        
        public PreorderTreeItemEnumeration(M rootNode) {
            // Vector is just used for getting an Enumeration easily
            Vector<M> v = new Vector<M>(1);
            v.addElement(rootNode);     
            stack = new ArrayDeque<Enumeration<M>>();
            stack.push(v.elements());
        }
        
        @Override
        public boolean hasMoreElements() {
            return (!stack.isEmpty() &&
                    stack.peek().hasMoreElements());
        }
        
        @Override
        public M nextElement() {
            Enumeration<M> enumer = stack.peek();
            M node = enumer.nextElement();
            Enumeration<M> children = getChildren(node);
            
            if (!enumer.hasMoreElements()) {
                stack.pop();
            }
            if (children.hasMoreElements()) {
                stack.push(children);
            }
            return node;
        }
        
        protected Enumeration<M> getChildren(M node) {
            Enumeration<M> children = Collections.enumeration(node.getChildren());
            return children;
        }
        
    }  // End of class PreorderEnumeration
    
    /**
     * Implementation of a preorder traversal across the expanded descendants
     * of a TreeItem. The direct children of the root are always included.
     */
    public static class ExpandedDescendants<M extends TreeItem> implements Iterator<M> {
        protected Iterator<M> EMPTY = Collections.emptyIterator(); 
        protected Deque<Iterator<M>> stack;
        protected M root; 
        
        public ExpandedDescendants(M rootNode) {
            this.root = rootNode;
            stack = new ArrayDeque<Iterator<M>>();
            stack.push(Collections.singletonList(rootNode).iterator());
        }
    
        @Override
        public boolean hasNext() {
            return (!stack.isEmpty() &&
                    stack.peek().hasNext());
        }
    
        @Override
        public M next() {
            Iterator<M> enumer = stack.peek();
            M node = enumer.next();
            Iterator<M> children = getChildren(node);
    
            if (!enumer.hasNext()) {
                stack.pop();
            }
            if (children.hasNext()) {
                stack.push(children);
            }
            return node;
        }
    
        protected Iterator<M> getChildren(M node) {
            if (node != root && (node.isLeaf() || !node.isExpanded())) return EMPTY;
            Iterator<M> children = node.getChildren().iterator();
            return children;
        }
    
    }  // End of class PreorderEnumeration

//    /**
//     * Implementation of a preorder traversal across the expanded descendants
//     * of a TreeItem. The direct children of the item are always included.
//     * 
//     * <p>
//     * 
//     * PENDING JW: change to Iterator.
//     */
//    public static class ExpandedDescendantEnumeration<M extends TreeItem> implements Enumeration<M> {
//        protected Deque<Enumeration<M>> stack;
//        protected M root; 
//        
//        public ExpandedDescendantEnumeration(M rootNode) {
//            this.root = rootNode;
//            // Vector is just used for getting an Enumeration easily
//            Vector<M> v = new Vector<M>(1);
//            v.addElement(rootNode);     
//            stack = new ArrayDeque<Enumeration<M>>();
//            stack.push(v.elements());
//        }
//        
//        @Override
//        public boolean hasMoreElements() {
//            return (!stack.isEmpty() &&
//                    stack.peek().hasMoreElements());
//        }
//        
//        @Override
//        public M nextElement() {
//            Enumeration<M> enumer = stack.peek();
//            M node = enumer.nextElement();
//            Enumeration<M> children = getChildren(node);
//            
//            if (!enumer.hasMoreElements()) {
//                stack.pop();
//            }
//            if (children.hasMoreElements()) {
//                stack.push(children);
//            }
//            return node;
//        }
//        
//        protected Enumeration<M> getChildren(M node) {
//            if (node != root && (node.isLeaf() || !node.isExpanded())) return Collections.emptyEnumeration();
//            Enumeration<M> children = Collections.enumeration(node.getChildren());
//            return children;
//        }
//        
//    }  // End of class PreorderEnumeration

}
