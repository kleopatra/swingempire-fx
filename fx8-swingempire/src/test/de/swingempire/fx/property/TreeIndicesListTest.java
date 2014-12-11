/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.property;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import de.swingempire.fx.collection.TreeIndicesList;
import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.util.TreeModificationReport;

import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeIndicesListTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    private TreeIndicesList indicesList;
    private TreeModificationReport report;
    private TreeItemX root;
    private ObservableList rawItems;
    private ObservableList<TreeItem> rootChildren;

    private TreeView tree;

    @Test
    public void testCollapseRoot() {
        int index = 2;
        indicesList.setIndices(index);
        assertEquals("sanity: expandedDescendents", rawItems.size() + 1, root.getExpandedDescendentCount());
        root.setExpanded(false);
        assertEquals("indices after collapse must be empty", 0, indicesList.size());
    }
    @Test
    public void testRemoveChild() {
        int index = 2;
        indicesList.setIndices(index);
        rootChildren.remove(0);
        assertEquals("index increased by one", index -1, indicesList.get(0).intValue());
    }
    
    @Test
    public void testAddChild() {
        int index = 2;
        indicesList.setIndices(index);
        rootChildren.add(0, createItem("newItemAt-0"));
        assertEquals("index increased by one", index + 1, indicesList.get(0).intValue());
    }
    
    @Test
    public void testSetIndices() {
        int index = 2;
        indicesList.setIndices(index);
        assertEquals("size after setting index", 1, indicesList.size());
        assertEquals("index stored", index, indicesList.get(0).intValue());
    }
    
    @Test
    public void testInitial() {
        assertEquals(0, indicesList.size());
    }
    
    @Before
    public void setup() {
        rawItems = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        root = createItem("root");
        rootChildren = root.getChildren();
        rootChildren.setAll(createItems(rawItems));
        root.setExpanded(true);
        tree = new TreeView(root);
        indicesList = new TreeIndicesList(tree);
    }
    
    protected TreeItemX createItem(Object item) {
        return new TreeItemX(item);
    }

    protected ObservableList<TreeItem> createItems(ObservableList other) {
        ObservableList items = FXCollections.observableArrayList();
        other.stream().forEach(item -> items.add(createItem(item)));
        return items;
    }

}
