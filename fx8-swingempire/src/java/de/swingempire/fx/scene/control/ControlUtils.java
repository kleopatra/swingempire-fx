/*
 * Created on 10.11.2014
 *
 */
package de.swingempire.fx.scene.control;

import java.util.Objects;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

/**
 * PENDING JW: unused, still favoring property access by name, old habits :-)
 */
public class ControlUtils {
    
    /**
     * Type-safe replacement of PropertyValueFactory.
     * 
     */
    public static class PropertyFactory<S, T> implements 
        Callback<CellDataFeatures<S, T>, ObservableValue<T>>{
    
        private final Function<S, ObservableValue<T>> function;
        public PropertyFactory(Function<S, ObservableValue<T>> function) {
            this.function = Objects.requireNonNull(function, "function must not be null");
        }
        @Override
        public ObservableValue<T> call(CellDataFeatures<S, T> data) {
            if (data == null || data.getValue() == null) return null;
            return function.apply(data.getValue());
        }
        
    }

    /**
     * Type-safe replacement of PropertyValueFactory.
     * 
     */
    public static class ObservableFactory<S, T> implements 
        Callback<CellDataFeatures<S, T>, ObservableValue<T>>{
        
        private final Function<S, T> function;
        public ObservableFactory(Function<S,T> method) {
            this.function = Objects.requireNonNull(method, "function must not be null");
        }
        
        @Override
        public ObservableValue<T> call(CellDataFeatures<S, T> data) {
            if (data == null || data.getValue() == null) return null;
            T result = function.apply(data.getValue());
            return new ReadOnlyObjectWrapper<T>(result);
        }
    }

    public static void requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(Control c) {
        Scene scene = c.getScene();
        final Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (focusOwner == null) {
            c.requestFocus();
        } else if (! c.equals(focusOwner)) {
            Parent p = focusOwner.getParent();
            while (p != null) {
                if (c.equals(p)) {
                    c.requestFocus();
                    break;
                }
                p = p.getParent();
            }
        }
    }

    private ControlUtils() {}

}
