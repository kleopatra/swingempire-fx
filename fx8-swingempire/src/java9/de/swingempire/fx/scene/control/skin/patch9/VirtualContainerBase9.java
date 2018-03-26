/*
 * Created on 05.08.2017
 *
 */
package de.swingempire.fx.scene.control.skin.patch9;

import static de.swingempire.fx.util.FXUtils.*;

import de.swingempire.fx.util.FXUtils;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.VirtualFlow;

/**
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings("unchecked")
public abstract class VirtualContainerBase9<C extends Control, I extends IndexedCell> extends VirtualContainerBase<C, I> {

    VirtualFlow<I> flowAlias;
    /**
     * @param arg0
     */
    public VirtualContainerBase9(C arg0, Object behaviour) {
        super(arg0);
        flowAlias = invokeGetVirtualFlow();
    }

    /**
     * While this method does not override super's method it returns the
     * same object that is access reflectively in the constructor.
     * 
     * @return
     */
    protected VirtualFlow<I> getVirtualFlow() {
        return flowAlias;
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


    
//------------------ reflection acrobatics
    
    private VirtualFlow<I> invokeGetVirtualFlow() {
        return (VirtualFlow<I>) invokeGetMethodValue(
                VirtualContainerBase.class, this, "getVirtualFlow");
    }
    
}
