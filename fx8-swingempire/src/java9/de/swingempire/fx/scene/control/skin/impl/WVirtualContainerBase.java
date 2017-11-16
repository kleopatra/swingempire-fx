/*
 * Created on 16.11.2017
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

/*
 * Copyright (c) 2010, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.control.SkinBase;

/**
 * Basically a plain copy of 9.0.1 to allow injection of custom flow. 
 * Changes are:
 * 
 * <ul>
 * <li> creates and returns a VirtualFlow of type XVirtualFlow
 * <li> protected scope of methods to create/access the flow
 * </ul>
 * 
 * ---------------- 
 * 
 * Parent class to control skins whose contents are virtualized and scrollable.
 * This class handles the interaction with the VirtualFlow class, which is the
 * main class handling the virtualization of the contents of this container.
 *
 * @since 9
 */
public abstract class WVirtualContainerBase<C extends Control, I extends IndexedCell> extends SkinBase<C> {

    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/

    private boolean itemCountDirty;

    /**
     * The virtualized container which handles the layout and scrolling of
     * all the cells.
     */
    private final WVirtualFlow<I> flow;



    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     *
     * @param control the control
     */
    public WVirtualContainerBase(final C control) {
        super(control);
        flow = createVirtualFlow();

        control.addEventHandler(ScrollToEvent.scrollToTopIndex(), event -> {
            // Fix for RT-24630: The row count in VirtualFlow was incorrect
            // (normally zero), so the scrollTo call was misbehaving.
            if (itemCountDirty) {
                // update row count before we do a scroll
                updateItemCount();
                itemCountDirty = false;
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
     * @return the total number of items in this container
     */
    protected abstract int getItemCount();

    /**
     * This method is called when it is possible that the item count has changed (i.e. scrolling has occurred,
     * the control has resized, etc). This method should recalculate the item count and store that for future
     * use by the {@link #getItemCount} method.
     */
    protected abstract void updateItemCount();



    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Call this method to indicate that the item count should be updated on the next pulse.
     */
    protected final void markItemCountDirty() {
        itemCountDirty = true;
    }

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
    protected WVirtualFlow<I> createVirtualFlow() {
        return new WVirtualFlow<>();
    }

    protected WVirtualFlow<I> getVirtualFlow() {
        return flow;
    }

    double getMaxCellWidth(int rowsToCount) {
        return snappedLeftInset() + getVirtualFlow().getMaxCellBreadth(rowsToCount) // was: flow.getMaxCellWidth(rowsToCount) 
            + snappedRightInset();
    }

    double getVirtualFlowPreferredHeight(int rows) {
        double height = 1.0;

        for (int i = 0; i < rows && i < getItemCount(); i++) {
            height += getVirtualFlow().getCellLength(i);
        }

        return height + snappedTopInset() + snappedBottomInset();
    }

    void checkState() {
        if (itemCountDirty) {
            updateItemCount();
            itemCountDirty = false;
        }
    }

    void requestRebuildCells() {
        getVirtualFlow().rebuildCells();
    }

}
