/*
 * Created on 27.04.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * Impossible to reliably start editing after
 * inserting an item.
 * 
 * http://stackoverflow.com/q/29863095/203657
 * 
 * <p>
 * Reason: treeCells are updated lazily on a layout pulse. So the
 * cell for a newly added treeItem is not-yet bound/updated/created  
 * before the pulse. 
 * <p>
 * Hack: manually call tree.layout before tree.edit (tree.refresh has
 * no effect, jumps in on next layout)
 * <p>
 * Note: neither Plaform.runlater nor adding a hard-coded delay
 * work reliably.
 */

public class TreeEdit extends Application {

    private TreeView<String> tree;

    private Parent getContent() {
        initializeTree();
        Button button = new Button("create item");
        button.setOnAction(e -> {
            createItem();
        });
        
        tree.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F1) {
                e.consume();
                createItem();
            }
        });
        VBox box = new VBox(tree, button );
        return box;
    }
    public void initializeTree() {
        tree = new TreeView<>();
        tree.setEditable(true);
        tree.setCellFactory(p -> new EditableTreeCell<>(new DefaultStringConverter()));

        TreeItem<String> root = new TreeItem<>();
        root.setValue("Items");
        root.setExpanded(true);

        tree.setRoot(root);
    }

    // just for debugging, to find the 
    public static class EditableTreeCell<T> extends TextFieldTreeCell<T> {
        
        public EditableTreeCell(StringConverter converter) {
            super(converter);
        }

        @Override
        public void updateIndex(int i) {
            super.updateIndex(i);
            LOG.info("  " + i + " / " +getTreeItem());
        }
        
        
    }

    public void createItem() {
        TreeItem<String>
            newItem = new TreeItem<>();
        int row = tree.getExpandedItemCount();
        newItem.setValue("Item " + row);
        tree.requestFocus();
        tree.getRoot().getChildren().add(newItem);
        tree.getSelectionModel().select(row);
        tree.layout();
        // no effect
        tree.scrollTo(row);
        // doesn't work
//        tree.refresh();
        tree.edit(newItem);
        // not enough time
//        Platform.runLater(() -> {
//        });
        // hard-coded delay
        // still unreliable and horrible user-experiencs 
//        PauseTransition p = new PauseTransition( Duration.millis( 10 ) );
//        p.setOnFinished( new EventHandler<ActionEvent>()
//        {
//            @Override
//            public void handle( ActionEvent event )
//            {
//                tree.edit( newItem );
//            }
//        } );
//        p.play();

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 400, 100));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
 
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeEdit.class.getName());
}

