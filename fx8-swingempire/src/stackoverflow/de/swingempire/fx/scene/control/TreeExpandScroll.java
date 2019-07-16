/*
 * Created on 15.07.2019
 *
 */
package de.swingempire.fx.scene.control;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * TreeView scrolling unpredictable when expanding/collapsing a node.
 * https://stackoverflow.com/q/57025089/203657
 * <p>
 * Looks like a bug. Suspect TreeViewSkin, but it does nothing but
 * registering a treeModification listener on the root, call markItemCountDirty 
 * which forces a updateItemCount (implemented to request a flow.rebuildCells
 * on the next layout pass).
 * <p>
 * Trying to dig
 * <ul>
 * <li> the basic idea: take over the positioning, add the notion of "currentIndexAtTop"
 * <li> we can listen to TreeModificationEvents of type expand/collapse
 * <li> on receiving the event, the internal state of the flow is not yet updated
 * <li> trying to defer the check until it will be ready: difficult, because that's done
 *   lazily during a layout pass
 * <li> runlater is not good enough, nesting doesn't help
 * <li> listen to changes of the scrollBar: at that time the cells seem to be stable
 * <li> but: when doing a scrollTo during (even delayed) expand notification, the flow's
 *    first cell is that before the scrolledTo - it's partially visible, ever so slightly
 *    scrollTo independently, scrolls to the exact cell, that is first == scrolledTo
 *    but not reliably ...
 * </ul>
 * 
 * Summary: VirtualFlow needs to be made aware of additional scrolling constraints. Not
 * perfect, because we need to wait for super to do its basic layout stuff which might
 * result in too much scrolling before we hook into the process. 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeExpandScroll extends Application {

    public static class MVirtualFlow<I extends IndexedCell> extends VirtualFlow<I> {
        private int lastExpanded = -1;
        private int childCount;
        
        public void setLastExpanded(int last, int childCount) {
            this.lastExpanded = last;
            this.childCount = childCount;
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            adjustToLastExpanded();
        }

        /**
         * super's state change is not predictable ... interfering here?
         * must take care of partially visible rows!
         */
        private void adjustToLastExpanded() {
            if (lastExpanded < 0) return;
            int targetIndex = lastExpanded;
            lastExpanded = -1;
            I firstCell = getFirstVisibleCell();
            int firstIndex = getCellIndex(firstCell);
            I lastCell = getLastVisibleCell();
            int lastIndex = getCellIndex(lastCell);
            LOG.info("row " + targetIndex + "/ first " + firstIndex + firstCell.getText() + 
                    " / last " + lastIndex + lastCell.getText());
            
            int visibleCells = lastIndex - firstIndex + 1;
            int lastChild = targetIndex + childCount + 1;
            LOG.info("visible: " + visibleCells + "/ target " + targetIndex + " / lastChild " + lastChild);
            String message = "what? ";
            if (targetIndex < firstIndex) {
                message += "target before " + targetIndex + " / " + firstIndex;
                scrollToTop(targetIndex);
            } else if (targetIndex >= firstIndex && targetIndex <= lastIndex) {
                 if (lastChild <= lastIndex) {
                     message += " child visible "  + lastChild + " / lastIndex " + lastIndex;
                    //  do nothing, all visible without scrolling
                } else { // child beyond last
                    if (childCount >= visibleCells) {
                        // don't fit all, scroll to top
                        message += "too many " + childCount + " / visible " + visibleCells;
                        scrollToTop(targetIndex);
                    } else {
                        // missing scrollToBottom(index), need to tweak
                        // scrollToBottom(lastChild)
                      // naive approach: doesn't work, cell not yet completely live ..  
//                    I targetCell = getCell(lastIndex);
//                    scrollToBottom(targetCell);
                        int deltaChild = lastChild - lastIndex;
                        int targetFirst = firstIndex + deltaChild;
                        scrollToTop(targetFirst);
                        message += " up to " + targetFirst;
                    }    
                }
            }
//            scrollToTop(targetIndex);
            LOG.info(message);
            
//            LOG.info("row " + targetIndex + "/ index" + firstCell.getIndex() + firstCell.getTreeItem());
        }
        
        
    }
    public static class MTreeViewSkin<T> extends TreeViewSkin<T> {

        /**
         * @param control
         */
        public MTreeViewSkin(TreeView <T>control) {
            super(control);
            installListeners();
        }

        /**
         * 
         */
        private void installListeners() {
            TreeView<T> tree = getSkinnable();
            tree.getRoot().addEventHandler(TreeItem.<T>treeNotificationEvent(), e -> {
                if (e.wasExpanded()) {
                    TreeItem<T> expandedItem = e.getSource();
                    // must do recursively or use custom treeItem that exposes the expandedDescendentCount!
                    int childCount = expandedItem.getChildren().size();
                    ((MVirtualFlow<TreeCell<T>>) getVirtualFlow()).setLastExpanded(tree.getRow(e.getSource()), childCount);
                }
            });
        }

        @Override
        protected MVirtualFlow<TreeCell<T>> createVirtualFlow() {
            return new MVirtualFlow<>();
        }
        
        
    }
    
    VirtualFlow flow;
    ScrollBar vbar;
    /**
     * @param tree
     */
    private void installListener(TreeView<String> tree) {
        tree.getRoot().addEventHandler(TreeItem.<String>treeNotificationEvent(), e -> {
            if (flow == null) {
                flow = (VirtualFlow) tree.lookup("VirtualFlow");
                vbar = (ScrollBar) tree.lookup(".scroll-bar");
                vbar.valueProperty().addListener((src, ov, nv) -> {
                    // returns the first at least partially visible cell
                    TreeCell cell = (TreeCell) flow.getFirstVisibleCell();
                    int index = -1;
                    if (cell != null) {
                        index = cell.getIndex();
                    }
                    // index of cell unexpected ...probably not yet updated?
                    LOG.info("scroll " + nv + "/index" + index + cell.getTreeItem() + cell.getBoundsInParent());
                   
                });
            }
            if (e.wasExpanded()) {
                LOG.info("" + e.getEventType() + e.getSource());
                Platform.runLater(() -> {
                    int row = tree.getRow(e.getSource());
                    TreeCell cell = (TreeCell) flow.getFirstVisibleCell();
                    int index = -1;
                    if (cell != null) {
                        index = cell.getIndex();
                    }
                    // at this time, the index of the first is not yet updated
                    LOG.info("row " + row + "/index" + index + cell.getTreeItem());
//                    if (index < row)
                    // results in scrolling, but the cell immediately before is partially 
                    // visible -> that is index != row, even if scrolling were complete
                        tree.scrollTo(row);
                    // nested runlater doesn't help much
                    Platform.runLater(() -> {
                        
                        // not good enough: the row is not yet updated
                        int rowAfter = tree.getRow(e.getSource());
                        TreeCell cellAfter = (TreeCell) flow.getFirstVisibleCell();
                        LOG.info("rowAfter " + rowAfter + "/index" + cellAfter.getIndex() + cellAfter.getTreeItem());
                    });
                    
                });
            }
        });
        
        tree.setOnScrollTo(e -> {
            LOG.info("" + e);
        });
    }

    private Parent createContent() {
        TreeItem<String> root = createSubTree("root");
        root.setExpanded(true);
        root.getChildren().stream().forEach(item -> createChildren(item, "x"));
        TreeView<String> tree = new TreeView<>(root) {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new MTreeViewSkin<>(this);
            }
            
        };
//        installListener(tree);
        
        Button scrollTo = new Button("scrollTo selected");
        scrollTo.setOnAction(e -> {
            int selected = tree.getSelectionModel().getSelectedIndex();
            if (selected > -1) {
                tree.scrollTo(selected);
            }
        });
        BorderPane content = new BorderPane(tree);
        content.setBottom(new HBox(10, scrollTo));
        return content;
    }

    ObservableList<String> rawItems = FXCollections.observableArrayList(
            "1-item", "2-item", "3-item", "4-item", 
            "5-item", "6-item", "7-item", "8-item", "9-item");
    
    protected TreeItem<String> createSubTree(String value) {
        TreeItem<String> child = new TreeItem<>(value);
        return createChildren(child, "");
    }

    /**
     * @param item
     * @return
     */
    protected TreeItem<String> createChildren(TreeItem<String> item, String pref) {
        item.getChildren().setAll((List<TreeItem<String>>) rawItems.stream()
                .map(s -> pref + s)
                .map(TreeItem::new)
                .collect(Collectors.toList()));
        return item;
    }

    protected TreeItem<String> createItem(String value) {
        return new TreeItem<>(value);
    }


    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 400, 300));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeExpandScroll.class.getName());

}
