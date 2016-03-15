/*
 * Created on 15.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;

/**
 * The glue layer: must talk to either patch8 or patch9.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class VirtualContainerBase<C extends Control, I extends IndexedCell> 
    extends de.swingempire.fx.scene.control.skin.patch8.VirtualContainerBase<C, I> {

    public VirtualContainerBase(C control, Object behavior) {
        super(control, behavior);
    }

    @Override
    protected VirtualFlow<I> createVirtualFlow() {
        return new VirtualFlow<>();
    }

}
