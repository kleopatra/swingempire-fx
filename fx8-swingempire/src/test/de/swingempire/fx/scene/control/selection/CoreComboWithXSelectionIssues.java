/*
t * Created on 30.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Objects;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.scene.control.comboboxx.ComboBoxXSelectionModel;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.skin.ComboBoxBaseSkin;
import javafx.scene.control.skin.ComboBoxListViewSkin;

/**
 * Testing core ComboBox with ComboBoxXSelectionModel.
 * 
 * commented the builder code - doesn't compile since long ..
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class CoreComboWithXSelectionIssues 
    extends AbstractChoiceInterfaceSelectionIssues<ComboBox, SingleSelectionModel> {

    
    /**
     * Trying to reproduce RT_26079 with builder: 
     * blowing if set equal but not same list
     * 
     */
//    @Test
//    @ConditionalIgnore(condition = SelectionIgnores.IgnoreRT26079.class)
//    public void testSelectFirstMemoryWithBuilderEqualsList() {
//        view = 
//                ComboBoxBuilder.<String>create()
//                .items(FXCollections.observableArrayList("E1", "E2", "E3"))
//                // no difference
////                .editable(false)
//                .build();
//        view.setSelectionModel(new ComboBoxXSelectionModel<>(view.itemsProperty()));
//        view.getSelectionModel().selectFirst();
//        view.getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E3"));
//        view.getSelectionModel().clearSelection();
//        assertEquals(-1, view.getSelectionModel().getSelectedIndex());
//        assertEquals(null, view.getSelectionModel().getSelectedItem());
//        assertEquals(null, view.getValue());
////        assertEquals("", getDisplayText());
//        
//    }
//    /**
//     * Trying to reproduce RT_26079 with builder: 
//     * fine if size of new list is different (here: longet)
//     * 
//     */
//    @Test
//    @ConditionalIgnore(condition = SelectionIgnores.IgnoreRT26079.class)
//    public void testSelectFirstMemoryWithBuilderSimilarLongerList() {
//        view = 
//                ComboBoxBuilder.<String>create()
//                .items(FXCollections.observableArrayList("E1", "E2", "E3"))
//                // no difference
////                .editable(false)
//                .build();
//        view.setSelectionModel(new ComboBoxXSelectionModel<>(view.itemsProperty()));
//        view.getSelectionModel().selectFirst();
//        view.getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E5", "E6"));
//        view.getSelectionModel().clearSelection();
//        assertEquals(-1, view.getSelectionModel().getSelectedIndex());
//        assertEquals(null, view.getSelectionModel().getSelectedItem());
//        assertEquals(null, view.getValue());
////        assertEquals("", getDisplayText());
//        
//    }
//    /**
//     * Trying to reproduce RT_26079 with builder: 
//     * blowing if set equal but not same list
//     * 
//     */
//    @Test
//    @ConditionalIgnore(condition = SelectionIgnores.IgnoreRT26079.class)
//    public void testSelectFirstMemoryWithBuilderSimilarList() {
//        view = 
//                ComboBoxBuilder.<String>create()
//                .items(FXCollections.observableArrayList("E1", "E2", "E3"))
//                // no difference
////                .editable(false)
//                .build();
//        view.setSelectionModel(new ComboBoxXSelectionModel<>(view.itemsProperty()));
//        view.getSelectionModel().selectFirst();
//        view.getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E5"));
//        view.getSelectionModel().clearSelection();
//        assertEquals(-1, view.getSelectionModel().getSelectedIndex());
//        assertEquals(null, view.getSelectionModel().getSelectedItem());
//        assertEquals(null, view.getValue());
////        assertEquals("", getDisplayText());
//
//    }
    
    
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
        combo.setSelectionModel(new ComboBoxXSelectionModel(combo.itemsProperty()));
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
    
    
    @Override
    protected boolean hasDependendSelectionModel() {
        return true;
    }
    @Override
    protected SelectionModel getDependentSelectionModel() {
        ComboBoxListViewSkin skin = (ComboBoxListViewSkin) getView().getSkin();
        // jdk8
//        return skin.getListView().getSelectionModel();
        // post-8
        return ((ListView) skin.getPopupContent()).getSelectionModel();
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
