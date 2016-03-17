/*
 * Created on 15.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch8;

import java.util.function.Consumer;

import com.sun.javafx.scene.control.behavior.BehaviorBase;

import de.swingempire.fx.util.FXUtils;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.util.Callback;


/**
 * Beware: not really functional in master9 - c&p'd from master8!
 * 
 * 
 * "Fixated" the B parameter to align with fx-9.
 * 
 * Subclasses in fx-8 are supposed to provide some kind of factory
 * such that this contructor can create a behavior and pass it on to super.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class VirtualContainerBase<C extends Control, I extends IndexedCell> 
    extends com.sun.javafx.scene.control.skin.VirtualContainerBase<C, BehaviorBase<C>, I> {

    public VirtualContainerBase(C control, Object behavior) {
        super(control, (BehaviorBase<C>) behavior);
    }

    /**
     * This enables skin subclasses to provide a custom VirtualFlow implementation,
     * rather than have VirtualContainerBase instantiate the default instance.
     */
    protected /*com.sun.javafx.scene.control.skin.*/VirtualFlow<I> createVirtualFlow() {
        return new /*com.sun.javafx.scene.control.skin.*/VirtualFlow<>();
    }

    protected /*com.sun.javafx.scene.control.skin.*/VirtualFlow<I> getVirtualFlow() {
        return (VirtualFlow<I>) flow;
    }

//--------- delegate to virtual flow: done reflectively or directly, as needed
    
    protected ScrollBar getHBar() {
        return (ScrollBar) FXUtils.invokeGetMethodValue(
                com.sun.javafx.scene.control.skin.VirtualFlow.class, getVirtualFlow(), "getHbar");
//        return getVirtualFlow().getHbar();
    }
    
    protected ScrollBar getVBar() {
        return (ScrollBar) FXUtils.invokeGetMethodValue(
                com.sun.javafx.scene.control.skin.VirtualFlow.class, getVirtualFlow(), "getVbar");
//        return getVirtualFlow().getVbar();
    }

    protected void setCellDirty(int index) {
//        FXUtils.invokeGetMethodValue(VirtualFlow.class, getVirtualFlow(), "setCellDirty", Integer.TYPE, index);
        getVirtualFlow().setCellDirty(index);
    }

    protected void setCreateCell(Callback cc) {
        setCellFactory(cc);
    }
    
    protected void setCellFactory(Callback cc) {
        getVirtualFlow().setCreateCell(cc);
    }
    
    /**
     * Method name in flow changed. Here: use new name.
     * @param cell
     */
    protected void scrollToTop(I cell) {
        getVirtualFlow().showAsFirst(cell);
    }
    
    protected void scrollTo(I cell) {
        getVirtualFlow().show(cell);
    }
    
    protected void scrollToBottom(I cell) {
        getVirtualFlow().showAsLast(cell);
    }
    protected void rebuildCells() {
        getVirtualFlow().rebuildCells();
    }
    
    protected void reconfigureCells() {
        getVirtualFlow().reconfigureCells();
    }
    
    protected void recreateCells() {
        getVirtualFlow().recreateCells();
    }

    protected I getLastVisibleCellWithinViewPort() {
        return getVirtualFlow().getLastVisibleCellWithinViewPort();
//        return (I) FXUtils.invokeGetMethodValue(VirtualFlow.class, getVirtualFlow(), "getLastVisibleCellWithinViewPort");
    }

    protected I getFirstVisibleCellWithinViewPort() {
        return getVirtualFlow().getFirstVisibleCellWithinViewPort();
//        return (I) FXUtils.invokeGetMethodValue(VirtualFlow.class, getVirtualFlow(), "getFirstVisibleCellWithinViewPort");
    }
    

//--------------- provide fx-9 dummy method, no-op in fx-8
    
    /**
     * FX-9 compatibility dummy: no-op in fx-8
     * @param property
     * @param consumer
     */
    protected final void registerChangeListener(ObservableValue<?> property, Consumer<ObservableValue<?>> consumer) {
    }
    
}
