/*
 * Created on 13.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.function.Function;

import de.swingempire.fx.scene.control.skin.XTextFieldSkin;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class XTextFieldInPopupTest extends TextFieldInPopupTest {

    @Override
    protected Function<TextField, TextFieldSkin> getSkinProvider() {
        return XTextFieldSkin::new;
    }
    
}
