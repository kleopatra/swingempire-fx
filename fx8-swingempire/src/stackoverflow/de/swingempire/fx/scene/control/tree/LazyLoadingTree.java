/*
 * Created on 17.09.2018
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Performance problem with lazy loading.
 * https://stackoverflow.com/q/52360195/203657
 * 
 * really weird: 
 * -when clicking the disclosure node, all is fine
 * - when double-clicking the cell there's the lag
 * 
 * seems to happen when expanding item is selected
 * hangs in TreeViewFocusModel: on receiving a treeModification of type added,
 *   it queries the row for each of the added items
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class LazyLoadingTree extends Application {

    private TreeView<Item> treeView = new TreeView<>();

    public static class ItemLoader implements Runnable {
        private static ItemLoader instance;
        private List<Task> queue = new ArrayList<>();
        private Task prevTask = null;

        private ItemLoader() {
            Thread runner = new Thread(this);
            runner.setName("ItemLoader thread");
            runner.setDaemon(true);
            runner.start();
        }

        public static ItemLoader getSingleton() {
            if (instance == null) {
                instance = new ItemLoader();
            }
            return instance;
        }

        public <T> void load(Task task) {
            if (queue.size() < 1) {
                queue.add(task);
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!queue.isEmpty()) {
//                    LOG.info("tasks: " + queue.size());
                    Task task = queue.get(0);
                    if (task != prevTask) {
                        prevTask = task;
                        task.run();
                        queue.remove(task);
                    }
                }
            }
        }
    }


    public static class LazyTreeItem extends TreeItem<Item> {
        private boolean childrenLoaded = false;
        private boolean isLoadingItems = false;

        public LazyTreeItem(Item value) {
            super(value);
            // Unload data on folding to reduce memory
            expandedProperty().addListener((observable, oldValue, newValue) -> {
//                LOG.info("getting expanded: " + newValue); 
//                        + " isLeaf: " + isLeaf() + " leafProp: " + leafProperty().get());
                if (!newValue) {
                    flush();
                }
            });
            
            leafProperty().addListener((src, ov, nv) -> {
//                LOG.info("getting leafProp: " + nv );
            });
            
            super.getChildren().addListener((ListChangeListener) c -> {
                LOG.info("change: " + c.getList().size() + Platform.isFxApplicationThread());
            });
        }

        @Override
        public ObservableList<TreeItem<Item>> getChildren() {
            if (childrenLoaded || !isExpanded()) {
                return super.getChildren();
            }
            if (super.getChildren().size() == 0) {
                // Filler node (will translate into loading icon in the
                // TreeCell factory)
                super.getChildren().add(new TreeItem<>(new SingleItem("loading ...")));
            }
            if (getValue() instanceof MultipleItem) {
                if (!isLoadingItems) {
//                    LOG.info("size: " + super.getChildren().size());
                    loadItems();
                }
            }
            return super.getChildren();
        }

        public void loadItems() {
            isLoadingItems = true;
            Task<List<TreeItem<Item>>> task = new Task<List<TreeItem<Item>>>() {
                @Override
                protected List<TreeItem<Item>> call() {
//                    LOG.info("in loading");
                    List<SingleItem> downloadSet = ((MultipleItem) LazyTreeItem.this.getValue()).getEntries();
                    List<TreeItem<Item>> treeNodes = new ArrayList<>();
                    for (SingleItem download : downloadSet) {
                        treeNodes.add(new TreeItem<>(download));
                    }
                    return treeNodes;
                }

//                @Override
//                protected void succeeded() {
//                    childrenLoaded = true;
//                    isLoadingItems = false;
//                        TreeItem.super.getChildren().clear();
//                        super.getChildren().addAll(task.getValue());
//                        Platform.runLater(() -> {
//                    });
//                }
                
            };
            task.valueProperty().addListener((src, ov, lv) -> {
//                LOG.info("in valueListener: " + lv.size() + super.getChildren());
                childrenLoaded = true;
                isLoadingItems = false;
//                super.getChildren().clear();
                super.getChildren().setAll(lv);
                Platform.runLater(() -> {
                });
                
            });
//            task.setOnSucceeded(e -> {
//                LOG.info("" + Platform.isFxApplicationThread());
//                childrenLoaded = true;
//                isLoadingItems = false;
//                    super.getChildren().clear();
//                    super.getChildren().addAll(task.getValue());
//                    Platform.runLater(() -> {
//                });
//            });
            ItemLoader.getSingleton().load(task);
        }

        private void flush() {
            childrenLoaded = false;
            super.getChildren().clear();
        }

        int callCount;
        @Override
        public boolean isLeaf() {
//            return leafProperty().get();
            if (childrenLoaded) {
//                LOG.info("calling isLeaf " + callCount++);
//                return true;
                return getChildren().isEmpty();
            }
            return false;
        }
    }


    private void initTreeView() {
//        treeView.setCellFactory(e -> createDefaultCellImpl());
//        treeView.setFocusModel(new MyTreeViewFocusModel<>(treeView));
        treeView.setShowRoot(false);
        treeView.setRoot(new TreeItem<>(null));
        TreeItem<Item> parentItem = new TreeItem<>(new Item());
        
        parentItem.setExpanded(true);
        
        List<SingleItem> items = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            items.add(new SingleItem(String.valueOf(i)));
        }
        // initialize MultipleItem with n entries and add as collapsed LazyTreeItem
        LazyTreeItem lazyItem = new LazyTreeItem(new MultipleItem(items));
        lazyItem.addEventHandler(TreeItem.treeNotificationEvent(), e -> {
//            LOG.info("tree event: " + e);
        });
        parentItem.getChildren().addAll(lazyItem, new LazyTreeItem(new MultipleItem(items)));
        
        treeView.getSelectionModel().selectedItemProperty().addListener((src, ov, nv) -> {
            LOG.info("selected: " + nv);
        });
        
        treeView.getFocusModel().focusedItemProperty().addListener((src, ov, nv) -> {
            LOG.info("focused: " + nv);
        });

        treeView.getRoot().getChildren().add(parentItem);
    }

    // c&p from TreeViewSkin to inject custom behavior
    private <T> TreeCell<T> createDefaultCellImpl() {
        return new TreeCell<T>() {
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

            {
                treeItemProperty().addListener(weakTreeItemListener);

                if (getTreeItem() != null) {
                    getTreeItem().graphicProperty().addListener(weakTreeItemGraphicListener);
                }
            }

            private void updateDisplay(T item, boolean empty) {
                if (item == null || empty) {
                    hbox = null;
                    setText(null);
                    setGraphic(null);
                } else {
                    // update the graphic if one is set in the TreeItem
                    TreeItem<T> treeItem = getTreeItem();
                    Node graphic = treeItem == null ? null : treeItem.getGraphic();
                    if (graphic != null) {
                        if (item instanceof Node) {
                            setText(null);

                            // the item is a Node, and the graphic exists, so
                            // we must insert both into an HBox and present that
                            // to the user (see RT-15910)
                            if (hbox == null) {
                                hbox = new HBox(3);
                            }
                            hbox.getChildren().setAll(graphic, (Node)item);
                            setGraphic(hbox);
                        } else {
                            hbox = null;
                            setText(item.toString());
                            setGraphic(graphic);
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

            @Override
            protected Skin<?> createDefaultSkin() {
                return new MyTreeCellSkin<>(this);
            }
            
            
        };
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("TreeView Lazy Load");
        primaryStage.setScene(new Scene(new StackPane(treeView), 300, 500));
        initTreeView();
        primaryStage.show();
        
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public static class Item {

        @Override
        public String toString() {
            return "blank item";
        }

        
    }
    /****************************************************************
     **********                  SingleItem              ************
     ****************************************************************/
    public static class SingleItem extends Item {
        private String id;

        public SingleItem(String id) {
            this.id = id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id;
        }
        
        
    }
    /****************************************************************
     **********                  MultipleItem            ************
     ****************************************************************/
    public static class MultipleItem extends Item {

        private List<SingleItem> entries = new ArrayList<>();

        public MultipleItem(List<SingleItem> entries) {
            this.entries = entries;
        }

        public List<SingleItem> getEntries() {
            return entries;
        }

        public void setEntries(List<SingleItem> entries) {
            this.entries = entries;
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return "multiple: " + getEntries().size();
        }
        
        
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(LazyLoadingTree.class.getName());
}

