/*
 * Created on 01.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.lang.reflect.Field;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.SelectionModel;


/**
 * Separating out the problem with correlated properties. Moved here the 
 * hack with replacing super's selectedIndex/selectedItem by their 
 * ReactFx counterparts.
 * 
 * NOTE: poc for fixing problem with correlated properties, requires reactFX 
 *  https://github.com/TomasMikula/ReactFX/wiki/InhiBeans - without that lib, simply comment
 *  the field replacement and access to guards.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class AbstractSelectionModelReact<T> extends AbstractSelectionModelBase<T> {

    private ReadOnlyObjectWrapper<T> itemReplacement;
    private ReadOnlyIntegerWrapper indexReplacement;

    public AbstractSelectionModelReact() {
        // going dirty: replace super's selectedItem/Index property 
        // with guarded cousins 
        itemReplacement = new ReadOnlyObjectWrapper<>(this, "selectedItem");
        replaceField("selectedItem", itemReplacement);
        indexReplacement = new ReadOnlyIntegerWrapper(this, "selectedIndex", -1);
        replaceField("selectedIndex", indexReplacement);
    }
    
    /**
     * Overridden to guard notification of selectedIndex/items
     */
    @Override
    protected void syncSingleSelectionState(int selectedIndex) {
        Guard guard = Guard.multi(itemReplacement.guard(), indexReplacement.guard());
        setSelectedIndex(selectedIndex);
        if (selectedIndex > -1) {
            setSelectedItem(getModelItem(selectedIndex));
        } else {
            // PENDING JW: do better? can be uncontained item
            setSelectedItem(null);
        } 
        guard.close();
        focus(selectedIndex);
    }

    /**
     * Overridden to guard notification of selectedIndex/items
     */
    @Override
    protected void selectExternalItem(T obj) {
        Guard guard = Guard.multi(itemReplacement.guard(), indexReplacement.guard());
        setSelectedItem(obj);
        setSelectedIndex(-1);
        guard.close();
    }

    protected void replaceField(String name, Object replacement) {
        Class<?> clazz = SelectionModel.class;
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            field.set(this, replacement);
        } catch (NoSuchFieldException | SecurityException 
                | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    


}
