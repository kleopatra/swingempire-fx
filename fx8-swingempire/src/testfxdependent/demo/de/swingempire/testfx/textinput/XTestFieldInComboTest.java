/*
 * Created on 22.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.function.Function;

import de.swingempire.fx.scene.control.skin.XTextFieldSkin;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;

/**
 * Here: use XTextFieldSkin - doesn't help. No wonder, it's the
 * combo that's misbehaving
 * @author Jeanette Winzenburg, Berlin
 */
public class XTestFieldInComboTest extends TextFieldInComboTest {

    
    @Override
    protected Function<TextField, TextFieldSkin> getSkinProvider() {
        return XTextFieldSkin::new;
    }

}
