/*
 * Created on 17.12.2014
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Example from api doc TreeItem.
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
public class FileTreeExample extends Application {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(FileTreeExample.class
            .getName());
    
    boolean syncInitially;

    private TreeItem<File> safedItem;
    
    /**
     * @return
     */
    private Parent getContent() {
        TreeView<File> tree = buildFileSystemBrowser();
        tree.getRoot().setExpanded(true);
        tree.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F1) {
                TreeItem<File> item = tree.getSelectionModel().getSelectedItem();
                if (item == null) return;
                // broken invariant of leaf: property and getter out off sync
                // first issue: initially for folders, all correct after first expansion
                debugLeaf(item, "Log -- ");
                e.consume();
            }
        });
        tree.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.DELETE) {
                TreeItem<File> item = tree.getSelectionModel().getSelectedItem();
                if (item == null || item.getValue().isFile()) return;
                item.getChildren().clear();
                // broken invariant of leaf: property and getter out off sync
                // second issue: even if hacked initially, clearing the children asyncs again
                debugLeaf(item, "Log after emove -- ");
                e.consume();
            }
        });
        
        // quick check: http://stackoverflow.com/q/23990493/203657
        // unrelated: select hidden item expands parents
        tree.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F2) {
                TreeItem<File> item = tree.getSelectionModel().getSelectedItem();
                safedItem = item;
                LOG.info("storing " + safedItem);
            }
        });
        
        tree.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F3) {
                tree.getSelectionModel().select(safedItem);
            }
        });
        //----------- end quick check
        
        BorderPane pane = new BorderPane(tree);
        return pane;
    }

    protected void debugLeaf(TreeItem<File> item, String mes) {
        String leafs = "getter: " + item.isLeaf() + " property: " + item.leafProperty().get(); 
        LOG.info(mes + leafs);
    }
    
    private TreeView<File> buildFileSystemBrowser() {
        TreeItem<File> root = createNode(new File("."));
        return new TreeView<File>(root);
    }

    // This method creates a TreeItem to represent the given File. It does this
    // by overriding the TreeItem.getChildren() and TreeItem.isLeaf() methods 
    // anonymously, but this could be better abstracted by creating a 
    // 'FileTreeItem' subclass of TreeItem. However, this is left as an exercise
    // for the reader.
    private TreeItem<File> createNode(final File f) {
        return new TreeItem<File>(f) {
            // We cache whether the File is a leaf or not. A File is a leaf if
            // it is not a directory and does not have any files contained within
            // it. We cache this as isLeaf() is called often, and doing the 
            // actual check on File is expensive.
            private boolean isLeaf;
   
            // We do the children and leaf testing only once, and then set these
            // booleans to false so that we do not check again during this
            // run. A more complete implementation may need to handle more 
            // dynamic file system situations (such as where a folder has files
            // added after the TreeView is shown). Again, this is left as an
            // exercise for the reader.
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;
             
            @Override public ObservableList<TreeItem<File>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
   
                    // First getChildren() call, so we actually go off and 
                    // determine the children of the File contained in this TreeItem.
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    File f = (File) getValue();
                    isLeaf = f.isFile();
                    // synchronize with property
                    if (syncInitially)
                        invokeSetLeaf(isLeaf);
                }

                return isLeaf;
            }
   
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

            private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem) {
                File f = TreeItem.getValue();
                if (f != null && f.isDirectory()) {
                    File[] files = f.listFiles();
                    if (files != null) {
                        ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();

                        for (File childFile : files) {
                            children.add(createNode(childFile));
                        }

                        return children;
                    }
                }

                return FXCollections.emptyObservableList();
            }
        };
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
