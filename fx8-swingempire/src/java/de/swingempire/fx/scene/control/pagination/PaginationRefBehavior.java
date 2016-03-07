/*
 * Created on 23.07.2015
 *
 */
package de.swingempire.fx.scene.control.pagination;

/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

//package com.sun.javafx.scene.control.behavior;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.NodeOrientation;
import javafx.scene.control.Pagination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.skin.PaginationSkin;

/**
 * Not needed in jdk9 - ignore.
 * 
 * Copy of PaginationBehavior.
 * 
 * Modified to expect skin of type PaginationXSkin.
 */
public class PaginationRefBehavior extends BehaviorBase<Pagination> {

    /**************************************************************************
     *                          Setup KeyBindings                             *
     *************************************************************************/
    private static final String LEFT = "Left";
    private static final String RIGHT = "Right";

    protected static final List<KeyBinding> PAGINATION_BINDINGS = new ArrayList<KeyBinding>();
    static {
        PAGINATION_BINDINGS.add(new KeyBinding(KeyCode.LEFT, LEFT));
        PAGINATION_BINDINGS.add(new KeyBinding(KeyCode.RIGHT, RIGHT));
    }

    protected String matchActionForEvent(KeyEvent e) {
        String action = super.matchActionForEvent(e);
        if (action != null) {
            if (e.getCode() == KeyCode.LEFT) {
                if (getControl().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                    action = RIGHT;
                }
            } else if (e.getCode() == KeyCode.RIGHT) {
                if (getControl().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                    action = LEFT;
                }
            }
        }
        return action;
    }

    @Override protected void callAction(String name) {
        if (LEFT.equals(name)) {
            PaginationRefSkin ps = (PaginationRefSkin)getControl().getSkin();
            ps.selectPrevious();
        } else if (RIGHT.equals(name)) {
            PaginationRefSkin ps = (PaginationRefSkin)getControl().getSkin();
            ps.selectNext();
        } else {
            super.callAction(name);
        }
    }

    /***************************************************************************
     *                                                                         *
     * Mouse event handling                                                    *
     *                                                                         *
     **************************************************************************/

    @Override public void mousePressed(MouseEvent e) {
        super.mousePressedInitial(e);
        Pagination p = getControl();
        p.requestFocus();
    }

    /**************************************************************************
     *                         State and Functions                            *
     *************************************************************************/

    public PaginationRefBehavior(Pagination pagination) {
        super(pagination, PAGINATION_BINDINGS);
    }
}
