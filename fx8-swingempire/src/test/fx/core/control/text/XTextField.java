/*
 * Created on 05.09.2018
 *
 */
package fx.core.control.text;

import javafx.scene.control.TextField;

/**
 * TextField implementation with bug fixes
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XTextField extends TextField {
 
    public XTextField() {
        super();
    }
    
    /**
     * @param text
     */
    public XTextField(String text) {
        super(text);
    }

    /**
     * Overridden to return if anchor and caret are already at the given 
     * positions.
     * 
     * Removes both double notification on backward/forward and
     * notification on unrelated focusOwner changes.
     */
    @Override
    public void selectRange(int anchor, int caretPosition) {
        if (getAnchor() == anchor && getCaretPosition() == caretPosition) return;
        super.selectRange(anchor, caretPosition);
    }
    
    
}