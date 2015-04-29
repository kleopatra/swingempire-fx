/*
 * Created on 22.05.2014
 *
 */
package de.swingempire.fx.property.albumdemo;

import java.util.logging.Logger;

import de.swingempire.fx.property.PresentationModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class AlbumFxView {

    private ObjectProperty<AlbumFxModel> albumModel;
    private TextField titleField;
    private TextField composerField;
    private CheckBox classicalBox;
    private Button applyButton;
    private Button cancelButton;
    private VBox content;
    private BooleanProperty disabled;
    
    public AlbumFxView() {
        this(null);
    }
    
    public AlbumFxView(AlbumFxModel model) {
        initComponents();
        initProperties();
        setAlbumModel(model);
    }

    /**
     * 
     */
    private void initProperties() {
        disabled = new SimpleBooleanProperty(this, "disabled");
        disabled.set(true);
        albumModel = new SimpleObjectProperty<AlbumFxModel>(this, "albumModel", null) {

            PresentationModel<AlbumFx> oldValue = get();
            @Override
            protected void invalidated() {
                unbindComponents(oldValue);
                bindComponents();
                oldValue = get();
            }
            
            
        };
//        BooleanBinding noModel = Bindings.isNull(albumModel);
//        BooleanBinding noBean = Bindings.isNull(albumModel.get().beanProperty());
//        disabledProperty = new BooleanBinding() {
//
//            @Override
//            protected boolean computeValue() {
//                return albumModel.get() == null || albumModel.getBean() == null;
//            }
//            
//        };
    }

    /**
     * 
     */
    protected void bindComponents() {
        AlbumFxModel current = getAlbumModel();
        if (current != null) {
            titleField.textProperty().bindBidirectional(current.getBufferedProperty("title"));
            // PENDING: if no Album bound, the value of the buffered property is null
            // which the boolean property can't cope with?
            classicalBox.selectedProperty().bindBidirectional(current.getBufferedProperty("classical"));
            composerField.textProperty().bindBidirectional(current.getBufferedProperty("composer"));
            composerField.disableProperty().bind(current.composerDisabledProperty());
            
            applyButton.disableProperty().bind(current.commitDisabledProperty());
            cancelButton.disableProperty().bind(current.flushDisabledProperty());
            
            applyButton.setOnAction(event -> current.commit());
            cancelButton.setOnAction(event -> current.flush());
            
            disabled.bind(Bindings.not(current.editableProperty()));
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
        composerField.setDisable(true);
        applyButton.setOnAction(null);
        cancelButton.setOnAction(null);

        disabled.set(true);
        
    }

    protected void unbindComponents(PresentationModel<AlbumFx> oldAlbum) {
        if (oldAlbum != null) {
            titleField.textProperty().unbindBidirectional(oldAlbum.getBufferedProperty("title"));
            classicalBox.selectedProperty().unbindBidirectional(oldAlbum.getBufferedProperty("classical"));
            composerField.textProperty().unbindBidirectional(oldAlbum.getBufferedProperty("composer"));
            composerField.disableProperty().unbind();
            applyButton.disableProperty().unbind();
            cancelButton.disableProperty().unbind();
            
            disabled.unbind();
        }
    }
    public Region getContent() {
        if (content == null) {
            content = new VBox();
            content.getChildren().addAll(titleField, classicalBox, composerField, applyButton, cancelButton);
            content.disableProperty().bind(disabled);
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
    
    public final AlbumFxModel getAlbumModel() {
        return albumModelProperty().get();
    }
    
    public final void setAlbumModel(AlbumFxModel album) {
        albumModelProperty().set(album);
    }
    
    public ObjectProperty<AlbumFxModel> albumModelProperty() {
        return albumModel;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(AlbumFxForm.class
            .getName());
}
