/*
 * Created on 15.10.2017
 *
 */
package de.swingempire.fx.scene.control.selection.treebugs;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Multiple tree selection issues, reported by scott palmer
 * https://bugs.openjdk.java.net/browse/JDK-8189228
 * 
 * 1. Ioob
 * - select an item, extend selection 
 * 
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeMultiselect_8189228 extends Application {
    private TextArea textArea;

    public static void main(String[] args) {
        launch(args);
    }
    private final ListChangeListener<TreeItem<String>> itemListener = new ListChangeListener<TreeItem<String>>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends TreeItem<String>> c) {
            appendLine("Selected Items Changed");
            while (c.next()) {
                if (c.wasAdded()) {
                    appendLine(" Added:");
                    for (TreeItem<String> added : c.getAddedSubList()) {
                        appendLine("  " + added.getValue());
                    }
                } else if (c.wasRemoved()) {
                    appendLine(" Removed:");
                    for (TreeItem<String> removed : c.getRemoved()) {
                        appendLine("  " + removed.getValue());
                    }
                } else if (c.wasPermutated()) {
                    appendLine(" Permuted.");
                } else if (c.wasReplaced()) {
                    appendLine(" Replaced.");
                } else if (c.wasUpdated()) {
                    appendLine(" Updated.");
                }
            }
        }
    };

    @Override
    public void start(Stage primaryStage) throws Exception {
        TreeItem<String> root = new TreeItem<>("Root");
        for (int i = 1; i <= 5; i++) {
            root.getChildren().add(buildTree(1, i));
        }
        root.setExpanded(true);

        TreeView<String> treeView = new TreeView<>(root);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeView.getSelectionModel().getSelectedItems().addListener(itemListener);
        BorderPane parent = new BorderPane(treeView);
        parent.setTop(new Label("Multi-select at level 4, then select node above\n"
                + "at level 3 or node below at level 3\n"
                + "Note missing Removes in list change - sometimes.\n"
                + "Or worse, IndexOutOfBounds."));
        textArea = new TextArea();
        parent.setRight(textArea);
        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.setTitle("TreeView Multiselect Test");
        primaryStage.show();
    }

    private TreeItem<String> buildTree(int depth, int n) {
        TreeItem<String> ti = new TreeItem<>(String.valueOf(depth) + " Item " + n);
        ti.setExpanded(true);
        if (depth < 4) {
            for (int i = 1; i <= 5; i++) {
                ti.getChildren().add(buildTree(depth+1, i));
            }
        }
        return ti;
    }

    private void appendLine(String msg) {
        textArea.appendText(msg);
        textArea.appendText("\n");
        System.out.println(msg);
    }
}