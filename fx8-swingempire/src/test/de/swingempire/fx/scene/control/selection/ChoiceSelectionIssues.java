/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;
import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.scene.control.choiceboxx.ChoiceBoxRT38724;
import de.swingempire.fx.scene.control.choiceboxx.ChoiceBoxX;
import de.swingempire.fx.scene.control.selection.AbstractChoiceInterfaceSelectionIssues.ChoiceInterface;
import de.swingempire.fx.util.StageLoader;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ChoiceSelectionIssues 
    extends AbstractChoiceInterfaceSelectionIssues<ChoiceBox> {

    /**
     * Used in bug report
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

    public void testSetSelectionModelUpdatesValueStandaloneFix() {
        ObservableList<String> items = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
    
        ChoiceBoxRT38724<String> box = new ChoiceBoxRT38724<>(items);
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



    @Override
    protected ChoiceBox createView(ObservableList items) {
        return new ChoiceCoreControl(items);
    }

    @Override
    protected SimpleChoiceSelectionModel createSimpleSelectionModel() {
        return new SimpleChoiceSelectionModel(getView());
    }
    
    @Override
    protected ChoiceInterface getChoiceView() {
        return (ChoiceInterface) getView();
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
    
    
    public static class ChoiceCoreControl<T> extends ChoiceBox<T> implements ChoiceInterface<T> {

        public ChoiceCoreControl() {
            super();
        }

        public ChoiceCoreControl(ObservableList<T> items) {
            super(items);
        }
        
        
    }


    @Override
    protected boolean supportsSeparators() {
        return true;
    }

    @Override
    protected boolean hasPopup() {
        return true;
    }

}
