/*
 * Created on 15.10.2017
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Collection;
import java.util.Objects;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;

/**
 * This her is the original example - seems to work.
 * 
 * Check why the adaption doesn't.
 * 
 * depends on count of items:  
 * fails on 5th expand with 3 items
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeTableSelection_original_8152396 extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        final TreeItem<String> childNode1 = new TreeItem<>("Child Node 1");
        childNode1.getChildren().addAll(
            new TreeItem<String>("Node 1-1"),
            new TreeItem<String>("Node 1-2"),
//            new TreeItem<String>("Node 1-2"),
            new TreeItem<String>("Node 1-2"),
            new TreeItem<String>("Node 1-3")
        );

        final TreeItem<String> root = new TreeItem<>("Root node");
        root.setExpanded(true);
        root.getChildren().add(childNode1);

        TreeTableColumn<String,String> column = new TreeTableColumn<>("Column");
        column.setPrefWidth(190);
        column.setCellValueFactory((CellDataFeatures<String, String> p) ->
            new ReadOnlyStringWrapper(p.getValue().getValue()));

        final TreeTableView<String> treeTableView = new TreeTableView<>(root);
        treeTableView.getColumns().add(column);
        treeTableView.setPrefWidth(200);
        treeTableView.setShowRoot(true);
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Select all children of expanded node.
        treeTableView.expandedItemCountProperty().addListener((observable, oldCount, newCount) -> {
            if (newCount.intValue() > oldCount.intValue()) {
                selectChildrenOfRows(treeTableView, treeTableView.getSelectionModel().getSelectedIndices());
            }
        });

        filteredList = treeTableView.getSelectionModel().getSelectedItems().filtered(Objects::nonNull);
        filteredList.addListener((ListChangeListener.Change<? extends TreeItem<String>> change) -> {
            System.out.printf("Change: %s\n", change);
        });

        final Scene scene = new Scene(new Group(), 200, 400);
        Group sceneRoot = (Group)scene.getRoot();
        sceneRoot.getChildren().add(treeTableView);

        stage.setTitle("Tree Table View Samples");
        stage.setScene(scene);
        stage.show();
    }

    private FilteredList<TreeItem<String>> filteredList;

    private void selectChildrenOfRows(TreeTableView<String> table, Collection<Integer> selectedRows) {
        for (int index: selectedRows) {
            TreeItem<String> item = table.getTreeItem(index);

            if (item != null && item.isExpanded() && !item.getChildren().isEmpty()) {
                int startIndex = index + 1;
                int maxCount   = startIndex + item.getChildren().size();

                table.getSelectionModel().selectRange(startIndex, maxCount);
            }
        }
    }
}
