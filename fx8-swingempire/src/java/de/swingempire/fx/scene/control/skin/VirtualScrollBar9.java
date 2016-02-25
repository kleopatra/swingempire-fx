/*
 * Created on 24.02.2016
 *
 */
package de.swingempire.fx.scene.control.skin;

/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */


import com.sun.javafx.util.Utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;

/**
 * Plain copy of VirtualScrollBar for the sake of VirtualFlow9.
 * 
 * ----------------
 * This custom ScrollBar is used to map the increment & decrement features
 * to pixel based scrolling rather than thumb/track based scrolling, if the
 * "virtual" attribute is true.
 */
public class VirtualScrollBar9 extends ScrollBar {

    /**************************************************************************
     *
     * Private fields
     *
     **************************************************************************/

    private final VirtualFlow9 flow;

    private boolean adjusting;



    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/

    /**
     * Creates a new VirtualScrollBar, for use by the VirtualFlow control.
     */
    public VirtualScrollBar9(final VirtualFlow9 flow) {
        this.flow = flow;

        super.valueProperty().addListener(valueModel -> {
            if (isVirtual()/* && oldValue != newValue*/) {
                if (adjusting) {
                    // no-op
                } else {
                    flow.setPosition(getValue());
                }
            }
        });
    }



    /**************************************************************************
     *
     * Properties
     *
     **************************************************************************/

    // --- virtual
    private BooleanProperty virtual = new SimpleBooleanProperty(this, "virtual");
    public final void setVirtual(boolean value) {
        virtual.set(value);
    }

    public final boolean isVirtual() {
        return virtual.get();
    }

    public final BooleanProperty virtualProperty() {
        return virtual;
    }


    /**************************************************************************
     *
     * Public API
     *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override public void decrement() {
        if (isVirtual()) {
            flow.scrollPixels(-10);
        } else {
            super.decrement();
        }
    }

    /** {@inheritDoc} */
    @Override public void increment() {
        if (isVirtual()) {
            flow.scrollPixels(10);
        } else {
            super.increment();
        }
    }

    // this method is called when the user clicks in the scrollbar track, so
    // we special-case it to allow for page-up and page-down clicking to work
    // as expected.
    /** {@inheritDoc} */
    @Override public void adjustValue(double pos) {
        if (isVirtual()) {
            adjusting = true;
            double oldValue = flow.getPosition();

            double newValue = ((getMax() - getMin()) * Utils.clamp(0, pos, 1))+getMin();
            if (newValue < oldValue) {
                IndexedCell cell = flow.getFirstVisibleCell();
                if (cell == null) return;
                flow.scrollToBottom(cell);
            } else if (newValue > oldValue) {
                IndexedCell cell = flow.getLastVisibleCell();
                if (cell == null) return;
                flow.scrollToTop(cell);
            }

            adjusting = false;
        } else {
            super.adjustValue(pos);
        }
    }
}
