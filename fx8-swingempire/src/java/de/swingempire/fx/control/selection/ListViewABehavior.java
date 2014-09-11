/*
 * Created on 03.09.2014
 *
 */
package de.swingempire.fx.control.selection;

/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventType;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.behavior.ListCellBehavior;
import com.sun.javafx.scene.control.behavior.OrientedKeyBinding;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusListBehavior;
import com.sun.javafx.scene.control.skin.Utils;

import static javafx.scene.input.KeyCode.*;

/**
 * Copy from 8u20 and changed to rely on selectionModel to cope with anchor. 
 * The driving force is to 
 * replace anchor handling here by anchor handling in selectionModel.
 * 
 * Note: at first tried to extend core ListViewBehaviour but didn't work (too much
 * privacy) - so c&p'd and changed the copy.
 * 
 * NOTE: List/CellBehaviour fiddles with Anchor on mousePressed!!
 * Need to adjust as well...
 * 
 * Changes:
 * - extracted listener install in overridable method installListener to
 *   allow subclasses to do their own, but doing nothing here
 * - commented all methods xxAnchor 
 * - commented all code blocks that accessed the xxAnchor methods  
 * - re-implemented to rely on model anchor as needed  
 *   
 * PENDING JW:
 * - alsoSelectNext/Previous now always clears the selection and selects
 *   a range: consequence are more notifications than before
 *   see code comment in alsoSelectPrevious
 * - discontinous modes not implemented (but then, not working in core as well,
 *   and not supported in win explorer)
 *   the difference between ctrl-shift-navigation and shift-navigation is
 *   (according to ux) that only the latter unselects all outside the range 
 * - discontinous keybinding only defined for vertical?     
 */
public class ListViewABehavior<T> extends BehaviorBase<ListView<T>> {

    /**************************************************************************
     *                          Setup KeyBindings                             *
     *************************************************************************/
    protected static final List<KeyBinding> LIST_VIEW_BINDINGS = new ArrayList<KeyBinding>();

    static {
        LIST_VIEW_BINDINGS.add(new KeyBinding(HOME, "SelectFirstRow"));
        LIST_VIEW_BINDINGS.add(new KeyBinding(END, "SelectLastRow"));
        LIST_VIEW_BINDINGS.add(new KeyBinding(HOME, "SelectAllToFirstRow").shift());
        LIST_VIEW_BINDINGS.add(new KeyBinding(END, "SelectAllToLastRow").shift());
        LIST_VIEW_BINDINGS.add(new KeyBinding(PAGE_UP, "SelectAllPageUp").shift());
        LIST_VIEW_BINDINGS.add(new KeyBinding(PAGE_DOWN, "SelectAllPageDown").shift());
        
        LIST_VIEW_BINDINGS.add(new KeyBinding(SPACE, "SelectAllToFocus").shift());
        LIST_VIEW_BINDINGS.add(new KeyBinding(SPACE, "SelectAllToFocusAndSetAnchor").shortcut().shift());
        
        LIST_VIEW_BINDINGS.add(new KeyBinding(PAGE_UP, "ScrollUp"));
        LIST_VIEW_BINDINGS.add(new KeyBinding(PAGE_DOWN, "ScrollDown"));

        LIST_VIEW_BINDINGS.add(new KeyBinding(ENTER, "Activate"));
        LIST_VIEW_BINDINGS.add(new KeyBinding(SPACE, "Activate"));
        LIST_VIEW_BINDINGS.add(new KeyBinding(F2, "Activate"));
        LIST_VIEW_BINDINGS.add(new KeyBinding(ESCAPE, "CancelEdit"));

        LIST_VIEW_BINDINGS.add(new KeyBinding(A, "SelectAll").shortcut());
        LIST_VIEW_BINDINGS.add(new KeyBinding(HOME, "FocusFirstRow").shortcut());
        LIST_VIEW_BINDINGS.add(new KeyBinding(END, "FocusLastRow").shortcut());
        LIST_VIEW_BINDINGS.add(new KeyBinding(PAGE_UP, "FocusPageUp").shortcut());
        LIST_VIEW_BINDINGS.add(new KeyBinding(PAGE_DOWN, "FocusPageDown").shortcut());
            
        if (PlatformUtil.isMac()) {
            LIST_VIEW_BINDINGS.add(new KeyBinding(SPACE, "toggleFocusOwnerSelection").ctrl().shortcut());
        } else {
            LIST_VIEW_BINDINGS.add(new KeyBinding(SPACE, "toggleFocusOwnerSelection").ctrl());
        }

        // if listView is vertical...
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(UP, "SelectPreviousRow").vertical());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(KP_UP, "SelectPreviousRow").vertical());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(DOWN, "SelectNextRow").vertical());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(KP_DOWN, "SelectNextRow").vertical());

        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(UP, "AlsoSelectPreviousRow").vertical().shift());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(KP_UP, "AlsoSelectPreviousRow").vertical().shift());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(DOWN, "AlsoSelectNextRow").vertical().shift());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(KP_DOWN, "AlsoSelectNextRow").vertical().shift());

        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(UP, "FocusPreviousRow").vertical().shortcut());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(DOWN, "FocusNextRow").vertical().shortcut());

        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(UP, "DiscontinuousSelectPreviousRow").vertical().shortcut().shift());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(DOWN, "DiscontinuousSelectNextRow").vertical().shortcut().shift());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(PAGE_UP, "DiscontinuousSelectPageUp").vertical().shortcut().shift());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(PAGE_DOWN, "DiscontinuousSelectPageDown").vertical().shortcut().shift());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(HOME, "DiscontinuousSelectAllToFirstRow").vertical().shortcut().shift());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(END, "DiscontinuousSelectAllToLastRow").vertical().shortcut().shift());
        // --- end of vertical



        // if listView is horizontal...
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(LEFT, "SelectPreviousRow"));
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(KP_LEFT, "SelectPreviousRow"));
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(RIGHT, "SelectNextRow"));
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(KP_RIGHT, "SelectNextRow"));

        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(LEFT, "AlsoSelectPreviousRow").shift());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(KP_LEFT, "AlsoSelectPreviousRow").shift());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(RIGHT, "AlsoSelectNextRow").shift());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(KP_RIGHT, "AlsoSelectNextRow").shift());

        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(LEFT, "FocusPreviousRow").shortcut());
        LIST_VIEW_BINDINGS.add(new ListViewKeyBinding(RIGHT, "FocusNextRow").shortcut());
        // --- end of horizontal

        LIST_VIEW_BINDINGS.add(new KeyBinding(BACK_SLASH, "ClearSelection").shortcut());
    }
    
    @Override
    protected /*final*/ String matchActionForEvent(KeyEvent e) {
        String action = super.matchActionForEvent(e);
        if (action != null) {
            if (e.getCode() == LEFT || e.getCode() == KP_LEFT) {
                if (getControl().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                    if (e.isShiftDown()) {
                        action = "AlsoSelectNextRow";
                    } else {
                        if (e.isShortcutDown()) {
                            action = "FocusNextRow";
                        } else {
                            action = getControl().getOrientation() == Orientation.HORIZONTAL ? "SelectNextRow" : "TraverseRight";
                        }
                    }
                }
            } else if (e.getCode() == RIGHT || e.getCode() == KP_RIGHT) {
                if (getControl().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                    if (e.isShiftDown()) {
                        action = "AlsoSelectPreviousRow";
                    } else {
                        if (e.isShortcutDown()) {
                            action = "FocusPreviousRow";
                        } else {
                            action = getControl().getOrientation() == Orientation.HORIZONTAL ? "SelectPreviousRow" : "TraverseLeft";
                        }
                    }
                }
            }
        }
        return action;
    }

    @Override protected void callAction(String name) {
        if ("SelectPreviousRow".equals(name)) selectPreviousRow();
        else if ("SelectNextRow".equals(name)) selectNextRow();
        else if ("SelectFirstRow".equals(name)) selectFirstRow();
        else if ("SelectLastRow".equals(name)) selectLastRow();
        else if ("SelectAllToFirstRow".equals(name)) selectAllToFirstRow();
        else if ("SelectAllToLastRow".equals(name)) selectAllToLastRow();
        else if ("SelectAllPageUp".equals(name)) selectAllPageUp();
        else if ("SelectAllPageDown".equals(name)) selectAllPageDown();
        else if ("AlsoSelectNextRow".equals(name)) alsoSelectNextRow();
        else if ("AlsoSelectPreviousRow".equals(name)) alsoSelectPreviousRow();
        else if ("ClearSelection".equals(name)) clearSelection();
        else if ("SelectAll".equals(name)) selectAll();
        else if ("ScrollUp".equals(name)) scrollPageUp();
        else if ("ScrollDown".equals(name)) scrollPageDown();
        else if ("FocusPreviousRow".equals(name)) focusPreviousRow();
        else if ("FocusNextRow".equals(name)) focusNextRow();
        else if ("FocusPageUp".equals(name)) focusPageUp();
        else if ("FocusPageDown".equals(name)) focusPageDown();
        else if ("Activate".equals(name)) activate();
        else if ("CancelEdit".equals(name)) cancelEdit();
        else if ("FocusFirstRow".equals(name)) focusFirstRow();
        else if ("FocusLastRow".equals(name)) focusLastRow();
        else if ("toggleFocusOwnerSelection".equals(name)) toggleFocusOwnerSelection();

        else if ("SelectAllToFocus".equals(name)) selectAllToFocus(false);
        else if ("SelectAllToFocusAndSetAnchor".equals(name)) selectAllToFocus(true);

        else if ("DiscontinuousSelectNextRow".equals(name)) discontinuousSelectNextRow();
        else if ("DiscontinuousSelectPreviousRow".equals(name)) discontinuousSelectPreviousRow();
        else if ("DiscontinuousSelectPageUp".equals(name)) discontinuousSelectPageUp();
        else if ("DiscontinuousSelectPageDown".equals(name)) discontinuousSelectPageDown();
        else if ("DiscontinuousSelectAllToLastRow".equals(name)) discontinuousSelectAllToLastRow();
        else if ("DiscontinuousSelectAllToFirstRow".equals(name)) discontinuousSelectAllToFirstRow();
        else super.callAction(name);
    }

    @Override protected void callActionForEvent(KeyEvent e) {
        // RT-12751: we want to keep an eye on the user holding down the shift key, 
        // so that we know when they enter/leave multiple selection mode. This
        // changes what happens when certain key combinations are pressed.
        isShiftDown = e.getEventType() == KeyEvent.KEY_PRESSED && e.isShiftDown();
        isShortcutDown = e.getEventType() == KeyEvent.KEY_PRESSED && e.isShortcutDown();

        super.callActionForEvent(e);
    }

    /**************************************************************************
     *                         State and Functions                            *
     *************************************************************************/
    
    private boolean isShiftDown = false;
    private boolean isShortcutDown = false;
    
    private Callback<Boolean, Integer> onScrollPageUp;
    private Callback<Boolean, Integer> onScrollPageDown;
    private Runnable onFocusPreviousRow;
    private Runnable onFocusNextRow;
    private Runnable onSelectPreviousRow;
    private Runnable onSelectNextRow;
    private Runnable onMoveToFirstCell;
    private Runnable onMoveToLastCell;

    public void setOnScrollPageUp(Callback<Boolean, Integer> c) { onScrollPageUp = c; }
    public void setOnScrollPageDown(Callback<Boolean, Integer> c) { onScrollPageDown = c; }
    public void setOnFocusPreviousRow(Runnable r) { onFocusPreviousRow = r; }
    public void setOnFocusNextRow(Runnable r) { onFocusNextRow = r; }
    public void setOnSelectPreviousRow(Runnable r) { onSelectPreviousRow = r; }
    public void setOnSelectNextRow(Runnable r) { onSelectNextRow = r; }
    public void setOnMoveToFirstCell(Runnable r) { onMoveToFirstCell = r; }
    public void setOnMoveToLastCell(Runnable r) { onMoveToLastCell = r; }
    
    private boolean selectionChanging = false;
    
    private TwoLevelFocusListBehavior tlFocus;

    public ListViewABehavior(ListView<T> control) {
        super(control, LIST_VIEW_BINDINGS);
        
        installListeners(control);
        // Only add this if we're on an embedded platform that supports 5-button navigation
        if (Utils.isTwoLevelFocus()) {
            tlFocus = new TwoLevelFocusListBehavior(control); // needs to be last.
        }
    }

    /**
     * Allow subclasses to install their own listeners.
     * Implemented to do nothing: the original has listeners for items/selectedIndices
     * for the sole issue of keeping the anchor in sync. That's handled by the model now,
     * so we don't need them.
     * @param control
     */
    protected void installListeners(ListView<T> control) {
//        control.itemsProperty().addListener(weakItemsListener);
//        if (control.getItems() != null) {
//            control.getItems().addListener(weakItemsListListener);
//        }
//        
//        // Fix for RT-16565
//        getControl().selectionModelProperty().addListener(weakSelectionModelListener);
//        if (control.getSelectionModel() != null) {
//            control.getSelectionModel().getSelectedIndices().addListener(weakSelectedIndicesListener);
//        }
//
    }
    
    @Override public void dispose() {
        ListCellBehavior.removeAnchor(getControl());
        if (tlFocus != null) tlFocus.dispose();
        super.dispose();
    }

//    private void setAnchor(int anchor) {
//        ListCellBehavior.setAnchor(getControl(), anchor < 0 ? null : anchor);
//    }
//    
//    private int getAnchor() {
//        return ListCellBehavior.getAnchor(getControl(), getControl().getFocusModel().getFocusedIndex());
//    }
//    
//    private boolean hasAnchor() {
//        return ListCellBehavior.hasAnchor(getControl());
//    }

    @Override public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
// CHANGED JW: commented anchor setting        
//        if (! e.isShiftDown() && ! e.isSynthesized()) {
//            int index = getControl().getSelectionModel().getSelectedIndex();
//            setAnchor(index);
//        }
        
        if (! getControl().isFocused() && getControl().isFocusTraversable()) {
            getControl().requestFocus();
        }
    }
    
    private int getRowCount() {
        return getControl().getItems() == null ? 0 : getControl().getItems().size();
    }

    private void clearSelection() {
        getControl().getSelectionModel().clearSelection();
    }

    private void scrollPageUp() {
        int newSelectedIndex = -1;
        if (onScrollPageUp != null) {
            newSelectedIndex = onScrollPageUp.call(false);
        }
        if (newSelectedIndex == -1) return;
        
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        sm.clearAndSelect(newSelectedIndex);
    }

    private void scrollPageDown() {
        int newSelectedIndex = -1;
        if (onScrollPageDown != null) {
            newSelectedIndex = onScrollPageDown.call(false);
        }
        if (newSelectedIndex == -1) return;
        
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        sm.clearAndSelect(newSelectedIndex);
    }
    
    private void focusFirstRow() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        fm.focus(0);
        
        if (onMoveToFirstCell != null) onMoveToFirstCell.run();
    }
    
    private void focusLastRow() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        fm.focus(getRowCount() - 1);
        
        if (onMoveToLastCell != null) onMoveToLastCell.run();
    }

    private void focusPreviousRow() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        
        fm.focusPrevious();
// CHANGED JW        
//        if (! isShortcutDown || getAnchor() == -1) {
//            setAnchor(fm.getFocusedIndex());
//        }
//        
        if (onFocusPreviousRow != null) onFocusPreviousRow.run();
    }

    private void focusNextRow() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        
        int focusFromSelection = sm.getSelectedIndex();
        int focus = fm.getFocusedIndex();
        fm.focusNext();
        
// CHANGED JW        
//        if (! isShortcutDown || getAnchor() == -1) {
//            setAnchor(fm.getFocusedIndex());
//        }
//        
        if (onFocusNextRow != null) onFocusNextRow.run();
    }
    
    private void focusPageUp() {
        int newFocusIndex = onScrollPageUp.call(true);
        
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        fm.focus(newFocusIndex);
    }
    
    private void focusPageDown() {
        int newFocusIndex = onScrollPageDown.call(true);
        
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        fm.focus(newFocusIndex);
    }

    /**
     * Expand/collapse selection (if focus isSelected) or select range 
     * between anchor and focus. (if focus is not selected)
     */
    private void alsoSelectPreviousRow() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        // REPLACED JW: 
        AnchoredSelectionModel am = (AnchoredSelectionModel) sm;
        int oldFocus = fm.getFocusedIndex();
        int newFocus = oldFocus -1;
        // anyway, range selection should handle all use cases
        // was a boundary issue: 
        // for prev, a true ascending decides about boundary
        // for next, ascending includes equality
//        boolean ascending = am.getAnchorIndex() < oldFocus;
        // PENDING JW: check against newFocus?
        // for symmetry? as in selectAllPageUP
        boolean ascending = am.getAnchorIndex() < newFocus;
        int boundary = ascending ? newFocus + 1 : newFocus - 1;
        int anchor = am.getAnchorIndex();
        // triggers notification on too many change indices 
        // here we need a clearAndSelectRange on the model
        // which could optimimize
        sm.clearSelection();
        sm.selectRange(anchor, boundary);
        
        // PENDING JW: collapse was not correct
//        if (sm.isSelected(oldFocus)) { // expand or collapse selection
//            if (sm.isSelected(newFocus)) {
//                sm.clearSelection(oldFocus); // collapse
//                fm.focus(newFocus);
//            } else {
//                sm.selectPrevious();
//            }
//        } else {
//        int anchor = am.getAnchorIndex();
//        sm.clearSelection();
        // select range between anchor and new focus inclusive
//        sm.selectRange(anchor, boundary);
//        }
// CHANGED JW   commented all old     
//        if (isShiftDown && getAnchor() != -1) {
//            int newRow = fm.getFocusedIndex() - 1;
//            if (newRow < 0) return;
//
//            int anchor = getAnchor();
//            
//            if (! hasAnchor()) {
//                setAnchor(fm.getFocusedIndex());
//            }
//
//            if (sm.getSelectedIndices().size() > 1) {
//                clearSelectionOutsideRange(anchor, newRow);
//            }
//
//            if (anchor > newRow) {
//                sm.selectRange(anchor, newRow - 1);
//            } else {
//                sm.selectRange(anchor, newRow + 1);
//            }
//        } else {
//            sm.selectPrevious();
//        }

        onSelectPreviousRow.run();
    }

    private void alsoSelectNextRow() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        // REPLACED JW see code comment in alsoFocusPrevious
        AnchoredSelectionModel am = (AnchoredSelectionModel) sm;
        int oldFocus = fm.getFocusedIndex();
        int newFocus = oldFocus + 1;
        // PENDING JW: check against newFocus?
        // was ascending = am.getAnchorIndex() <= oldFocus
        boolean ascending = am.getAnchorIndex() < newFocus;
        int boundary = ascending ? newFocus + 1 : newFocus - 1;
        int anchor = am.getAnchorIndex();
        sm.clearSelection();
        sm.selectRange(anchor, boundary);
        
//        if (sm.isSelected(oldFocus)) { // expand or collapse
//            if (sm.isSelected(newFocus)) {
//                sm.clearSelection(oldFocus); // collapse
//                fm.focus(newFocus);
//            } else {
//                sm.selectNext();
//            }   
//            
//        } else {
//        int anchor = am.getAnchorIndex();
//        sm.clearSelection();
//        sm.selectRange(anchor, boundary);
//        }
 // CHANGED JW comment all old       
//        if (isShiftDown && getAnchor() != -1) {
//            int newRow = fm.getFocusedIndex() + 1;
//            int anchor = getAnchor();
//            
//            if (! hasAnchor()) {
//                setAnchor(fm.getFocusedIndex());
//            } 
//
//            if (sm.getSelectedIndices().size() > 1) {
//                clearSelectionOutsideRange(anchor, newRow);
//            }
//
//            if (anchor > newRow) {
//                sm.selectRange(anchor, newRow - 1);
//            } else {
//                sm.selectRange(anchor, newRow + 1);
//            }
//        } else {
//            sm.selectNext();
//        }
        
        onSelectNextRow.run();
    }
    
    private void clearSelectionOutsideRange(int start, int end) {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        
        int min = Math.min(start, end);
        int max = Math.max(start, end);
        
        List<Integer> indices = new ArrayList<>(sm.getSelectedIndices());
        
        selectionChanging = true;
        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            if (index < min || index >= max) {
                sm.clearSelection(index);
            }
        }
        selectionChanging = false;
    }

    private void selectPreviousRow() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;

        int focusIndex = fm.getFocusedIndex();
        if (focusIndex <= 0) {
            return;
        }
// CHANGED JW: commented old
//        setAnchor(focusIndex - 1);
        getControl().getSelectionModel().clearAndSelect(focusIndex - 1);
        onSelectPreviousRow.run();
    }

    private void selectNextRow() {
        ListView<T> listView = getControl();
        FocusModel<T> fm = listView.getFocusModel();
        if (fm == null) return;
        
        int focusIndex = fm.getFocusedIndex();
        if (focusIndex == getRowCount() - 1) {
            return;
        }
        
        MultipleSelectionModel<T> sm = listView.getSelectionModel();
        if (sm == null) return;
        
     // CHANGED JW: commented old
//        setAnchor(focusIndex + 1);
        sm.clearAndSelect(focusIndex + 1);
        if (onSelectNextRow != null) onSelectNextRow.run();
    }

    private void selectFirstRow() {
        // PENDING JW: why check for size here but not in selectLastRow?
        if (getRowCount() > 0) {
            getControl().getSelectionModel().clearAndSelect(0);
            if (onMoveToFirstCell != null) onMoveToFirstCell.run();
        }
    }

    private void selectLastRow() {
        getControl().getSelectionModel().clearAndSelect(getRowCount() - 1);
        if (onMoveToLastCell != null) onMoveToLastCell.run();
    }
    
    /**
     * re-implement: same logic as alsoSelectNext/Previous
     * except that the boundary is returned by onScrollPageUp
     */
    private void selectAllPageUp() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        // REPLACED JW: 
        AnchoredSelectionModel am = (AnchoredSelectionModel) sm;
//        int oldFocus = fm.getFocusedIndex();
        int newFocus = onScrollPageUp.call(false);
        // anyway, range selection should handle all use cases
        // was a boundary issue: 
        // for prev, a true ascending decides about boundary
        // for next, ascending includes equality
        boolean ascending = am.getAnchorIndex() < newFocus;
        int boundary = ascending ? newFocus + 1 : newFocus - 1;
        int anchor = am.getAnchorIndex();
        sm.clearSelection();
        sm.selectRange(anchor, boundary);
        
    }
    private void selectAllPageUpOld() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;

        int leadIndex = fm.getFocusedIndex();
        // CHANGED JW: commented old
//        if (isShiftDown) {
//            leadIndex = getAnchor() == -1 ? leadIndex : getAnchor();
//            setAnchor(leadIndex);
//        }
        
        int leadSelectedIndex = onScrollPageUp.call(false);

        // fix for RT-34407
        int adjust = leadIndex < leadSelectedIndex ? 1 : -1;

        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        
        selectionChanging = true;
        if (sm.getSelectionMode() == SelectionMode.SINGLE) {
            sm.select(leadSelectedIndex);
        } else {
            sm.clearSelection();
            sm.selectRange(leadIndex, leadSelectedIndex + adjust);
        }
        selectionChanging = false;
    }
    
    /**
     * re-implement: same logic as alsoSelectNext/Previous
     * except that the boundary is returned by onScrollPageUp
     */
    private void selectAllPageDown() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        // REPLACED JW: 
        AnchoredSelectionModel am = (AnchoredSelectionModel) sm;
//        int oldFocus = fm.getFocusedIndex();
        int newFocus = onScrollPageDown.call(false);
        boolean ascending = am.getAnchorIndex() < newFocus;
        int boundary = ascending ? newFocus + 1 : newFocus - 1;
        int anchor = am.getAnchorIndex();
        sm.clearSelection();
        sm.selectRange(anchor, boundary);
    }

    private void selectAllPageDownOld() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        
        int leadIndex = fm.getFocusedIndex();
     // CHANGED JW: commented old
//        if (isShiftDown) {
//            leadIndex = getAnchor() == -1 ? leadIndex : getAnchor();
//            setAnchor(leadIndex);
//        }
        
        int leadSelectedIndex = onScrollPageDown.call(false);

        // fix for RT-34407
        int adjust = leadIndex < leadSelectedIndex ? 1 : -1;
        
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;

        selectionChanging = true;
        if (sm.getSelectionMode() == SelectionMode.SINGLE) {
            sm.select(leadSelectedIndex);
        } else {
            sm.clearSelection();
            sm.selectRange(leadIndex, leadSelectedIndex + adjust);
        }
        selectionChanging = false;
    }

    /** PENDING JW: this implementation doesn't comply to ux
     * shift-home/end is spec'ed to select all between anchor and first/last
     * this here selects all between focus and first/last
     * also: looks like wrong way round: range(0, something) when it should be
     * (something, 0) - anchor must be unchanged (but behaviour ok)
     * 
     * needed to adjust, re-written (see xxOld for original)
     */ 
    private void selectAllToFirstRow() {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        
        int anchor = ((AnchoredSelectionModel) sm).getAnchorIndex();
        sm.clearSelection();
        // range from anchor to 0: as the second boundary is non-inclusive ...
        sm.selectRange(anchor, -1);
        if (onMoveToFirstCell != null) onMoveToFirstCell.run();
    }
    
    private void selectAllToLastRow() {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        
        int anchor = ((AnchoredSelectionModel) sm).getAnchorIndex();
        sm.clearSelection();
        // range from anchor to last: as the second boundary is non-inclusive ...
        sm.selectRange(anchor, getRowCount());
        if (onMoveToLastCell != null) onMoveToLastCell.run();
        
    }
    
    private void selectAllToFirstRowOld() {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        // REPLACED JW: don't need care about anchor, handled by model
        // TBD JW: not the standard behvaiour in win
        int leadIndex = sm.getSelectedIndex();
        // CHANGED JW: commented old
//        if (isShiftDown) {
//            leadIndex = getAnchor() == -1 ? sm.getSelectedIndex() : getAnchor();
//        }

        sm.clearSelection();
        // JW: incorrect usage - the last of range will be the focus
        // so fix for RT-18413 would be to switch arguments
        sm.selectRange(0, leadIndex + 1);
        
        // CHANGED JW: commented old
//        if (isShiftDown) {
//            setAnchor(leadIndex);
//        }
        
        // RT-18413: Focus must go to first row
        getControl().getFocusModel().focus(0);

        if (onMoveToFirstCell != null) onMoveToFirstCell.run();
    }

    /**
     * misbehaviour:
     * 
     * - press END to select last 
     * - press (several) UP to move selection (== anchor)
     * - press shift-UP to extend selection (== lead)
     * - press shift-END to select till last
     * 
     * expected (as per ux spec and behaviour in win 7): 
     *    all items between anchor and last (inclusive) are selected
     * actual: 
     *    all tems between lead and last (inclusive) are selected
     */
    private void selectAllToLastRowOld() {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;

        int leadIndex = sm.getSelectedIndex();
        
        // REPLACED JW: don't need care about anchor, handled by model
        // CHANGED JW: commented old
//        if (isShiftDown) {
//            leadIndex = hasAnchor() ? sm.getSelectedIndex() : getAnchor();
//        }
        
        sm.clearSelection();
        sm.selectRange(leadIndex, getRowCount());
        
        // CHANGED JW: commented old
//        if (isShiftDown) {
//            setAnchor(leadIndex);
//        }

        if (onMoveToLastCell != null) onMoveToLastCell.run();
    }

    private void selectAll() {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        sm.selectAll();
    }
    
    /**
     * PENDING JW: setAnchorToFocus in real world?
     * 
     * UX states: "ctrl-shift-space select all between anchor and
     * focus, creates anchor" 
     * 
     * can't reproduce in win7 explorer
     * - select item (anchor)
     * - ctrl-down to move focus
     * - ctrl-shift-space to select range (focus == anchor)
     * - shift-down to extend selection (oldfocus +1)
     * 
     * expected as per ux: 2 lines selected
     * actual in win 7: all lines between old anchor and new focus selected
     * seems like there is no difference between shift-space and ctrl-shift-space
     * 
     * @param setAnchorToFocusIndex if true, anchors the focusedIndex
     */
    private void selectAllToFocus(boolean setAnchorToFocusIndex) {
        // Fix for RT-31241
        final ListView<T> listView = getControl();
        if (listView.getEditingIndex() >= 0) return;

        MultipleSelectionModel<T> sm = listView.getSelectionModel();
        if (sm == null) return;

        FocusModel<T> fm = listView.getFocusModel();
        if (fm == null) return;
        // REPLACED JW: 
        AnchoredSelectionModel am = (AnchoredSelectionModel) sm;
        int newFocus = fm.getFocusedIndex();
        boolean ascending = am.getAnchorIndex() < newFocus;
        int boundary = ascending ? newFocus + 1 : newFocus - 1;
        int anchor = am.getAnchorIndex();
        sm.clearSelection();
        sm.selectRange(anchor, boundary);

        if (setAnchorToFocusIndex) am.anchor();
        
        // CHANGED JW: commented old
//        int focusIndex = fm.getFocusedIndex();
//        int anchor = getAnchor();
//        
//        sm.clearSelection();
//        int startPos = anchor;
//        int endPos = anchor > focusIndex ? focusIndex - 1 : focusIndex + 1;
//        sm.selectRange(startPos, endPos);
//        setAnchor(setAnchorToFocusIndex ? focusIndex : anchor);
    }
    
    private void cancelEdit() {
        getControl().edit(-1);
    }

    private void activate() {
        int focusedIndex = getControl().getFocusModel().getFocusedIndex();
        getControl().getSelectionModel().select(focusedIndex);
        // CHANGED JW: commented old
//        setAnchor(focusedIndex);

        // edit this row also
        if (focusedIndex >= 0) {
            getControl().edit(focusedIndex);
        }
    }
    
    private void toggleFocusOwnerSelection() {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;

        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;

        int focusedIndex = fm.getFocusedIndex();
//        
//        
//        LOG.info("anchor/focus before toggle" + 
//                ((AnchoredSelectionModel) sm).getAnchorIndex()+ "/"+fm.getFocusedIndex());
        if (sm.isSelected(focusedIndex)) {
            sm.clearSelection(focusedIndex);
            fm.focus(focusedIndex);
        } else {
            sm.select(focusedIndex);
        }
//        LOG.info("anchor/focus after toggle" + 
//          ((AnchoredSelectionModel) sm).getAnchorIndex()+ "/"+fm.getFocusedIndex());
        ((AnchoredSelectionModel) sm).anchor();
        // CHANGED JW: commented old
//      setAnchor(focusedIndex);
    }
    
    /**************************************************************************
     * Discontinuous Selection                         
     *PENDING JW: not yet updated to anchoredModel                       *
     *************************************************************************/
    
    
    private void discontinuousSelectPreviousRow() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        // REPLACED JW: 
        AnchoredSelectionModel am = (AnchoredSelectionModel) sm;
        int oldFocus = fm.getFocusedIndex();
        int newFocus = oldFocus -1;
        // anyway, range selection should handle all use cases
        // was a boundary issue: 
        // for prev, a true ascending decides about boundary
        // for next, ascending includes equality
//        boolean ascending = am.getAnchorIndex() < oldFocus;
        // PENDING JW: check against newFocus?
        // for symmetry? as in selectAllPageUP
        boolean ascending = am.getAnchorIndex() < newFocus;
        int boundary = ascending ? newFocus + 1 : newFocus - 1;
        int anchor = am.getAnchorIndex();
        // triggers notification on too many change indices 
        // here we need a clearAndSelectRange on the model
        // which could optimimize
//        sm.clearSelection();
        sm.selectRange(anchor, boundary);
        

    }
    private void discontinuousSelectPreviousRowOld() {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;

        if (sm.getSelectionMode() != SelectionMode.MULTIPLE) {
            selectPreviousRow();
            return;
        }
        
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        
        int focusIndex = fm.getFocusedIndex();
        final int newFocusIndex = focusIndex - 1;
        if (newFocusIndex < 0) return;

        int startIndex = focusIndex;
        // CHANGED JW: commented old
//        if (isShiftDown) {
//            startIndex = getAnchor() == -1 ? focusIndex : getAnchor();
//        }

        sm.selectRange(newFocusIndex, startIndex + 1);
        fm.focus(newFocusIndex);

        if (onFocusPreviousRow != null) onFocusPreviousRow.run();
    }
    
    /**
     * we would need to re-anchor? Not here, that's done
     * by the user (f.i. via ctrl/-space)
     * 
     * same code as in alsoSelectNextRow except for not clearing the
     * selection
     */
    private void discontinuousSelectNextRow() {
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        // REPLACED JW see code comment in alsoFocusPrevious
        AnchoredSelectionModel am = (AnchoredSelectionModel) sm;
        int oldFocus = fm.getFocusedIndex();
        int newFocus = oldFocus + 1;
        // PENDING JW: check against newFocus?
        // was ascending = am.getAnchorIndex() <= oldFocus
        boolean ascending = am.getAnchorIndex() < newFocus;
        int boundary = ascending ? newFocus + 1 : newFocus - 1;
        int anchor = am.getAnchorIndex();
//        sm.clearSelection();
        sm.selectRange(anchor, boundary);

    }
    
    private void discontinuousSelectNextRowOld() {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;

        if (sm.getSelectionMode() != SelectionMode.MULTIPLE) {
            selectNextRow();
            return;
        }
        LOG.info("sanity ?");
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;

        int focusIndex = fm.getFocusedIndex();
        final int newFocusIndex = focusIndex + 1;
        if (newFocusIndex >= getRowCount()) return;

        int startIndex = focusIndex;
        // CHANGED JW: commented old
//        if (isShiftDown) {
//            startIndex = getAnchor() == -1 ? focusIndex : getAnchor();
//        }
//
        sm.selectRange(startIndex, newFocusIndex + 1);
        fm.focus(newFocusIndex);

        if (onFocusNextRow != null) onFocusNextRow.run();
    }
    
    private void discontinuousSelectPageUp() {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;

        int leadIndex = fm.getFocusedIndex();
        int leadSelectedIndex = onScrollPageUp.call(false);
        sm.selectRange(leadIndex, leadSelectedIndex - 1);
    }
    
    private void discontinuousSelectPageDown() {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;
        
        int leadIndex = fm.getFocusedIndex();
        int leadSelectedIndex = onScrollPageDown.call(false);
        sm.selectRange(leadIndex, leadSelectedIndex + 1);
    }
    
    private void discontinuousSelectAllToFirstRow() {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;

        int index = fm.getFocusedIndex();
        sm.selectRange(0, index);
        fm.focus(0);

        if (onMoveToFirstCell != null) onMoveToFirstCell.run();
    }
    
    private void discontinuousSelectAllToLastRow() {
        MultipleSelectionModel<T> sm = getControl().getSelectionModel();
        if (sm == null) return;
        
        FocusModel<T> fm = getControl().getFocusModel();
        if (fm == null) return;

        int index = fm.getFocusedIndex() + 1;
        sm.selectRange(index, getRowCount());

        if (onMoveToLastCell != null) onMoveToLastCell.run();
    }

    private static class ListViewKeyBinding extends OrientedKeyBinding {

        public ListViewKeyBinding(KeyCode code, String action) {
            super(code, action);
        }

        public ListViewKeyBinding(KeyCode code, EventType<KeyEvent> type, String action) {
            super(code, type, action);
        }

        @Override public boolean getVertical(Control control) {
            return ((ListView<?>)control).getOrientation() == Orientation.VERTICAL;
        }
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ListViewABehavior.class
            .getName());

// CHANGED JW: listeners no longer needed, moved commented code out of the was    
//  
//  private final ListChangeListener<Integer> selectedIndicesListener = c -> {
//      while (c.next()) {
//          MultipleSelectionModel<T> sm = getControl().getSelectionModel();
//
//          // there are no selected items, so lets clear out the anchor
////CHANGED JW            
////          if (! selectionChanging) {
////              if (sm.isEmpty()) {
////                  setAnchor(-1);
////              } else if (! sm.isSelected(getAnchor())) {
////                  setAnchor(-1);
////              }
////          }
////
////          int addedSize = c.getAddedSize();
////          if (addedSize > 0 && ! hasAnchor()) {
////              List<? extends Integer> addedSubList = c.getAddedSubList();
////              int index = addedSubList.get(addedSize - 1);
////              setAnchor(index);
////          }
//      }
//  };
//  
//  private final ListChangeListener<T> itemsListListener = c -> {
//      while (c.next()) {
////CHANGED JW            
////          if (c.wasAdded() && c.getFrom() <= getAnchor()) {
////              setAnchor(getAnchor() + c.getAddedSize());
////          } else if (c.wasRemoved() && c.getFrom() <= getAnchor()) {
////              setAnchor(getAnchor() - c.getRemovedSize());
////          }
//      }
//  };
//  
//  private final ChangeListener<ObservableList<T>> itemsListener = new ChangeListener<ObservableList<T>>() {
//      @Override
//      public void changed(
//              ObservableValue<? extends ObservableList<T>> observable,
//              ObservableList<T> oldValue, ObservableList<T> newValue) {
//          if (oldValue != null) {
//               oldValue.removeListener(weakItemsListListener);
//           } if (newValue != null) {
//               newValue.addListener(weakItemsListListener);
//           }
//      }
//  };
//  
//  private final ChangeListener<MultipleSelectionModel<T>> selectionModelListener = new ChangeListener<MultipleSelectionModel<T>>() {
//      @Override public void changed(
//                  ObservableValue<? extends MultipleSelectionModel<T>> observable, 
//                  MultipleSelectionModel<T> oldValue, 
//                  MultipleSelectionModel<T> newValue) {
//          if (oldValue != null) {
//              oldValue.getSelectedIndices().removeListener(weakSelectedIndicesListener);
//          }
//          if (newValue != null) {
//              newValue.getSelectedIndices().addListener(weakSelectedIndicesListener);
//          }
//      }
//  };
//  
//  private final WeakChangeListener<ObservableList<T>> weakItemsListener = 
//          new WeakChangeListener<ObservableList<T>>(itemsListener);
//  private final WeakListChangeListener<Integer> weakSelectedIndicesListener = 
//          new WeakListChangeListener<Integer>(selectedIndicesListener);
//  private final WeakListChangeListener<T> weakItemsListListener = 
//          new WeakListChangeListener<>(itemsListListener);
//  private final WeakChangeListener<MultipleSelectionModel<T>> weakSelectionModelListener = 
//          new WeakChangeListener<MultipleSelectionModel<T>>(selectionModelListener);

}
