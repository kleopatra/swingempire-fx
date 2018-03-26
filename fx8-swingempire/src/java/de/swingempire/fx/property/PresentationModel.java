/*
 * Created on 30.05.2014
 *
 */
package de.swingempire.fx.property;

import com.sun.javafx.property.PropertyReference;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class PresentationModel<B> {

    private ObjectProperty<B> bean;
    private ObservableMap<String, BufferedObjectProperty> bufferedProperties;
    private ObservableMap<BufferedObjectProperty, Class<?>> bufferedTypes;
    private ObservableMap<String, PropertyReference> propertyReferences;
    private Trigger trigger;
    private Class<B> beanClass;
    
    protected CollectionOrBinding buffering;
    protected ObservableBooleanValue commitDisabled;
    protected ObservableBooleanValue flushDisabled;

    public PresentationModel(Class<B> beanClass) {
        this.beanClass = beanClass;
        initMaps();
        trigger = new Trigger();
        createBufferingBinding();
        bean = new SimpleObjectProperty<B>(this, "bean", null) {

            @Override
            protected void invalidated() {
                updateSubjects();
            }
            
        };

    }
    
    public void commit() {
        trigger.triggerCommit();
    }

    public void flush() {
        trigger.triggerFlush();
    }

    public BufferedObjectProperty getBufferedProperty(String name) {
        return bufferedProperties.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> BufferedObjectProperty<T> getBufferedProperty(String name, Class<T> type) {
        BufferedObjectProperty<?> property = getBufferedProperty(name);
        Class<?> clazz = bufferedTypes.get(property); 
        if (type.equals(clazz)) {
            return (BufferedObjectProperty<T>) property;
        }
        return null;
    }

 // subclass and internal access 
    
    protected Trigger getTrigger() {
        return trigger;
    }

    protected Class<B> getBeanClass() {
        return beanClass;
    }

    protected <T> void addReference(String name, Class<T> clazz) {
        propertyReferences.put(name, new PropertyReference<T>(getBeanClass(), name));
    }

    protected void updateSubjects() {
        B albumFx = getBean();
        if (albumFx == null) {
            updateWithNull();
            return;
        }
        
        bufferedProperties.forEach((key, buffer) -> { 
            PropertyReference subjectProperty = propertyReferences.get(key);
            Property property = (Property) subjectProperty.getProperty(albumFx);
            buffer.setSubject(property);
        });
    }

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    protected void updateWithNull() {
        bufferedProperties.forEach((key, buffer) -> buffer.setSubject(null));
    }

    /**
     * Creates and installs a buffered property with the given name and type.
     * 
     * @param name the name of the property.
     * @param clazz = the type of the property
     * @return the installed buffered property.
     */
    protected <T> BufferedObjectProperty<T> initBufferedProperty(String name, Class<T> clazz) {
        T defaultValue = null;
        if (clazz == Boolean.class) {
            defaultValue = (T) Boolean.FALSE;
        }
        BufferedObjectProperty<T> buffer = new BufferedObjectProperty<T>(name, trigger, defaultValue);
        bufferedTypes.put(buffer, clazz);
        buffering.addDependencies(buffer);
        bufferedProperties.put(name, buffer);
        return buffer;
    }

    protected void initMaps() {
        bufferedProperties = FXCollections.observableHashMap(); //  new HashMap<>();
        bufferedTypes = FXCollections.observableHashMap(); //new HashMap<>();
        propertyReferences = FXCollections.observableHashMap(); //new HashMap<>();
    }

    /**
     * Binds the buffering property (and bindings that depend on the buffering)
     * to the buffering of the buffers.
     * 
     */
    private void createBufferingBinding() {
        // buffering = new BooleanOrBinding();
        buffering = new CollectionOrBinding();
        commitDisabled = Bindings.not(buffering);
        flushDisabled = Bindings.not(buffering);
    }

//-------------- property boilderplate    
    public final B getBean() {
        return beanProperty().get();
    }

    public final void setBean(B bean) {
        beanProperty().set(bean);
    }

    public ObjectProperty<B> beanProperty() {
        return bean;
    }

    public boolean isBuffering() {
        return buffering.get();
    }

    public ObservableBooleanValue bufferingProperty() {
        return buffering;
    }

    public final boolean isCommitDisabled() {
        return commitDisabledProperty().get();
    }

    public ObservableBooleanValue commitDisabledProperty() {
        return commitDisabled;
    }

    public final boolean isFlushDisabled() {
        return flushDisabledProperty().get();
    }

    public ObservableBooleanValue flushDisabledProperty() {
        return flushDisabled;
    }

    /**
     * Here we use another trick: 
     * bind to the collection/map that contains
     * the observable we'll bind later on. Adding items will
     * invalidate the collection which in turn invalidates the
     * binding. It's a bit brittle, as the invalidation happens
     * before the added item is bound here. Will work though, 
     * as long as the added item doesn't change between between
     * adding to the list and adding as dependency here.
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    protected class CollectionOrBinding extends BooleanBinding {
    
        public CollectionOrBinding() {
            bind(bufferedProperties);
        }
        
        /**
         * Binds additional dependencies. The dependencies must be 
         * added to the collection/map that serves as
         * the invalidation trigger, either immediately before of
         * after adding here.
         * 
         * THINK: immediately after will ensure the invalidation
         * happen after it's bound here?
         * 
         * @param dependencies
         */
        public void addDependencies(Observable... dependencies) {
            if (dependencies != null && dependencies.length > 0) {
                bind(dependencies);
            }
        }
        @Override
        protected boolean computeValue() {
            for (BufferedObjectProperty<?> property : bufferedProperties.values()) {
                if (property.isBuffering()) return true;
            }
            return false;
        }
        
    }

    /**
     * The boolean binding isn't meant for dynamically changing the dependencies.
     * It has no means to invalidate programmatically in its lifetime (
     * except when any of its dependencies is invalidated), other than that it's invalid at 
     * instantiation only.
     * 
     * The way out is to make a dependency fire:
     * - a dynabinding has a dummy property which exposes its fireEvent 
     *   (will work with any collection or internally added and otherwise
     *   unrelated dependencies)
     * - a collectionBinding binds to the collection that it computes 
     *   its value from (for observable collections)
     */
    protected class BooleanOrBinding extends DynamicBooleanBinding {
        
        @Override
        protected boolean computeValue() {
            for (BufferedObjectProperty<?> property : bufferedProperties.values()) {
                if (property.isBuffering()) return true;
            }
            return false;
        }
        
    }
}
