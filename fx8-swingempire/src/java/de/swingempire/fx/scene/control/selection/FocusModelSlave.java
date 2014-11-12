/*
 * Created on 11.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.collections.ListChangeListener.Change;

/**
 * Hack around competing responsibilities of MultipleSelectionModel and
 * FocusModel. Semantically, both must update themselves on changes to the items, 
 * often the SelectionModel does so on behalf of the FocusModel: happens
 * whenever it calls <code>select(index)</code> during its own update. 
 * FocusModel can't know whether or not that had happened, so the net effect
 * occasionally is doubling of the update.<p>
 * 
 * Simply removing the responsibility of updating from FocusModel blows if 
 * focus != selectedIndex. <p>
 *  
 * With this tagging interface,  we move the responsibility completely 
 * to the SelectionModel (aka: primary 
 * controller): it has to update the focus internally.  
 * <p>
 * 
 * Note: refactored to tagging (vs. providing a hook for rooting) looks fine so far.
 * PENDING JW: handle default focus
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface FocusModelSlave<T> {
    
//    /**
//     * Must be called by primary controller of list changes if it didn't
//     * update the focus itself. Must be called after the controller
//     * handled any partial change, that is implementations can assume
//     * that the state of the controller is stable.
//     * 
//     * Note that implementors must cope with a Change that is dirty, that
//     * is they must call c.reset before evaluating the change. 
//     *  
//     * @param c
//     */
//    void listChanged(Change<? extends T> c);

}
