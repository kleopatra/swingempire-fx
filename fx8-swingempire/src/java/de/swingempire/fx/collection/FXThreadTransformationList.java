/*
 * Created on 30.01.2018
 *
 */
package de.swingempire.fx.collection;

import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

/**
 * A 1:1 transform of the sourceList that guarantees to fire change notification
 * on the fx-thread.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class FXThreadTransformationList<E> extends TransformationList<E, E> {

    /**
     * @param source
     */
    public FXThreadTransformationList(ObservableList<E> source) {
        super(source);
    }

    @Override
    protected void sourceChanged(Change<? extends E> c) {
        beginChange();
        while (c.next()) {
            LOG.info("any: " + c.getFrom());
            if (c.wasPermutated()) {
                
            } else if (c.wasUpdated()) {
                update(c);
            } else if (c.wasReplaced()) {
                
            } else {
                addedOrRemoved(c);
            }
        }
        endChangeOnFXThread();
    }

    /**
     * 
     */
    public void endChangeOnFXThread() {
        Platform.runLater(() -> endChange());
    }

    /**
     * @param c
     */
    private void addedOrRemoved(Change<? extends E> c) {
        if (c.wasRemoved()) {
            nextRemove(c.getFrom(), c.getRemoved());
        } else if (c.wasAdded()) {
            nextAdd(c.getFrom(), c.getTo());  
        } else {
            throw new IllegalStateException("expected either removed or added, but was:" + c);
        }
    }

    /**
     * @param c
     */
    private void update(Change<? extends E> c) {
        LOG.info("update: " + c);
        for (int pos = c.getFrom(); pos < c.getTo(); pos++) {
            nextUpdate(pos);
        }
    }

    @Override
    public int getViewIndex(int index) {
        return index;
    }

    @Override
    public int getSourceIndex(int index) {
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
            .getLogger(FXThreadTransformationList.class.getName());
}
