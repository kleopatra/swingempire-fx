/*
 * Created on 21.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import com.sun.javafx.scene.control.behavior.BehaviorBase;

import javafx.scene.control.TableCell;

/**
 * Compatibility layer to inject behavior.
 * <p>
 * Note: we should extend TableCellSkinBase instead of TableCellSkin,
 * Can't - scope of columnVisibility/Width changed to package, no way to override.
 * TabelCellSkinBase rather useless, needed to 
 * <p>
 * reported: https://bugs.openjdk.java.net/browse/JDK-8148573
 * status: fixed, maybe not yet in public ea
 * <p>
 * 
 * needed to go dirty replacing Behavior:
 * <li> super behavior is final, can't hack a replace
 * <li> super behavior will interfere (it installed all the listeners)
 * <li> so we hack super and call dispose.
 * <li> not good enough by itself, because inputMap doesn't clenaup internals on removing mappings
 * <li> until that's fixed, we need to reflectively remove the mapping from internals
 * <p>
 * 
 * <b>Note</b>: a TableCell that's using this skin (or the un-hacked version XTableCellSkin
 * once the extending bug is fixed)
 * must also listen to a table's terminateEdit property (implemented in XTableView) and
 * commit as appropriate!
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCellSkin<S, T> 
    extends javafx.scene.control.skin.TableCellSkin<S, T>
    implements SkinBaseDecorator {

    private BehaviorBase<TableCell<S,T>> behaviorBase;
    
    /**
     * @param control
     */
    public TableCellSkin(TableCell<S, T> control, BehaviorBase<TableCell<S, T>> behavior) {
        super(control);
        disposeSuperBehavior(javafx.scene.control.skin.TableCellSkin.class);
        behaviorBase = behavior;

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

}
