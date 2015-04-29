/*
 * Created on 22.05.2014
 *
 */
package de.swingempire.fx.property.albumdemo;

import java.util.logging.Logger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class AlbumFxForm {

    private ObjectProperty<AlbumFx> album;
    private TextField titleField;
    private TextField composerField;
    private CheckBox classicalBox;
    private Button applyButton;
    private Button cancelButton;
    private VBox content;
    
    public AlbumFxForm() {
        initComponents();
        initProperties();
        resetComponents();
    }

    /**
     * 
     */
    private void initProperties() {
        TextField f;
        album = new SimpleObjectProperty<AlbumFx>(this, "album", null) {

            AlbumFx oldValue = get();
            @Override
            protected void invalidated() {
                unbindComponents(oldValue);
                bindComponents();
                oldValue = get();
            }
            
            
        };
    }

    /**
     * 
     */
    protected void bindComponents() {
        AlbumFx current = getAlbum();
        if (current != null) {
            titleField.textProperty().bindBidirectional(current.titleProperty());
            classicalBox.selectedProperty().bindBidirectional(current.classicalProperty());
            composerField.textProperty().bindBidirectional(current.composerProperty());
            composerField.editableProperty().bind(current.classicalProperty());
        } else {
            // Note: even if unbound, the components keep their previous
            // state, so have to reset explicitly!
            resetComponents();
        }
    }

    protected void resetComponents() {
        titleField.setText(null);
        classicalBox.setSelected(false);
        composerField.setText(null);
        composerField.setEditable(false);
    }

    protected void unbindComponents(AlbumFx oldAlbum) {
        if (oldAlbum != null) {
            titleField.textProperty().unbindBidirectional(oldAlbum.titleProperty());
            classicalBox.selectedProperty().unbindBidirectional(oldAlbum.classicalProperty());
            composerField.textProperty().unbindBidirectional(oldAlbum.composerProperty());
            composerField.editableProperty().unbind();
        }
    }
    public Region getContent() {
        if (content == null) {
            content = new VBox();
            content.getChildren().addAll(titleField, classicalBox, composerField, applyButton, cancelButton);
        }
        return content;
    }
    /**
     * 
     */
    private void initComponents() {
        titleField = new TextField();
        composerField = new TextField();
        classicalBox = new CheckBox("classical");
        
        applyButton = new Button("Apply");
        cancelButton = new Button("Cancel");
    }
    
// ---------------- property boilderplate
    
    public final AlbumFx getAlbum() {
        return albumProperty().get();
    }
    
    public final void setAlbum(AlbumFx album) {
        albumProperty().set(album);
    }
    
    public ObjectProperty<AlbumFx> albumProperty() {
        return album;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(AlbumFxForm.class
            .getName());
}
