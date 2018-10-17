/*
 * Created on 17.10.2018
 *
 */
package de.swingempire.fx.scene.control.skin;

import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.skin.VirtualFlow;

/**
 * API study: TableSkin with consistent selection/focus navigation.
 * https://bugs.openjdk.java.net/browse/JDK-8207942
 * 
 * <ul> Important parts:
 * <li> use same method names as the models
 * <li> provide runnable hooks for all navigation methods
 * </ul>
 * 
 * Note: alternatively, row (that is: aboveCell/belowCell) could be mapped to the more
 * intuitive previousRow/nextRow (?). Whatever the decision, names in behavior 
 * must be the same.
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface TableSkinDecorator<T, I extends IndexedCell<T>> {
    
    VirtualFlow<I> getVirtualFlow();
    
    // selection 
    TableSelectionModel<T> getSelectionModel();
    
    // hooks for selection navigation
    default void onSelectAboveCell() {
        if (getSelectionModel() == null) return;
        getVirtualFlow().scrollTo(getSelectionModel().getSelectedIndex());
    }
    
    default void onSelectBelowCell() {
        if (getSelectionModel() == null) return;
        getVirtualFlow().scrollTo(getSelectionModel().getSelectedIndex());
    }
    
    void onSelectLeftCell();
    
    void onSelectRightCell();
    
    // focus 
    TableFocusModel<T, ?> getFocusModel();
    
    // hooks for focus navigation 
    default void onFocusAboveCell() {
        if (getFocusModel() == null) return;
        getVirtualFlow().scrollTo(getFocusModel().getFocusedIndex());
    }
    
    default void onFocusBelowCell() {
        if (getFocusModel() == null) return;
        getVirtualFlow().scrollTo(getFocusModel().getFocusedIndex());
    }
    
    void onFocusLeftCell();
    
    void onFocusRightCell();

}
