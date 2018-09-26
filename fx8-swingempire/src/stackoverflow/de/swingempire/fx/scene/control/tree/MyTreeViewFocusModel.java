/*
 * Created on 18.09.2018
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.FocusModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.TreeView;

public class MyTreeViewFocusModel<T> extends FocusModel<TreeItem<T>> {

        private final TreeView<T> treeView;

        public MyTreeViewFocusModel(final TreeView<T> treeView) {
            this.treeView = treeView;
            this.treeView.rootProperty().addListener(weakRootPropertyListener);
            updateTreeEventListener(null, treeView.getRoot());

            if (treeView.getExpandedItemCount() > 0) {
                focus(0);
            }

            treeView.showRootProperty().addListener(o -> {
                if (isFocused(0)) {
                    focus(-1);
                    focus(0);
                }
            });

            focusedIndexProperty().addListener(o -> {
                treeView.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_ITEM);
            });
        }

        private final ChangeListener<TreeItem<T>> rootPropertyListener = (observable, oldValue, newValue) -> {
            updateTreeEventListener(oldValue, newValue);
        };

        private final WeakChangeListener<TreeItem<T>> weakRootPropertyListener =
                new WeakChangeListener<>(rootPropertyListener);

        private void updateTreeEventListener(TreeItem<T> oldRoot, TreeItem<T> newRoot) {
            if (oldRoot != null && weakTreeItemListener != null) {
                oldRoot.removeEventHandler(TreeItem.<T>expandedItemCountChangeEvent(), weakTreeItemListener);
            }

            if (newRoot != null) {
                weakTreeItemListener = new WeakEventHandler<>(treeItemListener);
                newRoot.addEventHandler(TreeItem.<T>expandedItemCountChangeEvent(), weakTreeItemListener);
            }
        }

        private EventHandler<TreeModificationEvent<T>> treeItemListener = new EventHandler<TreeModificationEvent<T>>() {
            @Override public void handle(TreeModificationEvent<T> e) {
                // don't shift focus if the event occurred on a tree item after
                // the focused row, or if there is no focus index at present
                if (getFocusedIndex() == -1) return;

                int row = treeView.getRow(e.getTreeItem());

                int shift = 0;
                Change change = invokeGetChange(e);
                if (change != null) { //(e.getChange() != null) {
//                    e.getChange().next();
                    change.next();
                }

                do {
                    if (e.wasExpanded()) {
                        if (row < getFocusedIndex()) {
                            // need to shuffle selection by the number of visible children
//                            shift += e.getTreeItem().getExpandedDescendentCount(false) - 1;
                            shift += invokeGetTreeItemExpandedDescendentCount(e.getTreeItem(), false) - 1;
                        }
                    } else if (e.wasCollapsed()) {
                        if (row < getFocusedIndex()) {
                            // need to shuffle selection by the number of visible children
                            // that were just hidden
//                            shift += -e.getTreeItem().previousExpandedDescendentCount + 1;
                            shift += invokeTreeItemPreviousExpandedDescendentCount(e.getTreeItem()) +1;
                        }
                    } else if (e.wasAdded()) {
                        // get the TreeItem the event occurred on - we only need to
                        // shift if the tree item is expanded
                        TreeItem<T> eventTreeItem = e.getTreeItem();
                        if (eventTreeItem.isExpanded()) {
                            // the added bunch of a sub-change is a range -> all are either before
                            // or after the focus, nothing to do for the latter
                            // for the former:
                            if (row < getFocusedIndex()) {
                                // no need for a null check, in cases of children modifications the change
                                // must not be null!
                                if (!change.wasAdded()) throw new IllegalStateException("expected added change but was: " + change);
                                TreeItem focusedItem = getFocusedItem();
//                                int delta = getFocusedIndex() - row;
//                                int last = change.getAddedSize()-1;
//                                TreeItem lastAddedItem = (TreeItem) change.getAddedSubList().get(last);
//                                row = treeView.getRow(lastAddedItem);
//                                shift = row + delta;
                                // original code: looping is performance bottleneck
                                // it's wrong to query the event's addedChildren, instead query the change's
//                                for (int i = 0; i < e.getAddedChildren().size(); i++) {
//                                    // get the added item and determine the row it is in
//                                    TreeItem<T> item = e.getAddedChildren().get(i);
//                                    row = treeView.getRow(item);
//    
//                                    if (item != null && row <= (shift+getFocusedIndex())) {
//    //                                    shift += item.getExpandedDescendentCount(false);
//                                        shift += invokeGetTreeItemExpandedDescendentCount(item, false);
//                                    }
//                                }
                                int index = treeView.getRow(focusedItem);
                                LOG.info("focusedIndex? " + index + " focusedItem " + focusedItem);
                                focus(index);
                            }   
                        }
                    } else if (e.wasRemoved()) {
//                        row += e.getFrom() + 1;

                        row += invokeGetFrom(e);
                        for (int i = 0; i < e.getRemovedChildren().size(); i++) {
                            TreeItem<T> item = e.getRemovedChildren().get(i);
                            if (item != null && item.equals(getFocusedItem())) {
                                focus(Math.max(0, getFocusedIndex() - 1));
                                return;
                            }
                        }

                        if (row <= getFocusedIndex()) {
                            // shuffle selection by the number of removed items
                            shift += e.getTreeItem().isExpanded() ? -e.getRemovedSize() : 0;
                        }
                    }
                } while (change != null && change.next()) ;// e.getChange() != null && e.getChange().next());

                if(shift != 0) {
                    final int newFocus = getFocusedIndex() + shift;
                    if (newFocus >= 0) {
                        Platform.runLater(() -> focus(newFocus));
                    }
                }
            }
        };

        private WeakEventHandler<TreeModificationEvent<T>> weakTreeItemListener;

        @Override protected int getItemCount() {
            return treeView == null ? -1 : treeView.getExpandedItemCount();
        }

        @Override protected TreeItem<T> getModelItem(int index) {
            if (treeView == null) return null;

            if (index < 0 || index >= treeView.getExpandedItemCount()) return null;

            return treeView.getTreeItem(index);
        }

        /** {@inheritDoc} */
        @Override public void focus(int index) {
            if (invokeTreeExpandedItemCountDirty()) { //(treeView.expandedItemCountDirty) {
                
//                treeView.updateExpandedItemCount(treeView.getRoot());
                invokeTreeUpdateExpandedItemCount(treeView.getRoot());
            }

            super.focus(index);
        }
        
        protected boolean invokeTreeExpandedItemCountDirty() {
            return (boolean) FXUtils.invokeGetFieldValue(TreeView.class, treeView, "expandedItemCountDirty");
        }
        
        protected void invokeTreeUpdateExpandedItemCount(TreeItem<T> item) {
            FXUtils.invokeGetMethodValue(TreeView.class, treeView, "updateExpandedItemCount", TreeItem.class, item);
        }
        
        protected int invokeGetTreeItemExpandedDescendentCount(TreeItem<T> item, boolean reset) {
            return (int) FXUtils.invokeGetMethodValue(TreeItem.class, item, "getExpandedDescendentCount", Boolean.TYPE, reset);
        }
        
        protected int invokeTreeItemPreviousExpandedDescendentCount(TreeItem<T> item) {
            return (int) FXUtils.invokeGetFieldValue(TreeItem.class, item, "previousExpandedDescendentCount");
        }
        @SuppressWarnings("unchecked")
        protected Change<T> invokeGetChange(TreeModificationEvent<T> event) {
            return (Change<T>) FXUtils.invokeGetMethodValue(TreeModificationEvent.class, event, "getChange");
        }
        
        protected int invokeGetFrom(TreeModificationEvent<T> event) {
            return (int) FXUtils.invokeGetMethodValue(TreeModificationEvent.class, event, "getFrom");
        }

        /**
         * @param focusModel
         */
        public static void dispose(TreeView tree) {
            FocusModel model = tree.getFocusModel();
            tree.rootProperty().removeListener(invokeRootPropertyListener(model));
            tree.setFocusModel(null);
        }
        
        protected static ChangeListener invokeRootPropertyListener(FocusModel model) {
            return (ChangeListener) FXUtils.invokeGetFieldValue(model.getClass(), model, "weakRootPropertyListener");
        }
        
        @SuppressWarnings("unused")
        private static final Logger LOG = Logger
                .getLogger(MyTreeViewFocusModel.class.getName());
        
    }