/*
 * Created on 03.06.2014
 *
 */
package de.swingempire.fx.scene.control;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.event.EventType;
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
 * <p>
 * Editing notes: problem is the focus of the ListView on its item type.
 * If we want to add editing per a property of the item, we have to add the
 * property type while keeping the ListCell (and all collaborators, like
 * the editCommitHandler, EditEvent ... ) focused on the item type.
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
        setOnEditCommit(e -> commitEdit(e));
    }
    

    /**
     * Callback for default commit handler, it can handle both basic 
     * EditEvents and value-editing EditXEvents.
     * 
     * PENDING JW: not really safely implemented!
     * 
     * @param e
     * @return
     */
    protected void commitEdit(EditEvent<T> e) {
        int index = e.getIndex();
        List<T> list = getItems();
        if (index < 0 || index >= list.size()) return;
        if (e instanceof EditXEvent) {
            EditXEvent<T, C> ex = (EditXEvent<T, C>) e;
            ObservableValue<C> cellValue = getCellObservableValue(index);
            if (cellValue instanceof WritableValue) {
                ((WritableValue)cellValue).setValue(ex.getNewCellValue());
            }
        } else { // default of super
            list.set(index, e.getNewValue());
        }
    }


    /** 
     * Returns the observableValue at the given index if in range,
     * or null otherwise.
     *
     * C&P from TableColumn.
     */
    public final ObservableValue<C> getCellObservableValue(int index) {
        if (index < 0 || getItems() == null) return null;
        
        if (index >= getItems().size()) return null; // Out of range
        
        final T rowData = getItems().get(index);
        return getCellObservableValue(rowData);
    }

    /** 
     * Returns the observableValue on the given item. Returns null if
     * there is no cellValueFactory or the item is null.
     * 
     * PENDING JW: who's responsible for null handling? We do it here
     * 
     * C&P from TableColumn.
     */
    public final ObservableValue<C> getCellObservableValue(T item) {
        if (item == null) return null;
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
 
    public static class EditXEvent<T, C> extends EditEvent<T> {

        private C newCellValue;
        
        /**
         * Constructor that takes a cell value in addition to the row value.
         * 
         * PENDING: specify the valid combinations of the params!
         * 
         * @param source
         * @param eventType
         * @param newValue
         * @param editIndex
         * @param newCellValue the value of a property of the item, if any
         */
        public EditXEvent(ListView<T> source,
                EventType<? extends javafx.scene.control.ListView.EditEvent<T>> eventType,
                T newValue, int editIndex, C newCellValue) {
            super(source, eventType, newValue, editIndex);
            this.newCellValue = newCellValue;
        }
        
        public final C getNewCellValue() {
            return newCellValue;
        }
        
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
