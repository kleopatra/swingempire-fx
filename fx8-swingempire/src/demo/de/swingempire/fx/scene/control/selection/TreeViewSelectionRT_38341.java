/*
 * Created on 22.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-38341
 * 
 * no notification of change of selectedItem if selected iem
 * is removed.
 * 
 * To reproduce:
 * - expand root1
 * - select child 1
 * - press delete: selection is moved to root1
 * - expected: notification of selectedItem change
 * - actual: no notification
 */
public class TreeViewSelectionRT_38341 extends Application {

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final StackPane stackPane = new StackPane();

        final TreeItem<String> root = new TreeItem<>();
        root.setExpanded(true);
        root.getChildren().addAll(createItem(1), createItem(2));

        final TreeView<String> treeView = new TreeView<>(root);
        treeView.setShowRoot(false);
        treeView.getSelectionModel().getSelectedItems().addListener(this::handleChange);
        treeView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                final TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
                selectedItem.getParent().getChildren().remove(selectedItem);
            }
        });

        stackPane.getChildren().add(treeView);

        final Scene scene = new Scene(stackPane, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TreeItem<String> createItem(final int number) {
        final TreeItem<String> root = new TreeItem<>("Root " + number);
        final TreeItem<String> child = new TreeItem<>("Child " + number);

        root.getChildren().add(child);
        return root;
    }

    private void handleChange(final Change<? extends TreeItem<String>> change) {
        System.out.println(change);
        System.out.println(change.getList());
    }

}
