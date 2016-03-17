/*
 * Created on 15.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch8;

import java.util.Map;
import java.util.function.Predicate;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;


/**
 * Beware: not really functional  in master9 - c&p'd from master8!
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

    public boolean hasInputMap() {
        return false;
    }
    
//------- compatibility API, delegate
    
    protected C getNode() {
        return getControl();
    }
    
//---------- compatibility API, no-ops
    
    /**
     * Compatibility API: no-op in fx-8, mainly because super's keyBindings
     * are final and immutatble in super. Nothing we can do about it ...
     * 
     * @param key
     * @param c
     */
    protected void addDefaultKeyBinding(KeyCode key, EventHandler<KeyEvent> handler) {
    }
    
    protected void addDefaultKeyBinding(KeyCodeCombination key, EventHandler<KeyEvent> c) {
    }

    protected void addDefaultFocusTraversalMapping() {
    }
        
    protected void addDefaultMouseBinding(EventType<MouseEvent> type, EventHandler<MouseEvent> handler) {
    }

    protected void createAndAddDefaultChildKeyBindings(
            Map<KeyCodeCombination, EventHandler<KeyEvent>> keyBindings, Predicate interceptor) {
    }

}
