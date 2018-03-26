/*
 * Created on 21.05.2014
 *
 */
package de.swingempire.fx.property.albumdemo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Implement properties that are:
 * 
 * - dependent on each other (here: composer must be null if !classical)
 * - have constraints (here: setting the composer must guarantee the null if !classical)
 * 
 * Approaches:
 * 
 * for the dependency:
 *  in classical invalidated, call a bean method on the bean to update the composer if necessary
 * 
 * for constraints: 
 * in composer set (??), replace the new value by null if !classical. Problem: how to handle a bound
 * value? Deeper question: who is responsible for keeping bound values clean? We either break the
 * internal constraint (not acceptable) or the binding constraint (not acceptable)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AlbumFx {

    private StringProperty artist;
    private StringProperty title;
    private BooleanProperty classical;
    private StringProperty composer;
    
    public AlbumFx() {
        initProperties();
    }

    /**
     * 
     */
    private void initProperties() {
        title = new SimpleStringProperty(this, "title", null);
        artist = new SimpleStringProperty(this, "artist", null);
        classical = new SimpleBooleanProperty(this, "classical", false) {

            @Override
            protected void invalidated() {
                updateComposerFromClassical();
            }
            
        };
        composer = new SimpleStringProperty(this, "composer", null) {

            @Override
            protected void invalidated() {
                // this is bean logic, shouldn't be here
                if (!isClassical() && get() != null) {
                    // this will break if bound
                    set(null);
                }
            }
            
        };
    }

    /**
     * 
     */
    protected void updateComposerFromClassical() {
        if (!isClassical()) {
            setComposer(null);
        }
    }
    
//---------- property boiler-plate
    
    public final String getComposer() {
        return composerProperty().get();
    }
    
    public final void setComposer(String composer) {
        composerProperty().set(composer);
    }
    
    public StringProperty composerProperty() {
        return composer;
    }
    
    public final boolean isClassical() {
        return classicalProperty().get();
    }
    
    public final void setClassical(boolean classical) {
        classicalProperty().set(classical);
    }
    
    public BooleanProperty classicalProperty() {
        return classical;
    }
    
    public final String getTitle() {
        return titleProperty().get();
    }
    
    public final void setTitle(String title) {
        titleProperty().set(title);
    }
    
    public StringProperty titleProperty() {
        return title;
    }

    public final String getArtist() {
        return artistProperty().get();
    }
    
    public final void setArtist(String artist) {
        artistProperty().set(artist);
    }
    /**
     * @return
     */
    public StringProperty artistProperty() {
        return artist;
    }

    @Override
    public String toString() {
        return getTitle() + " / " + getComposer();
    }
    
//-------- misc
    
    
}
