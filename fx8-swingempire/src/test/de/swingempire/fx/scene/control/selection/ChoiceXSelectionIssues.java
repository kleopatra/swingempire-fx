/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SingleSelectionModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.scene.control.choiceboxx.ChoiceBoxX;
import de.swingempire.fx.scene.control.choiceboxx.ChoiceSetItem.MySelectionModel;
import de.swingempire.fx.scene.control.choiceboxx.SeparatorMarker;

import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ChoiceXSelectionIssues extends 
    AbstractChoiceInterfaceSelectionIssues<ChoiceBoxX> {

    
//------------ test custom selectionModel
    
    @Test
    public void testCustomModelKeepOnSetItemAtSelectedIndex() {
        getView().setSelectionModel(new MySelectionModel(getView()));
        int index = 0;
        String item = "newValue";
        // select index
        getSelectionModel().select(index);
        // replace item
        getView().getItems().set(index, item);
        assertEquals("selectedIndex must be same", index, getSelectionModel().getSelectedIndex());
        assertEquals("selectedItem must be newItem ", item, getSelectionModel().getSelectedItem());
        assertEquals("value must be new Item", item, getView().getValue());
    }
    @Test
    public void testCustomModelKeepOnSetItemAtSelectedIndexPopupUpdated() {
        getView().setSelectionModel(new MySelectionModel(getView()));
        initSkin();
        int index = 0;
        String item = "newValue";
        // select index
        getSelectionModel().select(index);
        // replace item
        getView().getItems().set(index, item);
        // safe, no sparators
        RadioMenuItem radio = (RadioMenuItem) getPopup().getItems().get(index);
        assertEquals("sanity", item, radio.getText());
        assertEquals("menu must be selected", true, radio.isSelected());
    }
//----------- test enhanced ChoiceBoxSelectionModel
    
    @Test
    public void testSeparatorsList() {
        getView().addSeparator(2);
        assertEquals(1, getView().separatorsListProperty().getValue().size());
    }
    
    @Test
    public void testSeparatorsInListInPopup() {
        int index = 2;
        getView().addSeparator(index);
        initSkin();
        MenuItem separator = getPopup().getItems().get(index + 1);
        assertTrue("menuitem must be separator but was: " + separator, 
                separator instanceof SeparatorMenuItem);
    }
    
    @Test
    public void testSeparatorsInListInPopupSeparatorUpdated() {
        int index = 2;
        initSkin();
        getView().addSeparator(index);
        MenuItem separator = getPopup().getItems().get(index + 1);
        assertTrue("menuitem must be separator but was: " + separator, 
                separator instanceof SeparatorMenuItem);
    }
    
    @Test
    public void testMenuItemsProperty() {
        initSkin();
        int index = 2;
        MenuItem item = getPopup().getItems().get(index);
        assertEquals(index, item.getProperties().get("data-index"));
    }
    @Test
    public void testSeparatorTypeSafe() {
        ObservableList<Item> items = FXCollections.observableArrayList(
                new Item("one"), new Item("two"), new Item("threee"), new Item("four"));
        int index = 2;
        // can't due to type restriction
        // items.set(index, new Separator());
        items.set(index, new DummyItem());
        getChoiceView().setItems(items);
        getSelectionModel().select(index - 1);
        getSelectionModel().selectNext();
        assertEquals("selecting next must move over separator",
                index + 1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testSeparatorTypeSafeMenuItem() {
        initSkin();
        ObservableList<Item> items = FXCollections.observableArrayList(
                new Item("one"), new Item("two"), new Item("threee"), new Item("four"));
        int index = 2;
        // can't due to type restriction
        // items.set(index, new Separator());
        items.set(index, new DummyItem());
        getChoiceView().setItems(items);
        MenuItem menuItem = getPopup().getItems().get(index);
        assertTrue("expected separatorMenuItem but was " + menuItem.getClass(),
                menuItem instanceof SeparatorMenuItem);
    }
    
    public static class DummyItem extends Item implements SeparatorMarker {

        /**
         * @param name
         */
        public DummyItem() {
            super("separator");
        }
        
    }
    

// ------------
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
    
    @Override
    protected boolean supportsSeparators() {
        return true;
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
    
    public static class ChoiceXControl<T> extends ChoiceBoxX<T> implements ChoiceInterface<T> {

        public ChoiceXControl() {
            super();
        }

        public ChoiceXControl(ObservableList<T> items) {
            super(items);
        }
        
        
    }

    @Override
    protected boolean hasPopup() {
        return true;
    }

}
