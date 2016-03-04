/*
 * Created on 20.02.2015
 *
 */
package de.swingempire.fx.scene.control.et;

import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;
import javafx.scene.control.Skin;
import javafx.scene.control.TableRow;
import javafx.scene.control.skin.TableRowSkin;

/**
 * TableRow with extended event dispatch.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableRowET<S> extends TableRow<S> {

    /**
     * Overridden to allow the skin to hook into the event dispatch chain,
     * before calling super.
     */
    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        if (getSkin() instanceof EventTarget) {
            ((EventTarget) getSkin()).buildEventDispatchChain(tail);
        }
        return super.buildEventDispatchChain(tail);
    }
    
    /**
     * Overridden to return a skin that implements EventTarget.
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new TableRowETSkin<>(this);
    }



    public TableRowET() {
    }

}
