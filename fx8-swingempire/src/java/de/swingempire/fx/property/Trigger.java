/*
 * Created on 24.05.2014
 *
 */
package de.swingempire.fx.property;

import javafx.beans.value.ObservableValueBase;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class Trigger extends ObservableValueBase<Boolean> {
    private static final Boolean COMMIT  = Boolean.TRUE;
    private static final Boolean FLUSH   = Boolean.FALSE;
    private static final Boolean NEUTRAL = null;

    private Boolean state;
    
    @Override
    public Boolean getValue() {
        return state;
    }

    public void triggerCommit() {
        setState(COMMIT);
    }
    
    /**
     * @param commit2
     */
    private void setState(Boolean state) {
        Boolean old = getValue();
        if (old != null && old == state) {
            setState(null);
        }
        this.state = state;
        fireValueChangedEvent();
    }

    public void triggerFlush() {
        setState(FLUSH);
    }
}
