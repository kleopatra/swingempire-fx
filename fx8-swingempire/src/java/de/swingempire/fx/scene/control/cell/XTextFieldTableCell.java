/*
 * Created on 06.08.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;



/**
 * TexFieldTableCell which listens to focus changes.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XTextFieldTableCell<S, T> extends TextFieldTableCell<S, T> {
    
    private TextField myTextField;

    public XTextFieldTableCell() {
        this(null);
    }

    public XTextFieldTableCell(StringConverter<T> converter) {
        super(converter);
        focusedProperty().addListener((e, old, nvalue) -> LOG.info("" + e));
    }

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
                    T edited = getConverter().fromString(myTextField.getText());
                    commitEdit(edited);
                }
                
            });
        }
    }

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
        LOG.info(fields + "");
        return fields.size() == 1 ? (TextField) fields.get(0) : null;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(XTextFieldTableCell.class.getName());
    
}
