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

    @FXML TreeView<String> treeView;
    @FXML TextField inputText;
    @FXML TreeItem<String> treeRoot;
    
    @FXML
    private void initialize() {
//        treeView.setRoot(treeRoot);
    }
}
