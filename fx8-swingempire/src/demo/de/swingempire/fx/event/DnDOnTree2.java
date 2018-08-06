/*
 * Created on 06.08.2018
 *
 */
package de.swingempire.fx.event;

import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import de.swingempire.fx.event.DnDOnTree.PlanningItem;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * follow-up qa for dragging treeItem:
 * https://stackoverflow.com/q/51706628/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public final class DnDOnTree2 extends Application {

    private final DataFormat objectDataFormat = new DataFormat("application/x-java-serialized-object");

    /**
     * Constructor.
     */
    public DnDOnTree2() {
        // empty.
    }

    /**
     * @param args Program arguments.
     */
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {

        Label rootGraphic = new Label("ROOT");
        TreeItem<PlanningItem> treeItemRoot = new TreeItem<>(new PlanningItem(1.0), rootGraphic);

        TreeItem<PlanningItem> nodeItemA = new TreeItem<>(new PlanningItem(2), new Label("A"));
        TreeItem<PlanningItem> nodeItemB = new TreeItem<>(new PlanningItem(2), new Label("B"));
        treeItemRoot.getChildren().add(nodeItemA);
        treeItemRoot.getChildren().add(nodeItemB);

        TreeItem<PlanningItem> nodeItemA1 = new TreeItem<>(new PlanningItem("A1"), new Label("A1"));
        TreeItem<PlanningItem> nodeItemB1 = new TreeItem<>(new PlanningItem("B1"), new Label("B1"));
        nodeItemA.getChildren().add(nodeItemA1);
        nodeItemB.getChildren().add(nodeItemB1);

        TreeView<PlanningItem> treeView = new TreeView<>(treeItemRoot);

        treeView.setCellFactory(new Callback<TreeView<PlanningItem>, TreeCell<PlanningItem>>() {
            @Override
            public TreeCell call(TreeView<PlanningItem> siTreeView) {
                final PlanningCheckBoxTreeCell cell = new PlanningCheckBoxTreeCell();
//                final TreeCell cell = new DefaultTreeCell<>();
                cell.setOnDragDetected(new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent event) {
                        System.out.println("onDragDetected");

                        Dragboard db = treeView.startDragAndDrop(TransferMode.ANY);
                        TreeItem<PlanningItem> item = treeView.getSelectionModel().getSelectedItem();

                        ClipboardContent content = new ClipboardContent();
                        content.put(objectDataFormat, item.getValue());

                        // content has to be defined before it is set in the Dragboard.
                        db.setContent(content);

                        event.consume();
                    }
                });

                cell.setOnDragOver(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        System.out.println("onDragOver");

                        if (event.getGestureSource() == treeView) {

                            if (event.getDragboard().hasContent(objectDataFormat)) {
                                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                            }

                        } else {
                            event.acceptTransferModes(TransferMode.NONE);
                        }

                        event.consume();
                    }
                });

                cell.setOnDragEntered(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* the drag-and-drop gesture entered the target */
                        System.out.println("onDragEntered");
                        /* show to the user that it is an actual gesture target */
                        if (event.getGestureSource() != cell
                                && event.getDragboard().hasContent(objectDataFormat)) {
                            cell.setTextFill(Color.GREEN);
                        }

                        event.consume();
                    }
                });

                cell.setOnDragExited(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        System.out.println("onDragExited");

                        /* mouse moved away, remove the graphical cues */
                        cell.setTextFill(Color.BLACK);

                        event.consume();
                    }
                });

                cell.setOnDragDropped(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        System.out.println("onDragDropped");

                        Dragboard db = event.getDragboard();
                        boolean success = false;

                        PlanningItem source = (PlanningItem) db.getContent(objectDataFormat);

                        @SuppressWarnings("unchecked")
                        TreeItem<PlanningItem> sourceTreeItem = ((TreeView<PlanningItem>) event
                                .getGestureSource()).getSelectionModel().getSelectedItem();

                        TreeItem<PlanningItem> parent = sourceTreeItem.getParent();
                        cell.getTreeItem().getChildren().add(sourceTreeItem);
                        parent.getChildren().remove(sourceTreeItem);

                        event.setDropCompleted(success);

                        // controller.tvProject.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                        // controller.tvProject.refresh();

                        System.out.println("Drop Completed.");

                        event.consume();
                    }
                });

                cell.setOnDragDone(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* the drag-and-drop gesture ended */
                        System.out.println("onDragDone");
                        /* if the data was successfully moved, clear it */
                        if (event.getTransferMode() == TransferMode.MOVE) {
                            cell.setText("");
                        }

                        event.consume();
                    }
                });

                return cell;
            };
        });

        StackPane root = new StackPane();
        root.getChildren().add(treeView);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Test TreeView");
        primaryStage.show();
    }
    
    public static class HackedCheckBoxTreeCell<T> extends CheckBoxTreeCell<T> {
        
        CheckBox checkBoxAlias;
        public HackedCheckBoxTreeCell() {
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
    
    public class PlanningCheckBoxTreeCell extends 
//        TreeCell<PlanningItem> {
     HackedCheckBoxTreeCell<PlanningItem> {
        public PlanningCheckBoxTreeCell() {
            StringConverter<TreeItem<PlanningItem>> c = new StringConverter<>() {

                @Override
                public String toString(TreeItem<PlanningItem> item) {
                    PlanningItem value = item.getValue();
                    return value != null ? 
                            value.getScene() + value.getMove() + value.getPath() : "empty";
                }

                @Override
                public TreeItem<PlanningItem> fromString(String string) {
                    return null;
                }

                 
            };
            setConverter(c);
            
        }

   }
    
    class DefaultTreeCell<T> extends TreeCell<T> {

        private HBox hbox;

        private WeakReference<TreeItem<T>> treeItemRef;

        private InvalidationListener treeItemGraphicListener = observable -> {
            updateDisplay(getItem(), isEmpty());
        };

        private InvalidationListener treeItemListener = new InvalidationListener() {
            @Override public void invalidated(Observable observable) {
                TreeItem<T> oldTreeItem = treeItemRef == null ? null : treeItemRef.get();
                if (oldTreeItem != null) {
                    oldTreeItem.graphicProperty().removeListener(weakTreeItemGraphicListener);
                }

                TreeItem<T> newTreeItem = getTreeItem();
                if (newTreeItem != null) {
                    newTreeItem.graphicProperty().addListener(weakTreeItemGraphicListener);
                    treeItemRef = new WeakReference<TreeItem<T>>(newTreeItem);
                }
            }
        };

        private WeakInvalidationListener weakTreeItemGraphicListener =
                new WeakInvalidationListener(treeItemGraphicListener);

        private WeakInvalidationListener weakTreeItemListener =
                new WeakInvalidationListener(treeItemListener);

        public DefaultTreeCell() {
            treeItemProperty().addListener(weakTreeItemListener);

            if (getTreeItem() != null) {
                getTreeItem().graphicProperty().addListener(weakTreeItemGraphicListener);
            }
        }

        void updateDisplay(T item, boolean empty) {
            if (item == null || empty) {
                hbox = null;
                setText(null);
                setGraphic(null);
            } else {
                // update the graphic if one is set in the TreeItem
                TreeItem<T> treeItem = getTreeItem();
                if (treeItem != null && treeItem.getGraphic() != null) {
                    if (item instanceof Node) {
                        setText(null);

                        // the item is a Node, and the graphic exists, so
                        // we must insert both into an HBox and present that
                        // to the user (see RT-15910)
                        if (hbox == null) {
                            hbox = new HBox(3);
                        }
                        hbox.getChildren().setAll(treeItem.getGraphic(), (Node)item);
                        setGraphic(hbox);
                    } else {
                        hbox = null;
                        setText(item.toString());
                        setGraphic(treeItem.getGraphic());
                    }
                } else {
                    hbox = null;
                    if (item instanceof Node) {
                        setText(null);
                        setGraphic((Node)item);
                    } else {
                        setText(item.toString());
                        setGraphic(null);
                    }
                }
            }
        }

        @Override public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            updateDisplay(item, empty);
        }
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DnDOnTree2.class.getName());
}

