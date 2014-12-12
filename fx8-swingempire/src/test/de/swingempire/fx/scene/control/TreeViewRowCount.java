/*
 * Created on 12.12.2014
 *
 */
package de.swingempire.fx.scene.control;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * Sanity check for unexpected getRow: index of grandChild independent of 
 * its parent being expanded or not.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeViewRowCount extends Application {

    private Parent getContent() {
        TreeItem root = createSubTree("root");
        root.setExpanded(true);
        TreeView tree = new TreeView(root);
        
        int grandIndex = 3;
        TreeItem collapsedChild = createSubTree("collapsedChild");
        TreeItem expandedChild = createSubTree("expandedChild");
        expandedChild.setExpanded(true);
        
        IntegerProperty index = new SimpleIntegerProperty(2);
        Button addCollapsed = new Button("addCollapsed");
        addCollapsed.setOnAction(e -> {
            root.getChildren().add(index.get(), collapsedChild);
            LOG.info("collapsed added at " + index.get() + " row " + tree.getRow((TreeItem) collapsedChild.getChildren().get(grandIndex)));
            index.set(index.get() + 1);
        });
        Button addExpanded = new Button("addExpanded");
        addExpanded.setOnAction(e -> {
            root.getChildren().add(index.get(), expandedChild);
            LOG.info("expanded added at " + index.get() + " row " + tree.getRow((TreeItem) expandedChild.getChildren().get(grandIndex)));
            index.set(index.get() + 1);
        });
        FlowPane buttons = new FlowPane(addCollapsed, addExpanded);
        BorderPane pane = new BorderPane(tree);
        pane.setBottom(buttons);
        return pane;
    }

    protected TreeItem createItem(Object item) {
        return new TreeItem(item);
    }

    protected ObservableList<TreeItem> createItems(ObservableList other) {
        ObservableList items = FXCollections.observableArrayList();
        other.stream().forEach(item -> items.add(createItem(item)));
        return items;
    }
    ObservableList rawItems = FXCollections.observableArrayList(
            "9-item", "8-item", "7-item", "6-item", 
            "5-item", "4-item", "3-item", "2-item", "1-item");

    protected TreeItem createSubTree(Object item) {
        TreeItem child = createItem(item);
        child.getChildren().setAll(createItems(rawItems));
        return child;
    }


    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TreeViewRowCount.class
            .getName());
}
