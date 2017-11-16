/*
 * Created on 16.11.2017
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

import static de.swingempire.fx.util.FXUtils.*;

/*
 * Copyright (c) 2012, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
  */

import com.sun.javafx.scene.control.Properties;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import javafx.collections.WeakListChangeListener;
import com.sun.javafx.scene.control.skin.resources.ControlResources;

import java.lang.ref.WeakReference;
import java.util.List;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Basically a plain copy of 9.0.1 to allow injection of custom flow. 
 * Changes are:
 * 
 * <ul>
 * <li> reflection acrobatics plus semanic wrappers to access super/collaborator internal state
 * <li> always use method to access flow, no alias
 * <li> inline methods in TableSkinUtil as abstract here
 * </ul>
 * 
 * 
 * -----------------------------
 * TableViewSkinBase is the base skin class used by controls such as
 * {@link javafx.scene.control.TableView} and {@link javafx.scene.control.TreeTableView}
 * (the concrete classes are {@link TableViewSkin} and {@link TreeTableViewSkin},
 * respectively).
 *
 * @param <M> The type of the item stored in each row (for TableView, this is the type
 *           of the items list, and for TreeTableView, this is the type of the
 *           TreeItem).
 * @param <S> The type of the item, as represented by the selection model (for
 *           TableView, this is, again, the type of the items list, and for
 *           TreeTableView, this is TreeItem typed to the same type as M).
 * @param <C> The type of the virtualised control (e.g TableView, TreeTableView)
 * @param <I> The type of cell used by this virtualised control (e.g. TableRow, TreeTableRow)
 * @param <TC> The type of TableColumnBase used by this virtualised control (e.g. TableColumn, TreeTableColumn)
 *
 * @since 9
 * @see TableView
 * @see TreeTableView
 * @see TableViewSkin
 * @see TreeTableViewSkin
 */
public abstract class WTableViewSkinBase<M, S, 
                                         C extends Control, 
                                         I extends IndexedCell<M>, 
                                         TC extends TableColumnBase<S,?>> 
    extends WVirtualContainerBase<C, I> {

    /***************************************************************************
     *                                                                         *
     * Static Fields                                                           *
     *                                                                         *
     **************************************************************************/
    // PENDING JW: belongs into headerRow
    // Copied from TableColumn. The value here should always be in-sync with
    // the value in TableColumn
    static final double DEFAULT_COLUMN_WIDTH = 80.0F;

    private static final double GOLDEN_RATIO_MULTIPLIER = 0.618033987;

    // RT-34744 : IS_PANNABLE will be false unless
    // javafx.scene.control.skin.TableViewSkin.pannable
    // is set to true. This is done in order to make TableView functional
    // on embedded systems with touch screens which do not generate scroll
    // events for touch drag gestures.
    private static final boolean IS_PANNABLE =
            AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> Boolean.getBoolean("javafx.scene.control.skin.TableViewSkin.pannable"));



    /***************************************************************************
     *                                                                         *
     * Internal Fields                                                         *
     *                                                                         *
     **************************************************************************/

    // JDK-8090129: These constants should not be static, because the
    // Locale may change between instances.
    private final String EMPTY_TABLE_TEXT = ControlResources.getString("TableView.noContent");
    private final String NO_COLUMNS_TEXT = ControlResources.getString("TableView.noColumns");

//    XVirtualFlow<I> flow;

    private boolean contentWidthDirty = true;

    /**
     * This region is used to overlay atop the table when the user is performing
     * a column resize operation or a column reordering operation. It is a line
     * that runs the height of the table to indicate either the final width of
     * of the selected column, or the position the column will be 'dropped' into
     * when the reordering operation completes.
     */
    private Region columnReorderLine;

    /**
     * A region which is resized and positioned such that it perfectly matches
     * the dimensions of any TableColumn that is being reordered by the user.
     * This is useful, for example, as a semi-transparent overlay to give
     * feedback to the user as to which column is currently being moved.
     */
    private Region columnReorderOverlay;

    /**
     * The entire header region for all columns. This header region handles
     * column reordering and resizing. It also handles the positioning and
     * resizing of thte columnReorderLine and columnReorderOverlay.
     */
    private TableHeaderRow tableHeaderRow;

    private Callback<C, I> rowFactory;

    /**
     * Region placed over the top of the flow (and possibly the header row) if
     * there is no data and/or there are no columns specified.
     */
    private StackPane placeholderRegion;
    private Label placeholderLabel;

    private int visibleColCount;

    boolean needCellsRecreated = true;
    boolean needCellsReconfigured = false;

    private int itemCount = -1;



    /***************************************************************************
     *                                                                         *
     * Listeners                                                               *
     *                                                                         *
     **************************************************************************/

    private MapChangeListener<Object, Object> propertiesMapListener = c -> {
        if (! c.wasAdded()) return;
        if (Properties.REFRESH.equals(c.getKey())) {
            refreshView();
            getSkinnable().getProperties().remove(Properties.REFRESH);
        } else if (Properties.RECREATE.equals(c.getKey())) {
            needCellsRecreated = true;
            refreshView();
            getSkinnable().getProperties().remove(Properties.RECREATE);
        }
    };

    private ListChangeListener<S> rowCountListener = c -> {
        while (c.next()) {
            if (c.wasReplaced()) {
                // RT-28397: Support for when an item is replaced with itself (but
                // updated internal values that should be shown visually).

                // The ListViewSkin equivalent code here was updated to use the
                // flow.setDirtyCell(int) API, but it was left alone here, otherwise
                // our unit test for RT-36220 fails as we do not handle the case
                // where the TableCell gets updated (only the TableRow does).
                // Ideally we would use the dirtyCell API:
                //
                // for (int i = c.getFrom(); i < c.getTo(); i++) {
                //     flow.setCellDirty(i);
                // }
                itemCount = 0;
                break;
            } else if (c.getRemovedSize() == itemCount) {
                // RT-22463: If the user clears out an items list then we
                // should reset all cells (in particular their contained
                // items) such that a subsequent addition to the list of
                // an item which equals the old item (but is rendered
                // differently) still displays as expected (i.e. with the
                // updated display, not the old display).
                itemCount = 0;
                break;
            }
        }

        // fix for RT-37853
        if (getSkinnable() instanceof TableView) {
            ((TableView)getSkinnable()).edit(-1, null);
        }

        markItemCountDirty();
        getSkinnable().requestLayout();
    };

    private ListChangeListener<TC> visibleLeafColumnsListener = c -> {
        updateVisibleColumnCount();
        while (c.next()) {
            updateVisibleLeafColumnWidthListeners(c.getAddedSubList(), c.getRemoved());
        }
    };

    private InvalidationListener widthListener = observable -> {
        // This forces the horizontal scrollbar to show when the column
        // resizing occurs. It is not ideal, but will work for now.

        // using 'needCellsReconfigured' here rather than 'needCellsRebuilt'
        // as otherwise performance suffers massively (RT-27831)
        needCellsReconfigured = true;
        if (getSkinnable() != null) {
            getSkinnable().requestLayout();
        }
    };

    private InvalidationListener itemsChangeListener;

    private WeakListChangeListener<S> weakRowCountListener =
            new WeakListChangeListener<>(rowCountListener);
    private WeakListChangeListener<TC> weakVisibleLeafColumnsListener =
            new WeakListChangeListener<>(visibleLeafColumnsListener);
    private WeakInvalidationListener weakWidthListener =
            new WeakInvalidationListener(widthListener);
    private WeakInvalidationListener weakItemsChangeListener;



    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     *
     * @param control the control
     */
    public WTableViewSkinBase(final C control) {
        super(control);

        // init the VirtualFlow
//        flow = getVirtualFlow();
        getVirtualFlow().setPannable(IS_PANNABLE);
//        flow.setCellFactory(flow1 -> TableViewSkinBase.this.createCell());

        /*
         * Listening for scrolling along the X axis, but we need to be careful
         * to handle the situation appropriately when the hbar is invisible.
         */
        getHorizontalScrollBar().valueProperty().addListener(o -> horizontalScroll());

        // RT-37152
        getHorizontalScrollBar().setUnitIncrement(15);
        getHorizontalScrollBar().setBlockIncrement(/*TableColumnHeader.*/DEFAULT_COLUMN_WIDTH);

        columnReorderLine = new Region();
        columnReorderLine.getStyleClass().setAll("column-resize-line");
        columnReorderLine.setManaged(false);
        columnReorderLine.setVisible(false);

        columnReorderOverlay = new Region();
        columnReorderOverlay.getStyleClass().setAll("column-overlay");
        columnReorderOverlay.setVisible(false);
        columnReorderOverlay.setManaged(false);

        tableHeaderRow = createTableHeaderRow();
//        tableHeaderRow.setColumnReorderLine(columnReorderLine);
        tableHeaderRow.setFocusTraversable(false);

        getChildren().addAll(tableHeaderRow, getVirtualFlow(), columnReorderOverlay, columnReorderLine);

        updateVisibleColumnCount();
        updateVisibleLeafColumnWidthListeners(getVisibleLeafColumns(), FXCollections.<TC>emptyObservableList());

        reorderingProperty().addListener(valueModel -> {
            getSkinnable().requestLayout();
        });

        getVisibleLeafColumns().addListener(weakVisibleLeafColumnsListener);

        final ObjectProperty<ObservableList<S>> itemsProperty = itemsProperty();
                // was: TableSkinUtils.itemsProperty(this);
        updateTableItems(null, itemsProperty.get());
        itemsChangeListener = new InvalidationListener() {
            private WeakReference<ObservableList<S>> weakItemsRef = new WeakReference<>(itemsProperty.get());

            @Override public void invalidated(Observable observable) {
                ObservableList<S> oldItems = weakItemsRef.get();
                weakItemsRef = new WeakReference<>(itemsProperty.get());
                updateTableItems(oldItems, itemsProperty.get());
            }
        };
        weakItemsChangeListener = new WeakInvalidationListener(itemsChangeListener);
        itemsProperty.addListener(weakItemsChangeListener);

        final ObservableMap<Object, Object> properties = control.getProperties();
        properties.remove(Properties.REFRESH);
        properties.remove(Properties.RECREATE);
        properties.addListener(propertiesMapListener);

        control.addEventHandler(ScrollToEvent.<TC>scrollToColumn(), event -> {
            scrollHorizontally(event.getScrollTarget());
        });

        // flow and flow.vbar width observer
        InvalidationListener widthObserver = valueModel -> {
            contentWidthDirty = true;
            getSkinnable().requestLayout();
        };
        getVirtualFlow().widthProperty().addListener(widthObserver);
//        flow.getVbar()
        getVerticalScrollBar().widthProperty().addListener(widthObserver);

        final ObjectProperty<Callback<C, I>> rowFactoryProperty = rowFactoryProperty(); //TableSkinUtils.rowFactoryProperty(this);
        registerChangeListener(rowFactoryProperty, e -> {
            Callback<C, I> oldFactory = rowFactory;
            rowFactory = rowFactoryProperty.get();
            if (oldFactory != rowFactory) {
                requestRebuildCells();
            }
        });
        registerChangeListener(placeholderProperty(), //TableSkinUtils.placeholderProperty(this), 
                e -> updatePlaceholderRegionVisibility());
        registerChangeListener(//flow.getVbar()
                getVerticalScrollBar().visibleProperty(), e -> updateContentWidth());
    }

// ----------------- convenience semantic wrappers around reflection acrobatics
    
    protected ScrollBar getHorizontalScrollBar() {
        return getVirtualFlow().getHorizontalScrollBar();
    }
    
    protected ScrollBar getVerticalScrollBar() {
        return getVirtualFlow().getVerticalScrollBar();
    }
    
    protected BooleanProperty reorderingProperty() {
        return invokeReorderingProperty();
    }
    
    protected TableColumnHeader getReorderingRegion() {
        return invokeReorderingRegion();
    }
    
    protected boolean isReordering() {
        return invokeIsReordering();
    }
    
    protected TableColumnHeader getColumnHeaderFor(TableColumnBase column) {
        return invokeGetColumnHeaderFor(column);
    }
    /***************************************************************************
     *                                                                         *
     * Abstract Methods                                                        *
     *                                                                         *
     **************************************************************************/


     protected abstract ObjectProperty<ObservableList<S>> itemsProperty();

     protected abstract ObjectProperty<Callback<C, I>> rowFactoryProperty();
     
     protected abstract ObjectProperty<Node> placeholderProperty();

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override public void dispose() {
        final ObjectProperty<ObservableList<S>> itemsProperty = itemsProperty();//TableSkinUtils.itemsProperty(this);

        getVisibleLeafColumns().removeListener(weakVisibleLeafColumnsListener);
        itemsProperty.removeListener(weakItemsChangeListener);
        getSkinnable().getProperties().removeListener(propertiesMapListener);
        updateTableItems(itemsProperty.get(), null);

        super.dispose();
    }

    /** {@inheritDoc} */
    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return 400;
    }

    /** {@inheritDoc} */
    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double prefHeight = computePrefHeight(-1, topInset, rightInset, bottomInset, leftInset);

        List<? extends TC> cols = getVisibleLeafColumns();
        if (cols == null || cols.isEmpty()) {
            return prefHeight * GOLDEN_RATIO_MULTIPLIER;
        }

        double pw = leftInset + rightInset;
        for (int i = 0, max = cols.size(); i < max; i++) {
            TC tc = cols.get(i);
            pw += Math.max(tc.getPrefWidth(), tc.getMinWidth());
        }
//        return pw;
        return Math.max(pw, prefHeight * GOLDEN_RATIO_MULTIPLIER);
    }

    /** {@inheritDoc} */
    @Override protected void layoutChildren(final double x, double y,
            final double w, final double h) {

        C table = getSkinnable();

        // an unlikely scenario, but it does pop up in unit tests, so guarding
        // here to prevent test failures seems ok.
        if (table == null) {
            return;
        }

        super.layoutChildren(x, y, w, h);

        if (needCellsRecreated) {
            getVirtualFlow().recreateCells();
        } else if (needCellsReconfigured) {
            getVirtualFlow().reconfigureCells();
        }

        needCellsRecreated = false;
        needCellsReconfigured = false;

        final double baselineOffset = table.getLayoutBounds().getHeight() / 2;

        // position the table header
        double tableHeaderRowHeight = tableHeaderRow.prefHeight(-1);
        layoutInArea(tableHeaderRow, x, y, w, tableHeaderRowHeight, baselineOffset,
                HPos.CENTER, VPos.CENTER);
        y += tableHeaderRowHeight;

        // let the virtual flow take up all remaining space
        // TODO this calculation is to ensure the bottom border is visible when
        // placed in a Pane. It is not ideal, but will suffice for now. See
        // RT-14335 for more information.
        double flowHeight = Math.floor(h - tableHeaderRowHeight);
        if (getItemCount() == 0 || visibleColCount == 0) {
            // show message overlay instead of empty table
            layoutInArea(placeholderRegion, x, y,
                    w, flowHeight,
                    baselineOffset, HPos.CENTER, VPos.CENTER);
        } else {
            layoutInArea(getVirtualFlow(), x, y,
                    w, flowHeight,
                    baselineOffset, HPos.CENTER, VPos.CENTER);
        }

        // painting the overlay over the column being reordered
        if (getReorderingRegion() != null) {
            TableColumnHeader reorderingColumnHeader = getReorderingRegion();
            TableColumnBase reorderingColumn = reorderingColumnHeader.getTableColumn();
            if (reorderingColumn != null) {
                Node n = getReorderingRegion();

                // determine where to draw the column header overlay, it's
                // either from the left-edge of the column, or 0, if the column
                // is off the left-side of the TableView (i.e. horizontal
                // scrolling has occured).
                double minX = tableHeaderRow.sceneToLocal(n.localToScene(n.getBoundsInLocal())).getMinX();
                double overlayWidth = reorderingColumnHeader.getWidth();
                if (minX < 0) {
                    overlayWidth += minX;
                }
                minX = minX < 0 ? 0 : minX;

                // prevent the overlay going out the right-hand side of the
                // TableView
                if (minX + overlayWidth > w) {
                    overlayWidth = w - minX;

                    if (getVerticalScrollBar().isVisible()) {
                        overlayWidth -= getVerticalScrollBar().getWidth() - 1;
                    }
                }

                double contentAreaHeight = flowHeight;
                if (getHorizontalScrollBar().isVisible()) {
                    contentAreaHeight -= getHorizontalScrollBar().getHeight();
                }

                columnReorderOverlay.resize(overlayWidth, contentAreaHeight);

                columnReorderOverlay.setLayoutX(minX);
                columnReorderOverlay.setLayoutY(tableHeaderRow.getHeight());
            }

            // paint the reorder line as well
            double cw = columnReorderLine.snappedLeftInset() + columnReorderLine.snappedRightInset();
            double lineHeight = h - (getHorizontalScrollBar().isVisible() ? getHorizontalScrollBar().getHeight() - 1 : 0);
            columnReorderLine.resizeRelocate(0, columnReorderLine.snappedTopInset(), cw, lineHeight);
        }

        columnReorderLine.setVisible(isReordering());
        columnReorderOverlay.setVisible(isReordering());

        checkContentWidthState();
    }

    /**
     * PENDING: Blocker - TableHeaderRow expects type TableSkinBase ...
     * 
     * Creates a new TableHeaderRow instance. By default this method should not be overridden, but in some
     * circumstances it makes sense (e.g. testing, or when extreme customization is desired).
     *
     * @return A new TableHeaderRow instance.
     */
    protected TableHeaderRow createTableHeaderRow() {
        HeaderRow headerRow = null;
        try {
            headerRow = new HeaderRow(createFakeSkin());
        } catch (Exception ex) {
            
        } finally {
            headerRow.installRealSkin(this);
        }
        return headerRow;
    }

    /**
     * Dummy skin to make super TableHeaderRow happy. Not tested
     * for side-effects! But working somehow ...
     * 
     * @return
     */
    protected abstract TableViewSkinBase createFakeSkin();
    
    private static class HeaderRow extends TableHeaderRow {

        /**
         * @param arg0
         */
        public HeaderRow(TableViewSkinBase fake) {
            super(fake);
        }

        /**
         * @param arg0
         */
        protected void installRealSkin(WTableViewSkinBase skin) {
            // TODO Auto-generated method stub
            
        }
        
    }

    /***************************************************************************
     *                                                                         *
     * Private implementation                                                  *
     *                                                                         *
     **************************************************************************/

    final TableHeaderRow getTableHeaderRow() {
        return tableHeaderRow;
    }

    protected abstract TableSelectionModel<S> getSelectionModel();
//    {
//        return TableSkinUtils.getSelectionModel(this);
//    }

    protected abstract TableFocusModel<M,?> getFocusModel(); 
//    {
//        return TableSkinUtils.getFocusModel(this);
//    }

    // returns the currently focused cell in the focus model
    protected abstract TablePositionBase<? extends TC> getFocusedCell(); 
//    {
//        return TableSkinUtils.getFocusedCell(this);
//    }

    // returns an ObservableList of the visible leaf columns of the control
    protected abstract ObservableList<? extends TC> getVisibleLeafColumns();
//    {
//        return TableSkinUtils.getVisibleLeafColumns(this);
//    }
    
    protected abstract TC getVisibleLeafColumn(int index);

    /** {@inheritDoc} */
    @Override protected void updateItemCount() {
        updatePlaceholderRegionVisibility();

        int oldCount = itemCount;
        int newCount = getItemCount();

        itemCount = newCount;

        if (itemCount == 0) {
//            flow.getHbar()
            getHorizontalScrollBar().setValue(0.0);
        }

        // if this is not called even when the count is the same, we get a
        // memory leak in VirtualFlow.sheet.children. This can probably be
        // optimised in the future when time permits.
        getVirtualFlow().setCellCount(newCount);

        if (newCount != oldCount) {
            // FIXME updateItemCount is called _a lot_. Perhaps we can make rebuildCells
            // smarter. Imagine if items has one million items added - do we really
            // need to rebuildCells a million times? Maybe this is better now that
            // we do rebuildCells instead of recreateCells.
            requestRebuildCells();
        } else {
            needCellsReconfigured = true;
        }
    }

    private void checkContentWidthState() {
        // we test for item count here to resolve RT-14855, where the column
        // widths weren't being resized properly when in constrained layout mode
        // if there were no items.
        if (contentWidthDirty || getItemCount() == 0) {
            updateContentWidth();
            contentWidthDirty = false;
        }
    }

    void horizontalScroll() {
        //tableHeaderRow.updateScrollX();
        invokeUpdateScrollX();
    }

    void onFocusPreviousCell() {
        TableFocusModel<M,?> fm = getFocusModel();
        if (fm == null) return;

        getVirtualFlow().scrollTo(fm.getFocusedIndex());
    }

    void onFocusNextCell() {
        TableFocusModel<M,?> fm = getFocusModel();
        if (fm == null) return;

        getVirtualFlow().scrollTo(fm.getFocusedIndex());
    }

    void onSelectPreviousCell() {
        SelectionModel<S> sm = getSelectionModel();
        if (sm == null) return;

        getVirtualFlow().scrollTo(sm.getSelectedIndex());
    }

    void onSelectNextCell() {
        SelectionModel<S> sm = getSelectionModel();
        if (sm == null) return;

        getVirtualFlow().scrollTo(sm.getSelectedIndex());
    }

    void onSelectLeftCell() {
        scrollHorizontally();
    }

    void onSelectRightCell() {
        scrollHorizontally();
    }

    void onMoveToFirstCell() {
        getVirtualFlow().scrollTo(0);
        getVirtualFlow().setPosition(0);
    }

    void onMoveToLastCell() {
        int endPos = getItemCount();
        getVirtualFlow().scrollTo(endPos);
        getVirtualFlow().setPosition(1);
    }

    private void updateTableItems(ObservableList<S> oldList, ObservableList<S> newList) {
        if (oldList != null) {
            oldList.removeListener(weakRowCountListener);
        }

        if (newList != null) {
            newList.addListener(weakRowCountListener);
        }

        markItemCountDirty();
        getSkinnable().requestLayout();
    }

    Region getColumnReorderLine() {
        return columnReorderLine;
    }

    /**
     * Function used to scroll the container down by one 'page', although
     * if this is a horizontal container, then the scrolling will be to the right.
     */
    int onScrollPageDown(boolean isFocusDriven) {
        TableSelectionModel<S> sm = getSelectionModel();
        if (sm == null) return -1;

        final int itemCount = getItemCount();

        I lastVisibleCell = getVirtualFlow().getLastVisibleCellWithinViewPort();
        if (lastVisibleCell == null) return -1;

        int lastVisibleCellIndex = lastVisibleCell.getIndex();

        // we include this test here as the virtual flow will return cells that
        // exceed past the item count, so we need to clamp here (and further down
        // in this method also). See RT-19053 for more information.
        lastVisibleCellIndex = lastVisibleCellIndex >= itemCount ? itemCount - 1 : lastVisibleCellIndex;

        // isSelected represents focus OR selection
        boolean isSelected;
        if (isFocusDriven) {
            isSelected = lastVisibleCell.isFocused() || isCellFocused(lastVisibleCellIndex);
        } else {
            isSelected = lastVisibleCell.isSelected() || isCellSelected(lastVisibleCellIndex);
        }

        if (isSelected) {
            boolean isLeadIndex = isLeadIndex(isFocusDriven, lastVisibleCellIndex);

            if (isLeadIndex) {
                // if the last visible cell is selected, we want to shift that cell up
                // to be the top-most cell, or at least as far to the top as we can go.
                getVirtualFlow().scrollToTop(lastVisibleCell);

                I newLastVisibleCell = getVirtualFlow().getLastVisibleCellWithinViewPort();
                lastVisibleCell = newLastVisibleCell == null ? lastVisibleCell : newLastVisibleCell;
            }
        }

        int newSelectionIndex = lastVisibleCell.getIndex();
        newSelectionIndex = newSelectionIndex >= itemCount ? itemCount - 1 : newSelectionIndex;
        getVirtualFlow().scrollTo(newSelectionIndex);
        return newSelectionIndex;
    }

    /**
     * Function used to scroll the container up by one 'page', although
     * if this is a horizontal container, then the scrolling will be to the left.
     */
    int onScrollPageUp(boolean isFocusDriven) {
        I firstVisibleCell = getVirtualFlow().getFirstVisibleCellWithinViewPort();
        if (firstVisibleCell == null) return -1;

        int firstVisibleCellIndex = firstVisibleCell.getIndex();

        // isSelected represents focus OR selection
        boolean isSelected = false;
        if (isFocusDriven) {
            isSelected = firstVisibleCell.isFocused() || isCellFocused(firstVisibleCellIndex);
        } else {
            isSelected = firstVisibleCell.isSelected() || isCellSelected(firstVisibleCellIndex);
        }

        if (isSelected) {
            boolean isLeadIndex = isLeadIndex(isFocusDriven, firstVisibleCellIndex);

            if (isLeadIndex) {
                // if the first visible cell is selected, we want to shift that cell down
                // to be the bottom-most cell, or at least as far to the bottom as we can go.
                getVirtualFlow().scrollToBottom(firstVisibleCell);

                I newFirstVisibleCell = getVirtualFlow().getFirstVisibleCellWithinViewPort();
                firstVisibleCell = newFirstVisibleCell == null ? firstVisibleCell : newFirstVisibleCell;
            }
        }

        int newSelectionIndex = firstVisibleCell.getIndex();
        getVirtualFlow().scrollTo(newSelectionIndex);
        return newSelectionIndex;
    }

    private boolean isLeadIndex(boolean isFocusDriven, int index) {
        final TableSelectionModel<S> sm = getSelectionModel();
        final FocusModel<M> fm = getFocusModel();

        return (isFocusDriven && fm.getFocusedIndex() == index)
                || (! isFocusDriven && sm.getSelectedIndex() == index);
    }

    /**
     * Keeps track of how many leaf columns are currently visible in this table.
     */
    private void updateVisibleColumnCount() {
        visibleColCount = getVisibleLeafColumns().size();

        updatePlaceholderRegionVisibility();
        requestRebuildCells();
    }

    private void updateVisibleLeafColumnWidthListeners(
            List<? extends TC> added, List<? extends TC> removed) {

        for (int i = 0, max = removed.size(); i < max; i++) {
            TC tc = removed.get(i);
            tc.widthProperty().removeListener(weakWidthListener);
        }
        for (int i = 0, max = added.size(); i < max; i++) {
            TC tc = added.get(i);
            tc.widthProperty().addListener(weakWidthListener);
        }
        requestRebuildCells();
    }

    final void updatePlaceholderRegionVisibility() {
        boolean visible = visibleColCount == 0 || getItemCount() == 0;

        if (visible) {
            if (placeholderRegion == null) {
                placeholderRegion = new StackPane();
                placeholderRegion.getStyleClass().setAll("placeholder");
                getChildren().add(placeholderRegion);
            }

            Node placeholderNode = placeholderProperty().get();//TableSkinUtils.placeholderProperty(this).get();

            if (placeholderNode == null) {
                if (placeholderLabel == null) {
                    placeholderLabel = new Label();
                }
                String s = visibleColCount == 0 ? NO_COLUMNS_TEXT : EMPTY_TABLE_TEXT;
                placeholderLabel.setText(s);

                placeholderRegion.getChildren().setAll(placeholderLabel);
            } else {
                placeholderRegion.getChildren().setAll(placeholderNode);
            }
        }

        getVirtualFlow().setVisible(! visible);
        if (placeholderRegion != null) {
            placeholderRegion.setVisible(visible);
        }
    }

    /*
     * It's often important to know how much width is available for content
     * within the table, and this needs to exclude the width of any vertical
     * scrollbar.
     */
    private void updateContentWidth() {
        double contentWidth = getVirtualFlow().getWidth();

        if (getVerticalScrollBar().isVisible()) {
            contentWidth -= getVerticalScrollBar().getWidth();
        }

        if (contentWidth <= 0) {
            // Fix for RT-14855 when there is no content in the TableView.
            Control c = getSkinnable();
            contentWidth = c.getWidth() - (snappedLeftInset() + snappedRightInset());
        }

        contentWidth = Math.max(0.0, contentWidth);

        // FIXME this isn't perfect, but it prevents RT-14885, which results in
        // undesired horizontal scrollbars when in constrained resize mode
        getSkinnable().getProperties().put("TableView.contentWidth", Math.floor(contentWidth));
    }

    private void refreshView() {
        markItemCountDirty();
        Control c = getSkinnable();
        if (c != null) {
            c.requestLayout();
        }
    }

    // Handles the horizontal scrolling when the selection mode is cell-based
    // and the newly selected cell belongs to a column which is not totally
    // visible.
    void scrollHorizontally() {
        TableFocusModel<M,?> fm = getFocusModel();
        if (fm == null) return;

        TC col = getFocusedCell().getTableColumn();
        scrollHorizontally(col);
    }

    void scrollHorizontally(TC col) {
        if (col == null || !col.isVisible()) return;

        final Control control = getSkinnable();

        // RT-37060 - if we are trying to scroll to a column that has not
        // yet even been rendered, we must wait until the layout pass has
        // happened and then do the scroll. The laziest way to do this is to
        // queue up the task to run later, at which point we will have hopefully
        // fully run the column through layout and css.
        TableColumnHeader header = //tableHeaderRow.
                getColumnHeaderFor(col);
        if (header == null || header.getWidth() <= 0) {
            Platform.runLater(() -> scrollHorizontally(col));
            return;
        }

        // work out where this column header is, and it's width (start -> end)
        double start = 0;
        for (TC c : getVisibleLeafColumns()) {
            if (c.equals(col)) break;
            start += c.getWidth();
        }
        double end = start + col.getWidth();

        // determine the visible width of the table
        double headerWidth = control.getWidth() - snappedLeftInset() - snappedRightInset();

        // determine by how much we need to translate the table to ensure that
        // the start position of this column lines up with the left edge of the
        // tableview, and also that the columns don't become detached from the
        // right edge of the table
        double pos = getHorizontalScrollBar().getValue();
        double max = getHorizontalScrollBar().getMax();
        double newPos;

        if (start < pos && start >= 0) {
            newPos = start;
        } else {
            double delta = start < 0 || end > headerWidth ? start - pos : 0;
            newPos = pos + delta > max ? max : pos + delta;
        }

        // FIXME we should add API in VirtualFlow so we don't end up going
        // direct to the hbar.
        // actually shift the flow - this will result in the header moving
        // as well
        getHorizontalScrollBar().setValue(newPos);
    }

    private boolean isCellSelected(int row) {
        TableSelectionModel<S> sm = getSelectionModel();
        if (sm == null) return false;
        if (! sm.isCellSelectionEnabled()) return false;

        int columnCount = getVisibleLeafColumns().size();
        for (int col = 0; col < columnCount; col++) {
            if (sm.isSelected(row, getVisibleLeafColumn(col))) {
                return true;
            }
        }

        return false;
    }

    private boolean isCellFocused(int row) {
        TableFocusModel<S,TC> fm = (TableFocusModel<S,TC>)(Object)getFocusModel();
        if (fm == null) return false;

        int columnCount = getVisibleLeafColumns().size();
        for (int col = 0; col < columnCount; col++) {
            if (fm.isFocused(row, getVisibleLeafColumn(col))) {
                return true;
            }
        }

        return false;
    }



    /***************************************************************************
     *                                                                         *
     * A11y                                                                    *
     *                                                                         *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override protected Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        switch (attribute) {
            case FOCUS_ITEM: {
                TableFocusModel<M,?> fm = getFocusModel();
                int focusedIndex = fm.getFocusedIndex();
                if (focusedIndex == -1) {
                    if (placeholderRegion != null && placeholderRegion.isVisible()) {
                        return placeholderRegion.getChildren().get(0);
                    }
                    if (getItemCount() > 0) {
                        focusedIndex = 0;
                    } else {
                        return null;
                    }
                }
                return getVirtualFlow().getPrivateCell(focusedIndex);
            }
            case CELL_AT_ROW_COLUMN: {
                int rowIndex = (Integer)parameters[0];
                return getVirtualFlow().getPrivateCell(rowIndex);
            }
            case COLUMN_AT_INDEX: {
                int index = (Integer)parameters[0];
                TableColumnBase<S,?> column = getVisibleLeafColumn(index);
                return getColumnHeaderFor(column);
            }
            case HEADER: {
                /* Not sure how this is used by Accessibility, but without this VoiceOver will not
                 * look for column headers */
                return getTableHeaderRow();
            }
            case VERTICAL_SCROLLBAR: return getVerticalScrollBar();
            case HORIZONTAL_SCROLLBAR: return getHorizontalScrollBar();
            default: return super.queryAccessibleAttribute(attribute, parameters);
        }
    }

//------------------ reflection acrobatics ...
    
    private void invokeUpdateScrollX() {
        invokeGetMethodValue(TableHeaderRow.class, getTableHeaderRow(), "updateScrollX");
    }
    
    private boolean invokeIsReordering() {
        return (boolean) invokeGetMethodValue(TableHeaderRow.class, getTableHeaderRow(), "isReordering");
    }
    
    private BooleanProperty invokeReorderingProperty() {
        return (BooleanProperty) invokeGetMethodValue(TableHeaderRow.class, getTableHeaderRow(), "reorderingProperty");
    }
    
    private TableColumnHeader invokeReorderingRegion() {
        return (TableColumnHeader) invokeGetMethodValue(TableHeaderRow.class, getTableHeaderRow(), "getReorderingRegion");
        
    }
    
    private TableColumnHeader invokeGetColumnHeaderFor(TableColumnBase column) {
        return (TableColumnHeader) invokeGetMethodValue(TableHeaderRow.class, getTableHeaderRow(), 
                "getColumnHeaderFor", TableColumnBase.class, column);
        
    }

}
