/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.scene.control;

import javafx.scene.control.TreeItem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.scene.control.tree.TreeItemX;

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
