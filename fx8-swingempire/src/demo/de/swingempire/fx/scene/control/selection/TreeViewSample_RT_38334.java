/*
 * Created on 05.01.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * SelectedItems contains null if unselected 
 * grand-parent of selected item is removed
 * 
 * https://javafx-jira.kenai.com/browse/RT-38334
 */
public class TreeViewSample_RT_38334 extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
    
     // Construct and display a TreeView.
        primaryStage.setTitle("Tree View Sample");
        TreeItem<String> rootItem = new TreeItem<String> ("Root");
        TreeView<String> tree = new TreeView<String> (rootItem);
        rootItem.setExpanded(true);
        TreeItem<String> lastItem=rootItem;
        for (int i = 1; i < 4; i++) {
            TreeItem<String> item = new TreeItem<String> ("Level" + i);
            item.setExpanded(true);
            lastItem.getChildren().add(item);
            lastItem=item;
        }
        StackPane root = new StackPane();
        root.getChildren().add(tree);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
        
        // Select the Level 3 item.
        tree.getSelectionModel().selectLast();
        
        // Remove the (unselected) Level 2 item, including its selected Level 3 child.
        rootItem.getChildren().get(0).getChildren().remove(0);
        
        // Dump the selected items list to stdout. With Java 8 update 11, I see one "null" item in this output. I'd expect a zero-length list.
        for (TreeItem<String> item : tree.getSelectionModel().getSelectedItems())
         System.out.println("getSelectedItems() element = "+((item==null) ? "null" : item.toString()));
    }
}
