/*
 * Created on 06.08.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.swingempire.fx.scene.control.XTableView;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;



/**
 * TexFieldTableCell which supports terminating an ongoing edit.<p>
 * 
 * There are two parts of the support:
 * - listens to the textField's focusProperty and commits on loosing. This
 *   handles the case when the focus is moved to something outside the table
 * - installs a custom skin with a behaviour that tries to terminate the
 *   edit (vs. cancelling it)  
 * - listens to the table's terminatingCell property and commits if the new
 *   property matches this cell's position (requires the table to be
 *   of type XTableView)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XTextFieldTableCell<S, T> extends TextFieldTableCell<S, T> {
    
    /** local copy of the textfield that's installed by super. */
    private TextField myTextField;
    /** the changeListener for the table's terminatingCell property */
    private ChangeListener<TablePosition<S, ?>> terminatingListener = 
            (e, oldPosition, newPosition)  -> terminateEdit(newPosition);

    public XTextFieldTableCell() {
        this(null);
    }

    /**
     * Implemented to listen to the tableViewProperty and un-/wire the terminatingListener
     * as appropriate.
     * 
     * @param converter
     */
    public XTextFieldTableCell(StringConverter<T> converter) {
        super(converter);
        tableViewProperty().addListener((e, oldTable, newTable) -> {
            uninstallTerminatingListener(oldTable);
            installTerminatingListener(newTable);
        });
    }

    /**
     * {@inheritDoc} <p>
     * 
     * Overridden to lookup the textField created by super and register a listener
     * with its focusedProperty.
     * 
     * TBD: cleanup, probably needs WeakListener
     */
    @Override
    public void startEdit() {
        super.startEdit();
        if (isEditing() && myTextField == null) {
            myTextField = findTextField();
            if (myTextField == null) {
                // something unexpected happened ...
                // either throw an exception or return silently
                return;
            }
            myTextField.focusedProperty().addListener((e, old, nvalue) -> {
                if (!nvalue) {
                    commitEdit();
                }
                
            });
        }
    }

    /**
     * @param newPosition
     * @return
     */
    protected void terminateEdit(TablePosition<S, ?> newPosition) {
        if (!isEditing() || !match(newPosition)) return;
        commitEdit();
    }
    
    /**
     * 
     */
    protected void commitEdit() {
        T edited = getConverter().fromString(myTextField.getText());
        commitEdit(edited);
    }

    /**
     * Implemented to create XTableCellSkin which supports terminating edits.
     * 
     * <b>NOTE</b>: XTableCellSkin is HIGHLY version-dependent! The 
     * implementation for version jdk8_u5 doesn't compile for jdk8_u20 (and
     * the other way round)
     * 
     * <p>
     * 
     * Newest version (jdk9) uses a hack - XHackTableCellSkin that extends
     * from the concrete TableCellSkin (due to not being able to extend
     * TableCellSkinBase - https://bugs.openjdk.java.net/browse/JDK-8148573 )
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new XHackTableCellSkin<S, T>(this);
//        return new XTableCellSkin<S, T>(this);
    }


    /**
     * c&p of super (WTF is that method private?)
     * 
     * @param pos a TablePosition to check for matching
     * @return true if the given position matches this cell, false otherwise.
     */
    protected boolean match(TablePosition<S, ?> pos) {
        return pos != null && pos.getRow() == getIndex() && pos.getTableColumn() == getTableColumn();
    }

    /**
     * Lookup and returns the textField installed by super.
     * 
     * @return
     */
    protected TextField findTextField() {
        if (getGraphic() instanceof TextField) {
            // no "real" graphic so the textfield _is_ the graphic
            return (TextField) getGraphic();
        }
        // TBD: untested!
        // has a "real" graphic, the graphic property is a pane which 
        // contains the textfield
        Set<Node> nodes = lookupAll(".text-field");
        // sane use-case: it's only one field
        if (nodes.size() == 1) {
            return (TextField) nodes.toArray()[0];
        }
        // corner case: there's a "real" graphic which is/contains
        // a textfield, differentiate by text
        String expectedText = getConverter().toString(getItem());
        List<Node> fields = nodes.stream()
            .filter(field -> field instanceof TextField)    
            .filter(field -> expectedText.equals(((TextInputControl) field).getText()))
            .collect(Collectors.toList());
        return fields.size() == 1 ? (TextField) fields.get(0) : null;
    }

    /**
     * @param newTable
     */
    private void installTerminatingListener(TableView<S> newTable) {
        if (newTable instanceof XTableView) {
            ((XTableView<S>) newTable).terminatingCellProperty().addListener(terminatingListener);
        }
    }

    /**
     * @param oldTable
     */
    private void uninstallTerminatingListener(TableView<S> oldTable) {
        if (oldTable instanceof XTableView) {
            ((XTableView) oldTable).terminatingCellProperty().removeListener(terminatingListener);
        }
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(XTextFieldTableCell.class.getName());
    
}
