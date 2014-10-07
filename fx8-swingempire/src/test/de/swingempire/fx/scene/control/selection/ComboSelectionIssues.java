/*
 * Created on 30.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.control.SingleSelectionModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;
import com.sun.javafx.scene.control.skin.ComboBoxBaseSkin;

import static org.junit.Assert.*;

/**
 * Testing core ComboBox.
 * 
 * Note: replacing the combo's core selectionModel with ComboBoxSelectionModel 
 * (same as ComboBoxXSelectionModel except typing of control) makes most tests
 * pass.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ComboSelectionIssues 
    extends AbstractChoiceInterfaceSelectionIssues<ComboBox, SingleSelectionModel> {

    
    /**
     * Trying to reproduce RT_26079 with builder: 
     * blowing if set equal but not same list
     * 
     */
    @Test
    @ConditionalIgnore(condition = SelectionIgnores.IgnoreRT26079.class)
    public void testSelectFirstMemoryWithBuilderEqualsList() {
        view = 
                ComboBoxBuilder.<String>create()
                .items(FXCollections.observableArrayList("E1", "E2", "E3"))
                // no difference
//                .editable(false)
                .build();
        view.getSelectionModel().selectFirst();
        view.getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E3"));
        view.getSelectionModel().clearSelection();
        assertEquals(-1, view.getSelectionModel().getSelectedIndex());
        assertEquals(null, view.getSelectionModel().getSelectedItem());
        assertEquals(null, view.getValue());
//        assertEquals("", getDisplayText());
        
    }
    /**
     * Trying to reproduce RT_26079 with builder: 
     * fine if size of new list is different (here: longet)
     * 
     */
    @Test
    @ConditionalIgnore(condition = SelectionIgnores.IgnoreRT26079.class)
    public void testSelectFirstMemoryWithBuilderSimilarLongerList() {
        view = 
                ComboBoxBuilder.<String>create()
                .items(FXCollections.observableArrayList("E1", "E2", "E3"))
                // no difference
//                .editable(false)
                .build();
        view.getSelectionModel().selectFirst();
        view.getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E5", "E6"));
        view.getSelectionModel().clearSelection();
        assertEquals(-1, view.getSelectionModel().getSelectedIndex());
        assertEquals(null, view.getSelectionModel().getSelectedItem());
        assertEquals(null, view.getValue());
//        assertEquals("", getDisplayText());
        
    }
    /**
     * Trying to reproduce RT_26079 with builder: 
     * blowing if set equal but not same list
     * 
     */
    @Test
    @ConditionalIgnore(condition = SelectionIgnores.IgnoreRT26079.class)
    public void testSelectFirstMemoryWithBuilderSimilarList() {
        view = 
                ComboBoxBuilder.<String>create()
                .items(FXCollections.observableArrayList("E1", "E2", "E3"))
                // no difference
//                .editable(false)
                .build();
        view.getSelectionModel().selectFirst();
        view.getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E5"));
        view.getSelectionModel().clearSelection();
        assertEquals(-1, view.getSelectionModel().getSelectedIndex());
        assertEquals(null, view.getSelectionModel().getSelectedItem());
        assertEquals(null, view.getValue());
//        assertEquals("", getDisplayText());

    }
    @Override
    protected String getDisplayText() {
        Node node = ((ComboBoxBaseSkin) getView().getSkin()).getDisplayNode();
        if (node instanceof ListCell) {
            return ((ListCell) node).getText();
        } 
        return "";
    }

    @Override
    protected ComboBox createView(ObservableList items) {
        ComboCoreControl combo = new ComboCoreControl(items);
//        combo.setSelectionModel(new ComboBoxSelectionModel(combo));
        return combo;
    }

    @Override
    protected SimpleComboSelectionModel createSimpleSelectionModel() {
        return new SimpleComboSelectionModel(getView());
    }
    
    @Override
    protected ChoiceControl getChoiceView() {
        return (ChoiceControl) getView();
    }

    
    @Override
    protected boolean supportsSeparators() {
        return false;
    }

    @Override
    protected boolean hasPopup() {
        return false;
    }
    

    /**
     * Very simplistic model, just for testing setSelectionModel. Can't 
     * handle changes in the underlying items nor separators!
     */
    public static class SimpleComboSelectionModel extends SingleSelectionModel {

        private ComboBox choiceBox;

        /**
         * 
         */
        public SimpleComboSelectionModel(ComboBox box) {
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
    
    
    public static class ComboCoreControl<T> extends ComboBox<T> implements ChoiceControl<T, SingleSelectionModel<T>> {

        public ComboCoreControl() {
            super();
        }

        public ComboCoreControl(ObservableList<T> items) {
            super(items);
        }
        
        
    }


    @Override
    protected boolean isClearSelectionOnSetItem() {
        // TODO Auto-generated method stub
        return false;
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
