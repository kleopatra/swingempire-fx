/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.scene.control;

import javafx.scene.control.TreeItem;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import de.swingempire.fx.scene.control.TreeIgnores.IgnoreLog;
import de.swingempire.fx.scene.control.tree.TreeItemIterator;
import de.swingempire.fx.scene.control.tree.TreeItemStreamSupport;
import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.scene.control.tree.TreeItemX.ExpandedDescendants;

import static de.swingempire.fx.scene.control.tree.TreeItemX.*;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TreeItemXTest extends TreeItemTest {

    /**
     * Testing utility method.
     */
    @Test
    public void testIsVisible() {
        assertFalse("null must not be visible", isVisible(null));
        assertTrue("root must be visible " + treeItem, isVisible(treeItem));
        treeItem.setExpanded(true);
        TreeItem child = (TreeItem) treeItem.getChildren().get(0);
        assertTrue("child of expanded item must be visible " + child, isVisible(child));
        TreeItem branch = createBranch("branch");
        TreeItem grandChild = (TreeItem) branch.getChildren().get(0);
        treeItem.getChildren().add(branch);
        assertTrue("branch must be visible " + branch, isVisible(branch));
        assertFalse("grandChild in collapsed branch must not be visible " + grandChild, 
                isVisible(grandChild));
        branch.setExpanded(true);
        assertTrue("expanding parent makes grandChild visible", isVisible(grandChild));
        treeItem.setExpanded(false);
        assertFalse("collapsing root makes grandChilkd invisible", isVisible(grandChild));
    }
    /**
     * Testing expanded count of collapsed/expanded root after adding
     * collapsed/expanded root. 
     */
    @Test
    public void testDescendantCountExpandedCollapsed() {
        getRoot().setExpanded(true);
        int count = getRoot().getExpandedDescendantCount();
        assertEquals("sanity: initial count expanded", rawItems.size() + 1, count);
        TreeItemX child = createBranch("child");
        getRoot().getChildren().add(child);
        assertEquals("added collapsed child", count + 1, getRoot().getExpandedDescendantCount());
    }
    
    @Test
    public void testDescendantCountExpandedExpanded() {
        getRoot().setExpanded(true);
        int count = getRoot().getExpandedDescendantCount();
        assertEquals("sanity: initial count expanded", rawItems.size() + 1, count);
        TreeItemX child = createBranch("child");
        child.setExpanded(true);
        getRoot().getChildren().add(child);
        assertEquals("added expanded child", count *2, getRoot().getExpandedDescendantCount());
    }
    
    @Test
    public void testDescendantCountCollapsedCollapsed() {
        getRoot().setExpanded(false);
        int count = getRoot().getExpandedDescendantCount();
        assertEquals("sanity: initial count collapsed", 1, count);
        TreeItemX child = createBranch("child");
        getRoot().getChildren().add(child);
        assertEquals("added collapsed child", count, getRoot().getExpandedDescendantCount());
    }
    
    @Test
    public void testDescendantCountCollapsedExpanded() {
        getRoot().setExpanded(false);
        int count = getRoot().getExpandedDescendantCount();
        assertEquals("sanity: initial count collapsed", 1, count);
        TreeItemX child = createBranch("child");
        child.setExpanded(true);
        getRoot().getChildren().add(child);
        assertEquals("added expanded child", count, getRoot().getExpandedDescendantCount());
    }

    /**
     * Informal testing of TreeItemX.ExpandedDescendants
     */
    @Test
    public void testDescendantCountExpandedCollapsedIXterator() {
        getRoot().setExpanded(true);
        int count = getRoot().getExpandedDescendantCount();
        long iterCount = countExpandedDescendantsWithIter(getRoot());
        assertEquals("same count for iter", count, iterCount);
        TreeItemX child = createBranch("child");
        getRoot().getChildren().add(child);
        assertEquals("added collapsed child", count + 1, countExpandedDescendantsWithIter(getRoot()));
    }
    
    /**
     * Informal testing of TreeItemX.ExpandedDescendants
     */
    @Test
    public void testDescendantCountExpandedExpandedXIterator() {
        getRoot().setExpanded(true);
        int count = getRoot().getExpandedDescendantCount();
        TreeItemX child = createBranch("child");
        child.setExpanded(true);
        getRoot().getChildren().add(child);
        assertEquals("added expanded child", count *2, countExpandedDescendantsWithIter(getRoot()));
    }

    private int countExpandedDescendantsWithIter(TreeItem rootItem) {
        ExpandedDescendants<TreeItem> iter = 
                new ExpandedDescendants<TreeItem>(rootItem);
        int count = 0;
        while (iter.hasNext()) {
            count++;
            iter.next();
        }
        return count;
    }
    
    @Test 
    @ConditionalIgnore (condition = IgnoreLog.class)
    public void testLogTraversalExpandedChildX() {
        getRoot().setExpanded(true);
        TreeItemX child = createBranch("child");
        child.setExpanded(true);
        getRoot().getChildren().add(2, child);
        //        PreorderTreeItemEnumeration<TreeItem> iter = new PreorderTreeItemEnumeration<TreeItem>(getRoot());
//        ExpandedDescendantEnumeration<TreeItem> iter = new ExpandedDescendantEnumeration<TreeItem>(getRoot());
        ExpandedDescendants<TreeItem> iter = new ExpandedDescendants<TreeItem>(getRoot());
        int position = 0;
        System.out.println("Using TreeItemX: all expanded");
        while (iter.hasNext()) {
            System.out.println("count: " + position ++ + " / " + iter.next());
        }
    }
    @Test 
    @ConditionalIgnore (condition = IgnoreLog.class)
    public void testLogTraversalCollapsedChildX() {
        getRoot().setExpanded(true);
        TreeItemX child = createBranch("child");
        //        child.setExpanded(true);
        getRoot().getChildren().add(2, child);
        //        PreorderTreeItemEnumeration<TreeItem> iter = new PreorderTreeItemEnumeration<TreeItem>(getRoot());
//        ExpandedDescendantEnumeration<TreeItem> iter = new ExpandedDescendantEnumeration<TreeItem>(getRoot());
        ExpandedDescendants<TreeItem> iter = new ExpandedDescendants<TreeItem>(getRoot());
        int position = 0;
        System.out.println("Using TreeItemX: collapsed");
        while (iter.hasNext()) {
            System.out.println("count: " + position ++ + " / " + iter.next());
        }
    }
    @Test @Ignore
    public void testDescendantCountExpandedCollapsedIterator() {
        getRoot().setExpanded(true);
        int count = getRoot().getExpandedDescendantCount();
        long iterCount = countExpanded(getRoot());
        assertEquals("same count for iter", count, iterCount);
        TreeItemX child = createBranch("child");
        getRoot().getChildren().add(child);
        assertEquals("added collapsed child", count + 1, countExpanded(getRoot()));
    }
    @Test @Ignore
    public void testDescendantCountExpandedExpandedIterator() {
        getRoot().setExpanded(true);
        int count = getRoot().getExpandedDescendantCount();
        TreeItemX child = createBranch("child");
        child.setExpanded(true);
        getRoot().getChildren().add(child);
        assertEquals("added expanded child", count *2, countExpanded(getRoot()));
    }
    @Test 
    @ConditionalIgnore (condition = IgnoreLog.class)
    public void testLogTraversalSO() {
        getRoot().setExpanded(true);
        TreeItemX child = createBranch("child");
        child.setExpanded(true);
        getRoot().getChildren().add(2, child);
        TreeItemIterator iter = new TreeItemIterator(getRoot());
        int position = 0;
        System.out.println("Using SO iterator");
        while (iter.hasNext()) {
            System.out.println("count: " + position ++ + " / " + iter.next());
        }
    }
    /**
     * Using traversao from SO to count the expanded items. Not really doing
     * what I would expect.
     * 
     * @param rootItem
     * @return
     */
    private long countExpanded(TreeItem<?> rootItem) {
        return
                TreeItemStreamSupport.stream(rootItem)
                        .filter(t -> t.isLeaf() || !t.isExpanded())
                        .count();
    }
    
    protected TreeItemX getRoot() {
        return (TreeItemX) treeItem;
    }
    
    @Override
    protected TreeItemX createBranch(Object value) {
        return (TreeItemX) super.createBranch(value);
    }

    @Override
    protected TreeItemX createItem(Object item) {
        return new TreeItemX(item);
    }
    
}
