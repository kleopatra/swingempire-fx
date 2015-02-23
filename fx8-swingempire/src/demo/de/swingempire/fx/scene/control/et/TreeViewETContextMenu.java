/*
 * Created on 23.02.2015
 *
 */
package de.swingempire.fx.scene.control.et;

import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeViewETContextMenu extends Application {

    private Parent getContent() {
        TreeItem<String> root = createBranch();
        TreeView<String> tree = new TreeView<>();
        tree.setRoot(root);
        root.setExpanded(true);
        tree.setCellFactory(p -> new TreeCellC<>(new ContextMenu(new MenuItem("item"))));
        return tree;
    }    
    
    private static class TreeCellC<T> extends TreeCell<T> {
        
        public TreeCellC(ContextMenu menu) {
            setContextMenu(menu);
        }
        
        // boilerplate copy of default tree cell
        private HBox hbox;
        private void updateDisplay(T item, boolean empty) {
            if (item == null || empty) {
                hbox = null;
                setText(null);
                setGraphic(null);
            } else {
                // update the graphic if one is set in the TreeItem
                TreeItem<T> treeItem = getTreeItem();
                Node graphic = treeItem == null ? null : treeItem.getGraphic();
                if (graphic != null) {
                    if (item instanceof Node) {
                        setText(null);
                        
                        // the item is a Node, and the graphic exists, so 
                        // we must insert both into an HBox and present that
                        // to the user (see RT-15910)
                        if (hbox == null) {
                            hbox = new HBox(3);
                        }
                        hbox.getChildren().setAll(graphic, (Node)item);
                        setGraphic(hbox);
                    } else {
                        hbox = null;
                        setText(item.toString());
                        setGraphic(graphic);
                    }
                } else {
                    hbox = null;
                    if (item instanceof Node) {
                        setText(null);
                        setGraphic((Node)item);
                    } else {
                        setText(item.toString());
                        setGraphic(null);
                    }
                }
            }                
        }
        
        @Override public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            updateDisplay(item, empty);
        }
    }
    
    /**
     * @return
     */
    private TreeItem<String> createBranch() {
        List<String> rawItems = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        TreeItem<String> root = new TreeItem<>("root");
        rawItems.stream().forEach(s -> root.getChildren().add(new TreeItem<>(s)));
        return root;
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
