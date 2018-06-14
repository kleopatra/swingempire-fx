/*
 * Created on 29.04.2018
 *
 */
package de.swingempire.fx.scene.css;

import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxListViewSkin;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class CustomComboSkin<T> extends ComboBoxListViewSkin<T> {

    /**
     * @param control
     */
    public CustomComboSkin(ComboBox<T> control) {
        super(control);
    }

}
