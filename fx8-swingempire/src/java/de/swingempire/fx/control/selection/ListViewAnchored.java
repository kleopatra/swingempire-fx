/*
 * Created on 03.09.2014
 *
 */
package de.swingempire.fx.control.selection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ListViewAnchored<T> extends ListView<T> {

    /**
     * 
     */
    public ListViewAnchored() {
        this(FXCollections.<T>observableArrayList());
    }

    /**
     * @param items
     */
    public ListViewAnchored(ObservableList<T> items) {
        super(items);
        setSelectionModel(new AnchoredListViewSelectionModel<>(this));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ListViewAnchoredSkin<>(this);
    }
    
    

}
