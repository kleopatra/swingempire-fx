/*
 * Created on 05.09.2018
 *
 */
package fx.core.control.text;

import javafx.scene.control.TextField;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class XTextFieldTest extends TextFieldTest {

    @Override
    protected TextField createTextField() {
        return new XTextField();
    }

    @Override
    protected TextField createTextField(String text) {
        return new XTextField(text);
    }
    
}
