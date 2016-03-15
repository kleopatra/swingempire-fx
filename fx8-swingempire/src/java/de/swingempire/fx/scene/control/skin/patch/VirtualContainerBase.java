/*
 * Created on 14.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class VirtualContainerBase<C extends Control, I extends IndexedCell> extends
        de.swingempire.fx.scene.control.skin.patch9.VirtualContainerBase<C, I> {

    public VirtualContainerBase(C control) {
        super(control);
    }


}
