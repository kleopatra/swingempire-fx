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
     * A model that manages a list of categories and has the notion
     * of a current category with subCategories (it's a kind-of selectionModel)
     */
    public static class RelationModel<T> {
        
        private ListProperty<RelationProvider<T>> relationProviders;
        
        private ObjectProperty<RelationProvider<T>> currentRelationProvider;
        private ListProperty<T> currentRelations;
        
        /**
         * The list of categories managed by the model.
         */
        public ListProperty<RelationProvider<T>> providersProperty() {
            if (relationProviders == null) {
                // TODO: need to update current and sub on change
                relationProviders = new SimpleListProperty<>(this, "providers", observableArrayList());
            }
            return relationProviders;
        }

        /**
         * The current category.
         */
        public ObjectProperty<RelationProvider<T>> currentProviderProperty() {
            if (currentRelationProvider == null) {
                currentRelationProvider = new SimpleObjectProperty<>(this, "currentProvider") {

                    @Override
                    protected void invalidated() {
                        currentProviderInvalidated();
                    }
                    
                };
            }
            return currentRelationProvider;
        }
        

        public ListProperty<T> currentRelations() {
            if (currentRelations == null) {
                currentRelations = new SimpleListProperty<>(this, "currentRelations");
                currentProviderInvalidated();
            }
            return currentRelations;
        }
        
        protected void currentProviderInvalidated() {
            RelationProvider<T> value = getCurrentProvider();
            currentRelations().set(value != null ? value.getRelations() : emptyObservableList());
        }
        
        public RelationProvider<T> getCurrentProvider() {
            return currentProviderProperty().get();
        }
        
    }
    
    /**
     * Implement the ui against a CategoryModel. In this example it's 
     * configured to the show Continents which are enums.
     */
    private Parent createContent() {
        TabPane tabPane = new TabPane(
                new Tab("Enums", createRelationUI(createEnumRelationModel())),
                new Tab("Manual map", createRelationUI(createMapRelationModel()))
                );
        
        return tabPane;
    }

    /**
     * Common factory for UI: creates and returns a Parent that
     * contains two combo's configured to use the model.
     */
    protected <T> Parent createRelationUI(RelationModel<T> model) {
        ComboBox<RelationProvider<T>> providers = new ComboBox<>();
        providers.itemsProperty().bind(model.providersProperty());
        providers.valueProperty().bindBidirectional(model.currentProviderProperty());
        
        ComboBox<T> relations = new ComboBox<>();
        relations.itemsProperty().bind(model.currentRelations());
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
            model.providersProperty().add(new RelationProvider<String>() {

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
        model.providersProperty().setAll(Continent.values());
        return model;
    }

    /**
     * Example: Use a bunch of enums that implement Category
     */
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
