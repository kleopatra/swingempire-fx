/*
 * Created on 06.08.2018
 *
 */
package de.swingempire.fx.scene.control;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import javafx.scene.control.CheckBox;

/**
 * Divers controls.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ControlTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();


    @Test
    public void testCheckBoxIndeterminateSetSelectedThenIndeterminateThenFire() {
        CheckBox box = new CheckBox("normal");
        box.setAllowIndeterminate(true);
        box.setSelected(true);
        box.setIndeterminate(true);
        // properties are not coupled!
        // that is state is different from that after fire
//        assertTrue("indeterminate", box.isIndeterminate());
//        assertFalse("selected", box.isSelected());
        // now fire on this 
        box.fire();
        assertFalse("indeterminate", box.isIndeterminate());
        assertTrue("selected", box.isSelected());
    }
    
    @Test
    public void testCheckBoxIndeterminateSetSelectedThenIndeterminate() {
        CheckBox box = new CheckBox("normal");
        box.setAllowIndeterminate(true);
        box.setSelected(true);
        assertFalse("indeterminate", box.isIndeterminate());
        assertTrue("selected", box.isSelected());
        box.setIndeterminate(true);
        // properties are not coupled!
        // that is state is different from that after fire
        assertTrue("indeterminate", box.isIndeterminate());
        assertFalse("selected", box.isSelected());
    }
    
    @Test
    public void testCheckBoxIndeterminateInitialFireCycle() {
        CheckBox box = new CheckBox("normal");
        box.setAllowIndeterminate(true);
        // first fire
        box.fire();
        assertTrue("1. indeterminate after fire from initial !selected and !indeterminate", 
                box.isIndeterminate());
        assertFalse("1. selected unchanged after fire from initial !selected and !indeterminate",
                box.isSelected());
        // second fire
        box.fire();
        assertFalse("2. fire - state of indeterminate ", box.isIndeterminate());
        assertTrue("2. fire - state of selected", box.isSelected());
        // third fire
        box.fire();
        assertFalse("3. fire - state of indeterminate", box.isIndeterminate());
        assertFalse("3. fire - state of selected", box.isSelected());
        
    }
    
    @Test
    public void testCheckBoxIndeterminateInitial() {
        CheckBox box = new CheckBox("normal");
        box.setAllowIndeterminate(true);
        assertFalse("sanity: intitial state of selected", box.isSelected());
        assertFalse("sanity: intitial state of indeterminate", box.isIndeterminate());
    }
}
