/*
 * Created on 06.08.2018
 *
 */
package de.swingempire.fx.event;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Similar, very old bug:
 * https://bugs.openjdk.java.net/browse/JDK-8123633
 * graphic disappears on selecting
 * 
 * as is, it was a user-error: same imageView added - but here we add a different
 * instance of graphic
 * 
 * has reference to a bug around dirty regions which is still open
 * https://bugs.openjdk.java.net/browse/JDK-8091852
 * 
 * has reference to bug that's noted as fixed
 * https://bugs.openjdk.java.net/browse/JDK-8120095
 * checkbox requires two clicks to select
 * 
 * reported new bug:
 * https://bugs.openjdk.java.net/browse/JDK-8209017
 * 
 * @author Jeanette Winzenburg, Berlin
 * 
 * @see GraphicInTreeTableItem
 */
public class CheckBoxTreeCellWithGraphic extends Application {

    @SuppressWarnings("unchecked")
    private Parent createContent() {
        
        TreeItem<String> root = new TreeItem<>("ROOT", new Label("really"));
        root.getChildren().add(new TreeItem<>("firstChild"));
        root.getChildren().add(new TreeItem<>("secondChild"));
        
        root.getChildren().forEach(child -> {
            child.getChildren().addAll(
                    new TreeItem<>(" of " + child.getValue(), new Label("grand1 "))
                    ,new TreeItem<>(" of " + child.getValue(), new Label("grand2"))
                    );
            child.setGraphic(new Button(child.getValue()));
        });
        
        TreeView<String> tree = new TreeView<>(root);
        // core checkBoxTreeCell
        tree.setCellFactory(CheckBoxTreeCell.forTreeView());
        // hacked checkBoxTreeCell
//        tree.setCellFactory(c -> new WCheckBoxTreeCell<>());
        
        return new BorderPane(tree);
    }

    public static class WCheckBoxTreeCell<T> extends CheckBoxTreeCell<T> {
        
        CheckBox checkBoxAlias;
        public WCheckBoxTreeCell() {
            InvalidationListener checkBoxGrabber = new InvalidationListener() {
                
                @Override
                public void invalidated(Observable observable) {
                    if (checkBoxAlias == null) {
                        if (getGraphic() instanceof CheckBox) {
                            checkBoxAlias = (CheckBox) getGraphic();
                            graphicProperty().removeListener(this);
                        }
                    }
                }
                
            };
            graphicProperty().addListener(checkBoxGrabber);
        }
        
        @Override
        public void updateItem(T item, boolean empty) {
            if (checkBoxAlias !=  null) {
                checkBoxAlias.setGraphic(null);
            }
            super.updateItem(item, empty);
       }
 
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
            .getLogger(CheckBoxTreeCellWithGraphic.class.getName());


    public static class CheckAndGraphicTreeCell<T> extends TreeCell<T> {
        private CheckBox checkBox;

        public CheckAndGraphicTreeCell() {
            this.checkBox = new CheckBox();
            this.checkBox.setAllowIndeterminate(false);

            // by default the graphic is null until the cell stops being empty
            setGraphic(null);
            
//            treeItemProperty().addListener(o -> checkBox.setGraphic(null));
        }

        String itemAsString(T item) {
            return item != null ? item.toString() : "";
        }
        
        
        @Override
        public void updateIndex(int i) {
//            checkBox.setGraphic(null);
            super.updateIndex(i);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            checkBox.setGraphic(null);
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                TreeItem<T> treeItem = getTreeItem();
                
                String text = itemAsString(item);
                // update the node content
//                setText(c != null ? c.toString(treeItem) : (treeItem == null ? "" : treeItem.toString()));
                setText(text);
                checkBox.setGraphic(treeItem == null ? null : treeItem.getGraphic());
                setGraphic(checkBox);

//                // uninstall bindings
//                if (booleanProperty != null) {
//                    checkBox.selectedProperty().unbindBidirectional((BooleanProperty)booleanProperty);
//                }
//                if (indeterminateProperty != null) {
//                    checkBox.indeterminateProperty().unbindBidirectional(indeterminateProperty);
//                }
//
//                // install new bindings.
//                // We special case things when the TreeItem is a CheckBoxTreeItem
//                if (treeItem instanceof CheckBoxTreeItem) {
//                    CheckBoxTreeItem<T> cbti = (CheckBoxTreeItem<T>) treeItem;
//                    booleanProperty = cbti.selectedProperty();
//                    checkBox.selectedProperty().bindBidirectional((BooleanProperty)booleanProperty);
//
//                    indeterminateProperty = cbti.indeterminateProperty();
//                    checkBox.indeterminateProperty().bindBidirectional(indeterminateProperty);
//                } else {
//                    Callback<TreeItem<T>, ObservableValue<Boolean>> callback = getSelectedStateCallback();
//                    if (callback == null) {
//                        throw new NullPointerException(
//                                "The CheckBoxTreeCell selectedStateCallbackProperty can not be null");
//                    }
//
//                    booleanProperty = callback.call(treeItem);
//                    if (booleanProperty != null) {
//                        checkBox.selectedProperty().bindBidirectional((BooleanProperty)booleanProperty);
//                    }
//                }
            }
        }
        
        
    }

}
