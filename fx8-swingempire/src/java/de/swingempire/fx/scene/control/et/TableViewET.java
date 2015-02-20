/*
 * Created on 20.02.2015
 *
 */
package de.swingempire.fx.scene.control.et;

import javafx.collections.ObservableList;
import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;

/**
 * A TableView with extended event dispatching.<p>
 * 
 * Allows the skin to hook into the event dispatch chain, provided
 * it is of type EventTarget (which is created by default for this view).
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewET<S> extends TableView<S> {

    /**
     * Overridden to allow the skin hook into the dispatch chain before
     * calling super, if the skin is of type EventTarget. 
     */
    @Override
    public EventDispatchChain buildEventDispatchChain(
            EventDispatchChain tail) {
        if (getSkin() instanceof EventTarget) {
            ((EventTarget) getSkin()).buildEventDispatchChain(tail);
        }
        return super.buildEventDispatchChain(tail);
    }

    
    /**
     * Overridden to create and return a TableViewSkin
     * which implements EventTarget.
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new TableViewETSkin<>(this);
    }

    //---------- boiler-plate: super's constructors   
    public TableViewET() {
        this(null);
    }

    public TableViewET(ObservableList<S> items) {
        super(items);
        setRowFactory(p -> new TableRowET<>());
    }

}
