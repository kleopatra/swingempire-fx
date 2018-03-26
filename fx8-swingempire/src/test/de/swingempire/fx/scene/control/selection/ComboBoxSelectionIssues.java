/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.FocusModel;
import javafx.scene.control.SingleSelectionModel;

/**
 * PENDING: what is this? looks like a left-over 
 * before introducing the AbstractChoice layer?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ComboBoxSelectionIssues extends SelectionIssues<ComboBox, SingleSelectionModel> {

  
    @Override
    protected SingleSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }
    
    @Override
    protected ComboBox createView(ObservableList items) {
        return new ComboBox(items);
    }

    @Override
    protected FocusModel getFocusModel() {
        return null;
    }

    @Override
    protected int getAnchorIndex(int index) {
        return index;
    }

    @Override
    protected void setSelectionModel(SingleSelectionModel model) {
        getView().setSelectionModel(model);
    }

    @Override
    protected void resetItems(ObservableList other) {
        getView().setItems(other);
    }
    
    
}
