/*
 * Created on 03.09.2014
 *
 */
package de.swingempire.fx.control.selection;

import javafx.scene.control.ListView;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ListViewAnchoredBehavior<T> extends ListViewBehavior<T> {

    /**
     * @param control
     */
    public ListViewAnchoredBehavior(ListView<T> control) {
        super(control);
    }

    /**
     * Overridden to do nothing.
     * 
     * Super installs listeners on items property/its list changes and on selectionModel
     * property/ its list changes. All they are doing is to update the anchor, which 
     * will be handled by the model.
     * 
     */
    @Override
    protected void installListeners(ListView<T> control) {
    }
    
    

}
