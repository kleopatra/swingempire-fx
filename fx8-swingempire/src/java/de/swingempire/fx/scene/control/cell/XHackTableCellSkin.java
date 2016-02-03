/*
 * Created on 29.01.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.BehaviorBase;

import static de.swingempire.fx.util.FXUtils.*;

import javafx.scene.control.TableCell;
import javafx.scene.control.skin.TableCellSkin;
import javafx.scene.input.MouseEvent;

/**
 * Not really useful for replacing Behavior:
 * - super behavior is final, can't hack a replace
 * - super behavior will interfere (it installed all the listeners)
 * - so we hack super and call dispose.
 * - not good enough by itself, because inputMap doesn't clenaup internals on removing mappings
 * - until that's fixed, we need to reflectively remove the mapping from internals
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XHackTableCellSkin<S,T> extends TableCellSkin<S,T>  {

    private BehaviorBase<TableCell<S,T>> behaviorBase;
    
    /**
     * @param control
     */
    public XHackTableCellSkin(TableCell<S,T> control) {
        super(control);
        replaceBehavior(null);
    }
    
    /**
     * Overridden to dispose the replaced behaviour.
     */
    @Override
    public void dispose() {
        if (behaviorBase != null) {
            behaviorBase.dispose();
        }
        super.dispose();
    }

    /**
     * Just a marker - can't because super behaviour is final!
     * Trying to dispose (aka: remove all input bindings) from old - doesn't work:
     * Old mousePressed binding still called before the new.
     * 
     * @param xTableCellBehavior
     */
    private void replaceBehavior(XTableCellBehavior<S, T> xTableCellBehavior) {
        BehaviorBase<?> old = (BehaviorBase<?>) invokeGetFieldValue(TableCellSkin.class, this, "behavior");
        if (old != null) {
            // this removes all mappings, nothing left
            // old.getInputMap().dispose();
            // this removes the defaults (mappings are empty)
            // but still something is active: default mouse handlers
            // in cellBehaviour are invoked before the replaced
            // even though the mappings are removed
            old.dispose();
            // the "something" is a left-over reference in InputMap's internals
            // need to hack out
            cleanupInputMap(old.getInputMap(), MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_DRAGGED, MouseEvent.MOUSE_RELEASED);
        }
        behaviorBase = new XTableCellBehavior<>(getSkinnable());
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(XHackTableCellSkin.class.getName());
}
