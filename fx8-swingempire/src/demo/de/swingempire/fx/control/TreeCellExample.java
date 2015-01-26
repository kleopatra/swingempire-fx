/*
 * Created on 12.12.2014
 *
 */
package de.swingempire.fx.control;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Selection color only on text, not on whole row
 * http://stackoverflow.com/q/23792004/203657
 * 
 * - TreeCell fills the complete width
 * - Text.fill is bound (due to being Stylable?)
 * - Text has no background
 * - can't make css to look-up highlight color, only hard-coded
 * 
 * Links around native theming:
 * https://bitbucket.org/software4java/javafx-native-themes/src (doesn't contain
 * treeCell, nor other cells)
 * domain software4java.com expired (was: united media)
 * guigarage has several blogs (and started on aeroFX in mid-2014)
 * 
 * DECISION: don't go for a complete solution, just as far as to understand
 * since win7 the active area in win trees spans
 * (and highlights) the whole width of the tree, just as fx default! 
 * 
 * Question ask on SO:
 * http://stackoverflow.com/q/28113294/203657
 * 
 * main problem was a silly mistake: mixed up the style key <code>-fx-background-color</code>
 * with a global var for its value <code>-fx-background</code>
 * 
 * So:
 * - for showing the background, need to set the style key to a value, 
 *   either hard-coded or var
 * - for auto-adjust of text-fill, need to set the global var (because the 
 *   text-fill is ladder that's based on -fx-background)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeCellExample extends Application {

    public static class MyTreeCell extends TreeCell<String> {
       
        private Label label;

        public MyTreeCell() {
            getStyleClass().add("tree-text-only");
        }
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (label == null) {
                    label = new Label();
                }
                label.setText(item);
                setGraphic(label);
            }
        }
    }
    
    private Parent getContent() {
        TreeItem root = createSubTree("root");
        root.setExpanded(true);
        TreeView tree = new TreeView(root);
        tree.getStylesheets().add(
                getClass().getResource("treetextonlyso.css").toExternalForm());
        tree.setCellFactory(p -> new MyTreeCell());

        BorderPane pane = new BorderPane(tree);
        return pane;
    }

    ObservableList rawItems = FXCollections.observableArrayList(
            "9-item", "8-item", "7-item", "6-item", 
            "5-item", "4-item", "3-item", "2-item", "1-item");
    
    protected TreeItem createSubTree(Object value) {
        TreeItem child = new TreeItem(value);
        child.getChildren().setAll((List<TreeItem>) rawItems.stream()
                .map(TreeItem::new)
                .collect(Collectors.toList()));
        return child;
    }

    protected TreeItem createItem(Object value) {
        return new TreeItem(value);
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
