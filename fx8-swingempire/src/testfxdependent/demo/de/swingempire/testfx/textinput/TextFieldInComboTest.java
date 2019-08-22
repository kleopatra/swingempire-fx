/*
 * Created on 19.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import static de.swingempire.fx.scene.control.skin.XTextFieldSkin.*;
import static de.swingempire.testfx.util.TestFXUtils.*;
import static javafx.scene.input.KeyCode.*;
import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.*;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * test fix https://bugs.openjdk.java.net/browse/JDK-8145515
 * textField in editable combo: custom enter filter not invoked.  
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 * 
 */
public class TextFieldInComboTest extends ApplicationTest {

    protected TextFieldInComboPane root;
    protected TextField editor;

    
    /**
     * test https://bugs.openjdk.java.net/browse/JDK-8149622
     * original reported against not-editable combo.
     */
    @Test
    public void testEnterDispatchSequence() {
        Scene scene = root.getScene();
        EventHandlerReport report = new EventHandlerReport();
        List<EventHandler<KeyEvent>> expectedHandlers = List.of(
                report.addEventFilter(scene, KeyEvent.KEY_RELEASED)
                , report.addEventFilter(root.comboBox, KeyEvent.KEY_RELEASED)
                , report.addEventHandler(root.comboBox, KeyEvent.KEY_RELEASED)
                , report.addEventHandler(scene, KeyEvent.KEY_RELEASED)
                );
        
        runAndWaitForFx(() -> {
            root.comboBox.setEditable(false);
        });
        verifyThat(root.comboBox, NodeMatchers.isFocused());
        press(ENTER);
        release(ENTER);
        assertEquals("each of 4 handlers must be notified exactly once", 4, report.getEventCount());
        assertEquals("notified handlers must be same as added handlers", 
                expectedHandlers, report.getEventHandlers());
    }

    
    
    /**
     * test https://bugs.openjdk.java.net/browse/JDK-8149622
     * original reported against not-editable combo.
     * 
     * Here we test an editable combo (default for our test ui)
     */
    @Test
    public void testEnterDispatchSequenceEditable() {
        Scene scene = root.getScene();
        EventHandlerReport report = new EventHandlerReport();
        List<EventHandler<KeyEvent>> expectedHandlers = List.of(
                report.addEventFilter(scene, KeyEvent.KEY_RELEASED)
                , report.addEventFilter(root.comboBox, KeyEvent.KEY_RELEASED)
                , report.addEventHandler(root.comboBox, KeyEvent.KEY_RELEASED)
                , report.addEventHandler(scene, KeyEvent.KEY_RELEASED)
                );
        
        verifyThat(root.comboBox, NodeMatchers.isFocused());
        press(ENTER);
        release(ENTER);
        assertEquals("each of 4 handlers must be notified exactly once", 4, report.getEventCount());
        assertEquals("notified handlers must be same as added handlers", 
                expectedHandlers, report.getEventHandlers());
      }
    
    /**
     * Regression: filter on editor not invoked for enter key.
     * 
     */
    @Test
    public void testEnterFilter() {
        List<KeyEvent> keyEvents = new ArrayList<>();
        editor.addEventFilter(KeyEvent.KEY_PRESSED, keyEvents::add);
        press(ENTER);
        assertEquals(1, keyEvents.size());
    }
    
    /**
     * The editor is created by the ComboBox, the disableForward flag installed
     * by ComboBoxPopupSkin.getEditorNode, that is only after the skin is 
     * attached!
     */
    @Test
    public void testInitialEditorState() {
        assertNotNull(editor);
        assertNotNull(editor.getProperties().get(TEXT_FIELD_DISABLED_FORWARD_TO_PARENT));
        verifyThat(editor, NodeMatchers.isFocused());
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        root = new TextFieldInComboPane(getSkinProvider()); 
        editor = root.comboBox.getEditor();
        Scene scene = new Scene(root, 100, 100);
        stage.setScene(scene);
        stage.show();
    }

    protected Function<TextField, TextFieldSkin> getSkinProvider() {
        return TextFieldSkin::new;
    }


    /**
     * TestUI: have an editable ComboBox (aka: TextField in combo)
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class TextFieldInComboPane extends VBox {
        protected ComboBox<String> comboBox;
        
        public TextFieldInComboPane() {
            this(null);
        }
        
        /**
         * Instantiates testUi with a combo editor using the skin returned 
         * by the provider.
         * 
         * PENDING JW: not yet implemented, need to set the editor explicitly
         * @param skinProvider
         */
        public TextFieldInComboPane(Function<TextField, TextFieldSkin> skinProvider) {
            comboBox = new ComboBox<>();
            comboBox.setEditable(true);
            // don't care about content for now ...
            comboBox.getItems().addAll("something to choose", "another thingy to have");
            getChildren().addAll(comboBox);
            
            // unexpected: editor is readOnlyProperty of combo, lazyly created in getter
            // to FakeFocusTextField
            //LOG.info("have editor? " + comboBox.getEditor());
            // install eventFilter for enter
            
//            comboBox.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
//                
//            });
        }
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFieldInComboTest.class.getName());
}
