/*
 * Created on 15.07.2019
 *
 */
package de.swingempire.fx.scene.control;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TreeView scrolling unpredictable when expanding/collapsing a node.
 * https://stackoverflow.com/q/57025089/203657
 * 
 * Looks like a bug.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeExpandScroll extends Application {

    private Parent createContent() {
        TreeItem<String> root = createSubTree("root");
        TreeItem<String> treeItem = root.getChildren().get(5);
        createChildren(treeItem);
        TreeView<String> tree = new TreeView<>(root);
        BorderPane content = new BorderPane(tree);
        return content;
    }

    ObservableList<String> rawItems = FXCollections.observableArrayList(
            "9-item", "8-item", "7-item", "6-item", 
            "5-item", "4-item", "3-item", "2-item", "1-item");
    
    protected TreeItem<String> createSubTree(String value) {
        TreeItem<String> child = new TreeItem<>(value);
        return createChildren(child);
    }

    /**
     * @param item
     * @return
     */
    protected TreeItem<String> createChildren(TreeItem<String> item) {
        item.getChildren().setAll((List<TreeItem<String>>) rawItems.stream()
                .map(TreeItem::new)
                .collect(Collectors.toList()));
        return item;
    }

    protected TreeItem<String> createItem(String value) {
        return new TreeItem<>(value);
    }


    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeExpandScroll.class.getName());

}
