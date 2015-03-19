/*
 * Created on 19.03.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Was: invalid change events
Case 1: fixed 8u60b5
- select items: 'Item 1' and 'Item 2'
- collapse node: 'Root Node 1'

output >>
[null]
[]
[TreeItem [ value: Root Node 1 ]]

Case 2 - I suppose it may be another form of first case: fixed
- select items: 'Item 21' and 'Item 22'
- collapse node 'Item 2'

output>>
[TreeItem [ value: Root Node 2 ]]
[]
[TreeItem [ value: Item 2 ]]


 * 
 */
public class TreeSelection_37366 extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final VBox box = new VBox();
        final Scene scene = new Scene(box);
        primaryStage.setScene(scene);
        box.getChildren().add(getTreeView());
        primaryStage.show();
    }

    private Node getTreeView() {
        final TreeItem<String> treeItem2 = new TreeItem<String>("Item 2");
        treeItem2.getChildren().addAll(new TreeItem<String>("Item 21"), new TreeItem<String>("Item 22"));

        final TreeItem<String> root1 = new TreeItem<String>("Root Node 1");
        root1.getChildren().addAll(new TreeItem<String>("Item 1"), treeItem2, new TreeItem<String>("Item 3"));
        root1.setExpanded(true);

        final TreeItem<String> root2 = new TreeItem<String>("Root Node 2");

        final TreeItem<String> hiddenRoot = new TreeItem<String>("Hidden Root Node");
        hiddenRoot.getChildren().add(root1);
        hiddenRoot.getChildren().add(root2);

        final TreeView<String> treeView = new TreeView<String>(hiddenRoot);
        treeView.setShowRoot(false);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // listener which prints selected items
        treeView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TreeItem<String>>() {
            @Override
            public void onChanged(final ListChangeListener.Change<? extends javafx.scene.control.TreeItem<String>> c) {
                System.out.println(c.getList());
            }
        });
        return treeView;
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
