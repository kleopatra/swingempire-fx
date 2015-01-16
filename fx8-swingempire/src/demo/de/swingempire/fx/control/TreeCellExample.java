/*
 * Created on 12.12.2014
 *
 */
package de.swingempire.fx.control;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import com.sun.javafx.scene.control.skin.TreeCellSkin;

/**
 * Selection color only on text, not on whole row
 * http://stackoverflow.com/q/23792004/203657
 * 
 * - TreeCell fills the complete width
 * - Text.fill is bound (due to being Stylable?)
 * 
 * giving up for now ..
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeCellExample extends Application {

    public static class MyTreeCell extends TreeCell<String> {
       
        private Label label;

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
//                if (label == null) {
//                    label = new Label();
//                    label.setBackground(new Background(new BackgroundFill(Color.AZURE, null, null)));
//                }
//                label.setText(item);
                setText(item);
                setGraphic(label);
            }
            if (getSkin() instanceof TreeCellSkin) {
                ObservableList<Node> children = ((TreeCellSkin)getSkin()).getChildren();
                if (!children.isEmpty() && children.get(0) instanceof Text) {
                    Text text = (Text) children.get(0);
                    LOG.info("" + text);
                    text.setFill(Color.RED);
                }
            }
        }
 
        
    }
    
    private Parent getContent() {
        TreeItem root = createSubTree("root");
        root.setExpanded(true);
        TreeView tree = new TreeView(root);
        tree.setCellFactory(p -> new MyTreeCell());
        
        int grandIndex = 3;
        TreeItem collapsedChild = createSubTree("collapsedChild");
        TreeItem expandedChild = createSubTree("expandedChild");
        expandedChild.setExpanded(true);
        
        IntegerProperty index = new SimpleIntegerProperty(2);
        Button addCollapsed = new Button("addCollapsed");
        addCollapsed.setOnAction(e -> {
        });
        Button addExpanded = new Button("addExpanded");
        addExpanded.setOnAction(e -> {
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
    private static final Logger LOG = Logger.getLogger(TreeCellExample.class
            .getName());
}
