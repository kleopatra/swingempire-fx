/*
 * Created on 17.02.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextFormatter;
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
 * Beware: going dirty with reflection to inject our combo to super! we could c&p the
 * whole class to stay clean ..
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XComboBoxTableCell<S, T> extends ComboBoxTableCell<S, T> {
    
    private ComboBox<T> comboAlias;

    /**
     * Replaces super's combo by our own. Beware: using reflection to 
     * access hidden field, will break in security restricted environments!
     * If this works out, should be taken over by super.
     * 
     */
    private void installComboBox() {
        // create combo
        ComboBox<T> comboBox = new ComboBox<>(getItems());
        // just checking: the editor is always != null
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
    private void installComboListeners(ComboBox<T> comboBox) {
        // JW: PENDING: need to reset the formatter if the converter changes!
        // Doesn't work anyway ...initial value in textField is empty .. why?
        TextFormatter formatter = new TextFormatter(getConverter());
        comboBox.getEditor().setTextFormatter(formatter);
//        comboBox.valueProperty().bind(formatter.valueProperty());
        ComboBoxListViewSkin<T> skin = (ComboBoxListViewSkin<T>) comboBox.getSkin();
        ListView<T> list = (ListView<T>) skin.getPopupContent();
        list.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            commitEdit(comboBox.getValue());
        });
        list.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                // PENDING JW: not yet correct - 
                //have to commit a potentially edited value in the textfield first
                // needs more thought: if the popup is open, a value selected
                // then the user types something .. what does she want on pressing
                // enter?
                // commit-on-typing into textField? That would deselect
                comboBox.getEditor().commitValue();
                commitEdit(comboBox.getValue());
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
        // filter release is what we get always ...  
        comboBox.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                // PENDING JW: not entirely certain if the textField 
                // has already committed in all cased
                commitEdit(comboBox.getValue());
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
        this(FXCollections.observableArrayList());
    }

    public XComboBoxTableCell(ObservableList<T> items) {
        this(null, items);
    }

    /**
     * Implementation detail: this is super's catch-all constructor.
     * Inject not-null converter here to fake null
     * @param converter
     * @param items
     */
    public XComboBoxTableCell(StringConverter<T> converter,
            ObservableList<T> items) {
        super(converter, items);
        // PENDING JW; dammed generics ...
        if (converter ==null) setConverter(getDefaultConverter());
    }

    public XComboBoxTableCell(StringConverter<T> converter, T... items) {
        this(converter, FXCollections.observableArrayList(items));
    }

    public XComboBoxTableCell(T... items) {
        this(FXCollections.observableArrayList(items));
    }

    private StringConverter<T> getDefaultConverter() {
        LOG.info("getting here?");
        return (StringConverter<T>) defaultConverter;
    }
    
    /**
     * Note: this doesn't really make sense for any T except String.
     * Using code _must_ supply a converter for other types. So we 
     * are just keeping the compiler happy, same as CellUtils default.
     * 
     * Difference being that toString returns empty string vs. null in CellUtils:
     * the null wrecks the TextFormatter/TextInputControl interplay.
     */
    private static StringConverter<?> defaultConverter = new StringConverter<Object>() {
        @Override public String toString(Object t) {
            return t == null ? "" : t.toString();
        }

        @Override public Object fromString(String string) {
            return (Object) string;
        }
        
    };
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(XComboBoxTableCell.class.getName());
}
