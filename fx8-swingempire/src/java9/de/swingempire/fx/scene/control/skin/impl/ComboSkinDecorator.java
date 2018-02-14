/*
 * Created on 14.02.2018
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

import de.swingempire.fx.util.FXUtils;
import javafx.scene.control.PopupControl;
import javafx.scene.control.skin.ComboBoxPopupControl;

/**
 * Interface to access the popup of a comboBoxPopupControl. Must only
 * be implemented by skins of type ComboBoxPopupControl.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface ComboSkinDecorator {
    default PopupControl getPopupControl() {
        return invokeGetPopup();
    }

    /**
     * @return
     */
    private PopupControl invokeGetPopup() {
        return (PopupControl) FXUtils.invokeGetMethodValue(ComboBoxPopupControl.class, this, "getPopup");
    }

    
}
