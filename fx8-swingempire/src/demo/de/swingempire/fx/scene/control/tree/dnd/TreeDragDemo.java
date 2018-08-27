/*
 * Created on 27.08.2018
 *
 */
package de.swingempire.fx.scene.control.tree.dnd;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class TreeDragDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tree Drag Example");

        TreeItem<TaskNode> rootItem = new TreeItem<TaskNode>(new TaskNode("Tasks"));
        rootItem.setExpanded(true);
        
        createChildren(rootItem);

        rootItem.getChildren().forEach(this::createChildren);
        TreeView<TaskNode> tree = new TreeView<TaskNode>(rootItem);
        tree.setCellFactory(new TaskCellFactory());
        
        StackPane root = new StackPane();
        root.getChildren().add(tree);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

    int count = 0;
    
    /**
     * @param rootItem
     */
    public void createChildren(TreeItem<TaskNode> rootItem) {
        ObservableList<TreeItem<TaskNode>> children = rootItem.getChildren();
        children.add(new TreeItem<TaskNode>(new TaskNode("do laundry" + count)));
        children.add(new TreeItem<TaskNode>(new TaskNode("get groceries"+ count)));
        children.add(new TreeItem<TaskNode>(new TaskNode("drink beer"+ count)));
        children.add(new TreeItem<TaskNode>(new TaskNode("defrag hard drive"+ count)));
        children.add(new TreeItem<TaskNode>(new TaskNode("walk dog"+ count)));
        children.add(new TreeItem<TaskNode>(new TaskNode("buy beer"+ count)));
        count++;
    }
}
