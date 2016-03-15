/*
 * Created on 15.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch8;

import javafx.scene.control.Control;


/**
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class BehaviorBase<C extends Control> 
    extends com.sun.javafx.scene.control.behavior.BehaviorBase<C> {

    /**
     * Super constructor expects a list of KeyBindings. Note that its
     * bindings field is final (no injection!) and it creates a 
     * unmodifiable List from the given list. So here the parameter
     * must be some kind of factory again, that produces the list for
     * super.
     * 
     * @param control
     * @param bindings
     */
    public BehaviorBase(C control, String bindingsKey) {
        super(control, KeyBindingsFactory.createKeyBindings(bindingsKey));
    }

}
