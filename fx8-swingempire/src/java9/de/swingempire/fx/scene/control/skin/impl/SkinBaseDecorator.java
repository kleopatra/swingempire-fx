/*
 * Created on 21.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

import java.util.Map;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;

import static de.swingempire.fx.util.FXUtils.*;

import javafx.event.EventType;
import javafx.scene.control.SkinBase;

/**
 * Hacking around issues in skins: collection of default methods
 * implementing often-needed missing/buggy features in skins.
 * 
 * Must only be implemented by SkinBase (or its descendants)
 *  because it assumes to be of type SkinBase.
 * 
 * removed methods related to changeListenerHandler: 
 *      missing unregister was fixed in fx9
 * @author Jeanette Winzenburg, Berlin
 */
public interface SkinBaseDecorator {

    /**
     * Disposes behavior (and InputMap) that was installed by super.
     * 
     * @param targetClass parent class that installed the behavior.
     */
    default void disposeSuperBehavior(Class<? extends SkinBase> targetClass) {
        BehaviorBase<?> behavior = (BehaviorBase<?>) invokeGetFieldValue(targetClass, this, "behavior");
        if (behavior == null) return;
        // Hack around: JDK-8150636
        // InputMap not completely cleared on getMappings().clear
        // as done in behavior.dispose - so need to cleanup the map separately
        // the bug is fixed but didn't yet bubble up into public ea
        // as long as the inputMap is not a public property of the
        // Node, the behavior is the only collaborator, disposing all mapping might help:
        behavior.getInputMap().dispose();
        // this removes the defaults (mappings are empty)
        // but still something is active: default mouse handlers
        // in cellBehaviour are invoked before the replaced
        // even though the mappings are removed
        behavior.dispose();
    }
    
    /**
     * Disposes behavior that was installed by super. 
     * Explicitly removes the given mappings from its InputMap) 
     * 
     * Note: removing the mappings is a workaround JDK-8150636, 
     * going deeply dirty into internal data-structures of InputMap.
     * 
     * @param targetClass parent class that installed the behavior.
     */
    default void disposeSuperBehavior(Class<? extends SkinBase> targetClass, EventType... events) {
        BehaviorBase<?> behavior = (BehaviorBase<?>) invokeGetFieldValue(targetClass, this, "behavior");
        if (behavior == null) return;
        // this removes the defaults (mappings are empty)
        // but still something is active: default mouse handlers
        // in cellBehaviour are invoked before the replaced
        // even though the mappings are removed
        behavior.dispose();
        // the "something" is a left-over reference in InputMap's internals
        // need to hack out
        cleanupInputMap(behavior.getInputMap(), events);
    }
    
    /**
     * This is a hack around InputMap not cleaning up internals on removing mappings.
     * We remove MousePressed/MouseReleased/MouseDragged mappings from the internal map.
     * <p>
     * Beware: obviously this is dirty!
     * 
     * @param inputMap
     */
    public static void cleanupInputMap(InputMap<?> inputMap, EventType... types) {
        Map eventTypeMappings = (Map) invokeGetFieldValue(InputMap.class, inputMap, "eventTypeMappings");
        for (EventType eventType : types) {
            eventTypeMappings.remove(eventType);
        }
    }


}
