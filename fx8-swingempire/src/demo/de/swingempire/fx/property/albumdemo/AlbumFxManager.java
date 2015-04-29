/*
 * Created on 01.06.2014
 *
 */
package de.swingempire.fx.property.albumdemo;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

/**
 * PENDING JW: managing (aka: retrieving/storing items) the list of items is the task 
 * of the data layer, that is this manager. ObservableList doesn't observe mutations
 * to the underlying list, so this layer needs to keep an observableList directly.
 * That's mixing-in implementation details of the presentation laye. How to avoid? 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AlbumFxManager {

    /**
     * PENDING JW: to make this list observe its items, we have to attach the
     * extractor here (for list views).
     * 
     * Shouldn't need to, if the list would bind the elements as the tableCell
     * does. Except that we need the extractor even then, f.i. when sorting and
     * updateOnSortOrderChanged!
     */
    private ObservableList<AlbumFx> albums;
    
    public AlbumFxManager() {
        Callback<AlbumFx, Observable[]> extractor = new Callback<AlbumFx, Observable[]>() {

            @Override
            public Observable[] call(AlbumFx album) {
                return new Observable[] {album.titleProperty(), album.artistProperty(), 
                        album.composerProperty(), album.classicalProperty()};
            }};
        albums = FXCollections.observableArrayList(extractor);
        fillInitial();
    }

    public ObservableList<AlbumFx> getManagedAlbums() {
        return albums;
    }
    
    public AlbumFx createAlbum() {
        return createAlbum(null);
    }

    public void addAlbum(AlbumFx album) {
        getManagedAlbums().add(album);
    }
    
    public void deleteAlbum(AlbumFx album) {
        getManagedAlbums().remove(album);
    }
    
    private void fillInitial() {
        albums.add(createAlbum("My first title"));
        albums.add(createAlbum("Another useless title"));
        albums.add(createAlbum("Yet another ..."));
        albums.add(createAlbum("Good stuff is ... not usual"));
    }

    private AlbumFx createAlbum(String title) {
        AlbumFx album = new AlbumFx();
        album.setTitle(title);
        return album;
    }

    /**
     * for testing ...
     * @param i
     */
    public void addAlbums(int count) {
        for (int i = 0; i < count; i++) {
            albums.add(createAlbum("added " + i));
        }
    }
}
