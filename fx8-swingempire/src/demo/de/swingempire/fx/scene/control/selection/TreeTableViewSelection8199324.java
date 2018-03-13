/*
 * Created on 08.03.2018
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;

/**
 * Error in change notification from TreeTableView, fine for TreeView (the other)
 * https://bugs.openjdk.java.net/browse/JDK-8199324
 * 
 * Note: the children are replaced by themselves! throws on 2nd replaced
 * 
 */
public class TreeTableViewSelection8199324 extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Tree Table View");
        final Scene scene = new Scene(new Group(), 200, 400);
        Group sceneRoot = (Group)scene.getRoot();
        final TreeTableView<String> treeTableView = new TreeTableView<>();

        TreeItem<String> one = new TreeItem<>("One");
        TreeItem<String> two = new TreeItem<>("Two");

        TreeItem<String> root = new TreeItem<>("Root");
        root.setExpanded(true);
        root.getChildren().setAll(one, two);

        TreeTableColumn<String, String> col = new TreeTableColumn<>("Column");
        col.setCellValueFactory(param -> param.getValue().valueProperty());

        treeTableView.getColumns().add(col);
        treeTableView.setRoot(root);
        treeTableView.setShowRoot(true);
        sceneRoot.getChildren().add(treeTableView);
        treeTableView.getSelectionModel().select(1);
        // adding the children throws if one is selected
        root.getChildren().setAll(one, two);
        stage.setScene(scene);
        stage.show();
    }
}