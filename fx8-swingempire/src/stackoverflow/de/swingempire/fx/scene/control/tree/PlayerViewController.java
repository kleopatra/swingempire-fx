/*
 * Created on 26.10.2018
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.swingempire.fx.scene.control.tree.PlayerViewApp.Player;
import de.swingempire.fx.scene.control.tree.PlayerViewApp.PlayerList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

public class PlayerViewController implements Initializable {
    /**
     * 
     */
//    private final TreeItemBindValue controller;
    /**
     * @param treeItemBindValue
     */
//    PlayerViewController(TreeItemBindValue treeItemBindValue) {
//        controller = treeItemBindValue;
//    }
    @FXML
    private VBox vbox;
    private TreeView<String> treeView;
    public void setMainApp(PlayerViewApp main) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Player p1 = new Player("Name1", "Attr1", "Attr2");
        Player p2 = new Player("Name2", "Attr1", "Attr2");
        Player p3 = new Player("Name3", "Attr1", "Attr2");
        PlayerList.getInstance().getPlayerList().addAll(p1, p2, p3);//add players to list

        treeView = new TreeView<String>(PlayerList.rootNode);
        treeView.setShowRoot(false);
        vbox.getChildren().add(treeView);//add tree view to GUI

    }
    int count;
    @FXML
    private void handleButtonClick() {
        /*this method can hold code that would run in a loop, continuously setting new values
        on p1, p2, and p3 attributes and would show the tree items updating automatically*/
        
        TreeItem<String> selected = treeView.getSelectionModel().getSelectedItem();
        String orig = selected != null ? selected.getValue() : "";
        if (selected != null) {
            selected.setValue(orig + count++);
        }
        
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(PlayerViewController.class.getName());
}