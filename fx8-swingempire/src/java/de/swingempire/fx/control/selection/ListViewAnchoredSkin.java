/*
 * Created on 03.09.2014
 *
 */
package de.swingempire.fx.control.selection;

import javafx.scene.control.ListView;

/**
 * Playing with anchoredselectionModel.
 * 
 * Installs a ListViewABehaviour that relies on anchor handling by the
 * selectionModel.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ListViewAnchoredSkin<T> extends ListViewSkin<T> {

    /**
     * @param listView
     */
    public ListViewAnchoredSkin(ListView<T> listView) {
        super(listView, new ListViewABehavior<>(listView));
    }

    
}
