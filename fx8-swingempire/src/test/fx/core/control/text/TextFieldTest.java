/*
 * Created on 05.09.2018
 *
 */
package fx.core.control.text;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

//import javafx.scene.control.TextInputControlShim;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static fx.core.testsupport.ControlTestUtils.*;
//import static test.com.sun.javafx.scene.control.infrastructure.ControlTestUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.util.StageLoader;
import fx.core.testsupport.TextInputControlShim;
import javafx.beans.property.IntegerProperty;

/*
 * Copyright (c) 2010, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

//package test.javafx.scene.control;

//import test.com.sun.javafx.scene.control.infrastructure.StageLoader;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableSet;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * c&p from core
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class TextFieldTest {

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    protected TextField txtField;//Empty string
    protected TextField dummyTxtField;//With string value

    protected TextField navigationField; // with sentence "The quick brown fox"
    
    @Before 
    public void setup() {
        txtField = createTextField();
        dummyTxtField = createTextField("dummy");
        navigationField = createTextField("The quick brown fox");
    }

    protected TextField createTextField() {
        return new TextField();
    }
    
    protected TextField createTextField(String text) {
        return new TextField(text);
    }
    
    // Textformatter
    
    /**
     * Test notification on navigation: backward.
     */
    @Test
    public void testFormatterNotificationOnHome() {
        int pos = 3;
        assertFormatterNotificationOnNavigation(
                t -> t.selectRange(pos, pos),
                t -> t.home());
    }
    
    /**
     * Test notification on navigation: backward.
     */
    @Test
    public void testFormatterNotificationOnBackward() {
        int pos = 3;
        assertFormatterNotificationOnNavigation(
                t -> t.selectRange(pos, pos),
                t -> t.backward());
    }
    
    /**
     * Test notification on navigation: forward.
     */
    @Test
    public void testFormatterNotificationOnForward() {
        assertFormatterNotificationOnNavigation(t -> t.forward());
    }
    
    /**
     * Test notification on navigation: forward.
     */
    @Test
    public void testFormatterNotificationOnForwardWord() {
        assertFormatterNotificationOnNavigation(t -> t.endOfNextWord());
    }
    
    /**
     * Test notification on navigation: end.
     */
    @Test
    public void testFormatterWithFilterNotificationOnEnd() {
        assertFormatterNotificationOnNavigation(t -> t.end());
    }
    
    /**
     * Test that navigation produces a single notification.
     * 
     * @param preFormatter the initial state transitionbefore 
     * @param navigation the navigation
     *    setting the formatter
     * @param positionProvider the expected position for both caret and anchor after navigation.
     */
    protected void assertFormatterNotificationOnNavigation(
            Consumer<TextField> preFormatter,
            Consumer<TextField> navigation,
            Function<TextField, Integer> positionProvider) {
        assertFormatterNotificationOnNavigation(preFormatter, navigation, positionProvider, positionProvider);
    }
    /**
     * Test that navigation produces a single notification.
     * 
     * @param navigation the navigation
     * @param positionProvider the expected position for both caret and anchor after navigation.
     */
    protected void assertFormatterNotificationOnNavigation(
            Consumer<TextField> navigation,
            Function<TextField, Integer> positionProvider) {
        assertFormatterNotificationOnNavigation(null, navigation, positionProvider, positionProvider);
    }
    
    protected void assertFormatterNotificationOnNavigation(
            Consumer<TextField> preFormatter,
            Consumer<TextField> navigation) {
        assertFormatterNotificationOnNavigation(preFormatter, navigation, null, null);
    }
    
    protected void assertFormatterNotificationOnNavigation(Consumer<TextField> navigation) {
        assertFormatterNotificationOnNavigation(null, navigation, null, null);
    }
    
    /**
     * Test that navigation produces a single notification.
     * 
     * @param preFormatter the initial state transition before 
     *    setting the formatter, may be null if initial state is default
     * @param navigation the navigation
     * @param anchorProvider the expected anchor after navigation - sanity check, may be null.
     * @param caretProvider the expected caret after navigation - sanity check, may be null.
     */
    protected void assertFormatterNotificationOnNavigation(
            Consumer<TextField> preFormatter,
            Consumer <TextField> navigation, 
            Function<TextField, Integer> anchorProvider, 
            Function<TextField, Integer> caretProvider) {
        // textfield with text "The quick brown fox"
        TextField txtField = navigationField;
        // initial state
        if (preFormatter != null) {
            preFormatter.accept(txtField);
        }
        IntegerProperty count = new SimpleIntegerProperty(0);
        UnaryOperator<TextFormatter.Change> filter = c -> { 
            count.set(count.get() + 1);
            return c; 
        }; 
        TextFormatter<String> formatter = new TextFormatter<>(filter);
        txtField.setTextFormatter(formatter);
        // navigate
        navigation.accept(txtField);
        // sanity tests of anchor/caret pos
        // here we are not really interested in those (they are tested extensively
        // in TextInputControlTest) - just for digging if things go wrong unexpectedly
        if (anchorProvider != null) {
            int anchor = anchorProvider.apply(txtField);
            assertEquals("sanity: anchor at end", anchor, txtField.getAnchor());
        }
        if (caretProvider != null) {
            int caret = caretProvider.apply(txtField);
            assertEquals("sanity: caret at end", caret, txtField.getCaretPosition());
        }
        assertEquals("must have received a single notification", 1, count.get());
    }
    
    /**
     * Test that setting a do-nothing formatter with identity converter has no effect on anchor/caret.
     * 
     * Here: anchor/caret at 0
     */
    @Test
    public void testAnchorCaretAtThreeOnSetFormatterWithConverter() {
        TextFormatter<String> formatter = new TextFormatter<String>(TextFormatter.IDENTITY_STRING_CONVERTER);
        assertAnchorCaretOnSetFormatterWithConverter(formatter, 3);
    }
    
    /**
     * Test that setting a do-nothing formatter with identity converter has no effect on anchor/caret
     * Here: anchor/caret at 3
     * This fails - bug or feature?
     * Setting a formatter with converter will update the textField's text to the converted value
     * of the formatter.
     */
    @Test
    public void testAnchorCaretAtZeroOnSetFormatterWithConverter() {
        TextFormatter<String> formatter = new TextFormatter<String>(TextFormatter.IDENTITY_STRING_CONVERTER);
        assertAnchorCaretOnSetFormatterWithConverter(formatter, 0);
    }
 
    /**
     * Tests that the anchor/caret position is moved to the end of 
     * formatter's value.
     * 
     * @param formatter
     * @param initial
     */
    protected void assertAnchorCaretOnSetFormatterWithConverter(
            TextFormatter<String> formatter, int initial) {
        if (formatter.getValueConverter() == null) {
            fail("formatter must have converter");
        }
        String text = "The quick brown fox";
        int end = text.length();
        if (formatter.getValue() == null) {
            formatter.setValue(text);
        }
        txtField.setText(text);
        txtField.selectRange(initial, initial);
        int anchor = txtField.getAnchor();
        int caret = txtField.getCaretPosition();
        assertEquals("sanity: initial anchor", initial, anchor);
        assertEquals("sanity: initial caret", initial, caret);
        txtField.setTextFormatter(formatter);
        assertEquals(text, txtField.getText());
        assertEquals("sanity: initial anchor unchanged after setting formatter", end, txtField.getAnchor());
        assertEquals("sanity: initial caret unchanged after setting formatter", end, txtField.getCaretPosition());
    }
    
    

    /**
     * Test that setting a do-nothing formatter with filter has no effect on anchor/caret
     */
    @Test
    public void testAnchorCaretAtZeroOnSetFormatterWithFilter() {
        UnaryOperator<TextFormatter.Change> filter = c -> { 
            return c; 
        }; 
        TextFormatter<String> formatter = new TextFormatter<>(filter);
        assertAnchorCaretOnSetFormatterWithFilter(formatter, 0);
    }
    
    /**
     * Test that setting a do-nothing formatter with filter has no effect on anchor/caret
     */
    @Test
    public void testAnchorCaretAtThreeOnSetFormatterWithFilter() {
        UnaryOperator<TextFormatter.Change> filter = c -> { 
            return c; 
        }; 
        TextFormatter<String> formatter = new TextFormatter<>(filter);
        assertAnchorCaretOnSetFormatterWithFilter(formatter, 3);
    }

    /**
     * Tests that the anchor/caret position is unchanged on setting a formatter
     * that has a filter (no converter).
     * 
     * This does not make sense if the formatter has a converter and null value: 
     * textField text then updated to empty, so anchor/caret reset to 0
     * 
     * @param formatter
     * @param initial
     */
    protected void assertAnchorCaretOnSetFormatterWithFilter(
            TextFormatter<String> formatter, int initial) {
        if (formatter.getValueConverter() != null) {
            fail("formatter must not have a converter for this test");
        }
        String text = "The quick brown fox";
        txtField.setText(text);
        txtField.selectRange(initial, initial);
        int anchor = txtField.getAnchor();
        int caret = txtField.getCaretPosition();
        assertEquals("sanity: initial anchor", initial, anchor);
        assertEquals("sanity: initial caret", initial, caret);
        txtField.setTextFormatter(formatter);
        assertEquals(text, txtField.getText());
        assertEquals("sanity: initial anchor unchanged after setting formatter", initial, txtField.getAnchor());
        assertEquals("sanity: initial caret unchanged after setting formatter", initial, txtField.getCaretPosition());
    }
    
    
    /*********************************************************************
     * Tests for the constructors                                        *
     ********************************************************************/

    @Test public void defaultConstructorShouldHaveEmptyString() {
        assertEquals("", txtField.getText());
    }

    @Test public void oneStrArgConstructorShouldHaveString() {
        assertEquals("dummy", dummyTxtField.getText());
    }

    /*********************************************************************
     * Tests for the null checks                                         *
     ********************************************************************/

    @Test public void checkContentNotNull() {
        // PENDING JW: replaced with reflective access
        assertNotNull(TextInputControlShim.getContentObject(txtField));
//        assertNotNull(TextInputControlShim.getContent(txtField));
    }

    @Test public void checkCharNotNull() {
        assertNotNull(txtField.getCharacters());
    }

    @Test public void checkDefPromptTextEmptyString() {
        assertEquals("", txtField.getPromptText());
    }

    /*********************************************************************
     * Tests for default values                                         *
     ********************************************************************/
    @Test public void checkDefaultColCount() {
        assertEquals(TextField.DEFAULT_PREF_COLUMN_COUNT, 12);
    }

    @Test public void defaultActionHandlerIsNotDefined() {
        assertNull(txtField.getOnAction());
    }

    @Test public void defaultConstructorShouldSetStyleClassTo_textfield() {
        assertStyleClassContains(txtField, "text-field");
    }

    @Test public void checkCharsSameAsText() {
        assertEquals(dummyTxtField.getCharacters().toString(), dummyTxtField.getText());
    }

    @Test public void checkCharsSameAsContent() {
        assertEquals(dummyTxtField.getCharacters().toString(), TextInputControlShim.getContent_get(dummyTxtField, 0, dummyTxtField.getLength()).toString());
    }

    @Test public void checkTextSameAsContent() {
        assertEquals(dummyTxtField.getText(), TextInputControlShim.getContent_get(dummyTxtField, 0, dummyTxtField.getLength()));
    }

    @Test public void checkPromptTextPropertyName() {
        assertTrue(txtField.promptTextProperty().getName().equals("promptText"));
    }

    @Test public void prefColCountCannotBeNegative() {
        try {
            txtField.setPrefColumnCount(-1);
            fail("Prefcoulumn count cannot be null");//This is non reachable ode if everything goes fine(i.e Exception is thrown)
        } catch(IllegalArgumentException iae) {
            assertNotNull(iae);
        }
    }


    @Test public void oneArgStrConstructorShouldSetStyleClassTo_textfield() {
        assertStyleClassContains(dummyTxtField, "text-field");
    }

    @Test public void checkTextSetGet() {
        dummyTxtField.setText("junk");
        assertEquals(dummyTxtField.getText(), "junk");
    }

    /*********************************************************************
     * Tests for CSS                                                     *
     ********************************************************************/

    @Test public void prefColumnCountSetFromCSS() {
        txtField.setStyle("-fx-pref-column-count: 100");
        Scene s = new Scene(txtField);
        txtField.applyCss();
        assertEquals(100.0, txtField.getPrefColumnCount(), 0);
    }

    @Test public void pseudoClassState_isReadOnly() {
        StageLoader sl = new StageLoader(txtField);
        txtField.applyCss();

        txtField.setEditable(false);
        ObservableSet<PseudoClass> pcSet = txtField.getPseudoClassStates();
        boolean match = false;
        for (PseudoClass pc : pcSet) {
            if (match) break;
            match = "readonly".equals(pc.getPseudoClassName());
        }
        assertTrue(match);

        sl.dispose();
    }

    @Test public void pseudoClassState_isNotReadOnly() {
        StageLoader sl = new StageLoader(txtField);
        txtField.applyCss();

        txtField.setEditable(true);
        ObservableSet<PseudoClass> pcSet = txtField.getPseudoClassStates();
        boolean match = false;
        for (PseudoClass pc : pcSet) {
            if (match) break;
            match = "readonly".equals(pc.getPseudoClassName());
        }
        assertFalse(match);
        sl.dispose();
    }

    /*********************************************************************
     * Tests for property binding                                        *
     ********************************************************************/

    @Test public void checkPromptTextPropertyBind() {
        StringProperty strPr = new SimpleStringProperty("value");
        txtField.promptTextProperty().bind(strPr);
        assertTrue("PromptText cannot be bound", txtField.getPromptText().equals("value"));
        strPr.setValue("newvalue");
        assertTrue("PromptText cannot be bound", txtField.getPromptText().equals("newvalue"));
    }

    @Ignore("TODO: Please remove ignore annotation after RT-15799 is fixed.")
    @Test public void checkTextPropertyBind() {
        StringProperty strPr = new SimpleStringProperty("value");
        txtField.textProperty().bind(strPr);
        assertEquals("Text cannot be bound", txtField.getText(), "value");
        strPr.setValue("newvalue");
        assertEquals("Text cannot be bound", txtField.getText(),  "newvalue");
    }

    @Test public void checkOnActionPropertyBind() {
        ObjectProperty<EventHandler<ActionEvent>> op= new SimpleObjectProperty<EventHandler<ActionEvent>>();
        EventHandler<ActionEvent> ev = event -> {
            //Nothing to do
        };
        op.setValue(ev);
        txtField.onActionProperty().bind(op);
        assertEquals(ev, op.getValue());
    }
    /*********************************************************************
     * Miscellaneous Tests                                               *
     ********************************************************************/
    @Test public void lengthMatchesStringLengthExcludingControlCharacters() {
        final String string = "Hello\n";
        txtField.setText(string);
        assertEquals(string.length()-1, txtField.getLength());
    }

    @Test public void prefColumnCountPropertyHasBeanReference() {
        assertSame(txtField, txtField.prefColumnCountProperty().getBean());
    }

    @Test public void prefColumnCountPropertyHasName() {
        assertEquals("prefColumnCount", txtField.prefColumnCountProperty().getName());
    }

    @Test public void onActionPropertyHasBeanReference() {
        assertSame(txtField, txtField.onActionProperty().getBean());
    }

    @Test public void onActionPropertyHasName() {
        assertEquals("onAction", txtField.onActionProperty().getName());
    }

    @Test public void setPromptTextAndSeeValueIsReflectedInModel() {
        txtField.setPromptText("tmp");
        assertEquals(txtField.promptTextProperty().getValue(), "tmp");
    }

    @Test public void setPromptTextAndSeeValue() {
        txtField.setPromptText("tmp");
        assertEquals(txtField.getPromptText(), "tmp");
    }

    @Test public void setTextAndSeeValueIsReflectedInModel() {
        txtField.setText("tmp");
        assertEquals(txtField.textProperty().getValue(), txtField.getText());
    }

    @Test public void setTextAndSeeValue() {
        txtField.setText("tmp");
        assertEquals(txtField.getText() , "tmp");
    }

    @Test public void setPrefColCountAndSeeValueIsReflectedInModel() {
        txtField.setPrefColumnCount(10);
        assertEquals(txtField.prefColumnCountProperty().get(), 10.0, 0.0);
    }

    @Test public void setPrefColCountAndSeeValue() {
        txtField.setPrefColumnCount(10);
        assertEquals(txtField.getPrefColumnCount(), 10.0 ,0.0);
    }

    @Test public void insertAndCheckSubRangeInText() {
        TextInputControlShim.getContent_insert(dummyTxtField, 0, "x", true);
        assertEquals("x", dummyTxtField.getText().substring(0,1));
    }

    @Test public void insertAndCheckSubRangeInContent() {
        TextInputControlShim.getContent_insert(dummyTxtField, 0, "x", true);
        assertEquals("x", TextInputControlShim.getContent_get(dummyTxtField, 0, 1));
    }

    @Test public void appendAndCheckSubRangeInText() {
        dummyTxtField.appendText("x");
        assertEquals("x", dummyTxtField.getText().substring(dummyTxtField.getLength() - 1,dummyTxtField.getLength()));
    }

    @Test public void appendAndCheckSubRangeInContent() {
        dummyTxtField.appendText("x");
        assertEquals("x", TextInputControlShim.getContent_get(dummyTxtField, dummyTxtField.getLength() - 1,dummyTxtField.getLength()));
    }

    @Test public void deleteAndCheckText() {
        TextInputControlShim.getContent_insert(dummyTxtField, 0, "x", false);
        TextInputControlShim.getContent_delete(dummyTxtField, 1, dummyTxtField.getLength(), true);
        assertEquals("x", dummyTxtField.getText());
    }



}
