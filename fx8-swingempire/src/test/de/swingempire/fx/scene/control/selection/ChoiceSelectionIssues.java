/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Objects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import static org.junit.Assert.*;

import de.swingempire.fx.scene.control.choiceboxx.ChoiceBoxRT38724;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreSetSelectionModel;
import de.swingempire.fx.util.StageLoader;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ChoiceSelectionIssues 
    extends AbstractChoiceInterfaceSelectionIssues<ChoiceBox, SingleSelectionModel> {

    /**
     * Used in bug report, invers of RT-38724  
     * Standalone test: setting a selectionModel to the choicebox with
     * a bound value must update the selectedItem of the model.
     */
    @ConditionalIgnore (condition = IgnoreSetSelectionModel.class)
    @Test
    public void testSetSelectionModelWithSelectionBoundValueStandalone() {
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
//        StageLoader loader = new StageLoader(box);
        int index = 2;
        model.select(index);
        ObjectProperty property = new SimpleObjectProperty(items.get(1));
        box.valueProperty().bind(property);

        box.setSelectionModel(model);
        assertEquals("selectedItem must be updated to property", property.get(), model.getSelectedItem());
    }
    /**
     * Used in bug report
     * Standalone test: setting a selectionModel which has a selected item
     * must update the value 
     */
    @Test
    @ConditionalIgnore (condition = IgnoreSetSelectionModel.class)
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
//        StageLoader loader = new StageLoader(box);
        int index = 2;
        model.select(index);
        assertEquals("sanity: model is selecting index and item", items.get(index), 
                model.getSelectedItem());
        box.setSelectionModel(model);
        assertEquals("box value must be same as selected item", items.get(index), box.getValue());
    }

    /**
     * Used in bug report, invers of RT-38724  
     * Standalone test: setting a selectionModel to the choicebox with
     * a bound value must update the selectedItem of the model.
     */
    @ConditionalIgnore (condition = IgnoreSetSelectionModel.class)
    @Test
    public void testSetSelectionModelWithSelectionBoundValueStandaloneFix() {
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
//        StageLoader loader = new StageLoader(box);
        int index = 2;
        model.select(index);
        ObjectProperty property = new SimpleObjectProperty(items.get(1));
        box.valueProperty().bind(property);

        box.setSelectionModel(model);
        assertEquals("selectedItem must be updated to property", property.get(), model.getSelectedItem());
    }

    @Test
    @ConditionalIgnore (condition = IgnoreSetSelectionModel.class)
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
    protected ChoiceControl getChoiceView() {
        return (ChoiceControl) getView();
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
    
    
    public static class ChoiceCoreControl<T> extends ChoiceBox<T> implements ChoiceControl<T, SingleSelectionModel<T>> {

        public ChoiceCoreControl() {
            super();
        }

        public ChoiceCoreControl(ObservableList<T> items) {
            super(items);
        }

        /**
         * No-op: testing artefact
         * PENDING JW: cleanup test hierarchy
         */
        @Override
        public void setEditable(boolean editable) {
            // do nothing
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

    @Override
    protected boolean isClearSelectionOnSetItem() {
        return true;
    }

    @Override
    protected SingleSelectionModel getSelectionModel() {
        return getView().getSelectionModel();
    }

    @Override
    protected void setSelectionModel(SingleSelectionModel model) {
        getView().setSelectionModel(model);
    }

}
