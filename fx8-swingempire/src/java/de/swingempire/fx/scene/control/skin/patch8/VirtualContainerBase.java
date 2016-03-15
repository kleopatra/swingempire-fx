/*
 * Created on 15.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch8;

import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;

import com.sun.javafx.scene.control.behavior.BehaviorBase;


/**
 * "Fixated" the B parameter to align with fx-9.
 * 
 * Subclasses in fx-8 are supposed to provide some kind of factory
 * such that this contructor can create a behavior and pass it on to super.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class VirtualContainerBase<C extends Control, I extends IndexedCell> 
    extends com.sun.javafx.scene.control.skin.VirtualContainerBase<C, BehaviorBase<C>, I> {

    /**
     * @param control
     * @param behavior
     */
    public VirtualContainerBase(C control, Object behavior) {
        super(control, (BehaviorBase<C>) behavior);
    }

}
