/*
 * Created on 16.11.2017
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

/*
 * Copyright (c) 2010, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
  */

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.TableViewBehavior;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

/**
 * Default skin implementation for the {@link TableView} control.
 *
 * @see TableView
 * @since 9
 */
public class WTableViewSkin<T> extends WTableViewSkinBase<T, T, TableView<T>, TableRow<T>, TableColumn<T, ?>> {

    /***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/

    private final TableViewBehavior<T>  behavior;



    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new TableViewSkin instance, installing the necessary child
     * nodes into the Control {@link Control#getChildren() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public WTableViewSkin(final TableView<T> control) {
        super(control);

        // install default input map for the TableView control
        behavior = new TableViewBehavior<>(control);
//        control.setInputMap(behavior.getInputMap());

        getVirtualFlow().setFixedCellSize(control.getFixedCellSize());
        getVirtualFlow().setCellFactory(flow -> createCell());

        EventHandler<MouseEvent> ml = event -> {
            // RT-15127: cancel editing on scroll. This is a bit extreme
            // (we are cancelling editing on touching the scrollbars).
            // This can be improved at a later date.
            if (control.getEditingCell() != null) {
                control.edit(-1, null);
            }

            // This ensures that the table maintains the focus, even when the vbar
            // and hbar controls inside the flow are clicked. Without this, the
            // focus border will not be shown when the user interacts with the
            // scrollbars, and more importantly, keyboard navigation won't be
            // available to the user.
            if (control.isFocusTraversable()) {
                control.requestFocus();
            }
        };
        getVerticalScrollBar().addEventFilter(MouseEvent.MOUSE_PRESSED, ml);
        getHorizontalScrollBar().addEventFilter(MouseEvent.MOUSE_PRESSED, ml);

        // init the behavior 'closures'
        behavior.setOnFocusPreviousRow(() -> onFocusPreviousCell());
        behavior.setOnFocusNextRow(() -> onFocusNextCell());
        behavior.setOnMoveToFirstCell(() -> onMoveToFirstCell());
        behavior.setOnMoveToLastCell(() -> onMoveToLastCell());
        behavior.setOnScrollPageDown(isFocusDriven -> onScrollPageDown(isFocusDriven));
        behavior.setOnScrollPageUp(isFocusDriven -> onScrollPageUp(isFocusDriven));
        behavior.setOnSelectPreviousRow(() -> onSelectPreviousCell());
        behavior.setOnSelectNextRow(() -> onSelectNextCell());
        behavior.setOnSelectLeftCell(() -> onSelectLeftCell());
        behavior.setOnSelectRightCell(() -> onSelectRightCell());

        registerChangeListener(control.fixedCellSizeProperty(), e -> getVirtualFlow().setFixedCellSize(getSkinnable().getFixedCellSize()));

    }



    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/


    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        LOG.info("in tableskin");
    }



    /** {@inheritDoc} */
    @Override public void dispose() {
        super.dispose();

        if (behavior != null) {
            behavior.dispose();
        }
    }

    /** {@inheritDoc} */
    @Override public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        switch (attribute) {
            case SELECTED_ITEMS: {
                List<Node> selection = new ArrayList<>();
                TableViewSelectionModel<T> sm = getSkinnable().getSelectionModel();
                for (TablePosition<T,?> pos : sm.getSelectedCells()) {
                    TableRow<T> row = getVirtualFlow().getPrivateCell(pos.getRow());
                    if (row != null) selection.add(row);
                }
                return FXCollections.observableArrayList(selection);
            }
            default: return super.queryAccessibleAttribute(attribute, parameters);
        }
    }

    /** {@inheritDoc} */
    @Override protected void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        switch (action) {
            case SHOW_ITEM: {
                Node item = (Node)parameters[0];
                if (item instanceof TableCell) {
                    @SuppressWarnings("unchecked")
                    TableCell<T, ?> cell = (TableCell<T, ?>)item;
                    getVirtualFlow().scrollTo(cell.getIndex());
                }
                break;
            }
            case SET_SELECTED_ITEMS: {
                @SuppressWarnings("unchecked")
                ObservableList<Node> items = (ObservableList<Node>)parameters[0];
                if (items != null) {
                    TableSelectionModel<T> sm = getSkinnable().getSelectionModel();
                    if (sm != null) {
                        sm.clearSelection();
                        for (Node item : items) {
                            if (item instanceof TableCell) {
                                @SuppressWarnings("unchecked")
                                TableCell<T, ?> cell = (TableCell<T, ?>)item;
                                sm.select(cell.getIndex(), cell.getTableColumn());
                            }
                        }
                    }
                }
                break;
            }
            default: super.executeAccessibleAction(action, parameters);
        }
    }



    /***************************************************************************
     *                                                                         *
     * Private methods                                                         *
     *                                                                         *
     **************************************************************************/

    /** {@inheritDoc} */
    private TableRow<T> createCell() {
        TableRow<T> cell;

        TableView<T> tableView = getSkinnable();
        if (tableView.getRowFactory() != null) {
            cell = tableView.getRowFactory().call(tableView);
        } else {
            cell = new TableRow<T>();
        }

        cell.updateTableView(tableView);
        return cell;
    }

    /** {@inheritDoc} */
    @Override protected int getItemCount() {
        TableView<T> tableView = getSkinnable();
        return tableView.getItems() == null ? 0 : tableView.getItems().size();
    }

    /** {@inheritDoc} */
    @Override void horizontalScroll() {
        super.horizontalScroll();
        if (getSkinnable().getFixedCellSize() > 0) {
            getVirtualFlow().requestCellLayout();
        }
    }

//----------------- implementing super's abstract methods

    @Override
    protected ObjectProperty<ObservableList<T>> itemsProperty() {
        return getSkinnable().itemsProperty();
    }

    @Override
    protected ObjectProperty<Callback<TableView<T>, TableRow<T>>> rowFactoryProperty() {
        return getSkinnable().rowFactoryProperty();
    }

    @Override
    protected ObjectProperty<Node> placeholderProperty() {
        return getSkinnable().placeholderProperty();
    }

    @Override
    protected TableViewSkinBase createFakeSkin() {
        return new TableViewSkin(getSkinnable());
    }

    @Override
    protected TableSelectionModel<T> getSelectionModel() {
        return getSkinnable().getSelectionModel();
    }

    @Override
    protected TableFocusModel<T, ?> getFocusModel() {
        return getSkinnable().getFocusModel();
    }

    @Override
    protected TablePositionBase<? extends TableColumn<T, ?>> getFocusedCell() {
        return getSkinnable().getFocusModel().getFocusedCell();
    }

    @Override
    protected ObservableList<? extends TableColumn<T, ?>> getVisibleLeafColumns() {
        return getSkinnable().getVisibleLeafColumns();
    }

    @Override
    protected TableColumn<T, ?> getVisibleLeafColumn(int index) {
        return getSkinnable().getVisibleLeafColumn(index);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(WTableViewSkin.class.getName());
}
