/*
 * Created on 08.03.2018
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

/**
 * Error in change notification from TreeTableView, fine for TreeView (this)
 * https://bugs.openjdk.java.net/browse/JDK-8199324
 * 
 */
public class TreeViewSelection8199324 extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Tree View");
        final Scene scene = new Scene(new Group(), 200, 400);
        Group sceneRoot = (Group)scene.getRoot();
        final TreeView<String> treeView = new TreeView<>();

        TreeItem<String> one = new TreeItem<>("One");
        TreeItem<String> two = new TreeItem<>("Two");

        TreeItem<String> root = new TreeItem<>("Root");
        root.setExpanded(true);
        root.getChildren().setAll(one, two);

        treeView.setRoot(root);
        treeView.setShowRoot(true);
        sceneRoot.getChildren().add(treeView);
        treeView.getSelectionModel().select(1);
        root.getChildren().setAll(one, two);
        stage.setScene(scene);
        stage.show();
    }
}