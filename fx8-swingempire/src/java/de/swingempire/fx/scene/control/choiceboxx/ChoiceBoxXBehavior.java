/*
 * Created on 24.09.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 */


import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionModel;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusComboBehavior;
import com.sun.javafx.scene.control.skin.Utils;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;

/**
 * ChoiceBoxBehavior - default implementation
 * 
 * c&p core except re-typed where needed (PENDING JW: indecent local fields)
 *
 * @profile common
 */
public class ChoiceBoxXBehavior<T> extends BehaviorBase<ChoiceBoxX<T>> {
    /**
     * The key bindings for the ChoiceBox. It seems this should really be the
     * same as with the ButtonBehavior super class, but it doesn't handle ENTER
     * events on desktop, whereas this does. It may be a proper analysis of the
     * interaction logic would allow us to share bindings, but for now, we simply
     * build it up specially here.
     */
    protected static final List<KeyBinding> CHOICE_BUTTON_BINDINGS = new ArrayList<KeyBinding>();
    static {
        CHOICE_BUTTON_BINDINGS.add(new KeyBinding(SPACE, KEY_PRESSED, "Press"));
        CHOICE_BUTTON_BINDINGS.add(new KeyBinding(SPACE, KEY_RELEASED, "Release"));

        if (Utils.isTwoLevelFocus()) {
            CHOICE_BUTTON_BINDINGS.add(new KeyBinding(ENTER, KEY_PRESSED, "Press"));
            CHOICE_BUTTON_BINDINGS.add(new KeyBinding(ENTER, KEY_RELEASED, "Release"));
        }

        CHOICE_BUTTON_BINDINGS.add(new KeyBinding(ESCAPE, KEY_RELEASED, "Cancel"));
        CHOICE_BUTTON_BINDINGS.add(new KeyBinding(DOWN, KEY_RELEASED, "Down"));
        CHOICE_BUTTON_BINDINGS.add(new KeyBinding(CANCEL, KEY_RELEASED, "Cancel"));

    }

    private TwoLevelFocusComboBehavior tlFocus;

    /**************************************************************************
     *                          Setup KeyBindings                             *
     *************************************************************************/
    @Override protected void callAction(String name) {
        if (name.equals("Cancel")) cancel();
        else if (name.equals("Press")) keyPressed();
        else if (name.equals("Release")) keyReleased();
        else if (name.equals("Down")) showPopup();
        else super.callAction(name);
    }

    public ChoiceBoxXBehavior(ChoiceBoxX<T> control) {
        super(control, CHOICE_BUTTON_BINDINGS);
        // Only add this if we're on an embedded platform that supports 5-button navigation
        if (Utils.isTwoLevelFocus()) {
            tlFocus = new TwoLevelFocusComboBehavior(control); // needs to be last.
        }
    }

    @Override public void dispose() {
        if (tlFocus != null) tlFocus.dispose();
        super.dispose();
    }

    public void select(int index) {
        SelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;

        sm.select(index);
    }

    public void close() {
        getControl().hide();
    }

    public void showPopup() {
        getControl().show();
    }

    /**
     * Invoked when a mouse press has occurred over the box. In addition to
     * potentially arming the Button, this will transfer focus to the box
     */
    @Override public void mousePressed(MouseEvent e) {
        ChoiceBoxX<T> choiceButton = getControl();
        super.mousePressed(e);
        if (choiceButton.isFocusTraversable()) choiceButton.requestFocus();
    }

    /**
     * Invoked when a mouse release has occurred. We determine whether this
     * was done in a manner that would fire the box's action. This happens
     * only if the box was armed by a corresponding mouse press.
     */
    @Override public void mouseReleased(MouseEvent e) {
        ChoiceBoxX<T> choiceButton = getControl();
        super.mouseReleased(e);
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
    private void keyPressed() {
        ChoiceBoxX<T> choiceButton = getControl();
        if (!choiceButton.isShowing()) {
            choiceButton.show();
        }
    }

    /**
     * Invoked when a valid keystroke release occurs which causes the box
     * to fire if it was armed by a keyPress.
     */
    private void keyReleased() {
    }

    // no-op
    /**
     * Invoked when "escape" key is released
     */
    public void cancel() {
        ChoiceBoxX<T> choiceButton = getControl();
        choiceButton.hide();
    }

}
