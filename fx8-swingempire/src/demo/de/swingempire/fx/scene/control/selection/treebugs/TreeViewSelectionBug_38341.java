/*
 * Created on 09.03.2015
 *
 */
package de.swingempire.fx.scene.control.selection.treebugs;

import de.swingempire.fx.scene.control.selection.SimpleTreeSelectionModel;
import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * original issue: the item in the selectedItems wasn't updated
 * that's fixed (verified in 8u40b23). Still prevailing
 * the listChange is incorrect in not notifying about the removal
 * 
 * PENDING JW: SimpleTreeSelectionModel fires two events - one for 
 * removing, another for re-selecting. Why exactly? 
 * 
 * 
 * original steps:
Steps to reproduce:
- expand Root 1
- select Child 1
- press Delete on keyboard
Result/bug was: Root 1 is visually selected without having fired the notification on selectedItems

output >>
{ [TreeItem [ value: Child 1 ]] added at 0, }
[TreeItem [ value: Child 1 ]]

Missing notification that Root 1 is now selected. Changing the selection produces output:
{ [TreeItem [ value: Root 1 ]] replaced by [TreeItem [ value: Child 2 ]] at 0, }
[TreeItem [ value: Child 2 ]]

 */
public class TreeViewSelectionBug_38341 extends Application {

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final StackPane stackPane = new StackPane();

//        final TreeItem<String> root = new TreeItem<>();
//        root.setExpanded(true);
//        root.getChildren().addAll(createItem(1), createItem(2));
//        
        final TreeItemX<String> root = new TreeItemX<>();
        root.setExpanded(true);
        root.getChildren().addAll(createItemX(1), createItemX(2));

        final TreeView<String> treeView = new TreeView<>(root);
        treeView.setSelectionModel(new SimpleTreeSelectionModel<>(treeView));
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
    
    private TreeItem<String> createItemX(final int number) {
        final TreeItem<String> root = new TreeItemX<>("Root " + number);
        final TreeItem<String> child = new TreeItemX<>("Child " + number);

        root.getChildren().add(child);
        return root;
    }

    private int count;
    private void handleChange(final Change<? extends TreeItem<String>> change) {
        FXUtils.prettyPrint(change);
//        System.out.println(count++ + " change: "+ change);
//        System.out.println("on list" + change.getList());
    }

}