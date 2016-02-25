/*
 * Created on 17.02.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Skin;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

/**
 * Prototype to make a ComboBoxTableCell behave correctly. The basic part is
 * to install low-level listeners (keyHandler on both list and combo, mouseHandler
 * on list) that do the commit/cancel as appropriate.
 * <p>
 * Details: https://github.com/kleopatra/swingempire-fx/wiki/ComboBoxTableCell
 * 
 * <p>
 * Beware: this particular implementation is going dirty with reflection 
 * to inject our combo to super! we could c&p the
 * whole class to stay clean ..
 * 
 * <p>
 * fixed in http://hg.openjdk.java.net/openjfx/9-dev/rt/rev/4d8a1695b277
 * tiny glitch: no commit on clicking on item in list
 * 
 * @see ComboCellIssuesContinued
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
    private ComboBox<T> createAndInstallComboBox() {
        // create combo
        ComboBox<T> comboBox = new ComboBox<>(getItems());
        comboBox.converterProperty().bind(converterProperty());
        comboBox.editableProperty().bind(comboBoxEditableProperty());
        comboBox.setMaxWidth(Double.MAX_VALUE);
        
        invokeSetFieldValue(ComboBoxTableCell.class, this, "comboBox", comboBox);
        comboBox.skinProperty().addListener((src, ov, nv) -> {
            // PENDING JW: take care of uninstall on replacing the skin?
            // cells are re-created often, thus not probable
            // in the longer run, would use a custom skin anyway?
            
            // have to wait until skin is installed to hook listeners
            // particularly into the list in the popup
            installComboListeners(comboBox);
        } );
        
        return comboBox;
    }
    
    /**
     * Install the listeners after skin is installed on combo.
     */
    private void installComboListeners(ComboBox<T> comboBox) {
        
        // alternative: update selection on text change
        // the actual fix by Jonathan doesn't need this:
        // text is committed directly out from the textField
//        comboBox.getEditor().textProperty().addListener((src, ov, nv) -> {
//            StringConverter<T> c = comboBox.getConverter();
//            // PENDING: without converter (will not happen, cbtc enforces one)
//            // or if the converter returns null (aka: can't convert)
//            // effectively clears the selectionModel, not the value
//            // need to test!
//            T o = c != null ? c.fromString(nv) : null;
//            comboBox.getSelectionModel().select(o);
//        });
        
        // implementing commitOnFocusLost
        // Note JW: doesn't fully work without XTableView/XTextFieldTableCell mech!
        comboBox.getEditor().focusedProperty().addListener((src, ov, nv) -> {
            if (!nv) {
                //commitEdit(comboBox.getValue());
                tryComboBoxCommit(comboBox);
            }
        });
        
        ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) comboBox.getSkin();
        Node list = skin.getPopupContent();
        // PENDING JW: need to weed out the scroll bars as core does?
        // without this handler, the edit isn't committed on clicking into
        // the popup of an editable combo
        list.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            commitEdit(comboBox.getValue());
        });
        // filter release is what we get always ...  
        comboBox.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                // PENDING JW: not entirely certain if the textField 
                // has already committed in all cased
                // commitEdit(comboBox.getValue());
                // fix by Jonathan
                tryComboBoxCommit(comboBox);
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
            
        });
    }

    /**
     * This is the actual fix implemented in 
     * http://hg.openjdk.java.net/openjfx/9-dev/rt/rev/4d8a1695b277
     * 
     * The idea is to commit directly out off the textField for an 
     * editable box, so this can be called from all locations that
     * are interpreted as a user commit gesture, f.i. in the
     * enter handler (event filter on combo) or on focusLost.
     */
    private void tryComboBoxCommit(ComboBox<T> comboBox) {
        StringConverter<T> c = comboBox.getConverter();
        if (comboBox.isEditable() && c != null) {
            T nv = c.fromString(comboBox.getEditor().getText());
            commitEdit(nv);
        } else {
            commitEdit(comboBox.getValue());
        }
    }
    
    
    /**
     * Overridden to hook into creation of comboBox and 
     * request focus onto the combo if editing started.
     */
    @Override
    public void startEdit() {
        if (comboAlias == null) {
            comboAlias = createAndInstallComboBox();
        }
        super.startEdit();
        if (isEditing() && comboAlias != null) {
            comboAlias.requestFocus();
        }
        
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
