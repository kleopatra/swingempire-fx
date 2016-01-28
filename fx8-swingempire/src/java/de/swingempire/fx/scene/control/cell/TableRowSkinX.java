/*
 * Created on 07.11.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.skin.TableRowSkin;
import javafx.scene.control.skin.TableRowSkinBase;

//import com.sun.javafx.scene.control.skin.TableRowSkin;

/**
 * Skin that updates child cells in an InvalidationListener if
 * super's changeListener can't (that is if oldItem.equals(newItem)).
 *  
 * @author Jeanette Winzenburg, Berlin
 */
public class TableRowSkinX<T> extends TableRowSkin<T> {

    private WeakReference<T> oldItemRef;
    private InvalidationListener itemInvalidationListener;
    private WeakInvalidationListener weakItemInvalidationListener;
    
    private List<TableCell> cellsAlias;
    /**
     * @param tableRow
     */
    public TableRowSkinX(TableRow<T> tableRow) {
        super(tableRow);
        oldItemRef = new WeakReference<>(tableRow.getItem());
        itemInvalidationListener = o -> {
            T newItem = ((ObservableValue<T>) o).getValue();
            T oldItem = oldItemRef != null ? oldItemRef.get() : null;
            oldItemRef = new WeakReference<>(newItem);
            if (oldItem != null && newItem != null && oldItem.equals(newItem)) {
                forceCellUpdate();
            }
        };
        weakItemInvalidationListener = new WeakInvalidationListener(itemInvalidationListener);
        tableRow.itemProperty().addListener(weakItemInvalidationListener);
        
        cellsAlias = invokeGetCells();
    }
    
    /**
     * Try to force cell update for equal (but not same) items.
     * C&P'ed code from TableRowSkinBase.
     */
    private void forceCellUpdate() {
        invokeSetField("updateCells", true);
//        updateCells = true;
        getSkinnable().requestLayout();

        // update the index of all children cells (RT-29849).
        // Note that we do this after the TableRow item has been updated,
        // rather than when the TableRow index has changed (as this will be
        // before the row has updated its item). This will result in the
        // issue highlighted in RT-33602, where the table cell had the correct
        // item whilst the row had the old item.
        final int newIndex = getSkinnable().getIndex();
        for (int i = 0, max = cellsAlias.size(); i < max; i++) {
            cellsAlias.get(i).updateIndex(newIndex);
        }
   }
    
//------------ hacking around super fields changed to package/private in jdk9
    
    private List<TableCell> invokeGetCells() {
        Class target = TableRowSkinBase.class;
        try {
            Field field = target.getDeclaredField("cells");
            field.setAccessible(true);
            return (List<TableCell>) field.get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    private void invokeSetField(String name, boolean value) {
        Class target = TableRowSkinBase.class;
        try {
            Field field = target.getDeclaredField(name);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}