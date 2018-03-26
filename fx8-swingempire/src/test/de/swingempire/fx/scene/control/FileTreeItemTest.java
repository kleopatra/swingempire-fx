/*
 * Created on 08.01.2015
 *
 */
package de.swingempire.fx.scene.control;

import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static org.junit.Assert.*;

import de.swingempire.fx.scene.control.tree.FileTreeXExample.FileTreeItemX;
import javafx.scene.control.TreeItem;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class FileTreeItemTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    protected FileTreeItemX treeItem;

    /**
     * Trying to dig into misbehvaiour when lazily created children.
     * Giving up for now.
     */
    @Test
    public void testExpandedDescendantCount() {
        FileTreeItemX folder = findFolder();
        int index = treeItem.getChildren().indexOf(folder);
        LOG.info("" + index + folder + " -- all: " + treeItem.getChildren());
        FileTreeItemX next = (FileTreeItemX) folder.nextSibling();
        int nextIndex = treeItem.getChildren().indexOf(next);
        assertEquals("sanity: sibling is next", index + 1, nextIndex);
        File dir = next.getValue();
        assertFalse("sanity: next is folder " + next , dir.isFile());
        int fileCount = dir.listFiles().length;
        // remove children from first
        folder.getChildren().clear();
        next.setExpanded(true);
        assertEquals("descendantCount", fileCount + 1, next.getExpandedDescendantCount());
    }
    
    @Test
    public void testInitial() {
        assertTrue("root setup to expanded", treeItem.isExpanded());
        assertTrue("allowsChildren", treeItem.isAllowsChildren());
        assertTrue("askAllowsChildren", treeItem.isAskAllowsChildren());
        assertFalse("must not be leaf", treeItem.isLeaf());
        assertLeafSync(treeItem);
    }
    
    /**
     * Convenience: asserts sync of getter and leafProperty value.
     * @param treeItem
     */
    private void assertLeafSync(FileTreeItemX treeItem) {
        assertEquals("getter and property must be same for "+ treeItem , 
                treeItem.isLeaf(), treeItem.leafProperty().get());
        
    }

    /**
     * @return
     */
    private FileTreeItemX findFolder() {
        return findFolder(treeItem);
    }

    /**
     * Returns the first folder of the given item or null if has none. 
     * The item is expected to be a folder.
     * 
     * @param treeItem2
     * @return
     */
    private FileTreeItemX findFolder(FileTreeItemX treeItem) {
        if (treeItem.isLeaf()) throw new IllegalStateException("expected folder, but was " + treeItem);
        Optional<TreeItem<File>> first = treeItem.getChildren().stream().filter(t -> !t.isLeaf()).findFirst();
        return first.isPresent() ? (FileTreeItemX) first.get() : null;
    }

    @Before
    public void setup() {
        treeItem = new FileTreeItemX(new File("."));
        treeItem.setExpanded(true);
        FileTreeItemX folder = findFolder();
        assertNotNull("expected at least one child folder on " + treeItem , folder);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(FileTreeItemTest.class
            .getName());
}
