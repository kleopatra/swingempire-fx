/*
 * Created on 04.04.2019
 *
 */
package de.swingempire.testfx.textinput;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static de.swingempire.testfx.matcher.TextInputMatchers.*;
import static de.swingempire.testfx.util.TestFXUtils.*;

import static org.junit.Assert.*;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TextAreaScrollTest extends ApplicationTest {

    private  final static double DELTA = 0.0001;
    
    private TextArea textArea;
    private int count;

    /**
     * Here we add 100 lines into a single bunch, then some more.
     * passing
     */
    @Test
    public void testAddLines100InBunchPlusBunch() {
        runAndWaitForFx(() -> addLines(100));
        runAndWaitForFx(() -> addLines(10));
        assertScrollStateVertical(textArea, 0, 1, 1);
    }
    
    /**
     * Here we add 100 lines into a single bunch, then one more.
     * passing
     */
    @Test
    public void testAddLines100InBunchPlus1() {
        runAndWaitForFx(() -> addLines(100));
        runAndWaitForFx(() -> addLines(1));
        assertScrollStateVertical(textArea, 0, 1, 1);
    }
    
    /**
     * Here we add (prefRows  lines into a single bunch, then one more.
     * passing
     */
    @Test
    public void testAddLinesPrefRowsInBunchPlus1() {
        int prefRows = textArea.getPrefRowCount();
        runAndWaitForFx(() -> addLines(prefRows));
        runAndWaitForFx(() -> addLines(1));
        assertScrollStateVertical(textArea, 0, 1, 1);
    }

    /**
     * Here we add the (prefRows) lines one by one and wait 
     * after each insertion. failing!
     */
    @Test
    public void testAddLinesPrefRowsOneByOne() {
        int prefRows = textArea.getPrefRowCount();
        for(int i = 0; i < prefRows; i++) {
            runAndWaitForFx(( ) -> {
                addLines(1);
            });
        }
        assertScrollStateVertical(textArea, 0, 1, 1);
    }
    
    /**
     * Here we add the (prefRows+ 1) lines one by one and wait 
     * after each insertion. Passing!
     * 
     * Result: scrolling starts after adding the first row that
     * doesn't fit into the viewport.
     */
    @Test
    public void testAddLinesPrefRowsPlus1OneByOne() {
        int prefRows = textArea.getPrefRowCount();
        for(int i = 0; i < prefRows+1; i++) {
            runAndWaitForFx(( ) -> {
                    addLines(1);
            });
        }
        assertScrollStateVertical(textArea, 0, 1, 1);
    }
    
    /**
     * Here we add the (prefRows+1) lines one by one - but wait around
     * the loop. Failing!
     * 
     */
    @Test
    public void testAddLinesPrefRowPlus1OneByOneSingleWait() {
        int prefRows = textArea.getPrefRowCount();
        runAndWaitForFx(( ) -> {
            for(int i = 0; i < prefRows + 1; i++) {
                addLines(1);
            }
        });
        assertScrollStateVertical(textArea, 0, 1, 1);
    }
    
    /**
     * Here we add (prefRows + 2) lines into a single bunch.
     * Fails.
     */
    @Test
    public void testAddLinesInBunch() {
        int prefRows = textArea.getPrefRowCount();
        runAndWaitForFx(() -> addLines(prefRows + 2));
        assertScrollStateVertical(textArea, 0, 1, 1);
    }
    
    @Test
    public void testInitialScrollPane() {
        int prefRows = textArea.getPrefRowCount();
        assertEquals(10, prefRows);
        assertTrue(textArea.getText().isEmpty());
        assertScrollStateVertical(textArea, 0, 0, 1);
    }
    
    @Test
    public void testInitialHBar() {
        ScrollBar bar = getHorizontalScrollBar(textArea);
        assertScrollBar(bar, 0, 0, 1);
    }
    
    protected void assertScrollStateVertical(TextArea textArea, double min, double value, double max) {
        assertScrollPaneVertical(getScrollPane(textArea), min, value, max);
        assertScrollBar(getVerticalScrollBar(textArea), min, value, max);
    }
    
    protected void assertScrollPaneVertical(TextArea textArea, double min, double value, double max) {
        assertScrollPaneVertical(getScrollPane(textArea), min, value, max);
    }
    
    protected void assertScrollPaneVertical(ScrollPane scrollPane, double min, double value, double max) {
        assertEquals("scrollPane vmin", min, scrollPane.getVmin(), DELTA);
        assertEquals("scrollPane vmax", max, scrollPane.getVmax(), DELTA);
        assertEquals("scrollPane vvalue", value, scrollPane.getVvalue(), DELTA);
    }
    
    protected void assertScrollBar(ScrollBar bar, double min, double value, double max) {
        assertEquals("scrollbar min", min, bar.getMin(), DELTA);
        assertEquals("scrollbar max", max, bar.getMax(), DELTA);
        assertEquals("scrollbar value", value, bar.getValue(), DELTA);
        
    }
    protected void addLines(int lines) {
        for (int i = 0; i < lines; i++) {
            textArea.appendText("Hello" + count++ +"\n");
        }
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        textArea = new TextArea();
        
        Button add = new Button("add lines");
        
        BorderPane content = new BorderPane(textArea);
        stage.setScene(new Scene(content));
        stage.show();
    }
    
    
}
