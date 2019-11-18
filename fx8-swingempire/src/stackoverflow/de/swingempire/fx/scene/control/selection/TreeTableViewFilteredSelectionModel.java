/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package de.swingempire.fx.scene.control.selection;

import java.util.Arrays;
import java.util.stream.IntStream;

import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeTableView.TreeTableViewSelectionModel;

/**
 * https://stackoverflow.com/q/58904792/203657
 * disable selection for some items
 * 
 * Digging into failing tests. Might be a version problem, OP
 * works against fx-8.
 */
public class TreeTableViewFilteredSelectionModel<S> extends TreeTableViewSelectionModel<S> {

    private final TreeTableViewSelectionModel<S> selectionModel;
    private final TreeItemSelectionChecker<S> checker;

    public TreeTableViewFilteredSelectionModel(
            TreeTableView<S> treeTableView,
            TreeTableViewSelectionModel<S> selectionModel,
            TreeItemSelectionChecker<S> checker) {
        super(treeTableView);
        this.selectionModel = selectionModel;
        this.checker = checker;
        cellSelectionEnabledProperty().bindBidirectional(selectionModel.cellSelectionEnabledProperty());
        selectionModeProperty().bindBidirectional(selectionModel.selectionModeProperty());
    }

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return this.selectionModel.getSelectedIndices();
    }

    @Override
    public ObservableList<TreeItem<S>> getSelectedItems() {
        return this.selectionModel.getSelectedItems();
    }

    @Override
    public ObservableList<TreeTablePosition<S, ?>> getSelectedCells() {
        return this.selectionModel.getSelectedCells();
    }

    @Override
    public boolean isSelected(int index) {
        return this.selectionModel.isSelected(index);
    }

    @Override
    public boolean isSelected(int row, TableColumnBase<TreeItem<S>, ?> column) {
        return this.selectionModel.isSelected(row, column);
    }

    @Override
    public boolean isEmpty() {
        return this.selectionModel.isEmpty();
    }

    @Override
    public TreeItem<S> getModelItem(int index) {
        return this.selectionModel.getModelItem(index);
    }

    @Override
    public void focus(int row) {
        this.selectionModel.focus(row);
    }

    @Override
    public int getFocusedIndex() {
        return this.selectionModel.getFocusedIndex();
    }

    private TreeTablePosition<S,?> getFocusedCell() {
        TreeTableView<S> treeTableView = getTreeTableView();
        if (treeTableView.getFocusModel() == null) {
            return new TreeTablePosition<>(treeTableView, -1, null);
        }
        return treeTableView.getFocusModel().getFocusedCell();
    }

    private TreeTableColumn<S,?> getTableColumn(int pos) {
        return getTreeTableView().getVisibleLeafColumn(pos);
    }

    // Gets a table column to the left or right of the current one, given an offset
    private TreeTableColumn<S,?> getTableColumn(TreeTableColumn<S,?> column, int offset) {
        int columnIndex = getTreeTableView().getVisibleLeafIndex(column);
        int newColumnIndex = columnIndex + offset;
        return getTreeTableView().getVisibleLeafColumn(newColumnIndex);
    }

    private int getRowCount() {
        TreeTableView<S> treeTableView = getTreeTableView();
        return treeTableView.getExpandedItemCount();
    }

    @Override
    public void select(int row) {
        TreeTableView<S> treeTableView = getTreeTableView();
        TreeItem<S> treeItem = treeTableView.getTreeItem(row);
        if (this.checker.isSelectable(treeItem)) {
            this.selectionModel.select(row);
        }
    }

    @Override
    public void select(TreeItem<S> treeItem) {
        if (this.checker.isSelectable(treeItem)) {
            this.selectionModel.select(treeItem);
        }
    }

    @Override
    public void selectIndices(int row, int ... rows) {
        // If we have no trailing rows, we forward to normal row-selection.
        if (rows == null || rows.length == 0) {
            select(row);
            return;
        }

        // Filter rows so that we only end up with those rows whose corresponding
        // tree-items are selectable.
        TreeTableView<S> treeTableView = getTreeTableView();
        int[] filteredRows = IntStream.concat(IntStream.of(row), Arrays.stream(rows)).filter(rowToCheck -> {
            TreeItem<S> treeItem = treeTableView.getTreeItem(rowToCheck);
            return checker.isSelectable(treeItem);
        }).toArray();

        // If we have rows left, we proceed to forward to delegate selection-model.
        if (filteredRows.length > 0) {
            int newRow = filteredRows[0];
            int[] newRows = Arrays.copyOfRange(filteredRows, 1, filteredRows.length);
            this.selectionModel.selectIndices(newRow, newRows);
        }
    }

    @Override
    public void selectRange(int start, int end) {
        super.selectRange(start, end);
    }

    @Override
    public void selectRange(int minRow, TableColumnBase<TreeItem<S>, ?> minColumn, int maxRow, TableColumnBase<TreeItem<S>, ?> maxColumn) {
        super.selectRange(minRow, minColumn, maxRow, maxColumn);
    }

    @Override
    public void select(int row, TableColumnBase<TreeItem<S>, ?> column) {
        TreeTableView<S> treeTableView = getTreeTableView();
        TreeItem<S> treeItem = treeTableView.getTreeItem(row);
        if (this.checker.isSelectable(treeItem)) {
            this.selectionModel.select(row, column);
        }
    }

    @Override
    public void clearAndSelect(int row) {
        TreeTableView<S> treeTableView = getTreeTableView();
        TreeItem<S> treeItem = treeTableView.getTreeItem(row);

        // If the specified row is selectable, we forward clear-and-select
        // call to the delegate selection-model.
        if (this.checker.isSelectable(treeItem)) {
            this.selectionModel.clearAndSelect(row);
        }
        // Else, we just do a normal clear-selection call.
        else {
            this.selectionModel.clearSelection();
        }
    }

    @Override
    public void clearAndSelect(int row, TableColumnBase<TreeItem<S>, ?> column) {
        TreeTableView<S> treeTableView = getTreeTableView();
        TreeItem<S> treeItem = treeTableView.getTreeItem(row);

        // If the specified row is selectable, we forward clear-and-select
        // call to the delegate selection-model.
        if (this.checker.isSelectable(treeItem)) {
            this.selectionModel.clearAndSelect(row, column);
        }
        // Else, we just do a normal clear-selection call.
        else {
            this.selectionModel.clearSelection();
        }
    }

    @Override
    public void clearSelection(int index) {
        this.selectionModel.clearSelection(index);
    }

    @Override
    public void clearSelection(int row, TableColumnBase<TreeItem<S>, ?> column) {
        this.selectionModel.clearSelection(row, column);
    }

    @Override
    public void clearSelection() {
        this.selectionModel.clearSelection();
    }

    @Override
    public void selectAll() {
        int rowCount = getRowCount();

        // If we only have a single row to select, we forward to the
        // row-index select-method.
        if (rowCount == 1) {
            select(0);
        }
        // Else, if we have more than one row available, we construct an array
        // of all the indices and forward to the selectIndices-method.
        else if (rowCount > 1) {
            int row = 0;
            int[] rows = IntStream.range(1, rowCount).toArray();
            selectIndices(row, rows);
        }
    }

    @Override
    public void selectFirst() {
        TreeTablePosition<S,?> focusedCell = getFocusedCell();

        if (getSelectionMode() == SelectionMode.SINGLE) {
            // Forward to delegate to ensure selection is cleared quietly.
            this.selectionModel.selectFirst();
        }

        if (getRowCount() > 0) {
            if (isCellSelectionEnabled()) {
                select(0, focusedCell.getTableColumn());
            } else {
                select(0);
            }
        }
    }

    @Override
    public void selectLast() {
        TreeTablePosition<S,?> focusedCell = getFocusedCell();

        if (getSelectionMode() == SelectionMode.SINGLE) {
            // Forward to delegate to ensure selection is cleared quietly.
            this.selectionModel.selectLast();
        }

        int numItems = getRowCount();
        if (numItems > 0 && getSelectedIndex() < numItems - 1) {
            if (isCellSelectionEnabled()) {
                select(numItems - 1, focusedCell.getTableColumn());
            } else {
                select(numItems - 1);
            }
        }
    }

    @Override
    public void selectPrevious() {
        if (isCellSelectionEnabled()) {
            // in cell selection mode, we have to wrap around, going from
            // right-to-left, and then wrapping to the end of the previous line
            TreeTablePosition<S,?> pos = getFocusedCell();
            if (pos.getColumn() - 1 >= 0) {
                // go to previous row
                select(pos.getRow(), getTableColumn(pos.getTableColumn(), -1));
            } else if (pos.getRow() < getRowCount() - 1) {
                // wrap to end of previous row
                select(pos.getRow() - 1, getTableColumn(getTreeTableView().getVisibleLeafColumns().size() - 1));
            }
        } else {
            int focusIndex = getFocusedIndex();
            if (focusIndex == -1) {
                select(getRowCount() - 1);
            } else if (focusIndex > 0) {
                select(focusIndex - 1);
            }
        }
    }

    @Override
    public void selectNext() {
        if (isCellSelectionEnabled()) {
            // in cell selection mode, we have to wrap around, going from
            // left-to-right, and then wrapping to the start of the next line
            TreeTablePosition<S,?> pos = getFocusedCell();
            if (pos.getColumn() + 1 < getTreeTableView().getVisibleLeafColumns().size()) {
                // go to next column
                select(pos.getRow(), getTableColumn(pos.getTableColumn(), 1));
            } else if (pos.getRow() < getRowCount() - 1) {
                // wrap to start of next row
                select(pos.getRow() + 1, getTableColumn(0));
            }
        } else {
            int focusIndex = getFocusedIndex();
            if (focusIndex == -1) {
                select(0);
            } else if (focusIndex < getRowCount() -1) {
                select(focusIndex + 1);
            }
        }
    }

    @Override
    public void selectLeftCell() {
        if (!isCellSelectionEnabled()) {
            return;
        }

        TreeTablePosition<S,?> pos = getFocusedCell();
        if (pos.getColumn() - 1 >= 0) {
            select(pos.getRow(), getTableColumn(pos.getTableColumn(), -1));
        }
    }

    @Override
    public void selectRightCell() {
        if (!isCellSelectionEnabled()) {
            return;
        }

        TreeTablePosition<S,?> pos = getFocusedCell();
        if (pos.getColumn() + 1 < getTreeTableView().getVisibleLeafColumns().size()) {
            select(pos.getRow(), getTableColumn(pos.getTableColumn(), 1));
        }
    }

    @Override
    public void selectAboveCell() {
        TreeTablePosition<S,?> pos = getFocusedCell();
        if (pos.getRow() == -1) {
            select(getRowCount() - 1);
        } else if (pos.getRow() > 0) {
            select(pos.getRow() - 1, pos.getTableColumn());
        }
    }

    @Override
    public void selectBelowCell() {
        TreeTablePosition<S,?> pos = getFocusedCell();

        if (pos.getRow() == -1) {
            select(0);
        } else if (pos.getRow() < getRowCount() -1) {
            select(pos.getRow() + 1, pos.getTableColumn());
        }
    }
    
    public static interface TreeItemSelectionChecker<S> {

        public boolean isSelectable(TreeItem<S> treeItem);
    }
}

