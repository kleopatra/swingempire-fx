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

import de.swingempire.fx.scene.control.comboboxx.ComboBoxX;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ComboXSelectionIssues 
    extends AbstractChoiceInterfaceSelectionIssues<ComboBoxX> {


    
    @Override
    protected String getDisplayText() {
        Node node = ((ComboBoxBaseSkin) getView().getSkin()).getDisplayNode();
        if (node instanceof ListCell) {
            return ((ListCell) node).getText();
        } 
        return "";
    }

    @Override
    protected ComboBoxX createView(ObservableList items) {
        return new ComboXControl(items);
    }

    @Override
    protected SimpleComboXSelectionModel createSimpleSelectionModel() {
        return new SimpleComboXSelectionModel(getView());
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
    public static class SimpleComboXSelectionModel extends SingleSelectionModel {

        private ComboBoxX choiceBox;

        /**
         * 
         */
        public SimpleComboXSelectionModel(ComboBoxX box) {
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
    
    
    public static class ComboXControl<T> extends ComboBoxX<T> implements ChoiceInterface<T> {

        public ComboXControl() {
            super();
        }

        public ComboXControl(ObservableList<T> items) {
            super(items);
        }
        
        
    }


    @Override
    protected boolean isClearSelectionOnSetItem() {
        // TODO Auto-generated method stub
        return false;
    }


}
