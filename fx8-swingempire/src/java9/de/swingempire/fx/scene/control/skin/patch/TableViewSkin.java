/*
 * Created on 17.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import com.sun.javafx.scene.control.behavior.TableViewBehavior;

import de.swingempire.fx.scene.control.skin.impl.SkinBaseDecorator;
import de.swingempire.fx.util.FXUtils;
import javafx.scene.control.FocusModel;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.VirtualFlow;

/**
 * Extended for version compatibility
 * <ul>
 * <li> hacking around JDK-8197536: scrolling broken in fx9
 * <li> get access to TableHeaderRow.
 * <li> commented: unregister super's changeListeners on table properties
 * </ul>
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewSkin<T> 
    extends javafx.scene.control.skin.TableViewSkin<T> 
    implements SkinBaseDecorator {

    private TableHeaderRow headerAlias;
    private TableViewBehavior<T> behaviorAlias;
    private VirtualFlow<TableRow<T>> flowAlias;
    
    /**
     * @param control
     */
    public TableViewSkin(TableView<T> control) {
        super(control);
        // don't expose - will break compatibility! just use on this level
        behaviorAlias = invokeGetBehavior();
        flowAlias = (VirtualFlow<TableRow<T>>) getSkinnable().lookup("VirtualFlow");//invokeGetFlow();
        hackScrollingBehavior();
        // very quick check if SkinDecorator is working
        // unregisterChangeListener(table.fixedCellSizeProperty());
         // if we set the fixedCellSize here, the effect
         // (of having no effect) can't be seen - must do after
         // having been added to the scenegraph
         // table.setFixedCellSize(100);
    }
    
    
    
    /**
     * Hacking JDK-8197536: replace onSelect/Focus/Next/Prev of
     * behavior with our own methods.
     */
    private void hackScrollingBehavior() {
        behaviorAlias.setOnSelectNextRow(this::selectNextRow);
        behaviorAlias.setOnSelectPreviousRow(this::selectPreviousRow);
        behaviorAlias.setOnFocusNextRow(this::focusNextRow);
        behaviorAlias.setOnFocusPreviousRow(this::focusPreviousRow);
    }

    /**
     * Hacking JDK-8197536: by-pass virtualFlow scrollTo if next row is
     * just below the viewport.
     */
    private void focusNextRow() {
        FocusModel<T> sm = getSkinnable().getFocusModel();
        if (sm == null) return;
        
        int index = sm.getFocusedIndex();
        if (!handledOneOffScroll(index, 1)) 
            flowAlias.scrollTo(index);
    }
    
    /**
     * Hacking JDK-8197536: by-pass virtualFlow scrollTo if next row is
     * just below the viewport.
     */
    private void focusPreviousRow() {
        FocusModel<T> sm = getSkinnable().getFocusModel();
        if (sm == null) return;
        
        int index = sm.getFocusedIndex();
        if (!handledOneOffScroll(index, -1)) 
            flowAlias.scrollTo(index);
    }
    
    /**
     * Hacking JDK-8197536: by-pass virtualFlow scrollTo if next row is
     * just below the viewport.
     */
    private void selectNextRow() {
        SelectionModel<T> sm = getSkinnable().getSelectionModel();
        if (sm == null) return;
        
        int index = sm.getSelectedIndex();
        if (!handledOneOffScroll(index, 1)) 
            flowAlias.scrollTo(index);
    }
    
    /**
     * Hacking JDK-8197536: by-pass virtualFlow scrollTo if prev row is
     * just above the viewport.
     */
    private void selectPreviousRow() {
        SelectionModel<T> sm = getSkinnable().getSelectionModel();
        if (sm == null) return;

        int index = sm.getSelectedIndex();
        if (!handledOneOffScroll(index, -1)) 
            flowAlias.scrollTo(index);
    }

    /**
     * Handle scrolling if target index is just below/above viewport.
     * 
     * @param index the target index to scroll to
     * @param direction flag to handle below (== -1) or above (== +1) the viewport.
     * @return true if scrolled, false otherwise.
     */
    private boolean handledOneOffScroll(int index, int direction) {

        TableRow<?> cell = flowAlias.getVisibleCell(index);
        if (cell != null) return false;
        // not visible, check for just off - JDK-8197536
        // check if just off viewport in direction
        TableRow<T> prev = flowAlias.getVisibleCell(index - direction);
        if (prev != null) {
            // if so, scroll to that visible cell, then scroll pixels for row height 
            flowAlias.scrollTo(prev);
            double delta = getCellLength(index);
            flowAlias.scrollPixels(direction * delta);
            return true;
        }

        return false;
    }
    /**
     * Returns the "length" (aka:rowHeight) of the row at index.
     * Note this is hacking into virtualFlow's realm, fixing JDK-8197536.
     * 
     * @param index
     * @return
     */
    private double getCellLength(int index) {
        return invokeGetCellLength(index);
    }
    
    /**
     * Implemented to grab header. Super method is package and final.
     * 
     * @return
     */
    public TableHeaderRow getTableHeader() {
        return headerAlias;
    }

    @Override
    protected TableHeaderRow createTableHeaderRow() {
        headerAlias = super.createTableHeaderRow();
        return headerAlias;
    }

//----------------- private hacking with reflection
    
    private double invokeGetCellLength(int index) {
        return (double) FXUtils.invokeGetMethodValue(VirtualFlow.class, flowAlias, "getCellLength", Integer.TYPE, index);
    }
    
    private TableViewBehavior<T> invokeGetBehavior() {
        return (TableViewBehavior<T>) FXUtils.invokeGetFieldValue(javafx.scene.control.skin.TableViewSkin.class, this, "behavior");
    }
    
    private VirtualFlow<TableRow<T>> invokeGetFlow() {
        return (VirtualFlow<TableRow<T>>) FXUtils.invokeGetMethodValue(javafx.scene.control.skin.VirtualContainerBase.class, this, "getVirtualFlow");
    }
}
