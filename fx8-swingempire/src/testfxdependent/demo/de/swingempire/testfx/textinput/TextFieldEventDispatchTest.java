/*
 * Created on 16.09.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.ButtonMatchers;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.*;

import de.swingempire.testfx.textinput.TextFieldDefaultCancelButtonTest.TextFieldDefaultCancelButtonPane;
import fx.core.testsupport.KeyEventFirer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldEventDispatchTest extends ApplicationTest {

    protected TextFieldDefaultCancelButtonPane root;
    
    
    @Test
    public void testDefaultButtonAndConsumingEnter() {
        EventStackRecorder recorder = new EventStackRecorder();
        root.field.setOnKeyPressed(e -> {
            e.consume();
            recorder.record(e);
        });
        
        root.ok.setOnAction(recorder::record);
        press(ENTER);
        assertEquals(2, recorder.recordedSize());
        List<Object> sources = List.of(root.field, root.ok);
        
        for (int i = 0; i < recorder.recordedSize(); i++) {
            int index = i;
            recorder.forEvent(i, e -> assertEquals("event source at " + index, sources.get(index), e.getSource()));
        }
    }
    
    @Test
    public void testDefaultButtonAndNotConsumingEnter() {
        EventStackRecorder recorder = new EventStackRecorder();
        root.field.setOnKeyPressed(e -> {
            recorder.record(e);
        });
        
        root.ok.setOnAction(recorder::record);
        press(ENTER);
        assertEquals(2, recorder.recordedSize());
        List<Object> sources = List.of(root.field, root.ok);
        
        for (int i = 0; i < recorder.recordedSize(); i++) {
            int index = i;
            recorder.forEvent(i, e -> assertEquals("event source at " + index, sources.get(index), e.getSource()));
        }
    }
    
    @Ignore
    @Test
    public void logWithRecorderDefaultButtonAndConsumingEnter() {
        EventStackRecorder recorder = new EventStackRecorder();
        root.field.setOnKeyPressed(e -> {
            e.consume();
            recorder.record(e);
        });
        
        root.ok.setOnAction(recorder::record);
        press(ENTER);
//        recorder.forEach(action -> LOG.info("" + action));
        assertEquals(2, recorder.recordedSize());
        for (int i = 0; i < recorder.recordedSize(); i++) {
            Object event = recorder.getEventSource(i);
            LOG.info("" + event + recorder.getFirstStackFrame(i));
        }
    }
    
    @Ignore
    @Test
    public void logDefaultButtonAndConsumingEnter() {
        EventStackRecorder recorder = new EventStackRecorder();
        root.field.setOnKeyPressed(e -> {
            if (e.getCode() == ENTER) {
                e.consume();
                LOG.info("in field: " + e);
                
            }
        });
        
        root.ok.setOnAction(a -> LOG.info("getting action " + a));
        press(ENTER);
    }
    
    /**
     * use plain list to collect the events
     */
    @Test
    public void testEventSequenceEnterHandler() {
        Scene scene = root.getScene();
        List<Event> events = new ArrayList<>();
        EventHandler<KeyEvent> adder = events::add;
        scene.addEventHandler(KEY_PRESSED, adder);
        root.addEventHandler(KEY_PRESSED, adder);
        root.field.addEventHandler(KEY_PRESSED, adder);
        root.ok.setOnAction(events::add);
        KeyCode enter = ENTER;
        press(enter);
        assertEquals("event count", 4, events.size());
        List<Object> sources = events.stream()
                .map(e -> e.getSource())
                .collect(toList());      
        List<Object> expected = List.of(root.field, root, scene, root.ok);
        assertEquals(EventStackRecorder.compareSources(expected, sources), expected, sources);
    }
    
    /**
     * Sanity: test event sequence for F5.
     */
    @Test
    public void testEventSequenceF5Record() {
        Scene scene = root.getScene();
        EventStackRecorder recorder = new EventStackRecorder(20);
        EventHandler<KeyEvent> adder = recorder::record;
        scene.addEventHandler(KEY_PRESSED, adder);
        root.addEventHandler(KEY_PRESSED, adder);
        root.field.addEventHandler(KEY_PRESSED, adder);
        KeyCode enter = F5;
        press(enter);
        assertEquals("event count", 3, recorder.recordedSize());
        List<Object> expected = List.of(root.field, root, scene);
        assertEquals(recorder.compareSources(expected), expected, recorder.getEventSources());
    }
    
    @Test
    public void testEventSequenceEscapeHandlerRecord() {
        Scene scene = root.getScene();
        EventStackRecorder recorder = new EventStackRecorder(20);
        EventHandler<KeyEvent> adder = recorder::record;
        scene.addEventHandler(KEY_PRESSED, adder);
        root.addEventHandler(KEY_PRESSED, adder);
        root.field.addEventHandler(KEY_PRESSED, adder);
        root.cancel.setOnAction(recorder::record);
        KeyCode enter = ESCAPE;
        press(enter);
        assertEquals("event count", 4, recorder.recordedSize());
        List<Object> expected = List.of(root.field, root, scene, root.cancel);
        assertEquals(recorder.compareSources(expected), expected, recorder.getEventSources());
    }
    
    @Test
    public void testEventSequenceEscapeFilterRecord() {
        Scene scene = root.getScene();
        EventStackRecorder recorder = new EventStackRecorder(20);
        EventHandler<KeyEvent> adder = recorder::record;
        scene.addEventFilter(KEY_PRESSED, adder);
        root.addEventFilter(KEY_PRESSED, adder);
        root.field.addEventFilter(KEY_PRESSED, adder);
        root.cancel.setOnAction(recorder::record);
        KeyCode enter = ESCAPE;
        press(enter);
        assertEquals("event count", 4, recorder.recordedSize());
        List<Object> expected = List.of(scene, root, root.field, root.cancel);
        assertEquals(recorder.compareSources(expected), expected, recorder.getEventSources());
    }
    
    @Test
    public void testEventSequenceEnterHandlerRecord() {
        Scene scene = root.getScene();
        EventStackRecorder recorder = new EventStackRecorder(20);
        EventHandler<KeyEvent> adder = recorder::record;
        scene.addEventHandler(KEY_PRESSED, adder);
        root.addEventHandler(KEY_PRESSED, adder);
        root.field.addEventHandler(KEY_PRESSED, adder);
        root.ok.setOnAction(recorder::record);
        KeyCode enter = ENTER;
        press(enter);
        assertEquals("event count", 4, recorder.recordedSize());
        List<Object> expected = List.of(root.field, root, scene, root.ok);
        assertEquals(recorder.compareSources(expected), expected, recorder.getEventSources());
    }
    
    @Test
    public void testEventSequenceEnterFilterRecord() {
        Scene scene = root.getScene();
        EventStackRecorder recorder = new EventStackRecorder(20);
        EventHandler<KeyEvent> adder = recorder::record;
        scene.addEventFilter(KEY_PRESSED, adder);
        root.addEventFilter(KEY_PRESSED, adder);
        root.field.addEventFilter(KEY_PRESSED, adder);
        root.ok.setOnAction(recorder::record);
        KeyCode enter = ENTER;
        press(enter);
        assertEquals("event count", 4, recorder.recordedSize());
        List<Object> expected = List.of(scene, root, root.field, root.ok);
        assertEquals(recorder.compareSources(expected), expected, recorder.getEventSources());
    }
    
    @Ignore
    @Test
    public void logEventDispatch() {
        Scene scene = root.getScene();
        EventStackRecorder recorder = new EventStackRecorder(20);
        root.field.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            recorder.recordHandler(e, root.field);
            
        });
        root.field.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            recorder.recordFilter(e, root.field);
        });
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            recorder.recordHandler(e, scene);
            
        });
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            recorder.recordFilter(e, scene);
            
        });
        root.ok.setOnAction(a -> recorder.recordHandler(a, root.ok));
        KeyCode enter = ENTER;
        press(enter);
        release(enter);
        
        // this is real testing
//        assertEquals("each filter must be notified once", 5, recorder.getRecordSize());
        List<Object> sources = List.of(scene, root.field, root.field, scene, root.ok);
        
        recorder.logAll();
    }
    
    
    @Test
    public void testInitial() {
        assertNull(root.field.getTextFormatter());
    }
    
    @Test
    public void testTextIsFocused() {
        verifyThat(root.field, NodeMatchers.isFocused());
    }
    
    @Test
    public void testIsCancelButton() {
        verifyThat(root.cancel, ButtonMatchers.isCancelButton());
    }
    
    @Test
    public void testIsDefaultButton() {
       verifyThat(root.ok, ButtonMatchers.isDefaultButton());
    }

    @Override
    public void start(Stage stage) {
        // don't need here, tests will fail anyway - hack on sub
        //stopStoringFiredEvents(stage);
        root = new TextFieldDefaultCancelButtonPane(getSkinProvider());
        Scene scene = new Scene(root, 100, 100);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Function to provide a skin. May be null to indicate using the default.
     * @return
     */
    protected Function<TextField, TextFieldSkin> getSkinProvider() {
        return null;
    }
    
    @Before
    public void setup() {
        root.field.setTextFormatter(null);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFieldEventDispatchTest.class.getName());
}
