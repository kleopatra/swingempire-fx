/*
 * Created on 23.09.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.skin.ChoiceBoxSkin;

/**
 * Fix for RT-38724: update value on change of selectionModel.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChoiceBoxSkinRT38724<T> extends ChoiceBoxSkin<T> {

    public ChoiceBoxSkinRT38724(ChoiceBox<T> control) {
        super(control);
        registerChangeListener(control.selectionModelProperty(), e -> invokeUpdateSelection());
    }

    /**
     * Reflective method access of private super method
     */
    private void invokeUpdateSelection() {
        Class<?> clazz = ChoiceBoxSkin.class;
        try {
            Method method = clazz.getDeclaredMethod("updateSelection");
            method.setAccessible(true);
            method.invoke(this);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
