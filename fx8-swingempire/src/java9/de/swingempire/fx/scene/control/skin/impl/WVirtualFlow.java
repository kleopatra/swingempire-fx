/*
 * Created on 16.11.2017
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

import java.util.logging.Logger;

import static de.swingempire.fx.util.FXUtils.*;

import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.StackPane;

/**
 * Extension of VirtualFlow encapsulating reflection acrobatics.
 * 
 * Extending of functionality should be done in version-agnostic layer.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class WVirtualFlow<T extends IndexedCell> extends VirtualFlow<T> {

    StackPane leadingFiller;
    
    public WVirtualFlow() {
        // --- corner
        leadingFiller = new StackPane();
        leadingFiller.getStyleClass().setAll("corner");
        getChildren().add(leadingFiller);


    }
     
    @Override
    public void requestLayout() {
        super.requestLayout();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        layoutHorizontalScrollBar();
    }

    /**
     * 
     */
    protected void layoutHorizontalScrollBar() {
        ScrollBar bar = getHorizontalScrollBar();
        if (!bar.isVisible() || !isVertical()) {
            leadingFiller.setVisible(false);
            return;
        }
        leadingFiller.setVisible(true);
        double viewportBreadth = getViewportBreadth();
        double viewportLength = getViewportLength();
        
        double xoffset = viewportBreadth / 4;
        double effectiveBreadth = viewportBreadth - xoffset;
        bar.resizeRelocate(xoffset, viewportLength, effectiveBreadth, bar.prefHeight(effectiveBreadth));
        leadingFiller.resizeRelocate(0, viewportLength, xoffset, bar.getHeight());
        LOG.info("bar width? " + bar.getWidth());
    }
    
    protected double getViewportBreadth() {
        return invokeGetViewportBreadth();
    }
    
    protected double getViewportLength() {
        return invokeGetViewportLength();
    }

    protected double getMaxCellBreadth(int rowCount) {
        return invokeGetMaxCellWidth(rowCount);
    }
    
    /**
     * Note: this doesn't override super's method but returns the exact same
     * result by reflectively accessing super.
     * 
     * @param index
     * @return
     */
    protected double getCellLength(int index) {
        return invokeGetCellLength(index);
    }
    
    /**
     * Note: this doesn't override super's method but returns the exact same
     * result by reflectively accessing super.
     * 
     * @param index
     * @return
     */
    protected T getPrivateCell(int index) {
        return invokeGetPrivateCell(index);
    }
    
    /**
     * Note: this doesn't override super's method but has the exact same
     * effect by reflectively accessing super.
     */
    protected void rebuildCells() {
        invokeRebuildCells();
    }
    
    /**
     * Note: this doesn't override super's method but has the exact same
     * effect by reflectively accessing super.
     */
    protected void recreateCells() {
        invokeRecreateCells();
    }
    
    /**
     * Note: this doesn't override super's method but returns the exact same
     * result by reflectively accessing super.
     * 
     * @return
     */
    protected T getFirstVisibleCellWithinViewPort() {
        return invokeGetFirstVisibleCellWithinViewPort();
    }
    /**
     * Note: this doesn't override super's method but returns the exact same
     * result by reflectively accessing super.
     * 
     * @return
     */
    protected T getLastVisibleCellWithinViewPort() {
        return invokeGetLastVisibleCellWithinViewPort();
    }
    /**
     * Note: this doesn't override super's method but has the exact same
     * effect by reflectively accessing super.
     */
    protected void reconfigureCells() {
        invokeReconfigureCells();
    }
    
    /**
     * Note: this doesn't override super's method but has the exact same
     * effect by reflectively accessing super.
     */
    protected void requestCellLayout() {
        invokeRequestCellLayout();
    }

    protected ScrollBar getHorizontalScrollBar() {
        return invokeGetHbar();
    }
    
    protected ScrollBar getVerticalScrollBar() {
        return invokeGetVbar();
    }
    
//--------------- refelction 
    
    private  T invokeGetFirstVisibleCellWithinViewPort() {
        return (T) invokeGetMethodValue(VirtualFlow.class, this, "getFirstVisibleCellWithinViewPort");
    }
    
    private  T invokeGetLastVisibleCellWithinViewPort() {
        return (T) invokeGetMethodValue(VirtualFlow.class, this, "getLastVisibleCellWithinViewPort");
    }
    
    private ScrollBar invokeGetHbar() {
        return (ScrollBar) invokeGetMethodValue(VirtualFlow.class, this, "getHbar");
    }
    private ScrollBar invokeGetVbar() {
        return (ScrollBar) invokeGetMethodValue(VirtualFlow.class, this, "getVbar");
    }
    
    private void invokeRequestCellLayout() {
        invokeGetMethodValue(VirtualFlow.class, this, "requestCellLayout");
    }
    
    private void invokeReconfigureCells() {
        invokeGetMethodValue(VirtualFlow.class, this, "reconfigureCells");
    }
    
    private void invokeRecreateCells() {
        invokeGetMethodValue(VirtualFlow.class, this, "recreateCells");
    }
    
    private void invokeRebuildCells() {
        invokeGetMethodValue(VirtualFlow.class, this, "rebuildCells");
    }
    private T invokeGetPrivateCell(int index) {
        return (T) invokeGetMethodValue(VirtualFlow.class, this, 
                "getPrivateCell", Integer.TYPE, index);
        
    }
    private double invokeGetViewportLength() {
        return (double) invokeGetMethodValue(VirtualFlow.class, this, "getViewportLength");
    }
    
    private double invokeGetViewportBreadth() {
        return (double) invokeGetMethodValue(VirtualFlow.class, this, "getViewportBreadth");
    }
    
    private double invokeGetCellLength(int index) {
        return (double) invokeGetMethodValue(VirtualFlow.class, this, 
                "getCellLength", Integer.TYPE, index);
    }
    
    private double invokeGetMaxCellWidth(int rowCount) {
        return (double) invokeGetMethodValue(VirtualFlow.class, this, 
                "getMaxCellWidth", Integer.TYPE, rowCount);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(WVirtualFlow.class.getName());
}
