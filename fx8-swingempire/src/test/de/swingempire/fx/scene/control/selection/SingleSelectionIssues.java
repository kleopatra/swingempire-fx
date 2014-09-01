/*
 * Created on 06.11.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Arrays;
import java.util.Collection;

import javafx.scene.control.Control;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Super class for testing single selection behaviour of MultipleSelectionModel 
 * in both modes.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
//@RunWith(JUnit4.class)
public abstract class SingleSelectionIssues<C extends Control, M extends MultipleSelectionModel> extends SelectionIssues<C, M> {

    protected boolean multipleMode;
    
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
