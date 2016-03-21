/*
 * Created on 21.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

/**
 * No-op: does nothing, no way to somehow inject a behavior in
 * fx-8, can't dispose super properly
 * @author Jeanette Winzenburg, Berlin
 */
public interface SkinBaseDecorator {

    default void disposeSuperBehavior(Class<?> declaringClass) {
//        com.sun.javafx.scene.control.behavior.BehaviorBase base = 
//                (com.sun.javafx.scene.control.behavior.BehaviorBase) 
//                FXUtils.invokeGetFieldValue(BehaviorSkinBase.class, this, "behavior");
//        System.out.println("behavior? " + base.getClass());
//        base.dispose();
    }

}
