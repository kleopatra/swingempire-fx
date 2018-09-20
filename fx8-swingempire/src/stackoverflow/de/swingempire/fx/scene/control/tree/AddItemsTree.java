/*
 * Created on 18.09.2018
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TreeViewFocusModel (and/or its SelectionModel) still is broken at 
 * several aspects - giving up ...
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AddItemsTree extends Application {

    int count;

    private Parent createContent() {
        TreeView<String> tree = new TreeView<>();
//        MyTreeViewFocusModel.dispose(tree);
//        tree.setFocusModel(new MyTreeViewFocusModel<>(tree));
        TreeItem<String> root = new TreeItem<>("root");
        root.setExpanded(true);
        tree.setRoot(root);
        List<TreeItem<String>> nodeList = Stream
                .generate(() -> "item: " + count++).limit(100000)
                .map(TreeItem::new).collect(Collectors.toList());
        TreeItem<String> firstChild = new TreeItem<>("child");
//        firstChild.getChildren().setAll(nodeList);
        firstChild.setExpanded(true);
        tree.getRoot().getChildren().addAll(firstChild); //, new TreeItem("dummy"));
//        tree.getFocusModel().focus(0);
        tree.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F1) {
                firstChild.getChildren().setAll(nodeList);
            }
        });

        tree.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F2) {
                firstChild.getChildren().clear();
            }

        });

        BorderPane content = new BorderPane(tree);
        return content;
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
            .getLogger(AddItemsTree.class.getName());

}
