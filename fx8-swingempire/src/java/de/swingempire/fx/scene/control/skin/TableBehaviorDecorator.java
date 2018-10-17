/*
 * Created on 17.10.2018
 *
 */
package de.swingempire.fx.scene.control.skin;

import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TableSelectionModel;

/**
 * API study: Behaviour with consistent selection/focus navigation.
 * 
 * <ul> Important parts:
 * <li> use same method names as the models
 * <li> provide runnable hooks for all navigation methods
 * </ul>
 * 
 * Note: row (that is: aboveCell/belowCell) could be mapped to the more
 * intuitive previousRow/nextRow. Whatever the decision, names in skin 
 * must be the same.
 * 
 * Implementation note: behaviour <b>must not</b> interfere with model
 * implementation. In particular, it's wrong to message different 
 * selection navigation methods based on cellSelectionEnabled! Instead,
 * implement the model methods to comply with their contracts..
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface TableBehaviorDecorator<T> {

    //------ selection navigation
    TableSelectionModel<T> getSelectionModel();
    
    // use the names of the SelectionModel for row navigation
    default void selectAboveCell() {
        if (getSelectionModel() == null) return;
        getSelectionModel().selectAboveCell();
        if (getOnSelectAboveCell() == null) return;
        getOnSelectAboveCell().run();        
    }
    
    default Runnable getOnSelectAboveCell() {
        return null;
    }
    
    void setOnFocusSelectCell(Runnable r);
    
    default void selectBelowCell() {
        if (getSelectionModel() == null) return;
        getSelectionModel().selectBelowCell();
        if (getOnSelectBelowCell() == null) return;
        getOnSelectBelowCell().run();        
    }
    
    default Runnable getOnSelectBelowCell() {
        return null;
    }
    
    void setOnSelectBelowCell(Runnable r);
    
    // use names of selectionModel for column navigation
    // Note: the action methods are already available, 
    // runnable hooks are missing - should be there for symmetry
    default void selectLeftCell() {
        if (getSelectionModel() == null) return;
        getSelectionModel().selectLeftCell();
        if (getOnSelectLeftCell() == null) return;
        getOnSelectLeftCell().run();
    }
    
    default Runnable getOnSelectLeftCell() {
        return null;
    }
    
    void setOnSelectLeftCell(Runnable r);
    
    default void selectRightCell() {
        if (getSelectionModel() == null) return;
        getSelectionModel().selectRightCell();
        if (getOnSelectRightCell() == null) return;
        getOnSelectRightCell().run();
    }
    
    default Runnable getOnSelectRightCell() {
        return null;
    }
    
    void setOnSelectRightCell(Runnable r);
    
    //--------- focus navigation
    TableFocusModel<T, ?> getFocusModel();
    
    // use the names of the focusModel for row navigation
    default void focusAboveCell() {
        if (getFocusModel() == null) return;
        getFocusModel().focusAboveCell();
        if (getOnFocusAboveCell() == null) return;
        getOnFocusAboveCell().run();        
    }
    
    default Runnable getOnFocusAboveCell() {
        return null;
    }
    
    void setOnFocusAboveCell(Runnable r);
    
    default void focusBelowCell() {
        if (getFocusModel() == null) return;
        getFocusModel().focusBelowCell();
        if (getOnFocusBelowCell() == null) return;
        getOnFocusAboveCell().run();        
        
    }
 
    default Runnable getOnFocusBelowCell() {
        return null;
    }
    
    void setOnFocusBelowCell(Runnable r);
    
    // use names of focusModel for column navigation
    // Note: the action methods are already available, 
    // runnable hooks are missing - should be there for symmetry
    default void focusLeftCell() {
        if (getFocusModel() == null) return;
        getFocusModel().focusLeftCell();
        if (getOnFocusLeftCell() == null) return;
        getOnFocusLeftCell().run();
    }
    
    default Runnable getOnFocusLeftCell() {
        return null;
    }
    
    void setOnFocusLeftCell(Runnable r);
    
    default void focusRightCell() {
        if (getFocusModel() == null) return;
        getFocusModel().focusRightCell();
        if (getOnFocusRightCell() == null) return;
        getOnFocusRightCell().run();
    }
    
    default Runnable getOnFocusRightCell() {
        return null;
    }
    
    void setOnFocusRightCell(Runnable r);
    
    // alternative, use descriptive 2D names for up/down navigation
    // in both selection and focus 
/*    
    default void focusPreviousRow() {
        if (getFocusModel() == null) return;
        getFocusModel().focusAboveCell();
        if (getOnFocusAboveCell() == null) return;
        getOnFocusAboveCell().run();        
    }
    
    default Runnable getOnFocusPreviousRow() {
        return null;
    }
    
    void setOnFocusPreviousRow(Runnable r);
*/    
}
