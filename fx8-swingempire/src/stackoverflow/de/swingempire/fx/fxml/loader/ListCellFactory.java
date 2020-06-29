/*
 * Created on 29.06.2020
 *
 */
package de.swingempire.fx.fxml.loader;

import java.util.Objects;
import java.util.function.Function;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
/**
 * https://stackoverflow.com/q/44321738/203657
 * 
 * loading data into a combo via fxml.
 */
public class ListCellFactory<T> {
    
    private Function<T, String> textProvider;

    public ListCellFactory(Function<T, String> provider) {
        this.textProvider = provider;
    }

    public Callback<ListView<T>, ListCell<T>> getCellFactory() {
        return cc -> new CListCell<>(textProvider);
    }
    
    public ListCell<T> getButtonCell() {
        return getCellFactory().call(null);
    }
    
    public static class CListCell<T> extends ListCell<T> {
        
        private Function<T, String> converter;

        public CListCell(Function<T, String> converter) {
            this.converter = Objects.requireNonNull(converter, "converter must not be null");
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
            } else {
                setText(converter.apply(item));
            }
        }
        
    }

}
