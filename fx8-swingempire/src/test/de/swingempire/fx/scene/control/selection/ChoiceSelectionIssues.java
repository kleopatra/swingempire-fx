/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.FocusModel;
import javafx.scene.control.SingleSelectionModel;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ChoiceSelectionIssues extends SelectionIssues<ChoiceBox, SingleSelectionModel> {

  
    @Override
    protected SingleSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }
    
    @Override
    protected ChoiceBox createView(ObservableList items) {
        return new ChoiceBox(items);
    }

    @Override
    protected FocusModel getFocusModel() {
        return null;
    }
    
    @Override
    protected int getAnchorIndex(int index) {
        return index;
    }

}
