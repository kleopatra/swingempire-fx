/*
 * Created on 06.11.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

import javafx.scene.control.Control;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

/**
 * Super class for testing SelectionModel api of MultipleSelectionModel 
 * in both modes.
 * 
 * Also contains (for now) mode-dependent anchor behaviour tests, probably
 * should be moved.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
//@RunWith(JUnit4.class)
public abstract class SingleSelectionIssues<C extends Control, M extends MultipleSelectionModel> extends SelectionIssues<C, M> {

    protected boolean multipleMode;
    
    /**
     * Testing selectHome after having moved the focus below the last
     * selection.
     * 
     * MultipleMode only, need StageLoader because the behaviour is
     * spec'd against anchor by UX.
     * 
     * Hmm ... selectFirst != expandSelectionToHome - what I'm really after
     * is a clearAndSelectRange ... 
     */
    @Test
    public void testSelectionFirstWithUnselectedFocus() {
        if (!multipleMode) return;
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        getFocusModel().focusNext();
        getSelectionModel().selectFirst();
        assertEquals("anchor must be unchanged when moving focus", 
                index, getAnchorIndex(index));
        int next = getFocusedIndex(0);
        assertEquals("focus must be on first", 0, next);
    }

    /**
     * Anchor behaviour on selectNext (it's mode dependent!)
     * 
     * - single: anchor moved
     * - multiple: anchor not moved (TODO check UX)
     * 
     * THINK: move into MultipleSelection? 
     * 
     * 
     */
    @Test
    public void testAnchorPrevious() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().selectPrevious();
        int selected = getSelectedIndex();
        if (multipleMode) {
            assertEquals("multipleMode: anchor unchanged on adding selection", 
                    index, getAnchorIndex(index));
        } else {
            assertEquals("singleMode: anchor must move with selection",
                    selected, getAnchorIndex(selected));
        }
    }
    
    /**
     * Anchor behaviour on selectNext (it's mode dependent!)
     * 
     * - single: anchor moved
     * - multiple: anchor not moved (TODO check UX)
     * 
     * THINK: move into MultipleSelection? 
     * 
     */
    @Test
    public void testAnchorNext() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        getSelectionModel().selectNext();
        int selected = getSelectedIndex();
        if (multipleMode) {
            assertEquals("multipleMode: anchor must unchanged on adding selection", 
                    index, getAnchorIndex(index));
        } else {
            assertEquals("singleMode: anchor must move with selection",
                    selected, getAnchorIndex(selected));
                    
        }
    }

    public SingleSelectionIssues(boolean multiple) {
        this.multipleMode = multiple;
    }

    @Parameterized.Parameters
    public static Collection selectionModes() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    protected void checkMode(M model) {
       if (multipleMode && model.getSelectionMode() != SelectionMode.MULTIPLE) {
           model.setSelectionMode(SelectionMode.MULTIPLE);
       }
    }

}
