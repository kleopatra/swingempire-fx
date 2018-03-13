/*
 * Created on 10.12.2014
 *
 */
package de.swingempire.fx.scene.control;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import de.swingempire.fx.util.FXUtils;
import de.swingempire.fx.util.TreeModificationReport;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class TreeItemTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    protected TreeItem treeItem;
    protected ObservableList<TreeItem> children;
    protected ObservableList rawItems;

    
    /**
     * Test that leaf property and getter are the same.
     * Adding children will auto-sync the property, removing 
     * all children will break it again.
     */
    @Test
    public void testAllowsChildrenEvenIsFolderOnRemoveChildren() {
        int row = 0;
        TreeItem node = new FolderOrFileTreeItem(row, true);
        // don't care what we add, just for having something to remove
        node.getChildren().add(new TreeItem(3));
        assertEquals("sanity: even row must be folder " + row, row % 2 == 0, !node.isLeaf());
        node.getChildren().clear();
        assertEquals("getter and property must be same", node.isLeaf(), node.leafProperty().get());
    }
    
    /**
     * Test that leaf property and getter are the same.
     * forcing initial sync via reflective access of the setter 
     * see https://javafx-jira.kenai.com/browse/RT-39762
     */
    @Test
    public void testAllowsChildrenEvenIsFolderWithReflectiveSync() {
        int row = 0;
        TreeItem node = new FolderOrFileTreeItem(row, true);
        assertEquals("sanity: even row must be folder " + row, row % 2 == 0, !node.isLeaf());
        assertEquals("getter and property must be same", node.isLeaf(), node.leafProperty().get());
    }
    
    /**
     * Test that leaf property and getter are the same.
     * Fails because there's 
     * no api to sync them initially, setter is package-private.
     * https://javafx-jira.kenai.com/browse/RT-39762
     */
    @Test
    public void testAllowsChildrenEvenIsFolder() {
        int row = 0;
        TreeItem node = new FolderOrFileTreeItem(row, false);
        assertEquals("sanity: even row must be folder " + row, row % 2 == 0, !node.isLeaf());
        assertEquals("getter and property must be same", node.isLeaf(), node.leafProperty().get());
    }
    /**
     * TreeItem with value-dependent leafness (simulating a folder in a
     * filebrowser, f.i. )
     */
    private static class FolderOrFileTreeItem extends TreeItem<Integer> {
        
        private boolean allowsChildren;
        
        public FolderOrFileTreeItem(int value, boolean initialLeafProperty) {
            super(value);
            allowsChildren = value % 2 == 0;
            if (initialLeafProperty) {
                invokeSetLeaf(isLeaf());   
            }
        }
        
        @Override
        public boolean isLeaf() {
            return !allowsChildren;
        }

        /**
         * Reflectively force leafProperty to allowsChildren
         * @param leaf
         */
        private void invokeSetLeaf(boolean leaf) {
            Class<?> clazz = TreeItem.class;
            try {
                Method method = clazz.getDeclaredMethod("setLeaf", boolean.class);
                method.setAccessible(true);
                method.invoke(this, leaf);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }
    
    @Test
    public void testAllowsChildrenOddIsLeaf() {
        int row = 1;
        TreeItem node = new FolderOrFileTreeItem(row, false);
        assertEquals("sanity: odd row must be leaf " + row, row % 2 != 0, node.isLeaf());
    }
    
    @Test
    public void testChildEventPermutated() {
        EventHandler<TreeModificationEvent> l = e -> {
            assertTrue("sanity: got a permutated", e.wasPermutated());
            // now how to get the permutation indices?
        };
        treeItem.addEventHandler(TreeItem.childrenModificationEvent(), l);
        Comparator<TreeItem<String>> c = (TreeItem<String> o1, TreeItem<String> o2) 
                -> o1.getValue().compareTo(o2.getValue());
        children.sort((Comparator<? super TreeItem>) c);
    }
    
    @Test
    public void testChildEventSubChanges() {
        int last = children.size() -1;
        TreeModificationReport report = new TreeModificationReport(treeItem);
        children.removeAll(children.get(2), children.get(5));
        assertEquals("got single removed", 1, report.getEventCount());
        Change change = report.getLastChangeX();
        if (change != null) {
            assertEquals(2, FXUtils.getChangeCount(change));
        }
    }
    
    @Test
    public void testChildDiscontinousRemoved() {
        TreeModificationReport report = new TreeModificationReport(treeItem);
        children.removeAll(children.get(2), children.get(5));
        assertEquals("got single event", 1, report.getEventCount());
        assertTrue("got removed", report.getLastEvent().wasRemoved());
    }
    @Test
    public void testChildEventRemoved() {
        TreeModificationReport report = new TreeModificationReport(treeItem);
        treeItem.getChildren().remove(4);
        assertEquals("got single event", 1, report.getEventCount());
        assertTrue("got removed", report.getLastEvent().wasRemoved());
        
    }
    
    @Test
    public void testChildEventAdded() {
        TreeModificationReport report = new TreeModificationReport(treeItem);
        treeItem.getChildren().add(createItem("newItem"));
        assertEquals("single event", 1, report.getEventCount());
        assertTrue("was added", report.getLastEvent().wasAdded());
    }
    
    @Test @Ignore
    public void testChildEventSubChangesReport() {
        // simulate external storage of last index
        IntegerProperty p = new SimpleIntegerProperty(children.size()-1);
        EventHandler<TreeModificationEvent> l = e -> {
            assertTrue("sanity: got a removed", e.wasRemoved());
            // adjust index: fine for single subChange
            p.set(p.get() - e.getRemovedSize()); 
            // this must be valid but isn't because it's the first
            // of a sequence of separate notifications
            children.get(p.get());
        };
        treeItem.addEventHandler(TreeItem.childrenModificationEvent(), l);
        children.removeAll(children.get(2), children.get(5));
    }

    @Test @Ignore
    public void testChildEventDiscontinousRemovedReport() {
        IntegerProperty p = new SimpleIntegerProperty(0);
        EventHandler<TreeModificationEvent> l = e -> {
            assertTrue(e.wasRemoved());
            p.set(p.get() + 1);  
        };
        treeItem.addEventHandler(TreeItem.childrenModificationEvent(), l);
        children.removeAll(children.get(2), children.get(5));
        assertEquals("received singe removed", 1, p.get());
    }

    @Test @Ignore
    public void testChildEventRemovedReport() {
        IntegerProperty p = new SimpleIntegerProperty(0);
        EventHandler<TreeModificationEvent> l = e -> {
            assertTrue(e.wasRemoved());
            assertEquals(1, e.getRemovedSize());  
            p.set(p.get() + 1);  
        };
        treeItem.addEventHandler(TreeItem.childrenModificationEvent(), l);
        treeItem.getChildren().remove(4);
        assertEquals("received single removed", 1, p.get());
    }

    @Test @Ignore
    public void testChildEventAddedReport() {
        IntegerProperty p = new SimpleIntegerProperty(0);
        EventHandler<TreeModificationEvent> l = e -> {
           assertTrue(e.wasAdded());
           assertEquals(1, e.getAddedSize());  
           p.set(p.get() + 1);  
        };
        treeItem.addEventHandler(TreeItem.childrenModificationEvent(), l);
        treeItem.getChildren().add(createItem("newItem"));
        assertEquals("received single added", 1, p.get());
    }
    
    @Test
    public void testLeafExpanded() {
        TreeItem item = createItem("some");
        assertTrue("isLeaf", item.isLeaf());
        item.setExpanded(true);
        assertTrue("isExpanded", item.isExpanded());
        item.setExpanded(false);
        assertFalse("isExpanded", item.isExpanded());
    }
    
    
    @Test
    public void testInitial() {
        assertEquals(rawItems.size(), treeItem.getChildren().size());
    }
    
    @Before
    public void setup() {
        rawItems = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        treeItem = createBranch("root");
        children = treeItem.getChildren();
    }
    
    protected TreeItem createItem(Object value) {
        return new TreeItem(value);
    }

    protected Supplier<TreeItem> treeItemSupplier;
    
    protected TreeItem createBranch(Object value) {
        TreeItem branch = createItem(value);
        branch.getChildren().setAll((List<TreeItem>) rawItems.stream()
                .map(this::createItem)
                .collect(Collectors.toList()));
        return branch;
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TreeItemTest.class
            .getName());
}
