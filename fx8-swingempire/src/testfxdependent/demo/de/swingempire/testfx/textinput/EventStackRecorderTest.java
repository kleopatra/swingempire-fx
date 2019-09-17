/*
 * Created on 17.09.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.lang.StackWalker.StackFrame;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;
import static org.junit.Assert.*;

import de.swingempire.testfx.textinput.TextFieldDefaultCancelButtonTest.TextFieldDefaultCancelButtonPane;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.stage.Stage;

/**
 * Test the recorder (the particular pane is just an example)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class EventStackRecorderTest extends ApplicationTest {
    protected TextFieldDefaultCancelButtonPane root;

    @Test
    public void testRecorderDepth() {
        int depth = 5;
        EventStackRecorder recorder = new EventStackRecorder(depth);
        root.field.addEventHandler(KEY_PRESSED, recorder::record);
        press(F4);
        assertEquals(1, recorder.recordedSize());
        StackFrame stackFrame = recorder.getFirstStackFrame(0);
        assertTrue(stackFrame.getMethodName().contains("Bubbling"));
        recorder.log(0);
    }
    
    @Test
    public void testRecorderDepthLambda() {
        int depth = 5;
        EventStackRecorder recorder = new EventStackRecorder(2, depth);
        root.field.addEventHandler(KEY_PRESSED, e -> recorder.record(e));
        press(F4);
        assertEquals(1, recorder.recordedSize());
        StackFrame stackFrame = recorder.getFirstStackFrame(0);
        assertTrue(stackFrame.getMethodName().contains("Bubbling"));
        recorder.log(0);
    }
    
    @Test
    public void testRecorderState() {
        int depth = 5;
        EventStackRecorder recorder = new EventStackRecorder(2, depth);
        root.field.addEventHandler(KEY_PRESSED, e -> recorder.record(e));
        press(F4);
        assertEquals(1, recorder.recordedSize());
        List<StackFrame> frames = recorder.getStackFrames(0);
        assertEquals(depth, frames.size());
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


}
