/*
 * Created on 15.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import javafx.scene.control.Control;


/**
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class BehaviorBase<C extends Control> 
    extends de.swingempire.fx.scene.control.skin.patch9.BehaviorBase<C> {

    public BehaviorBase(C control, String bindingsKey) {
        super(control, bindingsKey);
    }

}
