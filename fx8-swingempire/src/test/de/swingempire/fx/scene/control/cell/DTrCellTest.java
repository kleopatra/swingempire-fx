/*
 * Created on 29.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeView.EditEvent;
import javafx.util.Callback;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DTrCellTest extends TrCellTest {

    
    @Override
    protected EditableControl<TreeView, TreeCell> createEditableControl() {
        EditableControl treeView = super.createEditableControl();
        treeView.setOnEditCommit(t -> {
            EditEvent e = (EditEvent) t;
            TreeItem editItem = e.getTreeItem();
            editItem.setValue(e.getNewValue());
        });
        return treeView;
    }

    @Override
    protected Callback createTextFieldCellFactory() {
        return e -> new DebugTextFieldTreeCell();
    }

}
