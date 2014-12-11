/*
 * Created on 11.12.2014
 *
 */
package de.swingempire.fx.scene.control;

import javafx.scene.control.TreeItem;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.scene.control.tree.TreeItemX;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TreeItemXTest extends TreeItemTest {

    @Override
    protected TreeItem createItem(Object item) {
        return new TreeItemX(item);
    }


    
}
