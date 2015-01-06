/*
 * Created on 17.12.2014
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.io.File;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Example from api doc TreeItem.
 * 
 * Inconsistent isLeaf vs. leafProperty.get()
 * Reported: https://javafx-jira.kenai.com/browse/RT-39762
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class FileTreeExample extends Application {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(FileTreeExample.class
            .getName());
    
    /**
     * @return
     */
    private Parent getContent() {
        TreeView<File> tree = buildFileSystemBrowser();
        tree.getRoot().setExpanded(true);
        for (TreeItem child : tree.getRoot().getChildren()) {
            if (child.isLeaf() != child.leafProperty().get()) {
//                throw new IllegalStateException("inconsistent leaf state " + child);
            }
        }
        tree.getSelectionModel().selectedItemProperty().addListener((p, old, item) -> {
            if (item == null) return;
            // invalid implementation of isLeaf: propery and getter out off synch
            // initially for folders, all correct after first expansion
            String leafs = "getter: " + item.isLeaf() + " property: " + item.leafProperty().get(); 
            LOG.info("" + leafs);
        });
        BorderPane pane = new BorderPane(tree);
        return pane;
    }
    
    private TreeView buildFileSystemBrowser() {
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
                    // dirty trick, doesn't work for root
                    if (!isLeaf) {
                        setExpanded(true);
                        setExpanded(false);
                    }
                }

                return isLeaf;
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
