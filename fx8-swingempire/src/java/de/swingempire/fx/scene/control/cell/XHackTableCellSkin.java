/*
 * Created on 29.01.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.BehaviorBase;

import de.swingempire.fx.scene.control.skin.patch9.SkinBaseDecorator;
import javafx.scene.control.TableCell;
import javafx.scene.control.skin.TableCellSkin;

/**
 * Not really useful for replacing Behavior:
 * - super behavior is final, can't hack a replace
 * - super behavior will interfere (it installed all the listeners)
 * - so we hack super and call dispose.
 * - not good enough by itself, because inputMap doesn't clenaup internals on removing mappings
 * - until that's fixed, we need to reflectively remove the mapping from internals
 * <p>
 * 
 * <b>Note</b>: a TableCell that's using this skin (or the un-hacked version XTableCellSkin
 * once the extending bug is fixed)
 * must also listen to a table's terminateEdit property (implemented in XTableView) and
 * commit as appropriate!
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XHackTableCellSkin<S,T> extends TableCellSkin<S,T> implements SkinBaseDecorator {

    private BehaviorBase<TableCell<S,T>> behaviorBase;
    
    /**
     * @param control
     */
    public XHackTableCellSkin(TableCell<S,T> control) {
        super(control);
        disposeSuperBehavior(TableCellSkin.class);
        behaviorBase = createBehavior();
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
     * Creates and returns the custom behavior to use.
     */
    protected BehaviorBase<TableCell<S, T>> createBehavior() {
        return new XTableCellBehavior<>(getSkinnable());
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(XHackTableCellSkin.class.getName());
}
