/*
 * Created on 11.09.2017
 *
 */
package de.swingempire.fx.scene.control.cell;

import de.swingempire.fx.scene.control.ControlUtils;
import de.swingempire.fx.util.FXUtils;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

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
 * <li> https://bugs.openjdk.java.net/browse/JDK-8187226 -
 *      in cancelEdit, pass correct index into editCancelEvent
 * </ul>
 * 
 * Note: the problem with skin canceling the edit is the same for all 
 * virtual controls, though not so obvious in Tree-/TableView for different 
 * reasons. Hacking around the problem with a flag seems whacky, but we might
 * get away with it. Not thoroughly tested, though, beware!
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugListCell<T> extends ListCell<T> implements CellDecorator<T> {

    private boolean ignoreCancel;

    /**
     * {@inheritDoc} <p>
     * Basically, a c&p of super except:
     * 
     * <ul>
     * <li> pass correct index into editStart, fix for https://bugs.openjdk.java.net/browse/JDK-8187432
     * </ul>
     * 
     */
    @Override
    public void startEdit() {
        final ListView<T> list = getListView();
        if (!isEditable() || (list != null && ! list.isEditable())) {
            return;
        }

        // it makes sense to get the cell into its editing state before firing
        // the event to the ListView below, so that's what we're doing here
        // by calling super.startEdit().
        //super.startEdit();
        
        cellStartEdit();
        // PENDING JW:shouldn't we back out if !isEditing? That is when
        // super refused to switch into editing state?
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
     */
    @Override
    public void commitEdit(T newValue) {
        if (! isEditing()) return;
        ListView<T> list = getListView();

        if (list != null) {
            int editingIndex = list.getEditingIndex();
            // this should be the same as cell index, if not, something is wrong!
            if (!(list.getEditingIndex() == getIndex())) 
                throw new IllegalStateException("on cancelEdit, list editing index must be same as my own: "
                        + getIndex() + " but was: " + editingIndex);

            // experiment around commit-fires-cancel:
            // surround with ignore-cancel to not react if skin
            // cancels our edit due to data change triggered by this commit
            ignoreCancel = true;
            // Inform the ListView of the edit being ready to be committed.
            list.fireEvent(new ListView.EditEvent<T>(list,
                    ListView.<T>editCommitEvent(),
                    newValue,
                    getIndex()));
            ignoreCancel = false;
        }

        // inform parent classes of the commit, so that they can switch us
        // out of the editing state.
        // This MUST come before the updateItem call below, otherwise it will
        // call cancelEdit(), resulting in both commit and cancel events being
        // fired (as identified in RT-29650)
//        super.commitEdit(newValue);

        cellCommitEdit(newValue);
        // update the item within this cell, so that it represents the new value
        // PENDING: JW
        // this is the same as cellUpdateItem - 
        // this base-implementation does not yet have a custom implementation
        // handled in subclasses like DebugTextFieldListCell
        updateItem(newValue, false);

        if (list != null) {
            // reset the editing index on the ListView. This must come after the
            // event is fired so that the developer on the other side can consult
            // the ListView editingIndex property (if they choose to do that
            // rather than just grab the int from the event).
            list.edit(-1);

            // request focus back onto the list, only if the current focus
            // owner has the list as a parent (otherwise the user might have
            // clicked out of the list entirely and given focus to something else.
            // It would be rude of us to request it back again.
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(list);
        }
    }

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
     * @see #ignoreCancel()
     */
    @Override
    public void cancelEdit() {
        if (ignoreCancel()) return; 
        // Inform the ListView of the edit being cancelled.
       ListView<T> list = getListView();
       cellCancelEdit();
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
           ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(list);

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
}
