/*
 * Created on 24.09.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */


import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusComboBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.InputMap.MouseMapping;
import com.sun.javafx.scene.control.skin.Utils;

import static javafx.scene.input.KeyCode.*;

import javafx.scene.control.SelectionModel;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * ChoiceBoxBehavior - default implementation
 * 
 * Plain c&p from core, 9-ea-107 - except for typing to ChoiceBoxX.
 */
public class ChoiceBoxXBehavior<T> extends BehaviorBase<ChoiceBoxX<T>> {

    private final InputMap<ChoiceBoxX<T>> choiceBoxInputMap;

    private TwoLevelFocusComboBehavior tlFocus;

    /**************************************************************************
     *                          Setup KeyBindings                             *
     *************************************************************************/

    public ChoiceBoxXBehavior(ChoiceBoxX<T> control) {
        super(control);

        // create a map for choiceBox-specific mappings (this reuses the default
        // InputMap installed on the control, if it is non-null, allowing us to pick up any user-specified mappings)
        choiceBoxInputMap = createInputMap();

        // choiceBox-specific mappings for key and mouse input
        addDefaultMapping(choiceBoxInputMap,
            new KeyMapping(SPACE, KeyEvent.KEY_PRESSED, this::keyPressed),
            new KeyMapping(SPACE, KeyEvent.KEY_RELEASED, this::keyReleased),

            new KeyMapping(ESCAPE, KeyEvent.KEY_RELEASED, e -> cancel()),
            new KeyMapping(DOWN, KeyEvent.KEY_RELEASED, e -> showPopup()),
            new KeyMapping(CANCEL, KeyEvent.KEY_RELEASED, e -> cancel()),

            new MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed),
            new MouseMapping(MouseEvent.MOUSE_RELEASED, this::mouseReleased)
        );

        // add some special two-level focus mappings
        InputMap<ChoiceBoxX<T>> twoLevelFocusInputMap = new InputMap<>(control);
        twoLevelFocusInputMap.setInterceptor(e -> !Utils.isTwoLevelFocus());
        twoLevelFocusInputMap.getMappings().addAll(
            new KeyMapping(ENTER, KeyEvent.KEY_PRESSED, this::keyPressed),
            new KeyMapping(ENTER, KeyEvent.KEY_RELEASED, this::keyReleased)
        );
        addDefaultChildMap(choiceBoxInputMap, twoLevelFocusInputMap);

        // Only add this if we're on an embedded platform that supports 5-button navigation
        if (Utils.isTwoLevelFocus()) {
            tlFocus = new TwoLevelFocusComboBehavior(control); // needs to be last.
        }
    }

    @Override public InputMap<ChoiceBoxX<T>> getInputMap() {
        return choiceBoxInputMap;
    }

    @Override public void dispose() {
        if (tlFocus != null) tlFocus.dispose();
        super.dispose();
    }

    public void select(int index) {
        SelectionModel<T> sm = getNode().getSelectionModel();
        if (sm == null) return;

        sm.select(index);
    }

    public void close() {
        getNode().hide();
    }

    public void showPopup() {
        getNode().show();
    }

    /**
     * Invoked when a mouse press has occurred over the box. In addition to
     * potentially arming the Button, this will transfer focus to the box
     */
    public void mousePressed(MouseEvent e) {
        ChoiceBoxX<T> choiceButton = getNode();
        if (choiceButton.isFocusTraversable()) choiceButton.requestFocus();
    }

    /**
     * Invoked when a mouse release has occurred. We determine whether this
     * was done in a manner that would fire the box's action. This happens
     * only if the box was armed by a corresponding mouse press.
     */
    public void mouseReleased(MouseEvent e) {
        ChoiceBoxX<T> choiceButton = getNode();
        if (choiceButton.isShowing() || !choiceButton.contains(e.getX(), e.getY())) {
            choiceButton.hide(); // hide if already showing
        }
        else if (e.getButton() == MouseButton.PRIMARY) {
            choiceButton.show();
        }
    }

    /**
     * This function is invoked when an appropriate keystroke occurs which
     * causes this box to be armed if it is not already armed by a mouse
     * press.
     */
    private void keyPressed(KeyEvent e) {
        ChoiceBoxX<T> choiceButton = getNode();
        if (!choiceButton.isShowing()) {
            choiceButton.show();
        }
    }

    /**
     * Invoked when a valid keystroke release occurs which causes the box
     * to fire if it was armed by a keyPress.
     */
    private void keyReleased(KeyEvent e) {
    }

    // no-op
    /**
     * Invoked when "escape" key is released
     */
    public void cancel() {
        ChoiceBoxX<T> choiceButton = getNode();
        choiceButton.hide();
    }

}
