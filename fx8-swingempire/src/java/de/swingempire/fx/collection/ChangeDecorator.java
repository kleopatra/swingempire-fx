/*
 * Created on 27.09.2018
 *
 */
package de.swingempire.fx.collection;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.javafx.collections.SourceAdapterChange;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import javafx.util.Duration;

/**
 * A transform that decorates the source list with a temporary change marker.
 * The marker is activated on receiving a change of type wasUpdated from the 
 * source list and removed after a configurable duration (default is 2 seconds).
 * <p>
 * PENDING JW: adds memory leaks? cpu usage and memory increasing ... inconclusive, 
 *    profiler reveals nothing
 * PENDING JW: threading, changes in source are assumed to happen on ... which thread?
 *    updates are fired on the fx thread (due to using Timeline).. hmm really?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChangeDecorator<E> extends TransformationList<E, E> {

    /**
     * Class that manages the marker timing per element.
     */
    protected class Marker<E> {
        private E element;

        protected Timeline recentTimer;

        private ReadOnlyBooleanWrapper recentlyChanged = new ReadOnlyBooleanWrapper(this, "changed", false);
 
        public Marker(E element) {
            this.element = element;
            restart();
        }

        public void restart() {
            if (recentTimer == null) {
                recentTimer = new Timeline(new KeyFrame(
                       markerDuration , e -> recentlyChanged.set(false)));
                
            }
            recentTimer.playFromStart();
            recentlyChanged.set(true);
        }
        
        public void dispose() {
            recentTimer.stop();
            element = null;
            recentTimer = null;
        }

        public E getElement() {
            return element;
        }
        
        public boolean isDisposed() {
            return element == null;
        }
        
        public ReadOnlyBooleanProperty changedProperty() {
            return recentlyChanged.getReadOnlyProperty();
        }
        
        public boolean isMarking(E element) {
            return element == this.element;
        }
        @Override
        public String toString() {
            return "marker for: " + element + " changed: " + recentlyChanged.get();
        }
    }

    protected static final Duration defaultDuration = Duration.seconds(2);
    protected ObservableList<Marker<E>> markers;
    
    protected Duration markerDuration;
    
    /**
     * Instantiates a update decorator on the given list with default duration of 2 seconds.
     * 
     * @param source the list to decorate
     */
    public ChangeDecorator(ObservableList<E> source) {
        this(source, defaultDuration);
    }
    
    
    /**
     * Instantiates an update decorator on the given list with the given 
     * marker duration.
     * 
     * @param source the list to decorate
     * @param markerDuration the duration of the marker
     */
    public ChangeDecorator(ObservableList<E> source, Duration markerDuration) {
        super(source);
        this.markerDuration = markerDuration;
        markers = FXCollections.observableArrayList(
                e -> new Observable[] { e.changedProperty() });
        markers.addListener(this::markersChanged);
    }
    
    /**
     * Returns a boolean to indicate whether or not the given element is 
     * currently marked.
     * @param element the element to check
     * @return true is marked, false otherwise.
     */
    public boolean isChanged(E element) {
        Optional<Marker<E>> marker = findMarkerFor(element);
        BooleanProperty result = new SimpleBooleanProperty();
        marker.ifPresentOrElse(m -> result.set(m.changedProperty().get()), () -> result.set(false));
        return result.get();
    }


    /**
     * Callback for changes in markers.
     * @param c
     */
    protected void markersChanged(Change<? extends Marker<E>> c) {
        beginChange();
        while (c.next()) {
            if (c.wasUpdated()) {
                markersUpdated(c);
            }
        }
        endChange();
    }

    @Override
    protected void sourceChanged(Change<? extends E> c) {
        beginChange();
        cleanupMarkers();
        while (c.next()) {
            if (c.wasAdded() || c.wasRemoved()) {
                sourceAddedRemoved(c);
            } else if (c.wasUpdated()) {
                sourceUpdated(c);
            } else {
//                cleanupMarkers();
            }
        }
        fireChange(new SourceAdapterChange<>(this, c));
        endChange();
    }

    /**
     * Called before processing notification from source. Remove disposed
     * markers
     */
    private void cleanupMarkers() {
//        for (int index = markers.size() - 1; index > 0; index--) {
//            if (markers.get(index).isDisposed()) {
//                markers.remove(index);
//            }
//        }
            for (Iterator<Marker<E>> iterator = markers.iterator(); iterator.hasNext();) {
                Marker<E> marker = (Marker<E>) iterator.next();
                if (marker.isDisposed()) {
                    iterator.remove();
                }
            }
    }

    /**
     * Note: must only be called in begin/end block
     * 
     * @param c
     */
    protected void markersUpdated(Change<? extends Marker<E>> c) {
        int from = c.getFrom();
        int to = c.getTo();
        LOG.info("markers in update: " + c.getList());
        for (int index = from; index < to; index++) {
            Marker<E> marker = c.getList().get(index);
            int sourceIndex = getSource().indexOf(marker.getElement());
            nextUpdate(sourceIndex);
            // dispose only, we are in notification code
            // must not change list
            c.getList().get(index).dispose();
        }
    }

    /**
     * @param c
     */
    protected void sourceUpdated(Change<? extends E> c) {
        int from = c.getFrom();
        int to = c.getTo();
        for (int index = from; index < to; index++) {
            startMarker(c.getList().get(index));
        }
    }

    /**
     * @param e
     */
    protected void startMarker(E e) {
        Optional<Marker<E>> marker = findMarkerFor(e);
        marker.ifPresentOrElse(Marker::restart, 
                () -> {markers.add(new Marker<>(e));
        });

    }

    /**
     * @param e
     * @return
     */
    protected Optional<Marker<E>> findMarkerFor(E e) {
        Optional<Marker<E>> marker = markers.stream()
                .filter(m -> m.isMarking(e)).findFirst();
        return marker;
    }

    protected void sourceAddedRemoved(Change<? extends E> c) {
        if (c.wasRemoved()) {
            List<ChangeDecorator.Marker> marked = markers.stream()
                    .filter(m -> c.getRemoved().contains(m.getElement()))
                    .collect(Collectors.toList());
            markers.remove(marked);
            markers.forEach(Marker::dispose);
        }

    }

    // ----------- implementing super abstract methods
    
    @Override
    public int getSourceIndex(int index) {
        return index;
    }

    @Override
    public int getViewIndex(int index) {
        return index;
    }

    @Override
    public E get(int index) {
        return getSource().get(index);
    }

    @Override
    public int size() {
        return getSource().size();
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ChangeDecorator.class.getName());
}