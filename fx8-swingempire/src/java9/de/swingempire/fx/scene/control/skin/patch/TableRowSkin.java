/*
 * Created on 01.04.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import java.util.List;

import de.swingempire.fx.util.FXUtils;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.skin.TableRowSkinBase;

/**
 * Compatibility layer to get internal api access off the main packages.
 * Plus allows to inject custom behavior.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableRowSkin<T> 
    extends javafx.scene.control.skin.TableRowSkin<T> 
    implements SkinBaseDecorator {

    private TableRowBehavior<T> behaviorBase;
    private List<TableCell<T, ?>> cellsAlias;
    /**
     * @param control the control to skin
     * @param behavior the behavior to inject, may be null to not replace
     *    super's
     */
    public TableRowSkin(TableRow<T> control, TableRowBehavior<T> behavior) {
        super(control);
        if (behavior != null) {
            disposeSuperBehavior(javafx.scene.control.skin.TableRowSkin.class);
            behaviorBase = behavior;
        }
    }

    protected void setUpdateCells(boolean flag) {
        FXUtils.invokeSetFieldValue(TableRowSkinBase.class, this, "updateCells", flag);
    }
    protected List<TableCell<T, ?>> getCells() {
        if (cellsAlias == null) {
            cellsAlias = (List<TableCell<T, ?>>) 
                    FXUtils.invokeGetFieldValue(TableRowSkinBase.class, this, "cells");
        }
        return cellsAlias;
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
