/*
 * Created on 03.06.2014
 *
 */
package de.swingempire.fx.scene.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * ListView which supports cellValueFactory (similar to TableColumn).
 * Requires a ListXCell to make the mechanism work. <p>
 * 
 * PENDING
 * <li> handle editing
 * <li> handle usage without cell factory
 * 
 * @param <T> the type of the data item
 * @param <C> the type of the cell (may be same or different from T)
 * 
 * @author Jeanette Winzenburg, Berlin
 * 
 */
public class ListXView<T, C> extends ListView<T> {
    
    
    public ListXView() {
        super();
        cellValueFactory = new SimpleObjectProperty<>(this, "cellValueFactory");
        setCellFactory(v -> new ListXCell<>());
    }
    

    /** {@inheritDoc} */
    public final ObservableValue<C> getCellObservableValue(int index) {
        if (index < 0 || getItems() == null) return null;
        
        if (index >= getItems().size()) return null; // Out of range
        
        final T rowData = getItems().get(index);
        return getCellObservableValue(rowData);
    }

    /** {@inheritDoc} */
    public final ObservableValue<C> getCellObservableValue(T item) {
        // Get the factory
        final Callback<T, ObservableValue<C>> factory = getCellValueFactory();
        if (factory == null) return null;
        
        // Call the factory
        return factory.call(item);
        
        // (mis-)using PropertyValueFactory of tableColumn 
//        final Callback<CellDataFeatures<T, C>, ObservableValue<C>> factory = getCellValueFactory();
//        if (factory == null) return null;
//        final CellDataFeatures<T, C> cdf = new CellDataFeatures<T, C>(null, null, item);
//        return factory.call(cdf);
    }

    
//-------- property boilderplate PropertyValueFactory
    
    private ObjectProperty<Callback<T, ObservableValue<C>>> cellValueFactory;
    public final void setCellValueFactory(Callback<T, ObservableValue<C>> factory) {
        cellValueFactoryProperty().set(factory);
    }
    
    public final Callback<T, ObservableValue<C>> getCellValueFactory() {
        return cellValueFactoryProperty().get();
    }
    
    /**
     * Returns the property holding the cellValueFactory. Note that here we use
     * PropertyFactory, it's basically c&p of core PropertyValueFactory, leaving
     * out the table-specifics.
     * 
     * @return
     */
    public ObjectProperty<Callback<T, ObservableValue<C>>> cellValueFactoryProperty() {
        if (cellValueFactory == null) {
            cellValueFactory = new SimpleObjectProperty<>(this, "cellValueFactory");
        }
        return cellValueFactory;
    }
 
//------------------------ boilerplate cellvalueFactory: use core PropertyValueFactory    
    
//    private ObjectProperty<Callback<CellDataFeatures<T, C>, ObservableValue<C>>> cellValueFactory;
//    public final void setCellValueFactory(Callback<CellDataFeatures<T, C>, ObservableValue<C>> factory) {
//        cellValueFactoryProperty().set(factory);
//    }
//    
//    public final Callback<CellDataFeatures<T, C>, ObservableValue<C>> getCellValueFactory() {
//        return cellValueFactoryProperty().get();
//    }
//    
//    /**
//     * Returns the property holding the cellValueFactory. Note that here we use
//     * core PropertyValueFactory (just for reducing the impact of this tweak).
//     * 
//     * @return
//     */
//    public ObjectProperty<Callback<CellDataFeatures<T, C>, ObservableValue<C>>> cellValueFactoryProperty() {
//        if (cellValueFactory == null) {
//            cellValueFactory = new SimpleObjectProperty<>(this, "cellValueFactory");
//        }
//        return cellValueFactory;
//    }
}
