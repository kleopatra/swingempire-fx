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
import com.sun.javafx.scene.control.behavior.ListViewBehavior;
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
 * 
 * Refactoring:
 * - extracted methods getFocusModel, getSelectionModel
 * - extracted isNavigable
 * - extracted calls to Runnable/Callable hooks (centralized null checks)
 *  
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
        
        ListCellBehavior c;
        ListViewBehavior lb;
    }

    /**
     * Allow subclasses to install their own listeners.
     * Implemented to do nothing: the original has listeners for items/selectedIndices
     * for the sole issue of keeping the anchor in sync. That's handled by the model now,
     * so we don't need them.
     * @param control
     */
    protected void installListeners(ListView<T> control) {
    }
    
    @Override public void dispose() {
//        ListCellBehavior.removeAnchor(getControl());
        if (tlFocus != null) tlFocus.dispose();
        super.dispose();
    }


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

    private boolean hasSelectionModel() {
        return getSelectionModel() != null;
    }
    
    private boolean hasFocusModel() {
        return getFocusModel() != null;
    }
    
    // PENDING JW: translated German "navigierbar" in http://www.dict.cc/englisch-deutsch/navigable.html
    // leo doesn't know it
    private boolean isNavigable() {
        return hasSelectionModel() && hasFocusModel();
    }

    protected MultipleSelectionModel<T> getSelectionModel() {
        return getControl().getSelectionModel();
    }

    protected FocusModel<T> getFocusModel() {
        return getControl().getFocusModel();
    }

    protected int callIt(Callback<Boolean, Integer> callback, boolean param) {
        return callback != null ? callback.call(param) : - 1;
    }

    protected void runIt(Runnable r) {
        if (r != null) r.run();
    }

    // PENDING JW: missing null check in core
    // doesn't show because the binding is ineffective on a German keyboard
    private void clearSelection() {
        if (hasSelectionModel())
            getSelectionModel().clearSelection();
    }

    // PENDING JW: core calls hook before checking for model
    // do same here
    private void scrollPageUp() {
        int newSelectedIndex = callIt(onScrollPageUp, false);
        if (newSelectedIndex == -1 || !hasSelectionModel()) return;
        getSelectionModel().clearAndSelect(newSelectedIndex);
    }

    // PENDING JW: core calls hook before checking for model
    // do same here
    private void scrollPageDown() {
        int newSelectedIndex = callIt(onScrollPageDown, false);
        if (newSelectedIndex == -1 || !hasSelectionModel()) return;
        getSelectionModel().clearAndSelect(newSelectedIndex);
    }
    
    private void focusFirstRow() {
        if (!hasFocusModel()) return;
        // PENDING JW: use focusFirst
        getFocusModel().focus(0);

        runIt(onMoveToFirstCell);
    }

    private void focusLastRow() {
        if (!hasFocusModel()) return;
        // PENDING JW: use focusLast
        getFocusModel().focus(getRowCount() - 1);
        runIt(onMoveToLastCell);
    }

    private void focusPreviousRow() {
        if (!isNavigable()) return;
        getFocusModel().focusPrevious();
        runIt(onFocusPreviousRow);
    }

    private void focusNextRow() {
        if (!isNavigable()) return;
        getFocusModel().focusNext();
        runIt(onFocusNextRow);
    }
    
    // PENDING JW: logic differs from scrollPageUp
    // here we handle a -1 from hook, in scroll we dont
    private void focusPageUp() {
        int newFocusIndex = callIt(onScrollPageUp, true); //.call(true);
        if (!hasFocusModel()) return;
        getFocusModel().focus(newFocusIndex);
    }
    
    private void focusPageDown() {
        int newFocusIndex = callIt(onScrollPageDown, true); //.call(true);
        if (!hasFocusModel()) return;
        getFocusModel().focus(newFocusIndex);
    }

    /**
     * Expand/collapse selection (if focus isSelected) or select range 
     * between anchor and focus. (if focus is not selected)
     */
    private void alsoSelectPreviousRow() {
        if (!isNavigable()) return;
        int newFocus = getFocusModel().getFocusedIndex() -1;
        selectTo(newFocus);
        
        runIt(onSelectPreviousRow); //.run();
    }

    /**
     * Selects range between anchor and newFocus, inclusive. Anchor is
     * unchanged, focus on newFocus, all selections external to the 
     * range are cleared.
     * 
     * Note: implemented to simply call clearSelection - might need to 
     * use clearOutside or enhanced api on model (clearAndSelectRange)
     * to keep notifications l
     * @param newFocus
     */
    protected void selectTo(int newFocus) {
        MultipleSelectionModel<T> sm = getSelectionModel();
        AnchoredSelectionModel am = (AnchoredSelectionModel) sm;
        // NOTE JW: DON't inline! We clear the selection thus removing the anchor
        int anchor = am.getAnchorIndex();
        boolean ascending = anchor < newFocus;
        int boundary = ascending ? newFocus + 1 : newFocus - 1;
        // triggers notification on too many change indices 
        // here we need a clearAndSelectRange on the model
        // which could optimimize
        sm.clearSelection();
        sm.selectRange(anchor, boundary);
    }

    private void alsoSelectNextRow() {
        if (!isNavigable()) return;
        int newFocus = getFocusModel().getFocusedIndex() + 1;
        selectTo(newFocus);
        runIt(onSelectNextRow); //.run();
    }
    
    private void selectPreviousRow() {
        if (!isNavigable()) return;
        int focusIndex = getFocusModel().getFocusedIndex();
        // PENDING JW: selectionModel is doc'ed to ignore off-range index
        // so don't need to check here?
        if (focusIndex <= 0) {
            return;
        }
        getSelectionModel().clearAndSelect(focusIndex - 1);
        runIt(onSelectPreviousRow); //.run();
    }

    private void selectNextRow() {
        if (!isNavigable()) return;
        int focusIndex = getFocusModel().getFocusedIndex();
        // PENDING JW: selectionModel is doc'ed to ignore off-range index
        // so don't need to check here?
        if (focusIndex == getRowCount() - 1) {
            return;
        }
        getSelectionModel().clearAndSelect(focusIndex + 1);
        runIt(onSelectNextRow); //.run();
    }

    private void selectFirstRow() {
        if (!hasSelectionModel()) return;
        // PENDING JW: why check for size here but not in selectLastRow?
        if (getRowCount() > 0) {
            // PENDING JW: use selectFirst (with clearSelection before)
            // or enhance api
            getSelectionModel().clearAndSelect(0);
            runIt(onMoveToFirstCell);
        }
    }

    private void selectLastRow() {
        if (!hasSelectionModel()) return;
        getSelectionModel().clearAndSelect(getRowCount() - 1);
        runIt(onMoveToLastCell); //.run();
    }
    
    /**
     * re-implement: same logic as alsoSelectNext/Previous
     * except that the boundary is returned by onScrollPageUp
     * 
     * NOTE JW: the fix for RT-34407 mentioned in the old method 
     * is the adjust for ascending or not
     */
    private void selectAllPageUp() {
        if (!isNavigable()) return;
        // PENDING JW: return if -1, similar to scrollPageUp?
        int newFocus = callIt(onScrollPageUp, false); //.call(false);
        selectTo(newFocus);
    }
    
    /**
     * re-implement: same logic as alsoSelectNext/Previous
     * except that the boundary is returned by onScrollPageUp
     */
    private void selectAllPageDown() {
        if (!isNavigable()) return;
        // PENDING JW: return if -1, similar to scrollPageDown?
        int newFocus = callIt(onScrollPageDown, false); //.call(false);
        selectTo(newFocus);
    }


    /** 
     */ 
    private void selectAllToFirstRow() {
        if (!hasSelectionModel()) return;
        MultipleSelectionModel<T> sm = getSelectionModel();
        
        int anchor = ((AnchoredSelectionModel) sm).getAnchorIndex();
        sm.clearSelection();
        // range from anchor to 0: as the second boundary is non-inclusive ...
        sm.selectRange(anchor, -1);
        runIt(onMoveToFirstCell);
        /*  Just for reference, commented the issue
        sm.clearSelection();
        // JW: incorrect usage - the last of range will be the focus
        // so fix for RT-18413 would be to switch arguments
        sm.selectRange(0, leadIndex + 1);
        // this is the fix which acutally is a hack
        getFocusModel().focus(0);
         */
    }
    /**
     * misbehaviour in core (ListView):
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
    private void selectAllToLastRow() {
        if (!hasSelectionModel()) return;
        MultipleSelectionModel<T> sm = getSelectionModel();
        
        int anchor = ((AnchoredSelectionModel) sm).getAnchorIndex();
        sm.clearSelection();
        // range from anchor to last: as the second boundary is non-inclusive ...
        sm.selectRange(anchor, getRowCount());
        runIt(onMoveToLastCell); //.run();
        
    }
    
    private void selectAll() {
        if (!hasSelectionModel()) return;
        getSelectionModel().selectAll();
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
        if (!isNavigable()) return;
        // Fix for RT-31241
        if (getControl().getEditingIndex() >= 0) return;
        int newFocus = getFocusModel().getFocusedIndex();
        selectTo(newFocus);
        AnchoredSelectionModel am = (AnchoredSelectionModel) getSelectionModel();
        if (setAnchorToFocusIndex) am.anchor();
        
    }
    
    private void cancelEdit() {
        getControl().edit(-1);
    }

    private void activate() {
        int focusedIndex = getFocusModel().getFocusedIndex();
        getSelectionModel().select(focusedIndex);
        // PENDING JW: really need anchor on editing index?
        // CHANGED JW: commented old
//        setAnchor(focusedIndex);

        // edit this row also
        if (focusedIndex >= 0) {
            getControl().edit(focusedIndex);
        }
    }
    
    private void toggleFocusOwnerSelection() {
        if (!isNavigable()) return;
        MultipleSelectionModel<T> sm = getSelectionModel();
        FocusModel<T> fm = getFocusModel();

        int focusedIndex = fm.getFocusedIndex();
        if (sm.isSelected(focusedIndex)) {
            sm.clearSelection(focusedIndex);
            fm.focus(focusedIndex);
        } else {
            sm.select(focusedIndex);
        }
        ((AnchoredSelectionModel) sm).anchor();
    }
    
    /**************************************************************************
     * Discontinuous Selection                         
     * PENDING JW: not all yet updated to anchoredModel                       *
     *************************************************************************/
    
    
    private void discontinuousSelectPreviousRow() {
        if (!isNavigable()) return;
        int newFocus = getFocusModel().getFocusedIndex() -1;
        MultipleSelectionModel<T> sm = getSelectionModel();
        AnchoredSelectionModel am = (AnchoredSelectionModel) sm;
        int anchorIndex = am.getAnchorIndex();
        boolean ascending = anchorIndex < newFocus;
        int boundary = ascending ? newFocus + 1 : newFocus - 1;
        sm.selectRange(anchorIndex, boundary);
        runIt(onFocusPreviousRow);
    }
    
    /**
     * we would need to re-anchor? Not here, that's done
     * by the user (f.i. via ctrl/-space)
     * 
     * same code as in alsoSelectNextRow except for not clearing the
     * selection
     */
    private void discontinuousSelectNextRow() {
        if (!isNavigable()) return;
        MultipleSelectionModel<T> sm = getSelectionModel();
        AnchoredSelectionModel am = (AnchoredSelectionModel) sm;
        int newFocus = getFocusModel().getFocusedIndex() + 1;
        int anchor = am.getAnchorIndex();
        boolean ascending = anchor < newFocus;
        int boundary = ascending ? newFocus + 1 : newFocus - 1;
        sm.selectRange(anchor, boundary);
        runIt(onFocusNextRow);
    }
    
    // PENDING JW: didn't really touch the methods below
    // shouldn't it be very similar to selectPageUp (except for not clearing)?
    private void discontinuousSelectPageUp() {
        if (!isNavigable()) return;
        // PENDING JW: why no back-out if callback returns -1?
        int leadSelectedIndex = callIt(onScrollPageUp, false); //.call(false);
        // PENDING JW: shouldn't we go from anchor (instead of focus)
        int leadIndex = getFocusModel().getFocusedIndex();
        getSelectionModel().selectRange(leadIndex, leadSelectedIndex - 1);
    }
    
    private void discontinuousSelectPageDown() {
        if (!isNavigable()) return;
        // PENDING JW: why no back-out if callback returns -1?
        int leadSelectedIndex = callIt(onScrollPageDown, false); //.call(false);
        // PENDING JW: shouldn't we go from anchor (instead of focus)
        int leadIndex = getFocusModel().getFocusedIndex();
        getSelectionModel().selectRange(leadIndex, leadSelectedIndex + 1);
    }
    
    private void discontinuousSelectAllToFirstRow() {
        if (!isNavigable()) return;
        FocusModel<T> fm = getFocusModel();
        int index = fm.getFocusedIndex();
        // PENDING JW: wrong way round again? new focus should be on 0
        getSelectionModel().selectRange(0, index);
        // if range is done correctly, we don't need to explicitly focus!
        fm.focus(0);

        runIt(onMoveToFirstCell);
    }
    
    /**
     * PENDING JW: probably same error as in selectAllToLast - extend from anchor, not
     * focus?
     */
    private void discontinuousSelectAllToLastRow() {
        if (!isNavigable()) return;
        int index = getFocusModel().getFocusedIndex() + 1;
        getSelectionModel().selectRange(index, getRowCount());

        runIt(onMoveToLastCell);
    }

    /**
     * Unused - keeping in case we want to be less aggressive than clearSelection
     * @param start
     * @param end
     */
    private void clearSelectionOutsideRange(int start, int end) {
        MultipleSelectionModel<T> sm = getSelectionModel();
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


}
