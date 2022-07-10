package de.swingempire.fx.scene.control.tree;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * https://stackoverflow.com/q/72625590/203657
 * isNeedLayout true for some cells
 */
public class TreeViewLayoutIssue11 extends Application {
    int k = 1;

    public static void main(String... a) {
        Application.launch(a);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final TreeView<String> fxTree = new TreeView<>();
        fxTree.setCellFactory(t -> new TreeCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item);
                } else {
                    setText(null);
                }
            }
        });
        fxTree.setShowRoot(false);
//        addData(fxTree);

        StackPane root = new StackPane(fxTree);
//        BorderPane root = new BorderPane(fxTree);
        root.setPadding(new Insets(15));
        final Scene scene = new Scene(root, 250, 250);
        scene.getStylesheets().add(this.getClass().getResource("treeviewlayoutissue.css").toExternalForm());

        String version = System.getProperty("javafx.runtime.version");
        primaryStage.setTitle("TreeView :: " + version);
        primaryStage.setX(50);
        primaryStage.setScene(scene);
        primaryStage.show();
        // orig: add Data after showing
        addData(fxTree);


        final Timeline timeline = new Timeline(new KeyFrame(Duration.millis(2000), e -> {
            System.out.println("\nIteration #" + k++);
            printNeedsLayout(fxTree);
            System.out.println("-----------------------------------------------------------------------------");
        }));
        timeline.setCycleCount(3);
        timeline.play();

        Button start = new Button("log");
        start.setOnAction(e -> {
        });


    }

    private void printNeedsLayout(final Parent parent) {
        System.out.println("  " + parent + " isNeedsLayout: " + parent.isNeedsLayout());
        for (final Node n : parent.getChildrenUnmodifiable()) {
            if (n instanceof Parent) {
                printNeedsLayout((Parent) n);
            }
        }
    }

    private void addData(TreeView<String> fxTree) {
        final TreeItem<String> rootNode = new TreeItem<>("");
        fxTree.setRoot(rootNode);
        final TreeItem<String> grp1Node = new TreeItem<>("Group 1");
        final TreeItem<String> grp2Node = new TreeItem<>("Group 2");
        rootNode.getChildren().addAll(grp1Node, grp2Node);

        final TreeItem<String> subNode = new TreeItem<>("Team");
        grp1Node.getChildren().addAll(subNode);

        final List<TreeItem<String>> groups = Stream.of("Red", "Green", "Yellow", "Blue").map(TreeItem::new).collect(Collectors.toList());
        groups.forEach(itm -> subNode.getChildren().add(itm));

        grp1Node.setExpanded(true);
        grp2Node.setExpanded(true);
        subNode.setExpanded(true);
    }
}

