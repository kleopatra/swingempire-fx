/*
 * Created on 24.02.2016
 *
 */
package de.swingempire.fx.scene.control.skin;

import de.swingempire.fx.util.FXUtils;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.VirtualFlow;

/**
 * Plain copy of VirtualContainerBase to allow subclassing.
 * 
 * Changes:
 * 
 * - changed abstract package private methods to protected
 * - changed virtualFlow creator/accessor to protected
 * - invoked access to package private virualFlow methods
 * - use VirtualFlow9 to allow access to changed method scope
 * - also: VirtualFlow9 uses VirtualScrollBar9
 * 
 * 
 * -----------
 * Parent class to control skins whose contents are virtualized and scrollable.
 * This class handles the interaction with the VirtualFlow class, which is the
 * main class handling the virtualization of the contents of this container.
 */
public abstract class VirtualContainerBase9<C extends Control, I extends IndexedCell> extends SkinBase<C> {

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
    private final VirtualFlow9<I> flow;



    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     *
     * @param control
     */
    public VirtualContainerBase9(final C control) {
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
    protected VirtualFlow9<I> createVirtualFlow() {
        return new VirtualFlow9<>();
    }

    protected final VirtualFlow9<I> getVirtualFlow() {
        return flow;
    }

    double getMaxCellWidth(int rowsToCount) {
        return snappedLeftInset() + flow.getMaxCellWidth(rowsToCount) + snappedRightInset();
//        return snappedLeftInset() + invokeGetMaxCellWidth(rowsToCount) + snappedRightInset();
    }

    double getVirtualFlowPreferredHeight(int rows) {
        double height = 1.0;

        for (int i = 0; i < rows && i < getItemCount(); i++) {
            height += flow.getCellLength(i);
//            height += invokeGetCellLength(i);
        }

        return height + snappedTopInset() + snappedBottomInset();
    }

    protected void checkState() {
        if (rowCountDirty) {
            updateRowCount();
            rowCountDirty = false;
        }
    }

//    protected double invokeGetMaxCellWidth(int rowsToCount) {
//        return (double) FXUtils.invokeGetMethodValue(VirtualFlow.class, getVirtualFlow(), "getMaxCellWidth", int.class, rowsToCount);
//    }
//    
//    protected double invokeGetCellLength(int index) {
//        return (double) FXUtils.invokeGetMethodValue(VirtualFlow.class, getVirtualFlow(), "getCellLength", int.class, index);
//        
//    }
}
