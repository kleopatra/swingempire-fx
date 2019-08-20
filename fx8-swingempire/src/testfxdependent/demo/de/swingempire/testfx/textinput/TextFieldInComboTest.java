/*
 * Created on 19.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import static de.swingempire.fx.scene.control.skin.XTextFieldSkin.*;
import static de.swingempire.testfx.util.TestFXUtils.*;
import static java.lang.StackWalker.Option.*;
import static javafx.scene.input.KeyCode.*;
import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.*;

import javafx.event.Event;
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
 * @author Jeanette Winzenburg, Berlin
 * 
 */
public class TextFieldInComboTest extends ApplicationTest {

    protected TextFieldInComboPane root;
    protected TextField editor;
    
    /**
     * test https://bugs.openjdk.java.net/browse/JDK-8149622
     * original reported against not-editable combo.
     * 
     * anything in the event to distinguish having
     *   received it in a filter vs handler? no, but can walk the stackframes.
     */
    @Test
    public void testEnterDispatchSequence() {
        Scene scene = root.getScene();
        List<KeyEvent> events = new ArrayList<>();
        List<List<StackFrame>> stackFrames = new ArrayList<>();
        int depth = 1;
        root.comboBox.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            collect(event, depth, events, stackFrames);
        });
        root.comboBox.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            collect(event, depth, events, stackFrames);
        });
        scene.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            collect(event, depth, events, stackFrames);
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            collect(event, depth, events, stackFrames);
        });
        runAndWaitForFx(() -> {
            root.comboBox.setEditable(false);
        });
        verifyThat(root.comboBox, NodeMatchers.isFocused());
        press(ENTER);
        release(ENTER);
        assertEquals("each filter must be notified once", 4, events.size());
        assertEquals("each filter has stackframe", 4, stackFrames.size());
        stackFrames.forEach(frames -> frames.forEach(f -> {
            System.out.println(f.getClass() + " " +  f.getMethodName());
        }));
        
        events.forEach(ev -> System.out.println(ev));
        
        stackFrames.forEach(stack -> assertEquals("size of stack: " + stack, 1, stack.size()));
        
        // check expected event and corresponding handler
        List<Object> sources = List.of(scene, root.comboBox, root.comboBox, scene);
        List<String> methods = List.of("Capturing", "Capturing", "Bubbling", "Bubbling");
        for (int i = 0; i < events.size(); i++) {
            assertEquals("expected event source", events.get(i).getSource(), sources.get(i));
            StackFrame frame = stackFrames.get(i).get(0);
            assertTrue("expected method: " + methods.get(i) + "but was: "  + frame.getMethodName(), 
                    frame.getMethodName().contains(methods.get(i)));
        }
        
    }
    
    public static class EventStackRecord {
        
        private List<Event> events;
        private List<List<StackFrame>> stackFrames;
        
        public EventStackRecord() {
            events = new ArrayList<>();
            stackFrames = new ArrayList<>();
        }
        /**
         * Collects the event and stackFrames with depth 1.
         * 
         * @param event the event passed into the caller
         */
        public void collect(KeyEvent event) {
            collect(event, 1);
        }
        /**
         * Collects the event and stackFrames with given depth.
         * 
         * @param event the event passed into the caller
         * @param depth the depth to walk into the stack (actual - 2 for this and caller)
         */
        public void collect(KeyEvent event, int depth) {
            events.add(event);
            List<StackFrame> stack = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(s -> s
                    .skip(1 /*this*/ + 1 /* caller */)
                    .limit(depth)
                    .collect(Collectors.toList()));
            stackFrames.add(stack);
        }
        
   }
    
    /**
     * Collects the event and stackFrames into the given lists.
     * 
     * @param event the event passed into the caller
     * @param depth the depth to walk into the stack (actual - 2 for this and caller)
     * @param events the list of events of collect into
     * @param stackFrames the list of StackFrames to collect into
     */
    public static void collect(KeyEvent event, int depth, List<KeyEvent> events, List<List<StackFrame>> stackFrames) {
        events.add(event);
        List<StackFrame> stack = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(s -> s
                .skip(1 /*this*/ + 1 /* caller */)
                .limit(depth)
                .collect(Collectors.toList()));
        stackFrames.add(stack);
    }
    /**
     * test https://bugs.openjdk.java.net/browse/JDK-8149622
     * original reported against not-editable combo.
     */
    @Test
    public void testEnterDispatchSequenceEditable() {
        Scene scene = root.getScene();
        List<KeyEvent> events = new ArrayList<>();
        root.comboBox.addEventHandler(KeyEvent.KEY_RELEASED, events::add);
        root.comboBox.addEventFilter(KeyEvent.KEY_RELEASED, events::add);
//        root.comboBox.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
//            events.add(event);
//        });
        scene.addEventHandler(KeyEvent.KEY_RELEASED, events::add);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, events::add);
//        runAndWaitForFx(() -> {
//            root.comboBox.setEditable(false);
//        });
        verifyThat(root.comboBox, NodeMatchers.isFocused());
        press(ENTER);
        release(ENTER);
        assertEquals("each filter must be notified once", 4, events.size());
    }
    
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
