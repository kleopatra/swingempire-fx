/*
 * Created on 30.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Objects;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.SingleSelectionModel;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.sun.javafx.scene.control.skin.ComboBoxBaseSkin;
import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ComboSelectionIssues 
    extends AbstractChoiceInterfaceSelectionIssues<ComboBox> {


    
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
        return new ComboCoreControl(items);
    }

    @Override
    protected SimpleComboSelectionModel createSimpleSelectionModel() {
        return new SimpleComboSelectionModel(getView());
    }
    
    @Override
    protected ChoiceInterface getChoiceView() {
        return (ChoiceInterface) getView();
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
    
    
    public static class ComboCoreControl<T> extends ComboBox<T> implements ChoiceInterface<T> {

        public ComboCoreControl() {
            super();
        }

        public ComboCoreControl(ObservableList<T> items) {
            super(items);
        }
        
        
    }


}
