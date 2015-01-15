/*
 * Created on 17.12.2014
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Example for semantic folder, that is a treeItem that's has no children
 * but is not a leaf.
 * 
 * Inconsistent isLeaf vs. leafProperty.get()
 * Reported as doc error: https://javafx-jira.kenai.com/browse/RT-39762
 * 
 * Real issue: 
 * <li> missing api for semantic leaf
 * <li> as per example, they should override isLeaf
 * <li> if they do, they break the invariant isLeaf == leafProperty.isLeaf initially
 *     because there is no public api to sync the leafProperty (RT-39762, and those 
 *     proposing protected setter/property access)
 * <li> if they somehow (going dirty or with protected setter) sync initially,  treeItem
 *     breaks the invariant in its listener to children 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SemanticLeafExample extends Application {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(SemanticLeafExample.class
            .getName());
    
    // flag to indicate whether to reflectively invoke setting the leaf property
    static boolean syncInitially = true;
    
    /**
     * @return
     */
    private Parent getContent() {
        TreeView<String> tree = buildTree();
        tree.getRoot().setExpanded(true);
        tree.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F1) {
                TreeItem<String> item = tree.getSelectionModel().getSelectedItem();
                if (item == null) return;
                // broken invariant of leaf: property and getter out off sync 
                // first issue: initially for folders
                debugLeaf(item, "Log -- ");
                e.consume();
            }
        });
        tree.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.DELETE) {
                SemanticLeafItem item = (SemanticLeafItem) tree.getSelectionModel().getSelectedItem();
                if (item == null) return;
                item.getChildren().clear();
                // broken invariant of leaf: property and getter out off sync
                // second issue: even if hacked initially, clearing the children asyncs again
                debugLeaf(item, "Log after emove -- ");
                e.consume();
            }
        });

        BorderPane pane = new BorderPane(tree);
        return pane;
    }

    protected TreeView<String> buildTree() {
        String[] grandNames = {"grand1", "grand2"};
        List<SemanticLeafItem> grandChildren = new ArrayList<>();
        for (int i = 0; i < grandNames.length; i++) {
            grandChildren.add(new SemanticLeafItem(grandNames[i], false, null));
        }
        // folder without children
        SemanticLeafItem emptyFolder = new SemanticLeafItem("empty Folder", true, null);
        // folder with children
        SemanticLeafItem folderWithChildren = new SemanticLeafItem("filled folder", true, grandChildren);
        SemanticLeafItem root = new SemanticLeafItem("root", true, null);
        root.getChildren().setAll(emptyFolder, folderWithChildren);
        TreeView<String> treeView = new TreeView<>(root);
        return treeView;
    }
    
    protected void debugLeaf(TreeItem<String> item, String mes) {
        String leafs = " getter: " + item.isLeaf() + " property: " + item.leafProperty().get(); 
        LOG.info(mes + item + leafs);
    }

    public static class SemanticLeafItem extends TreeItem<String> {
        
        private boolean allowsChildren;

        public SemanticLeafItem(String value, boolean allowsChildren, List<SemanticLeafItem> children) {
            super(value);
             this.allowsChildren = allowsChildren;
             if (syncInitially) {
                 invokeSetLeaf(!allowsChildren);
             }    
             if (children != null) {
                 getChildren().setAll(children);
             }
        }
        
        /**
         * Overriding leaf getter as done in example. Which actually is
         * NOT recommended as nasty thingies can happen:
         * - returning a value unrelated to the property breaks invariant
         *   getSomething() == somethingProperty().get()
         * - updating (if possible) the property during processing the getter
         *   results in observable side-effects which might confuse listeners 
         */
        @Override
        public boolean isLeaf() {
            return !allowsChildren;
        }

        /**
         * Simulating protected access to leafProperty via reflection.
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
