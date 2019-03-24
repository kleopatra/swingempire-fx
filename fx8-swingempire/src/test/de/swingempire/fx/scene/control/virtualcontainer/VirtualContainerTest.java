/*
 * Created on 22.03.2019
 *
 */
package de.swingempire.fx.scene.control.virtualcontainer;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TreeTableViewSkin;
import javafx.scene.control.skin.TreeViewSkin;

/**
 * Divers tests across all virtualized controls.
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class VirtualContainerTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();


    /**
     * bug in TableViewSkin: virtualFlow's cellCount not initialized.
     * reported
     * https://bugs.openjdk.java.net/browse/JDK-8221334
     */
    @Test
    public void testTableSkinCellCountInitial() {
        TableView<Locale> control = new TableView<>(FXCollections.observableArrayList(Locale.getAvailableLocales()));
        control.setSkin(new TableViewSkin<>(control) {
            {
                assertEquals("flow's cellCount must be initialized", control.getItems().size(), 
                        getVirtualFlow().getCellCount());
            }
        });
    }
    
    @Test
    public void testTreeTableSkinCellCountInitial() {
        List<TreeItem<Locale>> treeItems = Arrays.stream(Locale.getAvailableLocales())
                .map(TreeItem::new)
                .collect(Collectors.toList());
        TreeItem<Locale> root = new TreeItem<>(new Locale("dummy"));
        root.setExpanded(true);
        root.getChildren().addAll(treeItems);
        TreeTableView<Locale> control = new TreeTableView<>(root);
        control.setSkin(new TreeTableViewSkin<>(control) {
            {
                assertEquals("flow's cellCount must be initialized", treeItems.size() + 1, 
                        getVirtualFlow().getCellCount());
            }
        });
    }
    
    @Test
    public void testTreeSkinCellCountInitial() {
        List<TreeItem<Locale>> treeItems = Arrays.stream(Locale.getAvailableLocales())
                .map(TreeItem::new)
                .collect(Collectors.toList());
        TreeItem<Locale> root = new TreeItem<>(new Locale("dummy"));
        root.setExpanded(true);
        root.getChildren().addAll(treeItems);
        TreeView<Locale> control = new TreeView<>(root);
        control.setSkin(new TreeViewSkin<>(control) {
            {
                assertEquals("flow's cellCount must be initialized", treeItems.size() +1, 
                        getVirtualFlow().getCellCount());
            }
        });
    }
    
    @Test
    public void testListSkinCellCountInitial() {
        ListView<Locale> control = new ListView<>(FXCollections.observableArrayList(Locale.getAvailableLocales()));
        control.setSkin(new ListViewSkin<>(control) {
            {
                assertEquals("flow's cellCount must be initialized", control.getItems().size(), 
                        getVirtualFlow().getCellCount());
            }
        });
    }
    
}
