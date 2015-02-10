/*
 * Created on 03.06.2014
 *
 */
package de.swingempire.fx.property;


import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;
import sun.util.logging.PlatformLogger;
import sun.util.logging.PlatformLogger.Level;

import com.sun.javafx.property.PropertyReference;
import com.sun.javafx.scene.control.Logging;


/**
 * 
 * A convenience implementation of the Callback interface, designed specifically
 * for use within controls that have cells and support binding to its values (other
 * than those already available in core). <p>
 * 
 * Note: this is basically a copy of PropertyValueFactory, slightly simplified
 * and accidental type mixing removed. 
 * 
 * @param <T> The type of the bean that contains the property, that is the items
 *   contained in the collection view.
 * @param <C> The type of the property value, that is the value shown in the cell.
 * @since JavaFX 2.0
 */
public class PropertyFactory<T,C> implements Callback<T, ObservableValue<C>> {

    private final String name;

    // JW: renamed - it's the class of the containing bean, not the column's/cell's
    private Class<?> beanClass;
    // couldn't we remove this?
    // the name is final, can't change during the lifetime 
    private String previousProperty;
    private PropertyReference<C> propertyRef;

    /**
     * Creates a default PropertyFactory to extract the value from a given
     * data item reflectively, using the given property name.
     *
     * @param property The name of the property with which to attempt to
     *      reflectively extract a corresponding value for in a given object.
     */
    public PropertyFactory(String property) {
        this.name = property;
    }

    /** {@inheritDoc} */
    @Override
    public ObservableValue<C> call(T param) {
        return getCellDataReflectively(param);
    }

    /**
     * Returns the property name provided in the constructor.
     */
    public final String getPropertyName() { return name; }

    private ObservableValue<C> getCellDataReflectively(T rowData) {
        if (getPropertyName() == null || getPropertyName().isEmpty() || rowData == null) return null;

        try {
            // we attempt to cache the property reference here, as otherwise
            // performance suffers when working in large data models. For
            // a bit of reference, refer to RT-13937.
            if (beanClass == null || previousProperty == null ||
                    ! beanClass.equals(rowData.getClass()) ||
                    ! previousProperty.equals(getPropertyName())) {

                // create a new PropertyReference
                this.beanClass = rowData.getClass();
                this.previousProperty = getPropertyName();
                this.propertyRef = new PropertyReference<C>(rowData.getClass(), getPropertyName());
            }

            if (propertyRef.hasProperty()) {
                return propertyRef.getProperty(rowData);
            } else {
                C value = propertyRef.get(rowData);
                return new ReadOnlyObjectWrapper<C>(value);
            }
        } catch (IllegalStateException e) {
            // log the warning and move on
            final PlatformLogger logger = Logging.getControlsLogger();
            if (logger.isLoggable(Level.WARNING)) {
               logger.finest("Can not retrieve property '" + getPropertyName() +
                        "' in PropertyValueFactory: " + this +
                        " with provided class type: " + rowData.getClass(), e);
            }
        }

        return null;
    }
}
