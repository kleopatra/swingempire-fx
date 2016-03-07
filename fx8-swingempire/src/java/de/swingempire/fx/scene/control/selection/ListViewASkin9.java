/*
 * Created on 03.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 */


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.javafx.scene.control.VirtualScrollBar;
import com.sun.javafx.scene.control.skin.resources.ControlResources;

import de.swingempire.fx.property.BugPropertyAdapters;
import de.swingempire.fx.scene.control.skin.VirtualContainerBase9;
import javafx.beans.property.ListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
//import javafx.scene.accessibility.Action;
//import javafx.scene.accessibility.Attribute;
import javafx.scene.control.FocusModel;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * 
 * PENDING JW: on the way to update to jdk9
 * 
 * can't as long as VirtualContainerBase has package-private abstract methods ... 
 * Hack around: 
 * copied the whole stack of VirtualContainerBase/VirtualFlow/VirtualScrollBar just 
 * for the sake of access.
 * 
 * ---------------
 * 
 * Idea was (but no longer is)
 * Plain copy of core 8u20, except the (non-functional) changes listed below to allow subclassing
 * and pluggable behaviour.<p>
 * 
 * Note: on my way to completely give up on the idea of pluggable something - changes are getting
 * deeper, the extension does nothing!<p>
 * 
 * Changes: 
 * - hack access to getVirtualFlow()'s scrollBars
 * - added constructor which takes behaviour
 * - changed type of behavior to ListViewABehavior (after giving up on extending ListViewBehavior)
 * - changed listening to use listProperty (to fix 15793)
 */
public class ListViewASkin9<T> extends VirtualContainerBase9<ListViewAnchored<T>, ListCell<T>> {

 //--------------- hacking access   
//    protected void hackPackageAccess(EventHandler<MouseEvent> ml) {
//        getVirtualScrollBar("getVbar").addEventFilter(MouseEvent.MOUSE_PRESSED, ml);
//        getVirtualScrollBar("getHbar").addEventFilter(MouseEvent.MOUSE_PRESSED, ml);
////        getVirtualFlow().getVbar().addEventFilter(MouseEvent.MOUSE_PRESSED, ml);
////        getVirtualFlow().getHbar().addEventFilter(MouseEvent.MOUSE_PRESSED, ml);
//    }
//    
//    protected VirtualScrollBar getVirtualScrollBar(String name) {
//        try {
//            Method method = VirtualFlow.class.getDeclaredMethod(name);
//            method.setAccessible(true);
//            return (VirtualScrollBar) method.invoke(getVirtualFlow());
//        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return null;
//    }

///------------ end hacking access
    
    /**
     * Region placed over the top of the getVirtualFlow() (and possibly the header row) if
     * there is no data.
     */
    // FIXME this should not be a StackPane
    private StackPane placeholderRegion;
    private Node placeholderNode;
    private ListProperty<T> listProperty;
//    private Label placeholderLabel;
    private static final String EMPTY_LIST_TEXT = ControlResources.getString("ListView.noContent");

    // RT-34744 : IS_PANNABLE will be false unless
    // com.sun.javafx.scene.control.skin.ListViewSkin.pannable
    // is set to true. This is done in order to make ListView functional
    // on embedded systems with touch screens which do not generate scroll
    // events for touch drag gestures.
    private static final boolean IS_PANNABLE =
            AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> Boolean.getBoolean("com.sun.javafx.scene.control.skin.ListViewSkin.pannable"));

//    private ObservableList<T> listViewItems;

    ListViewABeahvior9<T> behavior;
    /**
     * Default constructor that installs core ListViewBehaviour.
     * @param listView
     */
    public ListViewASkin9(final ListViewAnchored<T> listView) {
        this(listView, new ListViewABeahvior9<T>(listView));

    }
    
    /**
     * Constructor to allow pluggable ListViewBehaviour.
     * @param listView
     * @param listViewBehavior
     */
    public ListViewASkin9(ListViewAnchored<T> listView,
            ListViewABeahvior9<T> listViewBehavior) {
        super(listView);
        behavior = listViewBehavior;

        listProperty = BugPropertyAdapters.listProperty(listView.itemsProperty());
        listProperty.addListener(weakListViewItemsListener);
//        updateListViewItems();
        
        // init the VirtualFlow
        getVirtualFlow().setId("virtual-getVirtualFlow()");
        getVirtualFlow().setPannable(IS_PANNABLE);
        getVirtualFlow().setVertical(getSkinnable().getOrientation() == Orientation.VERTICAL);
        getVirtualFlow().setCellFactory(flow1 -> ListViewASkin9.this.createCell());
        getVirtualFlow().setFixedCellSize(listView.getFixedCellSize());
        getChildren().add(getVirtualFlow());
        
        EventHandler<MouseEvent> ml = event -> {
            // RT-15127: cancel editing on scroll. This is a bit extreme
            // (we are cancelling editing on touching the scrollbars).
            // This can be improved at a later date.
            if (listView.getEditingIndex() > -1) {
                listView.edit(-1);
            }
            
            // This ensures that the list maintains the focus, even when the vbar
            // and hbar controls inside the getVirtualFlow() are clicked. Without this, the
            // focus border will not be shown when the user interacts with the
            // scrollbars, and more importantly, keyboard navigation won't be
            // available to the user.
            if (listView.isFocusTraversable()) {
                listView.requestFocus();
            }
        };
//        hackPackageAccess(ml);

        // PENDING JW: added delegate methods to VirtualContainerBase9
        getVBar().addEventFilter(MouseEvent.MOUSE_PRESSED, ml);
        getHBar().addEventFilter(MouseEvent.MOUSE_PRESSED, ml);

        updateRowCount();
        
        // init the behavior 'closures'
        getBehavior().setOnFocusPreviousRow(() -> { onFocusPreviousCell(); });
        getBehavior().setOnFocusNextRow(() -> { onFocusNextCell(); });
        getBehavior().setOnMoveToFirstCell(() -> { onMoveToFirstCell(); });
        getBehavior().setOnMoveToLastCell(() -> { onMoveToLastCell(); });
        getBehavior().setOnScrollPageDown(isFocusDriven -> onScrollPageDown(isFocusDriven));
        getBehavior().setOnScrollPageUp(isFocusDriven -> onScrollPageUp(isFocusDriven));
        getBehavior().setOnSelectPreviousRow(() -> { onSelectPreviousCell(); });
        getBehavior().setOnSelectNextRow(() -> { onSelectNextCell(); });
        
        // Register listeners old
//        registerChangeListener(listView.itemsProperty(), "ITEMS");
//        registerChangeListener(listView.orientationProperty(), "ORIENTATION");
//        registerChangeListener(listView.cellFactoryProperty(), "CELL_FACTORY");
//        registerChangeListener(listView.parentProperty(), "PARENT");
//        registerChangeListener(listView.placeholderProperty(), "PLACEHOLDER");
//        registerChangeListener(listView.fixedCellSizeProperty(), "FIXED_CELL_SIZE");
        
        // register listeners new
        // Register listeners
        // items updates are handled separately 
//        registerChangeListener(listView.itemsProperty(), o -> updateListViewItems());
        registerChangeListener(listView.orientationProperty(), o ->
            getVirtualFlow().setVertical(listView.getOrientation() == Orientation.VERTICAL)
        );
        // PENDING JW: flow.recreateCells  is not visible
//        registerChangeListener(listView.cellFactoryProperty(), o -> getVirtualFlow().recreateCells());
        registerChangeListener(listView.parentProperty(), o -> {
            if (listView.getParent() != null && listView.isVisible()) {
                listView.requestLayout();
            }
        });
        registerChangeListener(listView.placeholderProperty(), o -> updatePlaceholderRegionVisibility());
        registerChangeListener(listView.fixedCellSizeProperty(), o ->
            getVirtualFlow().setFixedCellSize(listView.getFixedCellSize())
        );

    }

    protected ListViewABeahvior9<T> getBehavior() {
        return behavior;
    }
    
    // Changed: old property listeners callback method
//    @Override protected void handleControlPropertyChanged(String p) {
//        super.handleControlPropertyChanged(p);
//        if ("ITEMS".equals(p)) {
////            updateListViewItems();
//        } else if ("ORIENTATION".equals(p)) {
//            getVirtualFlow().setVertical(getSkinnable().getOrientation() == Orientation.VERTICAL);
//        } else if ("CELL_FACTORY".equals(p)) {
//            getVirtualFlow().recreateCells();
//        } else if ("PARENT".equals(p)) {
//            if (getSkinnable().getParent() != null && getSkinnable().isVisible()) {
//                getSkinnable().requestLayout();
//            }
//        } else if ("PLACEHOLDER".equals(p)) {
//            updatePlaceholderRegionVisibility();
//        } else if ("FIXED_CELL_SIZE".equals(p)) {
//            getVirtualFlow().setFixedCellSize(getSkinnable().getFixedCellSize());
//        }
//    }
//
    private final ListChangeListener<T> listViewItemsListener = new ListChangeListener<T>() {
        @Override public void onChanged(Change<? extends T> c) {
            while (c.next()) {
                if (c.wasReplaced()) {
                    // RT-28397: Support for when an item is replaced with itself (but
                    // updated internal values that should be shown visually).
                    // This code was updated for RT-36714 to not update all cells,
                    // just those affected by the change
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        // PENDING JW: setCellDirty is not visible
                        getVirtualFlow().setCellDirty(i);
                    }

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
            
            rowCountDirty = true;
            getSkinnable().requestLayout();
        }
    };
    
    private final WeakListChangeListener<T> weakListViewItemsListener =
            new WeakListChangeListener<T>(listViewItemsListener);

//    public void updateListViewItems() {
//        if (listViewItems != null) {
//            listViewItems.removeListener(weakListViewItemsListener);
//        }
//
//        this.listViewItems = getSkinnable().getItems();
//
//        if (listViewItems != null) {
//            listViewItems.addListener(weakListViewItemsListener);
//        }
//
//        rowCountDirty = true;
//        getSkinnable().requestLayout();
//    }
    
    private int itemCount = -1;

    @Override public int getItemCount() {
//        return listViewItems == null ? 0 : listViewItems.size();
        return itemCount;
    }
    
    private boolean needCellsRebuilt = true;
    private boolean needCellsReconfigured = false;

    @Override protected void updateRowCount() {
        if (getVirtualFlow() == null) return;
        
        int oldCount = itemCount;
//        int newCount = listViewItems == null ? 0 : listViewItems.size();
        // CHANGED JW: 
        int newCount = listProperty.size();
        itemCount = newCount;
        
        getVirtualFlow().setCellCount(newCount);
        
        updatePlaceholderRegionVisibility();
        if (newCount != oldCount) {
            needCellsRebuilt = true;
        } else {
            needCellsReconfigured = true;
        }
    }
    
    protected final void updatePlaceholderRegionVisibility() {
        boolean visible = getItemCount() == 0;
        
        if (visible) {
            placeholderNode = getSkinnable().getPlaceholder();
            if (placeholderNode == null && (EMPTY_LIST_TEXT != null && ! EMPTY_LIST_TEXT.isEmpty())) {
                placeholderNode = new Label();
                ((Label)placeholderNode).setText(EMPTY_LIST_TEXT);
            }

            if (placeholderNode != null) {
                if (placeholderRegion == null) {
                    placeholderRegion = new StackPane();
                    placeholderRegion.getStyleClass().setAll("placeholder");
                    getChildren().add(placeholderRegion);
                }

                placeholderRegion.getChildren().setAll(placeholderNode);
            }
        }

        getVirtualFlow().setVisible(! visible);
        if (placeholderRegion != null) {
            placeholderRegion.setVisible(visible);
        }
    }

     private ListCell<T> createCell() {
        ListCell<T> cell;
        if (getSkinnable().getCellFactory() != null) {
            cell = getSkinnable().getCellFactory().call(getSkinnable());
        } else {
            cell = createDefaultCellImpl();
        }

        cell.updateListView(getSkinnable());

        return cell;
    }

    private static <T> ListCell<T> createDefaultCellImpl() {
        return new ListCell<T>() {
            @Override public void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (item instanceof Node) {
                    setText(null);
                    Node currentNode = getGraphic();
                    Node newNode = (Node) item;
                    if (currentNode == null || ! currentNode.equals(newNode)) {
                        setGraphic(newNode);
                    }
                } else {
                    /**
                     * This label is used if the item associated with this cell is to be
                     * represented as a String. While we will lazily instantiate it
                     * we never clear it, being more afraid of object churn than a minor
                     * "leak" (which will not become a "major" leak).
                     */
                    setText(item == null ? "null" : item.toString());
                    setGraphic(null);
                }
            }
        };
    }

    /** {@inheritDoc} */
    @Override protected void layoutChildren(final double x, final double y,
                                            final double w, final double h) {
        super.layoutChildren(x, y, w, h);

        if (needCellsRebuilt) {
            getVirtualFlow().rebuildCells();
        } else if (needCellsReconfigured) {
            getVirtualFlow().reconfigureCells();
        }

        needCellsRebuilt = false;
        needCellsReconfigured = false;

        if (getItemCount() == 0) {
            // show message overlay instead of empty listview
            if (placeholderRegion != null) {
                placeholderRegion.setVisible(w > 0 && h > 0);
                placeholderRegion.resizeRelocate(x, y, w, h);
            }
        } else {
            getVirtualFlow().resizeRelocate(x, y, w, h);
        }
    }
    
    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        checkState();

        if (getItemCount() == 0) {
            if (placeholderRegion == null) {
                updatePlaceholderRegionVisibility();
            }
            if (placeholderRegion != null) {
                return placeholderRegion.prefWidth(height) + leftInset + rightInset;
            }
        }

        return computePrefHeight(-1, topInset, rightInset, bottomInset, leftInset) * 0.618033987;
    }

    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return 400;
    }
    
    private void onFocusPreviousCell() {
        FocusModel<T> fm = getSkinnable().getFocusModel();
        if (fm == null) return;
        getVirtualFlow().scrollTo(fm.getFocusedIndex());
    }

    private void onFocusNextCell() {
        FocusModel<T> fm = getSkinnable().getFocusModel();
        if (fm == null) return;
        getVirtualFlow().scrollTo(fm.getFocusedIndex());
    }

    private void onSelectPreviousCell() {
        SelectionModel<T> sm = getSkinnable().getSelectionModel();
        if (sm == null) return;

        int pos = sm.getSelectedIndex();
        getVirtualFlow().scrollTo(pos);

        // Fix for RT-11299
        IndexedCell<T> cell = getVirtualFlow().getFirstVisibleCell();
        if (cell == null || pos < cell.getIndex()) {
            getVirtualFlow().setPosition(pos / (double) getItemCount());
        }
    }

    private void onSelectNextCell() {
        SelectionModel<T> sm = getSkinnable().getSelectionModel();
        if (sm == null) return;

        int pos = sm.getSelectedIndex();
        getVirtualFlow().scrollTo(pos);

        // Fix for RT-11299
        ListCell<T> cell = getVirtualFlow().getLastVisibleCell();
        if (cell == null || cell.getIndex() < pos) {
            getVirtualFlow().setPosition(pos / (double) getItemCount());
        }
    }

    private void onMoveToFirstCell() {
        getVirtualFlow().scrollTo(0);
        getVirtualFlow().setPosition(0);
    }

    private void onMoveToLastCell() {
//        SelectionModel sm = getSkinnable().getSelectionModel();
//        if (sm == null) return;
//
        int endPos = getItemCount() - 1;
//        sm.select(endPos);
        getVirtualFlow().scrollTo(endPos);
        getVirtualFlow().setPosition(1);
    }

    /**
     * Function used to scroll the container down by one 'page', although
     * if this is a horizontal container, then the scrolling will be to the right.
     */
    private int onScrollPageDown(boolean isFocusDriven) {
        ListCell<T> lastVisibleCell = getVirtualFlow().getLastVisibleCellWithinViewPort();
        if (lastVisibleCell == null) return -1;

        final SelectionModel<T> sm = getSkinnable().getSelectionModel();
        final FocusModel<T> fm = getSkinnable().getFocusModel();
        if (sm == null || fm == null) return -1;

        int lastVisibleCellIndex = lastVisibleCell.getIndex();

//        boolean isSelected = sm.isSelected(lastVisibleCellIndex) || fm.isFocused(lastVisibleCellIndex) || lastVisibleCellIndex == anchor;
        // isSelected represents focus OR selection
        boolean isSelected = false;
        if (isFocusDriven) {
            isSelected = lastVisibleCell.isFocused() || fm.isFocused(lastVisibleCellIndex);
        } else {
            isSelected = lastVisibleCell.isSelected() || sm.isSelected(lastVisibleCellIndex);
        }

        if (isSelected) {
            boolean isLeadIndex = (isFocusDriven && fm.getFocusedIndex() == lastVisibleCellIndex)
                               || (! isFocusDriven && sm.getSelectedIndex() == lastVisibleCellIndex);

            if (isLeadIndex) {
                // if the last visible cell is selected, we want to shift that cell up
                // to be the top-most cell, or at least as far to the top as we can go.
                getVirtualFlow().scrollToTop(lastVisibleCell);

                ListCell<T> newLastVisibleCell = getVirtualFlow().getLastVisibleCellWithinViewPort();
                lastVisibleCell = newLastVisibleCell == null ? lastVisibleCell : newLastVisibleCell;
            }
        } else {
            // if the selection is not on the 'bottom' most cell, we firstly move
            // the selection down to that, without scrolling the contents, so
            // this is a no-op
        }

        int newSelectionIndex = lastVisibleCell.getIndex();
        getVirtualFlow().scrollTo(lastVisibleCell);
        return newSelectionIndex;
    }

    /**
     * Function used to scroll the container up by one 'page', although
     * if this is a horizontal container, then the scrolling will be to the left.
     */
    private int onScrollPageUp(boolean isFocusDriven) {
        ListCell<T> firstVisibleCell = getVirtualFlow().getFirstVisibleCellWithinViewPort();
        if (firstVisibleCell == null) return -1;

        final SelectionModel<T> sm = getSkinnable().getSelectionModel();
        final FocusModel<T> fm = getSkinnable().getFocusModel();
        if (sm == null || fm == null) return -1;

        int firstVisibleCellIndex = firstVisibleCell.getIndex();

        // isSelected represents focus OR selection
        boolean isSelected = false;
        if (isFocusDriven) {
            isSelected = firstVisibleCell.isFocused() || fm.isFocused(firstVisibleCellIndex);
        } else {
            isSelected = firstVisibleCell.isSelected() || sm.isSelected(firstVisibleCellIndex);
        }

        if (isSelected) {
            boolean isLeadIndex = (isFocusDriven && fm.getFocusedIndex() == firstVisibleCellIndex)
                               || (! isFocusDriven && sm.getSelectedIndex() == firstVisibleCellIndex);

            if (isLeadIndex) {
                // if the first visible cell is selected, we want to shift that cell down
                // to be the bottom-most cell, or at least as far to the bottom as we can go.
                getVirtualFlow().scrollToBottom(firstVisibleCell);

                ListCell<T> newFirstVisibleCell = getVirtualFlow().getFirstVisibleCellWithinViewPort();
                firstVisibleCell = newFirstVisibleCell == null ? firstVisibleCell : newFirstVisibleCell;
            }
        } else {
            // if the selection is not on the 'top' most cell, we firstly move
            // the selection up to that, without scrolling the contents, so
            // this is a no-op
        }

        int newSelectionIndex = firstVisibleCell.getIndex();
        getVirtualFlow().scrollTo(firstVisibleCell);
        return newSelectionIndex;
    }

//    @Override
//    public Object accGetAttribute(Attribute attribute, Object... parameters) {
//        switch (attribute) {
//            case FOCUS_ITEM: {
//                FocusModel<?> fm = getSkinnable().getFocusModel();
//                int focusedIndex = fm.getFocusedIndex();
//                if (focusedIndex == -1) {
//                    if (placeholderRegion != null && placeholderRegion.isVisible()) {
//                        return placeholderRegion.getChildren().get(0);
//                    }
//                    if (getItemCount() > 0) {
//                        focusedIndex = 0;
//                    } else {
//                        return null;
//                    }
//                }
//                return getVirtualFlow().getPrivateCell(focusedIndex);
//            }
//            case ROW_AT_INDEX: {
//                Integer rowIndex = (Integer)parameters[0];
//                if (rowIndex == null) return null;
//                if (0 <= rowIndex && rowIndex < getItemCount()) {
//                    return getVirtualFlow().getPrivateCell(rowIndex);
//                }
//                return null;
//            }
//            case SELECTED_ROWS: {
//                MultipleSelectionModel<T> sm = getSkinnable().getSelectionModel();
//                ObservableList<Integer> indices = sm.getSelectedIndices();
//                List<Node> selection = new ArrayList<>(indices.size());
//                for (int i : indices) {
//                    ListCell<T> row = getVirtualFlow().getPrivateCell(i);
//                    if (row != null) selection.add(row);
//                }
//                return FXCollections.observableArrayList(selection);
//            }
//            case VERTICAL_SCROLLBAR: return getVirtualFlow().getVbar();
//            case HORIZONTAL_SCROLLBAR: return getVirtualFlow().getHbar();
//            default: return super.accGetAttribute(attribute, parameters);
//        }
//    }
//
//    @Override
//    public void accExecuteAction(Action action, Object... parameters) {
//        switch (action) {
//            case SCROLL_TO_INDEX: {
//                Integer index = (Integer)parameters[0];
//                if (index != null) getVirtualFlow().show(index);
//                break;
//            }
//            default: super.accExecuteAction(action, parameters);
//        }
//    }
}
