/*
 * Created on 17.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import javafx.scene.control.TableView;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewSkin<T>
        extends de.swingempire.fx.scene.control.skin.patch8.TableViewSkin<T> {

    /**
     * @param tableView
     */
    public TableViewSkin(TableView<T> tableView) {
        super(tableView);
    }

}
