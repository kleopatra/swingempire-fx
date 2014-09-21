/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.FocusModel;
import javafx.scene.control.SingleSelectionModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.util.StageLoader;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ChoiceSelectionIssues extends SelectionIssues<ChoiceBox, SingleSelectionModel> {

    /**
     * ChoiceBox.setSelectionModel must update box' value
     * To test we set a model which has a selection - doesn't update choiceBox value.
     * Here we test the skinless box
     */
    @Test
    public void testSetSelectionModelWithSelectionNoSkin() {
        SingleSelectionModel model = new SimpleChoiceSelectionModel(getView());
        int index = 2;
        model.select(index);
        assertEquals("sanity: model is selecting index and item", items.get(index), 
                model.getSelectedItem());
        getView().setSelectionModel(model);
        assertEquals("box value must be same as selected item", items.get(index), getView().getValue());
    }
    
    /**
     * ChoiceBox.setSelectionModel must update box' value
     * To test we set a model which has a selection - doesn't update choiceBox value.
     * Here we force the skin, just in case
     */
    @Test
    public void testSetSelectionModelWithSelectionWithSkin() {
        StageLoader loader = new StageLoader(getView());
        SingleSelectionModel model = new SimpleChoiceSelectionModel(getView());
        int index = 2;
        model.select(index);
        assertEquals("sanity: model is selecting index and item", items.get(index), 
                model.getSelectedItem());
        getView().setSelectionModel(model);
        assertEquals("box value must be same as selected item", items.get(index), getView().getValue());
    }
    
    /**
     * Standalone test: setting a selectionModel which has a selected item
     * must update the value 
     */
    @Test
    public void testSetSelectionModelUpdatesValueStandalone() {
        ObservableList<String> items = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");

        ChoiceBox box = new ChoiceBox(items);
        SingleSelectionModel model = new SingleSelectionModel() {
            @Override
            protected Object getModelItem(int index) {
                if (index < 0 || index >= getItemCount()) return null;
                return box.getItems().get(index);
            }

            @Override
            protected int getItemCount() {
                return box.getItems() != null ? box.getItems().size() : 0;
            }
            
        };
        // just to be on the safe side in case the skin/behaviour is
        // responsible for the update
        // doesn't make a difference, though
        StageLoader loader = new StageLoader(box);
        int index = 2;
        model.select(index);
        assertEquals("sanity: model is selecting index and item", items.get(index), 
                model.getSelectedItem());
        box.setSelectionModel(model);
        assertEquals("box value must be same as selected item", items.get(index), box.getValue());
    }
    /**
     * ChoiceBoxBehavior has some weird code around selection/model/changes.
     * trying to find macroscopic behaviour failure.
     * 
     * Here: sanity testing, selecting an item in the model updates box value
     */
    @Test
    public void testSetSelectionModelSelectAfterSetting() {
        StageLoader loader = new StageLoader(getView());
        SingleSelectionModel model = new SimpleChoiceSelectionModel(getView());
        int index = 2;
        getView().setSelectionModel(model);
        model.select(index);
        assertEquals("box value must be same as selected item", items.get(index), getView().getValue());
    }
  
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

    /**
     * Very simplistic model, just for testing setSelectionModel. Can't 
     * handle changes in the underlying items nor separators!
     */
    public static class SimpleChoiceSelectionModel extends SingleSelectionModel {

        private ChoiceBox choiceBox;

        /**
         * 
         */
        public SimpleChoiceSelectionModel(ChoiceBox box) {
            this.choiceBox = Objects.requireNonNull(box, "ChoiceBox must not be null");
        }

        @Override
        protected Object getModelItem(int index) {
            if (index < 0 || index >= getItemCount()) return null;
            return choiceBox.getItems().get(index);
        }

        @Override
        protected int getItemCount() {
            return choiceBox.getItems() != null ? choiceBox.getItems().size() : 0;
        }
        
    }
}
