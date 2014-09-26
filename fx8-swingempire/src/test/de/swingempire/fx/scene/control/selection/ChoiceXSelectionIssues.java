/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Objects;

import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.scene.control.rt38724.ChoiceBoxX;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ChoiceXSelectionIssues extends 
    AbstractChoiceInterfaceSelectionIssues<ChoiceBoxX> {

    @Test
    public void testItemsList() {
        assertSame(getView().itemsProperty().get(), getView().itemsListProperty().get());
    }
    
    @Override
    protected SimpleChoiceXSelectionModel createSimpleSelectionModel() {
        return new SimpleChoiceXSelectionModel(getView());
    }
  
    @Override
    protected ChoiceXControl createView(ObservableList items) {
        return new ChoiceXControl(items);
    }
    
    @Override
    protected ChoiceInterface getChoiceView() {
        return (ChoiceInterface) getView();
    }

    /**
     * Very simplistic model, just for testing setSelectionModel. Can't 
     * handle changes in the underlying items nor separators!
     */
    public static class SimpleChoiceXSelectionModel extends SingleSelectionModel {

        private ChoiceBoxX choiceBox;

        /**
         * 
         */
        public SimpleChoiceXSelectionModel(ChoiceBoxX box) {
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
    
    private static class ChoiceXControl<T> extends ChoiceBoxX<T> implements ChoiceInterface<T> {

        public ChoiceXControl() {
            super();
        }

        public ChoiceXControl(ObservableList<T> items) {
            super(items);
        }
        
        
    }
}
