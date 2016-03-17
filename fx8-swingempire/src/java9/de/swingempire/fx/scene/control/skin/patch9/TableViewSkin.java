/*
 * Created on 17.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch9;

import javafx.scene.control.TableView;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewSkin<T> extends javafx.scene.control.skin.TableViewSkin<T> {

    /**
     * @param control
     */
    public TableViewSkin(TableView<T> control) {
        super(control);
    }

}
