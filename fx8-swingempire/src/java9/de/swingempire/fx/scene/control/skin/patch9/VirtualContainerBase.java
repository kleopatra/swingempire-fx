/*
 * Created on 14.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch9;

import de.swingempire.fx.util.FXUtils;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.control.SkinBase;
import javafx.util.Callback;

/**
 * Can't extend VirtualContainerBase - due to abstract package-private methods.
 * Fix not in 9-ea-109?
 * 
 * Plain copy of core, except for 
 * - widening scope of some methods/fields
 * - reflective access to flow methods
 * - dummy parameter in constructor, for compatibility
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class VirtualContainerBase<C extends Control, I extends IndexedCell> 
    extends SkinBase<C> {
    
    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/

    protected boolean rowCountDirty;

    /**
     * The virtualized container which handles the layout and scrolling of
     * all the cells.
     */
    private final VirtualFlow<I> flow;



    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     *
     * @param control
     */
    public VirtualContainerBase(final C control, Object dummyBehavior) {
        super(control);
        flow = createVirtualFlow();

        control.addEventHandler(ScrollToEvent.scrollToTopIndex(), event -> {
            // Fix for RT-24630: The row count in VirtualFlow was incorrect
            // (normally zero), so the scrollTo call was misbehaving.
            if (rowCountDirty) {
                // update row count before we do a scroll
                updateRowCount();
                rowCountDirty = false;
            }
            flow.scrollToTop(event.getScrollTarget());
        });
    }



    /***************************************************************************
     *                                                                         *
     * Abstract API                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Returns the total number of items in this container, including those
     * that are currently hidden because they are out of view.
     */
    protected abstract int getItemCount();

    protected abstract void updateRowCount();



    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override protected void layoutChildren(double x, double y, double w, double h) {
        checkState();
    }



    /***************************************************************************
     *                                                                         *
     * Private methods                                                         *
     *                                                                         *
     **************************************************************************/

    /**
     * This enables skin subclasses to provide a custom VirtualFlow implementation,
     * rather than have VirtualContainerBase instantiate the default instance.
     */
    protected VirtualFlow<I> createVirtualFlow() {
        return new VirtualFlow<>();
    }

    protected final VirtualFlow<I> getVirtualFlow() {
        return flow;
    }

//--------- delegate to virtual flow: here done reflectively
    
    protected ScrollBar getHBar() {
        return (ScrollBar) FXUtils.invokeGetMethodValue(
                javafx.scene.control.skin.VirtualFlow.class, getVirtualFlow(), "getHbar");
//        return getVirtualFlow().getHbar();
    }
    
    protected ScrollBar getVBar() {
        return (ScrollBar) FXUtils.invokeGetMethodValue(
                javafx.scene.control.skin.VirtualFlow.class, getVirtualFlow(), "getVbar");
//        return getVirtualFlow().getVbar();
    }

    protected void setCellDirty(int index) {
        FXUtils.invokeGetMethodValue(
                javafx.scene.control.skin.VirtualFlow.class, getVirtualFlow(), "setCellDirty", Integer.TYPE, index);
    }
    
    protected void setCreateCell(Callback<VirtualFlow<I>, I> cc) {
        setCellFactory(cc);
    }
    
    protected void setCellFactory(Callback cc) {
        getVirtualFlow().setCellFactory(cc);
    }
    
    protected void scrollToTop(I cell) {
        getVirtualFlow().scrollToTop(cell);
    }
    
    protected void scrollTo(I cell) {
        getVirtualFlow().scrollTo(cell);
    }
    
    protected void scrollToBottom(I cell) {
        getVirtualFlow().scrollToBottom(cell);
    }
    
    protected void rebuildCells() {
        FXUtils.invokeMethod(
                javafx.scene.control.skin.VirtualFlow.class, getVirtualFlow(), "rebuildCells");
    }
    
    protected void reconfigureCells() {
        FXUtils.invokeMethod(
                javafx.scene.control.skin.VirtualFlow.class, getVirtualFlow(), "reconfigureCells");
    }
    
    protected void recreateCells() {
        FXUtils.invokeMethod(
                javafx.scene.control.skin.VirtualFlow.class, getVirtualFlow(), "recreateCells");
    }

    protected I getLastVisibleCellWithinViewPort() {
        return (I) FXUtils.invokeGetMethodValue(
                javafx.scene.control.skin.VirtualFlow.class, getVirtualFlow(), "getLastVisibleCellWithinViewPort");
    }

    protected I getFirstVisibleCellWithinViewPort() {
        return (I) FXUtils.invokeGetMethodValue(
                javafx.scene.control.skin.VirtualFlow.class, getVirtualFlow(), "getFirstVisibleCellWithinViewPort");
    }
    
    private double getCellLengthFromFlow(int i) {
        return (double) FXUtils.invokeGetMethodValue(
                javafx.scene.control.skin.VirtualFlow.class, getVirtualFlow(), "getCellLength", Integer.TYPE, i);
    }


    private double getMaxCellWidthFromFlow(int rowsToCount) {
        return (double) FXUtils.invokeGetMethodValue(
                javafx.scene.control.skin.VirtualFlow.class, getVirtualFlow(), "getMaxCellWidth", Integer.TYPE, rowsToCount);
//        return flow.getMaxCellWidth(rowsToCount);
    }


//--------- end delegate methods
    double getMaxCellWidth(int rowsToCount) {
        return snappedLeftInset() + getMaxCellWidthFromFlow(rowsToCount) + snappedRightInset();
    }


    double getVirtualFlowPreferredHeight(int rows) {
        double height = 1.0;

        for (int i = 0; i < rows && i < getItemCount(); i++) {
            height += getCellLengthFromFlow(i);
        }

        return height + snappedTopInset() + snappedBottomInset();
    }



    protected void checkState() {
        if (rowCountDirty) {
            updateRowCount();
            rowCountDirty = false;
        }
    }

//--------------- provide fx-9 dummy method, no-op in fx-8
    
    /**
     * FX-8 compatibility dummy: no-op in fx-9
     * @param property
     * @param consumer
     */
    protected final void registerChangeListener(ObservableValue<?> property, String key) {
    }
    
    /**
     * FX-8 compatibility dummy: no-op in fx-9
     * @param property
     * @param consumer
     */
    protected void handleControlPropertyChanged(String p) {
    }
    
}    
