/*
 * Created on 05.08.2017
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import de.swingempire.fx.scene.control.skin.impl.SkinBaseDecorator;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ListViewScratchSkin<T> 
    extends VirtualContainerBase<ListView<T>, ListCell<T>> 
    implements SkinBaseDecorator {

    /**
     * @param control
     * @param behavior
     */
    public ListViewScratchSkin(ListView<T> control, Object behavior) {
        super(control, behavior);
    }

    @Override
    protected int getItemCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void updateItemCount() {
        // TODO Auto-generated method stub

    }

}
