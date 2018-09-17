/*
 * Created on 17.09.2018
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.logging.Logger;

/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;

import javafx.scene.Node;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;

public class MyTreeCellBehavior<T> extends CellBehaviorBase<TreeCell<T>> {

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public MyTreeCellBehavior(final TreeCell<T> control) {
        super(control);
    }



    /***************************************************************************
     *                                                                         *
     * Private implementation                                                  *
     *                                                                         *
     **************************************************************************/

    @Override
    protected MultipleSelectionModel<TreeItem<T>> getSelectionModel() {
        return getCellContainer().getSelectionModel();
    }

    @Override
    protected FocusModel<TreeItem<T>> getFocusModel() {
        return getCellContainer().getFocusModel();
    }

    @Override
    protected TreeView<T> getCellContainer() {
        return getNode().getTreeView();
    }

    @Override
    protected void edit(TreeCell<T> cell) {
        TreeItem<T> treeItem = cell == null ? null : cell.getTreeItem();
        getCellContainer().edit(treeItem);
    }

    @Override
    protected void handleClicks(MouseButton button, int clickCount, boolean isAlreadySelected) {
        // handle editing, which only occurs with the primary mouse button
        TreeItem<T> treeItem = getNode().getTreeItem();
        if (button == MouseButton.PRIMARY) {
            LOG.info("status? " + clickCount + isAlreadySelected);
            if (clickCount == 1 && isAlreadySelected) {
                edit(getNode());
            } else if (clickCount == 1) {
                // cancel editing
                edit(null);
            } else if (clickCount == 2 && treeItem.isLeaf()) {
                LOG.info("tried editing? + clickCount");
                // attempt to edit
                edit(getNode());
            } else if (clickCount % 2 == 0) {
                // try to expand/collapse branch tree item
                boolean expanded = treeItem.isExpanded();
                LOG.info("double click: "  + treeItem+ expanded);
                treeItem.setExpanded(! expanded);
            }
        }
    }

    @Override protected boolean handleDisclosureNode(double x, double y) {
        TreeCell<T> treeCell = getNode();
        Node disclosureNode = treeCell.getDisclosureNode();
        if (disclosureNode != null) {
            if (disclosureNode.getBoundsInParent().contains(x, y)) {
                TreeItem<T> treeItem = treeCell.getTreeItem();
                boolean expanded = treeItem.isExpanded();
                LOG.info("disclosure "  + treeItem + expanded);
                if (treeItem != null) {
                    treeItem.setExpanded(! expanded);
                } else {
                }
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MyTreeCellBehavior.class.getName());
}
