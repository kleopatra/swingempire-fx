/*
 * Created on 15.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.InputMap.MouseMapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import static javafx.scene.input.KeyCombination.ModifierValue.*;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;


/**
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class BehaviorBase<C extends Control> 
    extends com.sun.javafx.scene.control.behavior.BehaviorBase<C> {

    private InputMap<C> inputMap;

    public BehaviorBase(C node, String bindingsKey) {
        super(node);
        inputMap = createInputMap();
    }
    
    protected boolean hasInputMap() {
        return true;
    }
    
    @Override
    public InputMap<C> getInputMap() {
        return inputMap;
    }

    protected void addDefaultKeyBinding(KeyCode key, EventHandler<KeyEvent> handler) {
        addDefaultMapping(new KeyMapping(new KeyBinding(key), handler));
    }
    
    protected void addDefaultKeyBinding(KeyCodeCombination key, EventHandler<KeyEvent> c) {
        KeyBinding binding = createKeyBinding(key);
        addDefaultMapping(new KeyMapping(binding, c));
    }

    protected void addDefaultFocusTraversalMapping() {
        addDefaultMapping(getInputMap(), FocusTraversalInputMap.getFocusTraversalMappings());
    }
    
    protected void addDefaultMouseBinding(EventType<MouseEvent> type, EventHandler<MouseEvent> handler) {
        addDefaultMapping(new MouseMapping(type, handler));
    }
    
    protected void createAndAddDefaultChildKeyBindings(
            Map<KeyCodeCombination, EventHandler<KeyEvent>> keyBindings, Predicate interceptor) {
        InputMap<C> childInputMap = new InputMap<>(getNode());
        childInputMap.setInterceptor(interceptor);
        Set<Entry<KeyCodeCombination, EventHandler<KeyEvent>>> entries = keyBindings.entrySet();
        entries.forEach(entry -> {
            KeyBinding binding = createKeyBinding(entry.getKey());
            addDefaultMapping(childInputMap, new KeyMapping(binding, entry.getValue()));
        });
        addDefaultChildMap(getInputMap(), childInputMap);
    }
    
    private KeyBinding createKeyBinding(KeyCodeCombination key) {
        KeyBinding binding = new KeyBinding(key.getCode());
        if (key.getAlt() == DOWN) {
            binding.alt();
        } 
        if (key.getControl() == DOWN) {
            binding.ctrl();
        }
        if (key.getShift() == DOWN) {
            binding.shift();
        }
        if (key.getMeta() == DOWN) {
            binding.meta();
        }
        if (key.getShortcut() == DOWN) {
            binding.shortcut();
        }
        return binding;
    }
    
//-------- compatibility layer: delegate
    
    /**
     * same as getNode to keep fx-8 happy.
     * @return
     */
    protected C getControl() {
        return getNode();
    }
    
//----------- compatibility layer: no-ops
    
    protected /*final*/ String matchActionForEvent(KeyEvent e) {
        return null;
    }
    
    protected void callAction(String name) {
    }    
    
    protected void callActionForEvent(KeyEvent e) {
    }
}
