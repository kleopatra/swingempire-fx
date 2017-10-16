/*
 * Created on 26.03.2015
 *
 */
package de.swingempire.fx.scene.control.selection.treebugs;

import javafx.application.Application;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Scene;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * SelectedItems don't fire on removing children
 * https://javafx-jira.kenai.com/browse/RT-40010
 * 
1. select 'item1' -> listener on selected items is called
2. press 'r' (it removes all children) -> listener is not called.
3. press 'g' (gets the current selection) -> current selection is correct, 'root' in this case.
 *
 * fixed in 8u60b5
 * --------------
 * 
 * still open:
 * - type of event on removeAll: only added for root, no removed for child
 * - replace selected item: no event at all
 * - shift-select: two added for the replacing child, no removed for old child 
 * 
 * reported: https://javafx-jira.kenai.com/browse/RT-40359
 * 
 */
public class TreeViewSelection_40010_40359 extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final VBox box = new VBox();
        final Scene scene = new Scene(box);
        primaryStage.setScene(scene);
        final TreeView<String> treeView = getTreeView();
        box.getChildren().add(treeView);
        primaryStage.show();

        // remove all children 
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DELETE), () -> {
            treeView.getRoot().getChildren().removeIf(item -> true);
        });
        // replace selected
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R), () -> {
            TreeItem<?> old = treeView.getSelectionModel().getSelectedItem();
            int childIndex = treeView.getRoot().getChildren().indexOf(old);
            if (childIndex < 0) return;
            treeView.getRoot().getChildren().set(childIndex, createTreeChild());
        });
        // add child
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.A), () -> {
            treeView.getRoot().getChildren().add(createTreeChild());
        });
        // print selectedItems
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.G), () -> {
            final MultipleSelectionModel<TreeItem<String>> selectionModel = treeView.getSelectionModel();
            System.out.println(selectionModel.getSelectedItems());
        });
    }

    private TreeView<String> getTreeView() {
        final TreeItem<String> root = createTreeItem("root ");
        root.getChildren().add(createTreeChild());

        final TreeView<String> treeView = new TreeView<String>(root);
        root.setExpanded(true);
//        treeView.setSelectionModel(new SimpleTreeSelectionModel<>(treeView));
        treeView.getSelectionModel().select(root);
        treeView.getSelectionModel().getSelectedItems().addListener((final Change<? extends TreeItem<String>> c) -> {
            System.out.println("--- change on selectedItems: " + c);
//            FXUtils.prettyPrint(c);
        });
        return treeView;
    }

    private int count;
    private TreeItem<String> createTreeChild() {
        return createTreeItem("child ");
    }
    private TreeItem<String> createTreeItem(String text) {
//        return new TreeItemX<>(text + count++);
        return new TreeItem<>(text + count++);
    }
    public static void main(final String[] args) {
        launch(args);
    }
}
