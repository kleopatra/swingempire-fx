/*
 * Created on 21.03.2018
 *
 */
package de.swingempire.fx.control;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static javafx.collections.FXCollections.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
/**
 * 
 * https://stackoverflow.com/q/49388232/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class CombosWithCategories extends Application {
    
    public interface RelationProvider<T> {
        default ObservableList<T> getRelations() {
            return emptyObservableList();
        };
    }
    
    /**
     * A model that manages a list of RelationProviders and has the notion
     * of a current relationProvider with relations (it's a kind-of selectionModel).
     * 
     * <T> the type of elements in the list of relations 
     */
    public static class RelationModel<T> {
        
        /**
         * all relationProviders managed by this model
         */
        private ListProperty<RelationProvider<T>> relationProviders;
        /**
         * The owner of the relations. Must be contained in the providers managed
         * by this model.
         */
        private ObjectProperty<RelationProvider<T>> relationProvider;
        private ListProperty<T> relations;
        
        public RelationModel() {
            initProperties();
        }
        
        /**
         * The RelationProviders managed by the model.
         */
        public ListProperty<RelationProvider<T>> relationProvidersProperty() {
            return relationProviders;
        }

        /**
         * The RelationProvider that manages the current relations.
         */
        public ObjectProperty<RelationProvider<T>> relationProviderProperty() {
            return relationProvider;
        }
        
        public RelationProvider<T> getRelationProvider() {
            return relationProviderProperty().get();
        }
        
        public ListProperty<T> relations() {
            return relations;
        }
        
        /**
         * Callback from invalidation of current relationProvider.
         * Implemented to update relations.
         */
        protected void relationProviderInvalidated() {
            RelationProvider<T> value = getRelationProvider();
            relations().set(value != null ? value.getRelations() : emptyObservableList());
        }
        
        /**
         * Creates and wires all properties.
         */
        private void initProperties() {
            relationProviders = new SimpleListProperty<>(this, "relationProviders", observableArrayList());
            relationProvider = new SimpleObjectProperty<>(this, "relationProvider") {
                
                @Override
                protected void invalidated() {
                    // todo: don't accept providers that are not in the list
                    relationProviderInvalidated();
                }
                
            };
            relations = new SimpleListProperty<>(this, "relations");
            relationProviderInvalidated();
            
        }
        
    }
    
    /**
     * Implement the ui against a RelationModel. Here we create
     * the same UI with a model backed by enums or a Map, respectively
     */
    private Parent createContent() {
        TabPane tabPane = new TabPane(
                new Tab("Enums", createRelationUI(createEnumRelationModel())),
                new Tab("Manual map", createRelationUI(createMapRelationModel()))
                );
        
        return new BorderPane(tabPane);
    }

    /**
     * Common factory for UI: creates and returns a Parent that
     * contains two combo's configured to use the model.
     */
    protected <T> Parent createRelationUI(RelationModel<T> model) {
        ComboBox<RelationProvider<T>> providers = new ComboBox<>();
        providers.itemsProperty().bind(model.relationProvidersProperty());
        providers.valueProperty().bindBidirectional(model.relationProviderProperty());
        
        ComboBox<T> relations = new ComboBox<>();
        relations.itemsProperty().bind(model.relations());
        relations.valueProperty().addListener((src, ov, nv) -> {
            LOG.info("relation changed: " + nv); 
        });
        
        return new VBox(10, providers, relations);
    }
    

    // ------------- manual with maps
    
    /**
     * On-the-fly creation of a RelationModel using a backing map.
     */
    protected RelationModel<String> createMapRelationModel() {
        RelationModel<String> model = new RelationModel<>();
        Map<String, ObservableList<String>> data = new HashMap<>();
        data.put("EUROPE", observableArrayList("GERMANY", "FRANCE"));
        data.put("AMERICA", observableArrayList("MEXICO", "USA"));
        for (String key: data.keySet()) {
            model.relationProvidersProperty().add(new RelationProvider<String>() {

                @Override
                public ObservableList<String> getRelations() {
                    return data.get(key);
                }

                @Override
                public String toString() {
                    return key;
                }
                
                
            });
        }
        return model;
    }
    //-------------------- enum
    /**
     * RelationModel using Enums.
     */
    protected RelationModel<Object> createEnumRelationModel() {
        RelationModel<Object> model = new RelationModel<Object>();
        model.relationProvidersProperty().setAll(Continent.values());
        return model;
    }

    public enum EuropeanCountry {
        FRANCE, GERMANY;
    }
    
    public enum AmericanCountry {
        MEXICO, CANADA, USA;
    }
    
    public enum Continent implements RelationProvider<Object> {
        AMERICA(AmericanCountry.values()),
        EUROPE(EuropeanCountry.values())
        ;
        
        ObservableList<Object> subs;
        private Continent(Object[] subs) {
            this.subs = FXCollections.observableArrayList(subs);
        }
        @Override
        public ObservableList<Object> getRelations() {
            return FXCollections.unmodifiableObservableList(subs);
        }
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(CombosWithCategories.class.getName());

}
