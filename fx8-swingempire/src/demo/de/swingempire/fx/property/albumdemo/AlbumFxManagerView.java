/*
 * Created on 01.06.2014
 *
 */
package de.swingempire.fx.property.albumdemo;

import java.util.logging.Logger;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Callback;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class AlbumFxManagerView {

    private ObjectProperty<AlbumFxManagerModel> manager;
//    private AlbumFxManagerModel manager;
    private ListView<AlbumFx> listView;
    private AlbumFxView detailsView;
    private Button createButton;
    private Button deleteButton;
    private HBox content;
    
    public AlbumFxManagerView() {
        this(null);
    }
    public AlbumFxManagerView(AlbumFxManagerModel manager) {
        initComponents();
        initProperties();
        setModel(manager);
    }

    /**
     * 
     */
    private void initProperties() {
        manager = new SimpleObjectProperty<AlbumFxManagerModel>(this, "model") {
            AlbumFxManagerModel old = get();

            @Override
            protected void invalidated() {
                unbindComponents(old);
                bindComponents();
                old = get();
            }
            
            
        };
        
        MultipleSelectionModel<AlbumFx> selectionModel = listView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        // can't bind directly, as we want to either set the bean or
        // prevent switching the selection away from the current bean,
        // based on buffering state
//        model.beanProperty().bind(selectionModel.selectedItemProperty());
        // PENDING JW: it's fishy because we are violating the basic
        // rule to not change the caller in a callback ...
        InvalidationListener l = observable -> {
            if (getModel() == null) return;
            AlbumFxModel model = getModel().getAlbumFxModel();
            if (model.isBuffering()) {
                selectionModel.select(model.getBean());
            } else {
                model.setBean(selectionModel.getSelectedItem());
            }
        };
        selectionModel.selectedItemProperty().addListener(l);
        
        Callback<ListView<AlbumFx>, ListCell<AlbumFx>> factory = 
                new Callback<ListView<AlbumFx>, ListCell<AlbumFx>>() {

            @Override
            public ListCell<AlbumFx> call(ListView<AlbumFx> view) {
                ListCell<AlbumFx> cell =  new ListCell<AlbumFx>() {
                    @Override 
                    protected void updateItem(AlbumFx item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            /**
                             * This label is used if the item associated with this cell is to be
                             * represented as a String. While we will lazily instantiate it
                             * we never clear it, being more afraid of object churn than a minor
                             * "leak" (which will not become a "major" leak).
                             */
                            setText(getModel().getText(item));
                            setGraphic(null);
                        }
                    }
                };
                return cell;
            }
         };
        listView.setCellFactory(factory );
//        listView.setCellValueFactory(new PropertyValueFactory<String, AlbumFx>("title"));
        
    }

    
    /**
     * 
     */
    protected void bindComponents() {
        if (getModel() == null) {
            resetComponents();
        } else {
            detailsView.setAlbumModel(getModel().getAlbumFxModel());
            listView.setItems(getModel().getManagedAlbums());
        }
    }
    /**
     * 
     */
    private void resetComponents() {
        detailsView.setAlbumModel(null);
        listView.setItems(null);
        
    }
    /**
     * @param old
     */
    protected void unbindComponents(AlbumFxManagerModel old) {
        
    }
    /**
     * 
     */
    private void initComponents() {
        listView = new ListView<>();
        detailsView = new AlbumFxView();
        createButton = new Button("New");
        deleteButton = new Button("Delete");
    }

    public Region getContent() {
        if (content == null) {
            BorderPane overview = new BorderPane();
            overview.setCenter(listView);
            Pane buttons = new HBox();
            buttons.getChildren().addAll(createButton, deleteButton);
            overview.setBottom(buttons);
            content = new HBox();
            content.getChildren().addAll(overview, detailsView.getContent());
        }
        return content;
    }
    
//------------ property boilerplate
    
    public AlbumFxManagerModel getModel() {
        return modelProperty().get();
    }
    public final void setModel(AlbumFxManagerModel model) {
        modelProperty().set(model);
    }
    public ObjectProperty<AlbumFxManagerModel> modelProperty() {
        return manager;
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(AlbumFxManagerView.class
            .getName());
}
