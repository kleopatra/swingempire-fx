/*
 * Created on 17.02.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

/**
 * PoC (and WiP :-) to make a ComboBoxTableCell behave correctly. The basic part is
 * to install low-level listeners (keyHandler on both list and combo, mouseHandler
 * on list) that do the commit/cancel as appropriate.
 * <p>
 * Beware: going dirty with reflection to inject our combo to super!
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XComboBoxTableCell<S, T> extends ComboBoxTableCell<S, T> {
    
    ComboBox<T> comboAlias;

    /**
     * Replaces super's combo by our own. Beware: using reflection to 
     * access hidden field, will break in security restricted environments!
     * If this works out, should be taken over by super.
     * 
     */
    private void installComboBox() {
        // create combo
        ComboBox<T> comboBox = new ComboBox<>(getItems());
        comboBox.converterProperty().bind(converterProperty());
        comboBox.editableProperty().bind(comboBoxEditableProperty());
        comboBox.setMaxWidth(Double.MAX_VALUE);
        
        invokeSetFieldValue(ComboBoxTableCell.class, this, "comboBox", comboBox);
        comboBox.skinProperty().addListener((src, ov, nv) -> {
            installComboListeners(comboBox);
        } );
        comboAlias = comboBox;
    }
    /**
     * Install the listeners after skin is installed on combo.
     */
    private void installComboListeners(ComboBox<T> combo) {
       
        ComboBoxListViewSkin<T> skin = (ComboBoxListViewSkin<T>) combo.getSkin();
        ListView<T> list = (ListView<T>) skin.getPopupContent();
        list.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            commitEdit(combo.getValue());
        });
        list.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                // PENDING JW: not yet correct - 
                //have to commit a potentially edited value in the textfield first
                commitEdit(combo.getValue());
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
        // filter release is what we get always ...  
        combo.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                // PENDING JW: not entirely certain if the textField 
                // has already committed in all cased
                commitEdit(combo.getValue());
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
            
        });
    }
    
    private static void invokeSetFieldValue(Class declaringClass, Object target, String name, Object value) {
        try {
            Field field = declaringClass.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * Overridden to hook into creation of comboBox.
     */
    @Override
    public void startEdit() {
        if (comboAlias == null) {
            installComboBox();
        }
        super.startEdit();
    }

// ------------------ just constructors from super

    public XComboBoxTableCell() {
        super();
    }

    public XComboBoxTableCell(ObservableList<T> items) {
        super(items);
    }

    public XComboBoxTableCell(StringConverter<T> converter,
            ObservableList<T> items) {
        super(converter, items);
    }

    public XComboBoxTableCell(StringConverter<T> converter, T... items) {
        super(converter, items);
    }

    public XComboBoxTableCell(T... items) {
        super(items);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(XComboBoxTableCell.class.getName());
}
