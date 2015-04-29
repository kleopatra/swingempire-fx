/*
 * Created on 01.06.2014
 *
 */
package de.swingempire.fx.property.albumdemo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class AlbumFxManagerModel {

    private AlbumFxManager manager;
    private ObjectProperty<AlbumFxModel> current;
    
    public AlbumFxManagerModel(AlbumFxManager manager) {
        this.manager = manager;
        current = new SimpleObjectProperty<AlbumFxModel>(this, "currentAlbum", new AlbumFxModel());
    }
    
    public ObservableList<AlbumFx> getManagedAlbums() {
        return manager.getManagedAlbums();
    }

    /**
     * @return
     */
    public AlbumFxModel getAlbumFxModel() {
        return current.get();
    }

    /**
     * @param item
     * @return
     */
    public String getText(AlbumFx item) {
        return item.getTitle();
    }
    
    
}
