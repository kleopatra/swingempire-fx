/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.scene.control;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.scene.control.tree.TreeItemX;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TreeItemXTest extends TreeItemTest {

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
