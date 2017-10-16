/*
 * Created on 29.03.2016
 *
 */
package de.swingempire.fx.scene.control.selection.treebugs;

import java.util.Collection;
import java.util.Objects;

import de.swingempire.fx.scene.control.selection.SimpleTreeSelectionModel;
import de.swingempire.fx.util.FXUtils;
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
 * variant for TreeTable (was original report on treeTable)
 * 
 * fx9-ea-180
 * 
 * for 2 items: no error
 * for 3 items: error after 5 expands
 * for 4 items: error after 3 expands
 */
public class TreeTableSelectionIssue_8152396 extends Application {

    int count;
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        final TreeItem<String> childNode1 = new TreeItem<>("Child Node 1");
        childNode1.getChildren().addAll(
              new TreeItem<String>("Node 1-1"),
              new TreeItem<String>("Node 1-2"),
              new TreeItem<String>("Node 1-3"),
//              new TreeItem<String>("Node 1-4"),
//          new TreeItem<String>("Node 1-5"),
          new TreeItem<String>("Node 1-last")
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
//        treeTableView.setSelectionModel(new SimpleTreeSelectionModel(treeTableView));
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Select all children of expanded node.
        treeTableView.expandedItemCountProperty().addListener((observable, oldCount, newCount) -> {
            
            
            if (newCount.intValue() > oldCount.intValue()) {
                System.out.println("expandedItems/expansionCount: " + newCount + " / " + count++);
                selectChildrenOfRows(treeTableView, treeTableView.getSelectionModel().getSelectedIndices());
            } else {
                System.out.println("collapsed, selectedItems: " + treeTableView.getSelectionModel().getSelectedItems().size());
            }
        });

        new FXUtils.PrintingListChangeListener("selectedItems", treeTableView.getSelectionModel().getSelectedItems());
        

        filteredList = treeTableView.getSelectionModel().getSelectedItems().filtered(Objects::nonNull);
        filteredList.addListener((ListChangeListener.Change<? extends TreeItem<String>> change) -> {
            System.out.printf("Change on filtered: %s\n", change);
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

