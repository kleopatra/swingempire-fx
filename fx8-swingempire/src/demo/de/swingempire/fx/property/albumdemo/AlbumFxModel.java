/*
 * Created on 25.05.2014
 *
 */
package de.swingempire.fx.property.albumdemo;

import java.util.logging.Logger;

import de.swingempire.fx.property.PresentationModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableBooleanValue;

/**
 * TODO
 * - implement property to disable whole content (if null album)
 * - batch boolean binding which is dynamic (to support lazy property creation)
 * 
 * DONE:
 * - implement reflexive property lookup (can use propertyReference)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AlbumFxModel extends PresentationModel<AlbumFx>{

    private ObservableBooleanValue composerDisabled;
    private ObservableBooleanValue editable;

    //    private 
    public AlbumFxModel() {
        super(AlbumFx.class);
        initProperties();
//        Bindings b;
//        PropertyDescriptor d;
//        PropertyValueFactory f;
//        TableColumn t;
    }

    /**
     * 
     * 
     */
    private void initProperties() {
        initBufferedProperty("artist", String.class);
        initBufferedProperty("title", String.class);
        // PENDING: here we rely on sequence of change notification:
        // the composer is internally nulled if not classical
        // so the boolean must be committed before the composer 
        // HMMMM... how does goodies handle such dependencies?
        initBufferedProperty("classical", Boolean.class);
        initBufferedProperty("composer", String.class);
        
        initBeanPropertyReferences();
        
        // with BufferedObjectProperty generically typed, we can use the 
        // wrapper method of the natively typed properties and use that 
        // with the Bindings methods
        // Note: this wrapper installs a bidi-binding which calls the buffer's
        // set method which put it into buffering mode!
        BooleanProperty wrapper = BooleanProperty.booleanProperty(
                getBufferedProperty("classical", Boolean.class));
        composerDisabled = Bindings.not(wrapper);
        editable = Bindings.isNotNull(beanProperty());
    }
    
    protected void initBeanPropertyReferences() {
        addReference("artist", String.class);
        addReference("composer", String.class);
        addReference("title", String.class);
        addReference("classical", Boolean.class);
    }

    public final boolean isComposerDisabled() {
        return composerDisabledProperty().get();
    }
    
    public ObservableBooleanValue composerDisabledProperty() {
        return composerDisabled;
    }
    
    public final boolean isEditable() {
        return editableProperty().get();
        
    }
    /**
     * @return
     */
    public ObservableBooleanValue editableProperty() {
        return editable;
    }
    //----------------- debug helper   
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(AlbumFxModel.class
            .getName());
}
