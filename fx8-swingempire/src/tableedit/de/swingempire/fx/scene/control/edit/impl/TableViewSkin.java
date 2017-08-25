/*
 * Created on 17.03.2016
 *
 */
package de.swingempire.fx.scene.control.edit.impl;

import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;

/**
 * Extended for version compatibility
 * 
 * - get access to TableHeaderRow.
 * - unregister super's changeListeners on table properties
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewSkin<T> 
    extends javafx.scene.control.skin.TableViewSkin<T> 
    implements SkinBaseDecorator {

    private TableHeaderRow header;

    /**
     * @param control
     */
    public TableViewSkin(TableView<T> control) {
        super(control);
        // very quick check if SkinDecorator is working
        // unregisterChangeListener(table.fixedCellSizeProperty());
         // if we set the fixedCellSize here, the effect
         // (of having no effect) can't be seen - must do after
         // having been added to the scenegraph
         // table.setFixedCellSize(100);
    }
    
    /**
     * Implemented to grab header. Super method is package and final.
     * 
     * @return
     */
    public TableHeaderRow getTableHeader() {
        return header;
    }

    @Override
    protected TableHeaderRow createTableHeaderRow() {
        header = super.createTableHeaderRow();
        return header;
    }


}
