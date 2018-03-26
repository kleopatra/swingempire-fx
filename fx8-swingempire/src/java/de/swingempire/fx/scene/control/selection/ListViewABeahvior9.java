/*
 * Created on 24.02.2016
 *
 */
package de.swingempire.fx.scene.control.selection;


import java.util.ArrayList;
import java.util.List;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.behavior.ListCellBehavior;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusListBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.InputMap.MouseMapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import com.sun.javafx.scene.control.skin.Utils;

import static javafx.scene.input.KeyCode.*;

import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

/**
* PENDING JW: the api doc is copied from the old (jdk8u20) version, 
* need to check for jdk9 - ntohing tested yet!!
* --------------------
* 
* Copy from 9ea-104 and mixed with improved jdk8 version to
*  rely on selectionModel to cope with anchor. 
* The driving force is to 
* replace anchor handling here by anchor handling in selectionModel. See
* https://javafx-jira.kenai.com/browse/RT-38509
* 
* Note: at first tried to extend core ListViewBehaviour but didn't work (too much
* privacy) - so c&p'd and changed the copy.
* 
* NOTE: List/CellBehaviour fiddles with Anchor on mousePressed!!
* Need to adjust as well? But then, we dont care as we are not listening. 
* 
* 
* Changes (incomplete probably):
* -- here: commented listeners, they are for anchor handling which now is the job of
*    the selectionModel 
* - copied every target method (to the binding) from the 8-version    
*   
* PENDING JW:
* - alsoSelectNext/Previous now always clears the selection and selects
*   a range: consequence are more notifications than before
*   see code comment in alsoSelectPrevious
* - discontinous modes not yet fully implemented (but then, code looks fishy in core)
*   the difference between ctrl-shift-navigation and shift-navigation is
*   (according to ux) that only the latter unselects all outside the range 
* - test corner cases (empty items, anchor/focus at first last)  
* 
* Refactoring:
* - extracted methods getFocusModel, getSelectionModel
* - extracted isNavigable
* - extracted calls to Runnable/Callable hooks (centralized null checks)
* - extracted the actual extend into <code>selectTo(newIndex, clear)</code> 
*/

public class ListViewABeahvior9<T> extends BehaviorBase<ListView<T>> {
    private final InputMap<ListView<T>> listViewInputMap;

    /**
     * Indicates that a keyboard key has been pressed which represents the
     * event (this could be space bar for example). As long as keyDown is true,
     * we are also armed, and will ignore mouse events related to arming.
     * Note this is made package private solely for the sake of testing.
     */
    private boolean keyDown;

    private final EventHandler<KeyEvent> keyEventListener = e -> {
        if (!e.isConsumed()) {
            // RT-12751: we want to keep an eye on the user holding down the shift key,
            // so that we know when they enter/leave multiple selection mode. This
            // changes what happens when certain key combinations are pressed.
            isShiftDown = e.getEventType() == KeyEvent.KEY_PRESSED && e.isShiftDown();
            isShortcutDown = e.getEventType() == KeyEvent.KEY_PRESSED && e.isShortcutDown();
        }
    };



    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public ListViewABeahvior9(ListView<T> control) {
        super(control);
        // create a map for listView-specific mappings
        listViewInputMap = createInputMap();

        // add focus traversal mappings
        addDefaultMapping(listViewInputMap, FocusTraversalInputMap.getFocusTraversalMappings());
        addDefaultMapping(listViewInputMap,
            new KeyMapping(HOME, e -> selectFirstRow()),
            new KeyMapping(END, e -> selectLastRow()),
            new KeyMapping(new KeyBinding(HOME).shift(), e -> selectAllToFirstRow()),
            new KeyMapping(new KeyBinding(END).shift(), e -> selectAllToLastRow()),
            new KeyMapping(new KeyBinding(PAGE_UP).shift(), e -> selectAllPageUp()),
            new KeyMapping(new KeyBinding(PAGE_DOWN).shift(), e -> selectAllPageDown()),

            new KeyMapping(new KeyBinding(SPACE).shift(), e -> selectAllToFocus(false)),
            new KeyMapping(new KeyBinding(SPACE).shortcut().shift(), e -> selectAllToFocus(true)),

            new KeyMapping(PAGE_UP, e -> scrollPageUp()),
            new KeyMapping(PAGE_DOWN, e -> scrollPageDown()),

            new KeyMapping(ENTER, e -> activate()),
            new KeyMapping(SPACE, e -> activate()),
            new KeyMapping(F2, e -> activate()),
            new KeyMapping(ESCAPE, e -> cancelEdit()),

            new KeyMapping(new KeyBinding(A).shortcut(), e -> selectAll()),
            new KeyMapping(new KeyBinding(HOME).shortcut(), e -> focusFirstRow()),
            new KeyMapping(new KeyBinding(END).shortcut(), e -> focusLastRow()),
            new KeyMapping(new KeyBinding(PAGE_UP).shortcut(), e -> focusPageUp()),
            new KeyMapping(new KeyBinding(PAGE_DOWN).shortcut(), e -> focusPageDown()),

            new KeyMapping(new KeyBinding(BACK_SLASH).shortcut(), e -> clearSelection()),

            new MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed)
        );

        // create OS-specific child mappings
        // --- mac OS
        InputMap<ListView<T>> macInputMap = new InputMap<>(control);
        macInputMap.setInterceptor(event -> !PlatformUtil.isMac());
        addDefaultMapping(macInputMap, new KeyMapping(new KeyBinding(SPACE).shortcut().ctrl(), e -> toggleFocusOwnerSelection()));
        addDefaultChildMap(listViewInputMap, macInputMap);

        // --- all other platforms
        InputMap<ListView<T>> otherOsInputMap = new InputMap<>(control);
        otherOsInputMap.setInterceptor(event -> PlatformUtil.isMac());
        addDefaultMapping(otherOsInputMap, new KeyMapping(new KeyBinding(SPACE).ctrl(), e -> toggleFocusOwnerSelection()));
        addDefaultChildMap(listViewInputMap, otherOsInputMap);

        // create two more child maps, one for vertical listview and one for horizontal listview
        // --- vertical listview
        InputMap<ListView<T>> verticalListInputMap = new InputMap<>(control);
        verticalListInputMap.setInterceptor(event -> control.getOrientation() != Orientation.VERTICAL);

        addDefaultMapping(verticalListInputMap,
            new KeyMapping(UP, e -> selectPreviousRow()),
            new KeyMapping(KP_UP, e -> selectPreviousRow()),
            new KeyMapping(DOWN, e -> selectNextRow()),
            new KeyMapping(KP_DOWN, e -> selectNextRow()),

            new KeyMapping(new KeyBinding(UP).shift(), e -> alsoSelectPreviousRow()),
            new KeyMapping(new KeyBinding(KP_UP).shift(), e -> alsoSelectPreviousRow()),
            new KeyMapping(new KeyBinding(DOWN).shift(), e -> alsoSelectNextRow()),
            new KeyMapping(new KeyBinding(KP_DOWN).shift(), e -> alsoSelectNextRow()),

            new KeyMapping(new KeyBinding(UP).shortcut(), e -> focusPreviousRow()),
            new KeyMapping(new KeyBinding(DOWN).shortcut(), e -> focusNextRow()),

            new KeyMapping(new KeyBinding(UP).shortcut().shift(), e -> discontinuousSelectPreviousRow()),
            new KeyMapping(new KeyBinding(DOWN).shortcut().shift(), e -> discontinuousSelectNextRow()),
            new KeyMapping(new KeyBinding(PAGE_UP).shortcut().shift(), e -> discontinuousSelectPageUp()),
            new KeyMapping(new KeyBinding(PAGE_DOWN).shortcut().shift(), e -> discontinuousSelectPageDown()),
            new KeyMapping(new KeyBinding(HOME).shortcut().shift(), e -> discontinuousSelectAllToFirstRow()),
            new KeyMapping(new KeyBinding(END).shortcut().shift(), e -> discontinuousSelectAllToLastRow())
        );

        addDefaultChildMap(listViewInputMap, verticalListInputMap);

        // --- horizontal listview
        InputMap<ListView<T>> horizontalListInputMap = new InputMap<>(control);
        horizontalListInputMap.setInterceptor(event -> control.getOrientation() != Orientation.HORIZONTAL);

        addDefaultMapping(horizontalListInputMap,
            new KeyMapping(LEFT, e -> selectPreviousRow()),
            new KeyMapping(KP_LEFT, e -> selectPreviousRow()),
            new KeyMapping(RIGHT, e -> selectNextRow()),
            new KeyMapping(KP_RIGHT, e -> selectNextRow()),

            new KeyMapping(new KeyBinding(LEFT).shift(), e -> alsoSelectPreviousRow()),
            new KeyMapping(new KeyBinding(KP_LEFT).shift(), e -> alsoSelectPreviousRow()),
            new KeyMapping(new KeyBinding(RIGHT).shift(), e -> alsoSelectNextRow()),
            new KeyMapping(new KeyBinding(KP_RIGHT).shift(), e -> alsoSelectNextRow()),

            new KeyMapping(new KeyBinding(LEFT).shortcut(), e -> focusPreviousRow()),
            new KeyMapping(new KeyBinding(RIGHT).shortcut(), e -> focusNextRow()),

            new KeyMapping(new KeyBinding(LEFT).shortcut().shift(), e -> discontinuousSelectPreviousRow()),
            new KeyMapping(new KeyBinding(RIGHT).shortcut().shift(), e -> discontinuousSelectNextRow())
        );

        addDefaultChildMap(listViewInputMap, horizontalListInputMap);

        // set up other listeners
        // We make this an event _filter_ so that we can determine the state
        // of the shift key before the event handlers get a shot at the event.
        control.addEventFilter(KeyEvent.ANY, keyEventListener);

//        control.itemsProperty().addListener(weakItemsListener);
//        if (control.getItems() != null) {
//            control.getItems().addListener(weakItemsListListener);
//        }
//
//        // Fix for RT-16565
//        control.selectionModelProperty().addListener(weakSelectionModelListener);
//        if (control.getSelectionModel() != null) {
//            control.getSelectionModel().getSelectedIndices().addListener(weakSelectedIndicesListener);
//        }

        // Only add this if we're on an embedded platform that supports 5-button navigation
        if (Utils.isTwoLevelFocus()) {
            tlFocus = new TwoLevelFocusListBehavior(control); // needs to be last.
        }
    }



    /***************************************************************************
     *                                                                         *
     * Implementation of BehaviorBase API                                      *
     *                                                                         *
     **************************************************************************/

    @Override public InputMap<ListView<T>> getInputMap() {
        return listViewInputMap;
    }

    @Override public void dispose() {
        ListView<T> control = getNode();
        // PENDING JW: keep this as cleanup (static anchoring still used in
        // mouse event handler of CellBehavior

        ListCellBehavior.removeAnchor(control);
        if (tlFocus != null) tlFocus.dispose();
        super.dispose();

        control.removeEventHandler(KeyEvent.ANY, keyEventListener);
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

//    private final ListChangeListener<Integer> selectedIndicesListener = c -> {
//        int newAnchor = getAnchor();
//
//        while (c.next()) {
//            if (c.wasReplaced()) {
//                if (ListCellBehavior.hasDefaultAnchor(getNode())) {
//                    ListCellBehavior.removeAnchor(getNode());
//                    continue;
//                }
//            }
//
//            final int shift = c.wasPermutated() ? c.getTo() - c.getFrom() : 0;
//
//            MultipleSelectionModel<T> sm = getNode().getSelectionModel();
//
//            // there are no selected items, so lets clear out the anchor
//            if (! selectionChanging) {
//                if (sm.isEmpty()) {
//                    newAnchor = -1;
//                } else if (hasAnchor() && ! sm.isSelected(getAnchor() + shift)) {
//                    newAnchor = -1;
//                }
//            }
//
//            // we care about the situation where the selection changes, and there is no anchor. In this
//            // case, we set a new anchor to be the selected index
//            if (newAnchor == -1) {
//                int addedSize = c.getAddedSize();
//                newAnchor = addedSize > 0 ? c.getAddedSubList().get(addedSize - 1) : newAnchor;
//            }
//        }
//
//        if (newAnchor > -1) {
//            setAnchor(newAnchor);
//        }
//    };
//
//    private final ListChangeListener<T> itemsListListener = c -> {
//        while (c.next()) {
//            if (!hasAnchor()) continue;
//
//            int newAnchor = (hasAnchor() ? getAnchor() : 0);
//
//            if (c.wasAdded() && c.getFrom() <= newAnchor) {
//                newAnchor += c.getAddedSize();
//            } else if (c.wasRemoved() && c.getFrom() <= newAnchor) {
//                newAnchor -= c.getRemovedSize();
//            }
//
//            setAnchor(newAnchor < 0 ? 0 : newAnchor);
//        }
//    };
//
//    private final ChangeListener<ObservableList<T>> itemsListener = new ChangeListener<ObservableList<T>>() {
//        @Override
//        public void changed(
//                ObservableValue<? extends ObservableList<T>> observable,
//                ObservableList<T> oldValue, ObservableList<T> newValue) {
//            if (oldValue != null) {
//                oldValue.removeListener(weakItemsListListener);
//            } if (newValue != null) {
//                newValue.addListener(weakItemsListListener);
//            }
//        }
//    };
//
//    private final ChangeListener<MultipleSelectionModel<T>> selectionModelListener = new ChangeListener<MultipleSelectionModel<T>>() {
//        @Override public void changed(
//                ObservableValue<? extends MultipleSelectionModel<T>> observable,
//                MultipleSelectionModel<T> oldValue,
//                MultipleSelectionModel<T> newValue) {
//            if (oldValue != null) {
//                oldValue.getSelectedIndices().removeListener(weakSelectedIndicesListener);
//            }
//            if (newValue != null) {
//                newValue.getSelectedIndices().addListener(weakSelectedIndicesListener);
//            }
//        }
//    };
//
//    private final WeakChangeListener<ObservableList<T>> weakItemsListener =
//            new WeakChangeListener<ObservableList<T>>(itemsListener);
//    private final WeakListChangeListener<Integer> weakSelectedIndicesListener =
//            new WeakListChangeListener<Integer>(selectedIndicesListener);
//    private final WeakListChangeListener<T> weakItemsListListener =
//            new WeakListChangeListener<>(itemsListListener);
//    private final WeakChangeListener<MultipleSelectionModel<T>> weakSelectionModelListener =
//            new WeakChangeListener<MultipleSelectionModel<T>>(selectionModelListener);
//
    private TwoLevelFocusListBehavior tlFocus;

    // CHANGED JW: removed anchor-related methods
//    private void setAnchor(int anchor) {
//        ListCellBehavior.setAnchor(getNode(), anchor < 0 ? null : anchor, false);
//    }
//
//    private int getAnchor() {
//        return ListCellBehavior.getAnchor(getNode(), getNode().getFocusModel().getFocusedIndex());
//    }
//
//    private boolean hasAnchor() {
//        return ListCellBehavior.hasNonDefaultAnchor(getNode());
//    }

    private void mousePressed(MouseEvent e) {
     // CHANGED JW: commented anchor setting        
//       if (! e.isShiftDown() && ! e.isSynthesized()) {
//            int index = getNode().getSelectionModel().getSelectedIndex();
//            setAnchor(index);
//        }

        if (! getNode().isFocused() && getNode().isFocusTraversable()) {
            getNode().requestFocus();
        }
    }

    //--------------- Utility methods    
    protected int getRowCount() {
        return getNode().getItems() == null ? 0 : getNode().getItems().size();
    }

    /**
     * Returns true if the control has a SelectionModel != null, false otherwise.
     * @return
     */
    protected boolean hasSelectionModel() {
        return getSelectionModel() != null;
    }
    
    /**
     * Returns true if the control has a FocusModel != null, false otherwise.
     * @return
     */
    protected boolean hasFocusModel() {
        return getFocusModel() != null;
    }
    
    /**
     * Returns true if the control has both a selection- and a focusModel, false
     * otherwise.
     * PENDING JW: translated German "navigierbar" in http://www.dict.cc/englisch-deutsch/navigable.html
     * leo doesn't know it
     * @return
     */
    protected boolean isNavigable() {
        return hasSelectionModel() && hasFocusModel();
    }

    /**
     * Convenience wrapper to access the control's selectionModel
     * @return
     */
    protected MultipleSelectionModel<T> getSelectionModel() {
        return getNode().getSelectionModel();
    }

    /**
     * Convenience wrapper to access the control's focusModel.
     * @return
     */
    protected FocusModel<T> getFocusModel() {
        return getNode().getFocusModel();
    }

    /**
     * Calls the callback and returns the value, if != null. Returns -1 otherwise.
     *  
     * @param callback the callback to call, may be null.
     * @param param the parameter to pass into the callable
     * 
     * @return the return value of the callable if != null, -1 otherwise
     */
    protected int callIt(Callback<Boolean, Integer> callback, boolean param) {
        return callback != null ? callback.call(param) : - 1;
    }

    /**
     * Runs the given runnable if it is != null. Does nothing otherwise.
     * @param r the runnable to run, may be null.
     */
    protected void runIt(Runnable r) {
        if (r != null) r.run();
    }


// ------ end utility methods    
    
    // PENDING JW: missing null check in core
    // doesn't show because the binding is ineffective on a German keyboard
    private void clearSelection() {
        getNode().getSelectionModel().clearSelection();
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
        // PENDING JW: why navigable here but not in focusLast/First?
        // copied from core
        if (!isNavigable()) return;
        getFocusModel().focusPrevious();
        runIt(onFocusPreviousRow);
    }

    private void focusNextRow() {
        // PENDING JW: why navigable here but not in focusLast/First?
        // copied from core
        if (!isNavigable()) return;
        getFocusModel().focusNext();
        runIt(onFocusNextRow);
    }
    

    // PENDING JW: logic differs from scrollPageUp/Down
    // here we don't handle a -1 from hook, in scroll we do?
    private void focusPageUp() {
        int newFocusIndex = callIt(onScrollPageUp, true); //.call(true);
        if (!hasFocusModel()) return;
        getFocusModel().focus(newFocusIndex);
    }
    
    // PENDING JW: logic differs from scrollPageUp/Down
    // here we don't handle a -1 from hook, in scroll we do?
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
        selectTo(newFocus, true);
        
        runIt(onSelectPreviousRow); //.run();
    }

    /**
     * Selects range between anchor and newFocus, inclusive. Anchor is
     * unchanged, focus on newFocus.  Selections external to the 
     * range are cleared, if clearOutside is true.<p>
     * 
     * PENDING JW: implemented to simply call clearSelection - might need to 
     * use clearOutside or enhanced api on model (clearAndSelectRange)
     * to keep notifications low. On the other hand, most core methods
     * (exceptions: alsoSelectNext/PrevRow, and single mode)
     * also simply call clearSelection before selecting the range. 
     * A side-effect of clearSelection is that the later rangeSelection
     * will update anchor/focus automatically: if we would call clearSelectionOutside
     * of this class manually, we would have to take care of them ourselves. 
     * Or let an enhanced selectionModel api do it.
     * 
     * <p>
     * PENDING JW: check effect of newFocus < 0 or >= itemCount 
     * 
     * @param newFocus the end of range to select to, inclusive
     * @param clearOutside flag to control selections outside of the range -
     *   if true, they are cleared, otherwise kept as are
     */
    protected void selectTo(int newFocus, boolean clearOutside) {
        MultipleSelectionModel<T> sm = getSelectionModel();
        AnchoredSelectionModel am = (AnchoredSelectionModel) sm;
        // NOTE JW: DON't inline! We may clear the selection thus removing the anchor
        int anchor = am.getAnchorIndex();
        int boundary = anchor < newFocus ? newFocus + 1 : newFocus - 1;
        // triggers notification on too many change indices 
        // here we need a clearAndSelectRange on the model
        // which could optimimize
        if (clearOutside) {
            sm.clearSelection();
        }
        sm.selectRange(anchor, boundary);
    }
    private void alsoSelectNextRow() {
        if (!isNavigable()) return;
        int newFocus = getFocusModel().getFocusedIndex() + 1;
        selectTo(newFocus, true);
        runIt(onSelectNextRow); //.run();
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
        selectTo(newFocus, true);
    }
    

    /**
     * re-implement: same logic as alsoSelectNext/Previous
     * except that the boundary is returned by onScrollPageUp
     */
    private void selectAllPageDown() {
        if (!isNavigable()) return;
        // PENDING JW: return if -1, similar to scrollPageDown?
        int newFocus = callIt(onScrollPageDown, false); //.call(false);
        selectTo(newFocus, true);
    }

    /** 
     */ 
    private void selectAllToFirstRow() {
        if (!hasSelectionModel()) return;
        selectTo(0, true);
        runIt(onMoveToFirstCell);
        /*  
            Just for reference, commented the issue
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
        selectTo(getRowCount() - 1, true);
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
     * 
     * 
     * @param setAnchorToFocusIndex controls behaviour of anchor in multiple selection mode:
     *   if true anchors the focusedIndex, if false leaves the anchor unchanged.
     */
    private void selectAllToFocus(boolean setAnchorToFocusIndex) {
        if (!isNavigable()) return;
        // Fix for RT-31241
        if (getNode().getEditingIndex() >= 0) return;
        int newFocus = getFocusModel().getFocusedIndex();
        selectTo(newFocus, true);
        AnchoredSelectionModel am = (AnchoredSelectionModel) getSelectionModel();
        if (setAnchorToFocusIndex) am.anchor();
    }
    
    private void cancelEdit() {
        getNode().edit(-1);
    }

    private void activate() {
        int focusedIndex = getFocusModel().getFocusedIndex();
        getSelectionModel().select(focusedIndex);
        // PENDING JW: really need anchor on editing index?
        // CHANGED JW: commented old
//        setAnchor(focusedIndex);

        // edit this row also
        if (focusedIndex >= 0) {
            getNode().edit(focusedIndex);
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
        selectTo(newFocus, false);
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
        int newFocus = getFocusModel().getFocusedIndex() + 1;
        selectTo(newFocus, false);
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
}
