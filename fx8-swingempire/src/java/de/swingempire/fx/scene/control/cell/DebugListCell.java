/*
 * Created on 11.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import de.swingempire.fx.scene.control.ControlUtils;
import de.swingempire.fx.util.FXUtils;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;

/**
 * Custom ListCell which overrides start/commit/cancelEdit and
 * takes over completely (c&p from super and reflective access to Cell methods).
 * This is (mostly) done to understand the editing mechanism. 
 * <p>
 * 
 * Bug fixes:
 * <ul>
 * <li> https://bugs.openjdk.java.net/browse/JDK-8187307 - 
 *      The problem is that skin cancels any edit if it detects a change
 *      of items. This change happens on commit. So the idea is to ignore
 *      the cancel request during firing the editCommitEvent. So the fix 
 *      consists of two parts: a) in commitEdit, surround firing of editCommit 
 *      with a ignoreCancel flag 
 *      b) in cancelEdit, do nothing if ignoreCancel() 
 * <li> https://bugs.openjdk.java.net/browse/JDK-8187432 - 
 *      in startEdit, pass correct index into editStartEvent 
 *      (core code looks fixed in fx10 webrev http://cr.openjdk.java.net/~jgiles/8089514.1/)
 * <li> https://bugs.openjdk.java.net/browse/JDK-8187226 -
 *      in cancelEdit, pass correct index into editCancelEvent
 *      (core code looks fixed in fx10 webrev http://cr.openjdk.java.net/~jgiles/8089514.1/)
 * </ul>
 * 
 * Note: the problem with skin canceling the edit is the same for all 
 * virtual controls, though not so obvious in Tree-/TableView for different 
 * reasons. Hacking around the problem with a flag seems whacky, but we might
 * get away with it. Not thoroughly tested, though, beware!
 * 
 * <p>
 * 
 * Now testing another approach. The reason the cell is disturbed by backing
 * data being changed (triggered by controls skin or any other collaborator)
 * seems to be that it is still in editing state. So changing the sequence of
 * steps from
 * 
 * Core commitEdit FX9:
 * <ul>
 * <li> notify commitHandler
 * <li> switch out editing state 
 * <li> updateVisuals
 * <li> update editing location
 * </ul>
 * 
 * Current (pre-jan 2018) here:
 * <ul>
 * <li> switch out editing state // no need for ignoreCancel
 * <li> update editing location  // allow modifications of the list in commitHandler 
 * <li> fire commithandler       
 * <li> update visuals
 * </ul>
 * 
 * That's similar to fx10 webrev http://cr.openjdk.java.net/~jgiles/8089514.1/)
 *
 * Current (jan-2018)
 * <ol>
 * <li> switch out editing state
 * <li> update visuals 
 * <li> update editing location
 * <li> notify commithandler
 * </ol>
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugListCell<T> extends ListCell<T> implements CellDecorator<ListView<T>, T> {

    private static int counter;
    private final int myId;
    private boolean ignoreCancel;

    /** 
     * the idea was to not reset editing in commitEditing if
     * list commitHandler did somehow start another edit.
     * 
     * didn't work out - gets totally confused.
     */
//    private static Consumer<ListView> defaultPostCommit = list -> {
//        list.edit(-1);
////        ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(list);
//    };
//    
//    private Consumer<ListView> postCommit;
//    
//    public void setPostCommit(Consumer<ListView> postCommit) {
//        this.postCommit = postCommit;
//    }
//    
//    public Consumer<ListView> getPostCommit() {
//        return postCommit != null ? postCommit :  defaultPostCommit;
//    }
    
    //------------- end of idea
    
    /**
     * Debug ... to identify a single cell.
     */
    public DebugListCell() {
        myId = counter++;
    }
    
    @Override
    public int getCounter() {
        return myId;
    }
    
    /**
     * {@inheritDoc} <p>
     * Basically, a c&p of super except:
     * 
     * <ul>
     * <li> pass correct index into editStart, fix for https://bugs.openjdk.java.net/browse/JDK-8187432
     * <li> do nothing if !canStartEdit
     * <li> back out if super.startEdit failed to switch into editing state
     * </ul>
     * 
     * Order of mods both in core and here:
     * <ol>
     * <li> switch into editing state
     * <li> notify start handlers
     * <li> update editing location
     * </ol>
     * 
     * @see #canStartEdit()
     */
    @Override
    public void startEdit() {
        // PENDING JW: core is inconsistent if editing - tree returns, list/table start again
//        if (isEditing()) return;
        if (!canStartEdit()) return;
        final ListView<T> list = getListView();
        
        cellStartEdit();
        // PENDING JW:shouldn't we back out if !isEditing? That is when
        // super refused to switch into editing state?
        if (!isEditing()) return;
        // Inform the ListView of the edit starting.
        if (list != null) {
            list.fireEvent(new ListView.EditEvent<T>(list,
                            ListView.<T>editStartEvent(),
                            null,
                            // PENDING JW: this looks fishy - the index that will become 
                            // editing is not the list's but this cell's index
                            // reported as https://bugs.openjdk.java.net/browse/JDK-8187432
            //                    list.getEditingIndex()
                            // changed to our own index
                            getIndex()
                            ));
            list.edit(getIndex());
            list.requestFocus();
        }
    }

    /**
     * {@inheritDoc} <p>
     * Basically, a c&p of super except:
     * 
     * <ul>
     * <li> surround firing of commitEvent with ignoreCancel, 
     *    one part of fixing https://bugs.openjdk.java.net/browse/JDK-8187432
     * </ul>
     * 
     * Order of mods core:
     * <ol>
     * <li> notify commitHandler
     * <li> switch out editing state: if done first, no need for ignoreCancel
     * <li> updateVisuals
     * <li> update editing location
     * </ol>
     * 
     * Order of mods here:
     * <ol>
     * <li> switch out editing state
     * <li> update visuals 
     * <li> update editing location
     * <li> notify commithandler
     * </ol>
     * 
     * now can modify items during commit without receiving cancel Note that 
     * update visuals must happen before update of editing location
     */
    @Override
    public void commitEdit(T newValue) {
        if (! isEditing()) return;

        // this order of actions allows for dynamic adding item/start edit in list
        resetEditingFlag(newValue);
        updateCellVisuals(newValue);
        resetEditingLocation();
        notifyCommitHandler(newValue);
        
        // this order is older, suggested in my git wiki on cell edit events
        // with this we get problems with dynamic adding item/start edit in list
        // a newly added item is incorrect if a cell needed to be created on addition
//        resetEditingFlag(newValue);
//        resetEditingLocation();
//        notifyCommitHandler(newValue);
//        updateCellVisuals(newValue);

        // this is order in core
        // looks okay, but assumption of list.editingIndex == cell.index fails
        // tentative reason: the default handler sets the value (aka: change data)
        // and skin cancels edit ... so can't do this for full control!
//        notifyCommitHandler(newValue);
//        resetEditingFlag(newValue);
//        updateCellVisuals(newValue);
//        resetEditingLocation();
//        if (list != null) {
            // reset the editing index on the ListView. This must come after the
            // event is fired so that the developer on the other side can consult
            // the ListView editingIndex property (if they choose to do that
            // rather than just grab the int from the event).
            // JW: disagree - the event carries all that's needed, the other way round:
            // there's no spec regarding the state of the listView at the time of
            // notifying the edit handler
            //  list.edit(-1);

            // request focus back onto the list, only if the current focus
            // owner has the list as a parent (otherwise the user might have
            // clicked out of the list entirely and given focus to something else.
            // It would be rude of us to request it back again.
            // JW: can be rude anyway, f.i. when new edit has started on another cell
            // ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(list);
//        }
    }

    
//------------------------ api to easily play with order of actions in commit
    
    protected void resetEditingFlag(T newValue) {
        // inform parent classes of the commit, so that they can switch us
        // out of the editing state.
        // This MUST come before the updateItem call below, otherwise it will
        // call cancelEdit(), resulting in both commit and cancel events being
        // fired (as identified in RT-29650)
        cellCommitEdit(newValue);
    }
    
    protected void updateCellVisuals(T newValue) {
        // update the item within this cell, so that it represents the new value
        // PENDING: JW
        // this is the same as cellUpdateItem - 
        // this base-implementation does not yet have a custom implementation
        // handled in subclasses like DebugTextFieldListCell
        // NOTE: update the value must be done before notifying the edit handler - 
        // otherwise we get weird state when adding items during a commit
        updateItem(newValue, false);
    }
    
    protected void notifyCommitHandler(T newValue) {
        ListView<T> list = getListView();
        // experiment around commit-fires-cancel:
        // surround with ignore-cancel to not react if skin
        // cancels our edit due to data change triggered by this commit
        // not needed if switching out of editing before notification
        // ignoreCancel = true;
        // Inform the ListView of the edit being ready to be committed.
        list.fireEvent(new ListView.EditEvent<T>(list,
                ListView.<T>editCommitEvent(),
                newValue,
                getIndex()
                ));
        ignoreCancel = false;
        
    }
    
    protected void resetEditingLocation() {
        ListView<T> list = getListView();
        int editingIndex = list.getEditingIndex();
        // this should be the same as cell index, if not, something is wrong!
        // Note: this assumption fails for core order, both in dynamic add/edit 
        // and in tests. 
        // probable reason: when notifying the commit handler before updating 
        // the location, the handler modifies the data which triggers the skin 
        // to cancel the edit 
        if (!(editingIndex == getIndex())) 
            throw new IllegalStateException("on commitEdit, list editing index must be same as my own: "
                    + getIndex() + " but was: " + editingIndex);

        list.edit(-1);
        
    }
//---------- end of playing api    
    /**
     * {@inheritDoc} <p>
     * 
     * Basically, a c&p of super except:
     * 
     * <ul>
     * <li> do nothing if ignoreCancel, fix for https://bugs.openjdk.java.net/browse/JDK-8187307
     * <li> pass correct index into editCancel, fix for https://bugs.openjdk.java.net/browse/JDK-8187226
     * </ul>
     * 
     * Sequence of mods core
     * - switch out off editing state
     * - update editing location (if not coming from updateEditing)
     * - notify cancel handler
     * 
     * @see #ignoreCancel()
     */
    @Override
    public void cancelEdit() {
       if (ignoreCancel()) return; 
        // Inform the ListView of the edit being cancelled.
       cellCancelEdit();
       
       ListView<T> list = getListView();
       if (list != null) {
           int editingIndex = list.getEditingIndex();
           // this should be the same as cell index, if not, something is wrong!
           // no, happens if the editingIndex changes on the list (triggered by something
           // else than us)
//           if (!(list.getEditingIndex() == getIndex())) 
//               throw new IllegalStateException("on cancelEdit, list editing index must be same as my own: "
//                       + getIndex() + " but was: " + editingIndex);

           // reset the editing index on the ListView
           if (resetListEditingIndexInCancel()) list.edit(-1);

           // request focus back onto the list, only if the current focus
           // owner has the list as a parent (otherwise the user might have
           // clicked out of the list entirely and given focus to something else.
           // It would be rude of us to request it back again.
//           ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(list);
           if (ControlUtils.isCurrentFocusOwnerChildOf(list) && list.getEditingIndex() == -1)
               list.requestFocus();

           list.fireEvent(new ListView.EditEvent<T>(list,
                           ListView.<T>editCancelEvent(),
                           null,
                           // PENDING JW
                           // list has different editingIndex when we get here from
                           // listener to editingIndex
                           getIndex()
        //                   editingIndex
                           ));
       }
    }

    /**
     * Hook to control whether or not cancelEdit should be processed.
     * This implementation returns true if we are not in editing state or
     * the ignoreCancel flag is set.
     *  
     * @return true if cancel request should be ignored.
     * 
     * @see #cancelEdit()
     */
    protected boolean ignoreCancel() {
        return !isEditing() || ignoreCancel;
    }
    
    /**
     * Returns a flag indicating whether the list editingIndex should be 
     * reset in cancelEdit. Implemented to reflectively access super's
     * hidden field <code>updateEditingIndex</code>
     * @return
     */
    protected boolean resetListEditingIndexInCancel() {
        return (boolean) FXUtils.invokeGetFieldValue(ListCell.class, this, "updateEditingIndex");
    }

    @Override
    public ReadOnlyObjectProperty<ListView<T>> controlProperty() {
        return listViewProperty();
    }

    /**
     * {@inheritDoc} <p>
     * Implemented check if the listView is editable in addition to super.
     */
    @Override
    public boolean canStartEdit() {
        return CellDecorator.super.canStartEdit() && getListView().isEditable();
    }
    
    
}
