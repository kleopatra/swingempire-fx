/*
 * Created on 23.09.2015
 *
 */
package de.swingempire.fx.control;

import javafx.scene.control.ComboBox;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

/**
 * Subclass of ComboBoxListViewSkin that forces commit-on-focusLost. Implemented in 
 * a listener to focusedProperty to call the appropriate protected method of super.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class CommitOnFocusLostComboSkin<T> extends ComboBoxListViewSkin<T> {

        public CommitOnFocusLostComboSkin(ComboBox<T> comboBox) {
            super(comboBox);
            getSkinnable().focusedProperty().addListener((source, ov, nv) -> {
                if (!nv) {
                    // this method is exposed post-jdk845
                    setTextFromTextFieldIntoComboBoxValue();
                }
            });
        }
        
    }