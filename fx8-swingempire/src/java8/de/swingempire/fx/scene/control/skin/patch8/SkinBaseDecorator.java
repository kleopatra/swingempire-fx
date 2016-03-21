/*
 * Created on 21.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch8;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

import de.swingempire.fx.util.FXUtils;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public interface SkinBaseDecorator {

    default void disposeSuperBehavior(Class<?> declaringClass) {
        com.sun.javafx.scene.control.behavior.BehaviorBase base = 
                (com.sun.javafx.scene.control.behavior.BehaviorBase) 
                FXUtils.invokeGetFieldValue(BehaviorSkinBase.class, this, "behavior");
        System.out.println("behavior? " + base.getClass());
        base.dispose();
    }
}
