/*
 * Created on 17.12.2014
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import de.swingempire.fx.scene.control.selection.SimpleTreeSelectionModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
public class FileTreeXExample extends Application {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(FileTreeXExample.class
            .getName());
    
    boolean syncInitially;
    
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
        
        // trying to track-down issue with incorrect children on expansion
        tree.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F2) {
                TreeItem<File> item = tree.getSelectionModel().getSelectedItem();
                FileTreeItemX nextFolder =  (FileTreeItemX) item.previousSibling();
                while (nextFolder != null) {
                    if (!nextFolder.isLeaf()) break;
                    nextFolder = (FileTreeItemX) nextFolder.previousSibling();
                }
                if (nextFolder != null) {
                    File dir = nextFolder.getValue();
                    int fileCount = dir.listFiles().length;
                    nextFolder.setExpanded(true);
                    LOG.info("files/expandedDesc " + fileCount + "/ "+ nextFolder.getExpandedDescendantCount());
                }
                e.consume();
            }
        });
        Button b = new Button();
        BorderPane pane = new BorderPane(tree);
        return pane;
    }

    protected void debugLeaf(TreeItem<File> item, String mes) {
        String leafs = "getter: " + item.isLeaf() + " property: " + item.leafProperty().get(); 
        LOG.info(mes + leafs);
    }
    
    private TreeView<File> buildFileSystemBrowser() {
        TreeItem<File> root = createNode(new File("."));
        TreeView<File> treeView = new TreeView<File>(root);
        treeView.setSelectionModel(new SimpleTreeSelectionModel<>(treeView));
        return treeView;
    }

    private static TreeItem<File> createNode(final File f) {
        return new FileTreeItemX(f);
    }

    /**
     * Modified from core: don't override the getter! As per convention, properties
     * and their getters/setters must be sync'ed at all times. Even with access to
     * super's property (plus base playing cleanly) this will result in a getter
     * with observable side-effects, never a good idea!
     * <p>
     * 
     * PENDING JW: misbehaviour on expansion (happens only if next had never been expanded)
     * - select first folder
     * (not needed: - press DELETE to remove children)
     * - expand next any folder below
     * - expected: showing children as always
     * - actual: showing children plus same number of empty lines
     * 
     * Seems to be related to lazy eval of children, somehow the dirtyness around
     * base's previous/expandedDescendantCount seems to get confused.
     * 
     * Actually, unrelated to deletion - happens after selecting any node above. So looks
     * like a bug in selection if children are lazily evaluated? Hmm ... do we want to
     * dig into it? Could be previous/expandedCount getting confused?
     * 
     * Problem might be interleaved treeModification events: children are added while
     * receiving the expansion notification, thus firing a list change "in-between".
     * 
     * 
     */
    public static class FileTreeItemX extends TreeItemX<File> {
        private boolean isFirstTimeChildren = true;
        private boolean allowsChildren;

        public FileTreeItemX(File file) {
            super(file);
            updateLeaf(file);
        }

//------------------- implement Leafness
        
        protected void updateLeaf(File file) {
            allowsChildren = file != null && !file.isFile();
            invokeSetLeaf(!allowsChildren);
        }
        
        @Override
        protected void updateLeaf(ObservableList<? extends TreeItem<File>> list) {
            if (isAskAllowsChildren()) {
                invokeSetLeaf(!isAllowsChildren());
            } else {
                super.updateLeaf(list);
            }
        }


        @Override
        public boolean isAskAllowsChildren() {
            return true;
        }


        @Override
        public boolean isAllowsChildren() {
            return allowsChildren;
        }

        @Override public ObservableList<TreeItem<File>> getChildren() {
            if (isFirstTimeChildren) {
                isFirstTimeChildren = false;

                // First getChildren() call, so we actually go off and 
                // determine the children of the File contained in this TreeItem.
                // needs to be queued for building, otherwise listener might
                // get confused by interleaved treeModification events
                // on expansion
                Platform.runLater(() -> super.getChildren().setAll(buildChildren(this)));
//                super.getChildren().setAll(buildChildren(this));
            }
            return super.getChildren();
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
