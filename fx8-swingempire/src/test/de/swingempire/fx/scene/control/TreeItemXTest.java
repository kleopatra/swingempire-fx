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
import de.swingempire.fx.scene.control.tree.TreeItemX.ExpandedDescendantEnumeration;
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
        long iterCount = countExpandedX(getRoot());
        assertEquals("same count for iter", count, iterCount);
        TreeItemX child = createBranch("child");
        getRoot().getChildren().add(child);
        assertEquals("added collapsed child", count + 1, countExpandedDescendants(getRoot()));
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
        assertEquals("added expanded child", count *2, countExpandedDescendants(getRoot()));
    }


    private int countExpandedDescendants(TreeItem rootItem) {
        ExpandedDescendants<TreeItem> enumer = 
                new ExpandedDescendants<TreeItem>(rootItem);
        int count = 0;
        while (enumer.hasNext()) {
            count++;
            enumer.next();
        }
        return count;
    }
    

    @Test
    public void testDescendantCountExpandedCollapsedX() {
        getRoot().setExpanded(true);
        int count = getRoot().getExpandedDescendantCount();
        long iterCount = countExpandedX(getRoot());
        assertEquals("same count for iter", count, iterCount);
        TreeItemX child = createBranch("child");
        getRoot().getChildren().add(child);
        assertEquals("added collapsed child", count + 1, countExpandedX(getRoot()));
    }
    
    @Test
    public void testDescendantCountExpandedExpandedX() {
        getRoot().setExpanded(true);
        int count = getRoot().getExpandedDescendantCount();
        TreeItemX child = createBranch("child");
        child.setExpanded(true);
        getRoot().getChildren().add(child);
        assertEquals("added expanded child", count *2, countExpandedX(getRoot()));
    }

    private int countExpandedX(TreeItem rootItem) {
        ExpandedDescendantEnumeration<TreeItem> enumer = 
                new ExpandedDescendantEnumeration<TreeItem>(rootItem);
        int count = 0;
        while (enumer.hasMoreElements()) {
            count++;
            enumer.nextElement();
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
        ExpandedDescendantEnumeration<TreeItem> iter = new ExpandedDescendantEnumeration<TreeItem>(getRoot());
        int position = 0;
        System.out.println("Using TreeItemX: all expanded");
        while (iter.hasMoreElements()) {
            System.out.println("count: " + position ++ + " / " + iter.nextElement());
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
        ExpandedDescendantEnumeration<TreeItem> iter = new ExpandedDescendantEnumeration<TreeItem>(getRoot());
        int position = 0;
        System.out.println("Using TreeItemX: collapsed");
        while (iter.hasMoreElements()) {
            System.out.println("count: " + position ++ + " / " + iter.nextElement());
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
    protected TreeItemX createBranch(String item) {
        TreeItemX child = createItem(item);
        child.getChildren().setAll(createItems(rawItems));
        return child;
    }
    
    protected TreeItemX getRoot() {
        return (TreeItemX) treeItem;
    }
    @Override
    protected TreeItemX createItem(Object item) {
        return new TreeItemX(item);
    }
    
}
