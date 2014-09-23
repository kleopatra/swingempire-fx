/*
 * Created on 23.09.2014
 *
 */
package de.swingempire.fx.scene.control.rt38724;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javafx.scene.control.ChoiceBox;

import com.sun.javafx.scene.control.skin.ChoiceBoxSkin;

/**
 * Fix for RT-38724: update value on change of selectionModel.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChoiceBoxSkinRT38724<T> extends ChoiceBoxSkin<T> {

    public ChoiceBoxSkinRT38724(ChoiceBox<T> control) {
        super(control);
    }

    @Override
    protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        if ("SELECTION_MODEL".equals(p)) {
            // super called updateSelectionModel
            // super forgot to call updateSelection
            // invoking the latter here
            invokeUpdateSelection();
        }
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
