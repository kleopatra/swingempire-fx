/*
 * Created on 29.02.2016
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;

import static javafx.scene.input.KeyCode.*;

import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.SelectionModel;

/**
 * Plain c&p from core.
 */
public class ComboBoxXListViewBehavior9<T> extends ComboBoxBaseBehavior<T> {

    /**
     * @param comboBox
     */
    public ComboBoxXListViewBehavior9(ComboBoxX<T> comboBox) {
        super(comboBox);
        // Add these bindings as a child input map, so they take precedence
        InputMap<ComboBoxBase<T>> comboBoxListViewInputMap = new InputMap<>(comboBox);
        comboBoxListViewInputMap.getMappings().addAll(
            new InputMap.KeyMapping(UP, e -> selectPrevious()),
            new InputMap.KeyMapping(DOWN, e -> selectNext())
        );
        addDefaultChildMap(getInputMap(), comboBoxListViewInputMap);
    }

    /***************************************************************************
     *                                                                         *
     * Key event handling                                                      *
     *                                                                         *
     **************************************************************************/

    private ComboBoxX<T> getComboBox() {
        return (ComboBoxX<T>) getNode();
    }

    private void selectPrevious() {
        SelectionModel<T> sm = getComboBox().getSelectionModel();
        if (sm == null) return;
        sm.selectPrevious();
    }

    private void selectNext() {
        SelectionModel<T> sm = getComboBox().getSelectionModel();
        if (sm == null) return;
        sm.selectNext();
    }

}
