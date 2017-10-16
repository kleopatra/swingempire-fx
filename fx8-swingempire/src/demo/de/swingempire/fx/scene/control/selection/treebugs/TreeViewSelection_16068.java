/*
 * Created on 19.03.2015
 *
 */
package de.swingempire.fx.scene.control.selection.treebugs;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Was: selection moved up if item below was removed. 
 * Fixed for 8u40/8u60 - check u20!
 * fixed fx9
 */
public class TreeViewSelection_16068 extends Application {


    @Override
    public void start(Stage stage) {
        VBox content = new VBox();
        final TreeView tree_view = new TreeView();
        TreeItem<String> tree_model = new TreeItem<String>("Root");
        for (int i = 0; i < 3; i++) {
            TreeItem node = new TreeItem("Node " + i);
            tree_model.getChildren().add(node);
        }
        tree_view.setRoot(tree_model);
        tree_view.setShowRoot(true);
        tree_model.setExpanded(true);
        Button removeBelow = new Button("Remove \"Node 2\"");
        removeBelow.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                tree_view.getRoot().getChildren().remove(2);
            }
        });
        content.getChildren().add(tree_view);
        content.getChildren().add(removeBelow);
        stage.setScene(new Scene(content, 200, 200));
        stage.setTitle(System.getProperty("java.version"));
        stage.show();
        tree_view.getSelectionModel().select(2);
    }

    public static void main(String[] args) {
        launch(TreeViewSelection_16068.class, args);
    }
}