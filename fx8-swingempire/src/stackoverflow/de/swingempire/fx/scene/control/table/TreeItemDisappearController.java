package de.swingempire.fx.scene.control.table;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

/**
 *
 */
public class TreeItemDisappearController {
    @FXML private Button btnAdd;
    @FXML private Button btnDelete;
    @FXML private TreeTableView<String> treeTableView;
    @FXML private TreeTableColumn<String, String> columnC1;

    public void initialize() {
        final TreeItem<String> childNode1 = new TreeItem<>("Child Node 1");
        final TreeItem<String> childNode2 = new TreeItem<>("Child Node 2");
        final TreeItem<String> childNode3 = new TreeItem<>("Child Node 3");

        final TreeItem<String> root = new TreeItem<>("Root node");
        root.setExpanded(true);
        root.getChildren().setAll(childNode1, childNode2, childNode3);

        columnC1.setCellValueFactory((TreeTableColumn.CellDataFeatures<String, String> p) ->
        new ReadOnlyStringWrapper(p.getValue().getValue()));

        treeTableView.setRoot(root);
        treeTableView.setShowRoot(true);
        btnAdd.setOnAction(actionEvent -> root.getChildren().add(new TreeItem<>("Another Child")));
        btnDelete.setOnAction(actionEvent -> root.getChildren().remove(root.getChildren().size()-1));
    }
}
