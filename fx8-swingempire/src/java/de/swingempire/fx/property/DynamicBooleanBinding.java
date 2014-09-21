/*
 * Created on 30.05.2014
 *
 */
package de.swingempire.fx.property;

import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;

/**
 * A boolean binding that supports adding observables.
 * 
 * This is a trick: Bindings are designed for static dependencies,
 * there's no way to make it invalid except indirectly through
 * a change in a dependency.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class DynamicBooleanBinding extends BooleanBinding {

    BooleanChangeMarker marker;
    
    public DynamicBooleanBinding() {
        marker = new BooleanChangeMarker();
        bind(marker);
    }
    
    public void addDependencies(Observable... dependencies) {
        if (dependencies != null && dependencies.length > 0) {
            bind(dependencies);
            marker.fireValueChangedEvent();
        }
    }
//    @Override
//    protected boolean computeValue() {
//        return false;
//    }

}
