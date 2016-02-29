/*
 * Created on 30.09.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

import java.util.ArrayList;
import java.util.List;

import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;

import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionModel;

/**
 * PENDING JW: suspect that copied only due to coupling - pre-9, behaviour expected
 * ComboBoxListViewSkin. Still need it, because it expects control as ComboBox -
 * here it's ComboBoxX, a complete re-write.
 * 
 * Plain C&P of core class?
 */
public class ComboBoxXListViewBehavior<T> extends ComboBoxBaseBehavior<T> {

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/
    
    /**
     * 
     */
    public ComboBoxXListViewBehavior(final ComboBoxX<T> comboBox) {
        super(comboBox, COMBO_BOX_BINDINGS);
    }

    /***************************************************************************
     *                                                                         *
     * Key event handling                                                      *
     *                                                                         *
     **************************************************************************/

    protected static final List<KeyBinding> COMBO_BOX_BINDINGS = new ArrayList<KeyBinding>();
    static {
        COMBO_BOX_BINDINGS.add(new KeyBinding(UP, KEY_PRESSED, "selectPrevious"));
        COMBO_BOX_BINDINGS.add(new KeyBinding(DOWN, "selectNext"));
        COMBO_BOX_BINDINGS.addAll(COMBO_BOX_BASE_BINDINGS);
    }

    @Override protected void callAction(String name) {
        if ("selectPrevious".equals(name)) {
            selectPrevious();
        } else if ("selectNext".equals(name)) {
            selectNext();
        } else {
            super.callAction(name);
        }
    }
    
    private ComboBox<T> getComboBox() {
        return (ComboBox<T>) getControl();
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
