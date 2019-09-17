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
import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.*;

import de.swingempire.testfx.textinput.TextFieldDefaultCancelButtonTest.TextFieldDefaultCancelButtonPane;
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
    
    
    @Test
    public void testEventDispatch() {
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
        
        for (int i = 0; i < recorder.recordedSize(); i++) {
            recorder.log(i);
//            int index = i;
//            recorder.forEvent(i, e -> assertEquals("event source at " + index, sources.get(index), e.getSource()));
        }
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
