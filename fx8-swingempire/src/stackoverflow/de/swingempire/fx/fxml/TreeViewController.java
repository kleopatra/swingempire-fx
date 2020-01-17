/*
 * Created on 16.01.2020
 *
 */
package de.swingempire.fx.fxml;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeViewController {

    @FXML private TextField inputText;
    @FXML private TreeView<String> treeView;
    
    @FXML
    private void initialize() {
        treeView.setRoot(createItemHierarchy(new TreeItem<String>("root1"), new TreeItem<String>("root2")));
    }
    
    private TreeItem<String> createItemHierarchy(TreeItem<String>... root1) {
        TreeItem<String> root = new TreeItem<>("base root");
        root.getChildren().addAll(root1);
        return root ;
    }

}
