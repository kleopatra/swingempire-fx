/*
 * Created 10.11.2020 
 */

package de.swingempire.fx.scene.control.tree;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/64769593/203657
 * Initial focus at second child if
 * 
 * - showRoot is false
 * - children added one-by-one (vs. via addAll)
 */
public class TreeInitialFocus extends Application {

    @Override
    public void start(Stage stage) {

        TreeView<String> treeView = new TreeView<>();
        TreeItem<String> root = new TreeItem<>("Root");
        root.setExpanded(true);
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        
//        root.getChildren().addAll(new TreeItem<>("Foo"), 
//                new TreeItem<>("Bar"),
//                new TreeItem<>("Baz")
//                );
        
        root.getChildren().add(new TreeItem<>("Foo"));
        root.getChildren().add(new TreeItem<>("Bar"));
        root.getChildren().add(new TreeItem<>("Baz"));

        Button hideRoot = new Button("hide root");
        hideRoot.setOnAction(e -> {
            treeView.setShowRoot(false);
        });
        Button showRoot = new Button("show root");
        showRoot.setOnAction(e -> {
            treeView.setShowRoot(true);
        });
        BorderPane content = new BorderPane(treeView);
        content.setBottom(new HBox(10, hideRoot, showRoot));
        var scene = new Scene(content, 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}