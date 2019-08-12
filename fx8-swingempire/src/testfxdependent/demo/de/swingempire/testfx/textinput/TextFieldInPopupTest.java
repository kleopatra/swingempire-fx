/*
 * Created on 12.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import static org.testfx.util.WaitForAsyncUtils.*;
import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.*;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 * Focused textField in popup prevents closing of popup by ESC
 * https://bugs.openjdk.java.net/browse/JDK-8090230
 * 
 * always in fx8: the old mechanism in behaviorbase consumes if it finds
 * a matching action in its list of bindings
 * 
 * fixed in fx9: consume the esc only if formatter set, then the 
 * bug is back
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldInPopupTest extends ApplicationTest {

    @Test
    public void testOpenPopup() {
        clickOn(".button");
//        TestFXUtils.runAndWaitForFx(r);
        waitForFxEvents();
        verifyThat(".text-field", NodeMatchers.isFocused());
        press(KeyCode.DIGIT1);
        press(KeyCode.ESCAPE);
    }
    
    public static Parent createContent() {
        final Button button = new Button("Click to Show Popup");

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Popup popup = new Popup();
                popup.setAutoHide(true);
                popup.setHideOnEscape(true);
                // informal testing: XTextFieldSkin is okay
                TextField textField = new TextField("Focus on this one, ESCAPE doesn't work") {

//                    @Override
//                    protected Skin<?> createDefaultSkin() {
//                        return new XTextFieldSkin(this);//super.createDefaultSkin();
//                    }
                    
                };
                textField.setTextFormatter(new TextFormatter(TextFormatter.IDENTITY_STRING_CONVERTER));
                VBox box = new VBox(10, textField, new CheckBox("Focus on this one, ESCAPE works"));
                box.setPadding(new Insets(10));
                popup.getContent().addAll(box);
                Point2D p = button.localToScreen(0, 0);
                popup.show(button, p.getX(), p.getY() + button.getHeight());
            }
        });

        VBox vbox = new VBox(6, button, new Label("Clicking on the button to show a popup. Focus on the text field and press ESCAPE. The popup doesn't hide." +
                "\nYou can read comment in the file for more information"));
        vbox.setPadding(new Insets(10));
        return vbox;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = createContent();
        Scene scene = new Scene(root, 100, 100);
        stage.setScene(scene);
        stage.show();
    }
    
    
}
