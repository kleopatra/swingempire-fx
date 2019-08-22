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

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * This Test has examples for different ways to test the sequence
 * of eventhandler notification during dispatch. Nothing really
 * tested, the textInput in a combo is just an example.
 * 
 * @author Jeanette Winzenburg, Berlin
 * 
 */
public class EventDispatchTestExample extends ApplicationTest {

    protected TextFieldInComboTest.TextFieldInComboPane root;
    protected TextField editor;

// --------- manually compare against known handlers    
    
    /**
     * Demonstrates use EventHandlerReport. 
     */
    @Test
    public void testEnterDispatchSequenceReport() {
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
        assertEquals(4, report.getEventCount());
        assertEquals(expectedHandlers, report.getEventHandlers());
    }

    /**
     * Demonstrates use of utility method to compare stackFrame.
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
     * Demonstrates use of EventStackRecorder.
     */
    @Test
    public void testEnterDispatchSequenceWithRecord() {
        Scene scene = root.getScene();
        EventStackRecorder recorder = new EventStackRecorder();
        root.comboBox.addEventHandler(KeyEvent.KEY_RELEASED, recorder::record);
        root.comboBox.addEventFilter(KeyEvent.KEY_RELEASED, recorder::record);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, recorder::record);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, recorder::record);
        runAndWaitForFx(() -> {
            root.comboBox.setEditable(false);
        });
        verifyThat(root.comboBox, NodeMatchers.isFocused());
        press(ENTER);
        release(ENTER);
        // this is real testing
        assertEquals("each filter must be notified once", 4, recorder.getRecordSize());
        
        // this is testing of recorder
        recorder.forEach(stack -> assertEquals("size of stack: " + stack, 1, stack.size()));
        
        // check expected event and corresponding handler
        List<Object> sources = List.of(scene, root.comboBox, root.comboBox, scene);
        List<String> methods = List.of("Capturing", "Capturing", "Bubbling", "Bubbling");
        // use accessors on recorder
        for (int i = 0; i < sources.size(); i++) {
            assertEquals("expected event source", sources.get(i), recorder.getEventSource(i));
            StackFrame frame = recorder.getFirstStackFrame(i);
            String methodFragment = methods.get(i);
            assertTrue("expected method: " + methodFragment + "but was: "  + frame.getMethodName(), 
                    frame.getMethodName().contains(methodFragment));
         }
        
        // use recorder api + functions
        for (int i = 0; i < sources.size(); i++) {
            Object source = sources.get(i);
            recorder.forEvent(i, event -> 
                assertEquals("expected event source", source, event.getSource()));
            String methodFragment = methods.get(i);
            recorder.forFirstStackFrame(i, first -> assertTrue(
                    "expected method: " + methodFragment + "but was: "  + first.getMethodName(), 
                    first.getMethodName().contains(methodFragment)));
        }
        
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
        root = new TextFieldInComboTest.TextFieldInComboPane(getSkinProvider()); 
        editor = root.comboBox.getEditor();
        Scene scene = new Scene(root, 100, 100);
        stage.setScene(scene);
        stage.show();
    }

    protected Function<TextField, TextFieldSkin> getSkinProvider() {
        return TextFieldSkin::new;
    }


    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(EventDispatchTestExample.class.getName());
}
