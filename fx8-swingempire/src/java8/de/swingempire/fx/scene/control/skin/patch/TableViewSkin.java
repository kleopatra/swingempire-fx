/*
 * Created on 17.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import com.sun.javafx.scene.control.skin.TableHeaderRow;

import javafx.scene.control.TableView;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewSkin<T>
        extends com.sun.javafx.scene.control.skin.TableViewSkin<T> {

    /**
     * @param tableView
     */
    public TableViewSkin(TableView<T> tableView) {
        super(tableView);
    }

//---------- compatibility api: getTableHeaderRow is package in fx-9
    
    public TableHeaderRow getTableHeader() {
        return getTableHeaderRow();
    }
}
