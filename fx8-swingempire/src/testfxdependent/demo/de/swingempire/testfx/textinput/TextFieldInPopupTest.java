/*
 * Created on 12.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.function.Function;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.base.WindowMatchers;

import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.*;
import static org.testfx.util.WaitForAsyncUtils.*;
import static org.testfx.matcher.base.WindowMatchers.*;
import static org.testfx.matcher.base.NodeMatchers.*;

import de.swingempire.fx.scene.control.skin.XTextFieldSkin;
import de.swingempire.testfx.util.TestFXUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;

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

    protected TextFieldInPopupPane root;
    
    /**
     * Escape on field with uncommitted changes must revert the change
     * and not close the popup
     */
    @Test
    public void testEscapeOnFormatterWithUncommittedChanges() {
        root.installTextFormatter();
        clickOn(".button");
        String text = "text";
        root.textField.appendText(text);
        assertEquals(text, root.textField.getText());
        press(KeyCode.ESCAPE);
        assertEquals("", root.textField.getText());
        verifyThat(root.popup, isShowing());
    }
    /**
     * Leftover from fixed bug: unconditionally consumes escape if textField
     * has formatter.
     */
    @Test
    public void testEscapeOnFormatterHidesPopup() {
        root.installTextFormatter();
        clickOn(".button");
        press(KeyCode.ESCAPE);
        verifyThat(root.popup, isNotShowing());
    }
    
    @Test
    public void testPopupShowingFormatter() {
        root.installTextFormatter();
        clickOn(".button");
        verifyThat(root.popup, isShowing());
        verifyThat(root.textField, NodeMatchers.isFocused());
    }
    
//-------------------- plain textfield (without formatter)    
    @Test
    public void testEscapeOnTextFieldHidesPopup() {
        clickOn(".button");
        press(KeyCode.ESCAPE);
        verifyThat(root.popup, isNotShowing());
   }
    
    @Test
    public void testOpenPopupTextFieldFocused() {
        clickOn(".button");
        verifyThat(root.textField, NodeMatchers.isFocused());
    }
    
    /**
     * sanity: checkBox doesn't eat escape 
     */
    @Test
    public void testEscapeOnCheckBoxHidesPopup() {
        clickOn(".button");
        Platform.runLater(() -> {
            root.checkBox.requestFocus();
        });
        waitForFxEvents();
        verifyThat(root.checkBox, NodeMatchers.isFocused());
        press(KeyCode.ESCAPE);
        verifyThat(root.popup, isNotShowing());
    }
    @Test
    public void testPopupShowing() {
        clickOn(".button");
        verifyThat(root.popup, isShowing());
    }
    
    @Test
    public void testOpenPopup() throws Exception {
        clickOn(".button");
        //LOG.info("" + targetWindow());
        Node textField = lookup(".text-field").query();
        Window popup = window(textField);
        verifyThat(".text-field", NodeMatchers.isFocused());
        verifyThat(popup, WindowMatchers.isShowing());
        press(KeyCode.ESCAPE);
//        sleep(1000, TimeUnit.MILLISECONDS);
        waitForFxEvents();
        LOG.info("after escape: " + popup.isShowing());
//        ObservableBooleanValue notShowing = Bindings.not(popup.showingProperty());
//        waitFor(2000, TimeUnit.MILLISECONDS, notShowing);
        verifyThat(popup, WindowMatchers.isNotShowing());
    }

    protected Function<TextField, TextFieldSkin> getSkinProvider() {
        return TextFieldSkin::new;
    }

    @Override
    public void start(Stage stage) throws Exception {
        // PENDING move to XTest methods where needed
        TestFXUtils.stopStoringFiredEvents(stage);
        root = new TextFieldInPopupPane(getSkinProvider()); // createContent();
        Scene scene = new Scene(root, 100, 100);
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * per fixture test: ensure that we start without textFormatter
     */
    @Before
    public void setUp() {
        assertFalse("textField must not have formatter", root.hasTextFormatter());
    }
    /**
     * UI to test https://bugs.openjdk.java.net/browse/JDK-8090230
     * Popup not closed on escape if contained textField is focused.
     * 
     * Fixed for textField without formatter.
     * Still virulent for textField with formatter: implementation not good enough - should
     * check if the textField has uncommitted changes before !consuming.
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class TextFieldInPopupPane extends VBox {
        protected TextField textField;
        protected Popup popup;
        protected Node checkBox;
        
        public TextFieldInPopupPane() {
            this(null);
        }

        /**
         * @param skin
         */
        public TextFieldInPopupPane(Function<TextField, TextFieldSkin> skinProvider) {
            popup = createPopup();
            textField = new TextField("Focus on this one, ESCAPE doesn't work") {

              @Override
              protected Skin<?> createDefaultSkin() {
                  return skinProvider == null ? super.createDefaultSkin() : skinProvider.apply(this);
              }
              
            };
            checkBox = new CheckBox("Focus on this one, ESCAPE works");
            VBox box = new VBox(10, textField, checkBox);
            box.setPadding(new Insets(10));
            popup.getContent().addAll(box);
            
            final Button button = new Button("Click to Show Popup");
            button.setOnAction(e -> {
                Point2D p = button.localToScreen(0, 0);
                popup.show(button, p.getX(), p.getY() + button.getHeight());
                
            });

            getChildren().addAll(button, 
                    new Label("Clicking on the button to show a popup. Focus on the text field and press ESCAPE. "
                            + "The popup doesn't hide." +
                    "\nYou can read comment in the file for more information"));

        }

        /**
         * @return
         */
        private Popup createPopup() {
            Popup popup = new Popup();
            popup.showingProperty().addListener((src, ov, nv) -> {
//                LOG.info("showing: " + nv);
//                if (!nv) {
//                    
////                    new RuntimeException("who is calling? \n").printStackTrace();
//                }
            });
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);
            return popup;
        }
        
        public void installTextFormatter() {
            textField.setTextFormatter(new TextFormatter(TextFormatter.IDENTITY_STRING_CONVERTER));
        }
        
        public void removeTextFormatter() {
            textField.setTextFormatter(null);
        }
        
        public boolean hasTextFormatter() {
            return textField.getTextFormatter() != null;
        }
    }
    
    /** 
     * Code adapted from original report.
     * @return
     */
    public static Parent createContent() {
            final Button button = new Button("Click to Show Popup");
    
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    Popup popup = new Popup();
                    popup.showingProperty().addListener((src, ov, nv) -> {
                        LOG.info("showing: " + nv);
                        if (!nv) {
                            
    //                        new RuntimeException("who is calling? \n").printStackTrace();
                        }
                    });
                    popup.setAutoHide(true);
                    popup.setHideOnEscape(true);
                    // informal testing: XTextFieldSkin is okay
                    TextField textField = new TextField("Focus on this one, ESCAPE doesn't work") {
    
    //                    @Override
    //                    protected Skin<?> createDefaultSkin() {
    //                        return new XTextFieldSkin(this);//super.createDefaultSkin();
    //                    }
                        
                    };
//                    textField.setTextFormatter(new TextFormatter(TextFormatter.IDENTITY_STRING_CONVERTER));
                    CheckBox checkBox = new CheckBox("Focus on this one, ESCAPE works");
                    VBox box = new VBox(10, textField, checkBox);
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

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFieldInPopupTest.class.getName());



}