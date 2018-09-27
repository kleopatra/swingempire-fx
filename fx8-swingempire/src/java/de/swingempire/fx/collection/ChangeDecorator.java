/*
 * Created on 27.09.2018
 *
 */
package de.swingempire.fx.collection;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.javafx.collections.SourceAdapterChange;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import javafx.util.Duration;

public class ChangeDecorator<E> extends TransformationList<E, E> {

    private class Marker<E> {
        private E element;

        private Timeline recentTimer;

        ReadOnlyBooleanWrapper recentlyChanged = new ReadOnlyBooleanWrapper() {

            @Override
            protected void invalidated() {
                if (get()) {
                    if (recentTimer == null) {
                        recentTimer = new Timeline(new KeyFrame(
                                Duration.millis(2000), ae -> set(false)));
                    }
                    recentTimer.playFromStart();
                } else {
                    if (recentTimer != null)
                        recentTimer.stop();
                }
            }

        };

        Marker(E element) {
            this.element = element;
            recentlyChanged.set(true);
        }

        public void dispose() {
            recentTimer.stop();
//                recentTimer = null;
//                recentlyChanged = null;
            element = null;
        }

        @Override
        public String toString() {
            return "marker for: " + element;
        }
    }

    private ObservableList<Marker<E>> markers;

    private ListChangeListener markerListener;

    /**
     * @param source
     */
    public ChangeDecorator(ObservableList<E> source) {
        super(source);
        markers = FXCollections.observableArrayList(
                e -> new Observable[] { e.recentlyChanged });
        markerListener = c -> {
            beginChange();
            while (c.next()) {
                if (c.wasUpdated()) {
                    markerUpdated(c);
                }
            }
            endChange();
        };
        markers.addListener(markerListener);
    }

    public boolean isDirty(E element) {
        Optional<Marker<E>> marker = findMarkerFor(element);
        return marker.isPresent();
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
        for (int index = markers.size() - 1; index > 0; index--) {
            if (markers.get(index).element == null) {
                markers.remove(index);
            }
        }
//            for (Iterator iterator = markers.iterator(); iterator.hasNext();) {
//                Marker marker = (Marker) iterator.next();
//                if (marker.element == null) {
//                    iterator.remove();
//                }
//            }
    }

    /**
     * Note: must only be called in begin/end block
     * 
     * @param c
     */
    protected void markerUpdated(Change<Marker<E>> c) {
        int from = c.getFrom();
        int to = c.getTo();
        for (int index = from; index < to; index++) {
            Marker<E> marker = c.getList().get(index);
            int sourceIndex = getSource().indexOf(marker.element);
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
        marker.ifPresentOrElse(m -> m.recentlyChanged.set(true), () -> {
            markers.add(new Marker<>(e));
        });

    }

    /**
     * @param e
     * @return
     */
    protected Optional<Marker<E>> findMarkerFor(E e) {
        Optional<Marker<E>> marker = markers.stream()
                .filter(m -> e.equals(m.element)).findFirst();
        return marker;
    }

    protected void sourceAddedRemoved(Change<? extends E> c) {
        if (c.wasRemoved()) {
            List<ChangeDecorator.Marker> marked = markers.stream()
                    .filter(m -> c.getRemoved().contains(m.element))
                    .collect(Collectors.toList());
            markers.remove(marked);
            markers.forEach(Marker::dispose);
        }

    }

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