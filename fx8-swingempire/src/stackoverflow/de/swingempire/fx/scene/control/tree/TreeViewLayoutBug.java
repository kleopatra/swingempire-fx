/*
 * Created 17.06.2022
 */

package de.swingempire.fx.scene.control.tree;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


/**
 *
 */
public class TreeViewLayoutBug extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final TreeView<String> fxTree = new TreeView<>();
        BorderPane root = new BorderPane(fxTree);
        final Scene scene = new Scene(root, 250, 300);
        String version = System.getProperty("javafx.runtime.version");
        scene.getStylesheets().add(this.getClass().getResource("treeviewlayoutissue.css").toExternalForm());
        primaryStage.setTitle("TreeView " + version);
        primaryStage.setScene(scene);
        primaryStage.show();

        // set data after showing the tree
        addData(fxTree);

    }

    private void addData(TreeView<String> tree) {
        // build the item hierarchy
        final TreeItem<String> rootNode = new TreeItem<>("root");
        rootNode.setExpanded(true);
        final TreeItem<String> firstRootChild = new TreeItem<>("Child A");
        final TreeItem<String> secondRootChild = new TreeItem<>("Child B");
        rootNode.getChildren().addAll(firstRootChild, secondRootChild);

        final TreeItem<String> subNode = new TreeItem<>("GrandChild A");
        firstRootChild.getChildren().addAll(subNode);
        // uncomment to see initial broken layout moving to last expanded
//         secondRootChild.getChildren().add(new TreeItem<>("GrandChild B"));

        firstRootChild.setExpanded(true);
        secondRootChild.setExpanded(true);
        subNode.setExpanded(true);
        // set root - note: doing at the end to not have effect of showRoot
        tree.setRoot(rootNode);
    }


    public static void main(String[] args) {
        launch(args);
    }

}
