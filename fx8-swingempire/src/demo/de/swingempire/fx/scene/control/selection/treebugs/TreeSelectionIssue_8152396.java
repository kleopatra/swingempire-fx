/*
 * Created on 29.03.2016
 *
 */
package de.swingempire.fx.scene.control.selection.treebugs;

import java.util.Collection;
import java.util.Objects;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

/**
 * Same for TreeView.
 * 
 * Reproduce by:
 * 1. Select a non-leaf TreeItem that is a child of the root node in the TreeTableView.
 * 2. Rapidly expand/collapse the selected 
 * TreeItem by clicking quickly on the expand icon.
 *
 * Actually, there's no need for being quick, it's regular: always happens
 * after n expands, n depending on the number of child nodes
 * 
 * 2 children -> n = 6
 * 3 children -> n = 4
 * 4 or more children -> n = 3
 * 
 * Commented issue.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeSelectionIssue_8152396 extends Application {

    int count;
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        final TreeItem<String> childNode2 = new TreeItem<>("Child Node 2");
        childNode2.getChildren().addAll(
//                new TreeItem<String>("Node 2-1"),
//                new TreeItem<String>("Node 2-2"),
//                new TreeItem<String>("Node 2-3"),
//                new TreeItem<String>("Node 2-4"),
                new TreeItem<String>("Node 2-5"),
                new TreeItem<String>("Node 2-6")
                );
        final TreeItem<String> childNode1 = new TreeItem<>("Child Node 1");
        childNode1.getChildren().addAll(
//                new TreeItem<String>("Node 1-1"),
//                new TreeItem<String>("Node 1-2"),
                new TreeItem<String>("Node 1-1"),
                new TreeItem<String>("Node 1-2"),
            new TreeItem<String>("Node 1-3"),
            new TreeItem<String>("Node 1-4")
        );

        final TreeItem<String> root = new TreeItem<>("Root node");
        root.setExpanded(true);
        root.getChildren().addAll(childNode1); //, childNode2);

//        TreeTableColumn<String,String> column = new TreeTableColumn<>("Column");
//        column.setPrefWidth(190);
//        column.setCellValueFactory((CellDataFeatures<String, String> p) ->
//            new ReadOnlyStringWrapper(p.getValue().getValue()));

        final TreeView<String> treeView = new TreeView<>(root);
        treeView.setPrefWidth(200);
        treeView.setShowRoot(true);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Select all children of expanded node.
        treeView.expandedItemCountProperty().addListener((observable, oldCount, newCount) -> {
            if (newCount.intValue() > oldCount.intValue()) {
                System.out.println("expandedItems/expansionCount: " + newCount + " / " + count++);
                selectChildrenOfRows(treeView, treeView.getSelectionModel().getSelectedIndices());
            } else {
                System.out.println("collapsed, selectedItems: " + treeView.getSelectionModel().getSelectedItems().size());
            }
        });

        new FXUtils.PrintingListChangeListener("selectedItems", treeView.getSelectionModel().getSelectedItems());
        
        filteredList = treeView.getSelectionModel().getSelectedItems().filtered(Objects::nonNull);
        filteredList.addListener((ListChangeListener.Change<? extends TreeItem<String>> change) -> {
            System.out.printf("Change on Filtered: %s\n", change);
        });

        final Scene scene = new Scene(new Group(), 200, 400);
        Group sceneRoot = (Group)scene.getRoot();
        sceneRoot.getChildren().add(treeView);

        stage.setTitle("Tree Table View Samples");
        stage.setScene(scene);
        stage.show();
    }

    private FilteredList<TreeItem<String>> filteredList;

    private void selectChildrenOfRows(TreeView<String> table, Collection<Integer> selectedRows) {
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

