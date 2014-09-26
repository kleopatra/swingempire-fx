/*
 * Created on 16.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.SkinBase;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.util.StageLoader;
import static org.junit.Assert.*;

/**
 * Extracted common ancestor for ChoiceBox/ChoiceBoxX testing.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public abstract class AbstractChoiceInterfaceSelectionIssues<V extends Control> 
    extends SelectionIssues<V, SingleSelectionModel>{

//------------------- Tests around replacing internals
    
    /**
     * Replace custom objectProperty selectionModel by simple objectProperty 
     * plus PathAdapter.
     * 
     * make sure that PathAdapter updates value as expected.
     */
    @Test
    public void testSelectionUpdatesValue() {
       int index = 2;
       getSelectionModel().select(index);
       assertEquals("sanity: selectedItem", items.get(index), getSelectionModel().getSelectedItem());
       assertEquals("value must be updated to selected item", 
               getSelectionModel().getSelectedItem(), getChoiceView().getValue());
    }
    
    @Test
    public void testValueUpdatesSelection() {
        Object value = items.get(3);
        getChoiceView().setValue(value);
        assertEquals(value, getSelectionModel().getSelectedItem());
    }
    
    /**
     * Here we test the selectedIndex and selectedItem - logic seems inconsistent
     * for contained items
     * 
     * - on clear, selectedItem is nulled
     * - on remove, it is kept
     * 
     *
     * @see #testClearItemsResetsSelection()
     * @See #testSelectedOnRemoveItemAtSelectedFocused()
     */
    @Test
    public void testRemoveSelectedItemIfSelectedItemContained() {
        int index = 2;
        getSelectionModel().select(index);
        Object item = items.get(index);
        items.remove(index);
        assertEquals(-1, getSelectionModel().getSelectedIndex());
        assertEquals(null, getSelectionModel().getSelectedItem());
    }
    
    /**
     * can't really test: which item to remove? All?
     * 
     * @see #testClearItemsResetsSelection()
     */
    @Test
    public void testRemoveSelectedItemIfSelectedItemUncontained() {
        String uncontained = "uncontained";
        getSelectionModel().select(uncontained);
        items.clear();
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
    }
    /**
     * Implementation of itemsProperty in choiceBox vs. itemsChangeListener in 
     * model are inconclusive:
     * - former clears selection except if uncontained selectedItem
     * - latter clears only on empty
     * 
     * Taking former as the real intention: 
     * if (selectedItem uncontained before setting the list)
     *     keep selectedItem, update index if contained in new list
     * else // selectedItem had been in old list     
     *     clearSelection 
     */
    @Test
    public void testSetItemsIfSelectedItemContained() {
        int index = items.size() - 5;
        getSelectionModel().select(index);
        ObservableList subList = FXCollections.observableList(items.subList(index, items.size() - 1));
        getChoiceView().setItems(subList);
        assertEquals("selectedItem cleared if contained before setting", 
                null, getSelectionModel().getSelectedItem());
    }
    
    /**
     * Implementation of itemsProperty in choiceBox vs. itemsChangeListener in 
     * model are inconclusive:
     * - former clears selection except if uncontained selectedItem
     * - latter clears only on empty
     */
    @Test
    public void testSetItemsIfSelectedItemUncontained() {
        int index = items.size() - 5;
        getSelectionModel().select("uncontained");
        Object selectedItem = getSelectionModel().getSelectedItem();
        ObservableList subList = FXCollections.observableList(items.subList(index, items.size() - 1));
        getChoiceView().setItems(subList);
        assertEquals("selectedItem unchanged if not in new list", selectedItem, getSelectionModel().getSelectedItem());
    }
    
    /**
     * Regression testing: https://javafx-jira.kenai.com/browse/RT-29433
     */
    @Test
    public void testSetEmptyItemsResetsSelectedItemUncontained() {
        String uncontained = "uncontained";
        getSelectionModel().select(uncontained);
        getChoiceView().setItems(FXCollections.emptyObservableList());
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals(uncontained, getChoiceView().getValue());
    }
    
    /**
     * Regression testing: https://javafx-jira.kenai.com/browse/RT-29433
     * 
     * Here we test the effect of clearing the selection.
     * 
     * Logic somehwat inconsistent with removing? Or are we again on 
     * contained vs uncontained selectedItem?
     */
    @Test
    public void testClearItemsNotResetsSelectedItemUncontained() {
        String uncontained = "uncontained";
        getSelectionModel().select(uncontained);
        getChoiceView().getItems().clear();
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals(uncontained, getChoiceView().getValue());
    }
    
    
    /**
     * Regression testing: https://javafx-jira.kenai.com/browse/RT-29433
     */
    @Test
    public void testSetEmptyItemsResetsSelection() {
        int index = 2;
        getSelectionModel().select(index);
        getChoiceView().setItems(FXCollections.emptyObservableList());
        assertEquals(null, getSelectionModel().getSelectedItem());
        assertEquals(null, getChoiceView().getValue());
    }
    
    /**
     * Regression testing: https://javafx-jira.kenai.com/browse/RT-29433
     * 
     * Here we test the effect of clearing the selection.
     * 
     * Logic somehwat inconsistent with removing? Or are we again on 
     * contained vs uncontained selectedItem? Here the selectedItem
     * is contained in the selection ...
     * 
     * hmm .. or does it make a difference how it is selected 
     * indirectly via select(index) or directly via select(item)?
     */
    @Test
    public void testClearItemsResetsSelectionBySelectItem() {
        int index = 2;
        getSelectionModel().select(items.get(index));
        getChoiceView().getItems().clear();
        assertEquals(null, getSelectionModel().getSelectedItem());
        assertEquals(null, getChoiceView().getValue());
    }
    
    /**
     * Regression testing: https://javafx-jira.kenai.com/browse/RT-29433
     * 
     * Here we test the effect of clearing the selection.
     * 
     * Logic somehwat inconsistent with removing? Or are we again on 
     * contained vs uncontained selectedItem? Here the selectedItem
     * is contained in the selection ...
     * 
     * hmm .. or does it make a difference how it is selected 
     * indirectly via select(index) or directly via select(item)?
     */
    @Test
    public void testClearItemsResetsSelection() {
        int index = 2;
        getSelectionModel().select(index);
        getChoiceView().getItems().clear();
        assertEquals(null, getSelectionModel().getSelectedItem());
        assertEquals(null, getChoiceView().getValue());
    }
    
    /**
     * Arguable:
     * - selectedIndex is cleared on modifying the selectedItem
     * - selectedItem is old selectedItem
     * - value is oldValue (aka: synched to selectedItem) but not showing
     * 
     * Same behavior as selecting an uncontained item in the model
     * Note: api doc of ChoiceBox explicitly allows programmatic (vs. user
     * induced) selection of uncontained elements!
     * 
     * So here we test the synch of selectedItem/value - ignore the
     * other tests for now, should be driven by model!
     */
    @Test
    public void testSetItemAtSelectedIndexEffectSynchValueSelectedItem() {
        StageLoader loader = new StageLoader(getView());
        int index = 2;
        getSelectionModel().select(index);
        Object selected = getSelectionModel().getSelectedItem();
        Object modified = selected + "dummy";
        getChoiceView().getItems().set(index, modified);
        assertEquals(getSelectionModel().getSelectedItem(), getChoiceView().getValue());
    }

    /**
     * Issue: uncontained selectedItem allowed but not shown in choicebox
     * Beware: bowel testing of skin!
     */
    @Test
    public void testUncontainedSelectedItemShown() {
        StageLoader loader = new StageLoader(getView());
        Object uncontained = "here we go with something";
        getSelectionModel().select(uncontained);
        Label label = getLabel();
        assertEquals("choice must show uncontained item", uncontained, label.getText());
    }

    /**
     * Beware: relying on implementation detail - label is first of skin's children.
     * @return the label that's showing the choicbox text
     */
    protected Label getLabel() {
        SkinBase skin = (SkinBase) getView().getSkin();
        return (Label) skin.getChildren().get(0);
    }

    /**
     * Arguable:
     * - selectedIndex is cleared on modifying the selectedItem
     * - selectedItem is old selectedItem
     * - value is oldValue (aka: synched to selectedItem) but not showing
     * 
     * Same behavior as selecting an uncontained item in the model
     * Note: api doc of ChoiceBox explicitly allows programmatic (vs. user
     * induced) selection of uncontained elements!
     * 
     */
    @Test @Ignore
    public void testSetItemAtSelectedIndexEffectOnSelectedIndex() {
        StageLoader loader = new StageLoader(getView());
        int index = 2;
        getSelectionModel().select(index);
        Object selected = getSelectionModel().getSelectedItem();
        Object modified = selected + "dummy";
        getChoiceView().getItems().set(index, modified);
        assertEquals(index, getSelectionModel().getSelectedIndex());
    }
    
    /**
     * Arguable:
     * - selectedIndex is cleared on modifying the selectedItem
     * - selectedItem is old selectedItem
     * - value is oldValue (aka: synched to selectedItem) but not showing
     */
    @Test @Ignore
    public void testSetItemAtSelectedIndexEffectOnValue() {
        StageLoader loader = new StageLoader(getView());
        int index = 2;
        getSelectionModel().select(index);
        Object selected = getSelectionModel().getSelectedItem();
        Object modified = selected + "dummy";
        getChoiceView().getItems().set(index, modified);
        assertEquals("selectedItem must be updated", modified, getChoiceView().getValue());
    }
    
    /**
     * Arguable:
     * - selectedIndex is cleared on modifying the selectedItem
     * - selectedItem is old selectedItem
     * - value is oldValue (aka: synched to selectedItem) but not showing
     * 
     * skin or not doesn't make a difference!
     * 
     */
    @Test @Ignore
    public void testSetItemAtSelectedIndexEffectOnSelectedItem() {
        StageLoader loader = new StageLoader(getView());
        int index = 2;
        getSelectionModel().select(index);
        Object selected = getSelectionModel().getSelectedItem();
        Object modified = selected + "dummy";
        getChoiceView().getItems().set(index, modified);
        assertEquals("selectedItem must be updated", modified, getSelectionModel().getSelectedItem());
    }
    
    /**
     * Super's assumption (on not-assumption, general behaviour might be open
     * to debate) not applicable here. Replaced by 
     * 
     * PENDING JW: ignor is ignored .. why? ahh ... have to repeat the @Test 
     * annotation
     * 
     * @see #testRemoveSelectedItemIfSelectedItemContained()
     * @see #testRemoveSelectedItemIfSelectedItemUncontained()
     */
    @Override @Test @Ignore
    public void testSelectedOnRemoveItemAtSelectedFocused() {
        super.testSelectedOnRemoveItemAtSelectedFocused();
    }


    //-------------------    
    /**
     * The stageLoader used to force skin creation. It's an artefact of fx
     * instantiation process, not meant to be really used.
     * Note that it's the responsibility of the test method itself (not the setup)
     * to init if needed.
     */
    protected StageLoader loader;

    
    /**
     * ChoiceBox.setSelectionModel must update box' value
     * To test we set a model which has a selection - doesn't update choiceBox value.
     * Here we test the skinless box
     */
    @Test
    public void testSetSelectionModelWithSelectionNoSkin() {
        SingleSelectionModel model = createSimpleSelectionModel();
        int index = 2;
        model.select(index);
        assertEquals("sanity: model is selecting index and item", items.get(index), 
                model.getSelectedItem());
        getChoiceView().setSelectionModel(model);
        assertEquals("box value must be same as selected item", items.get(index), getChoiceView().getValue());
    }

    /**
     * ChoiceBox.setSelectionModel must update box' value
     * To test we set a model which has a selection - doesn't update choiceBox value.
     * Here we force the skin, just in case
     */
    @Test
    public void testSetSelectionModelWithSelectionWithSkin() {
        StageLoader loader = new StageLoader(getView());
        SingleSelectionModel model = createSimpleSelectionModel();
        int index = 2;
        model.select(index);
        assertEquals("sanity: model is selecting index and item", items.get(index), 
                model.getSelectedItem());
        getChoiceView().setSelectionModel(model);
        assertEquals("box value must be same as selected item", items.get(index), getChoiceView().getValue());
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
        SingleSelectionModel model = createSimpleSelectionModel();
        int index = 2;
        getChoiceView().setSelectionModel(model);
        model.select(index);
        assertEquals("box value must be same as selected item", items.get(index), getChoiceView().getValue());
    }

//------------------- Null selectionModel
    
    /**
     * Logs exception stacktrace that doesn't show up as error (actually not at all)
     * in test log - why not? Thread issue? 
     */
    @Test @Ignore
    public void testNullSelectionModelOnSetValue() {
        setSelectionModel(null);
        getChoiceView().setValue(items.get(3));
        assertEquals(items.get(3), getChoiceView().getValue());
    }
    
    @Test @Ignore
    public void testNullSelectionModelPreviouslySelectedOnSetValue() {
        getSelectionModel().select(2);
        setSelectionModel(null);
        assertEquals(null, getChoiceView().getValue());
        getChoiceView().setValue(items.get(3));
        assertEquals(items.get(3), getChoiceView().getValue());
    }
    
    
// ------------------ infrastructure
    
    
    @Override
    protected SingleSelectionModel getSelectionModel() {
        return getChoiceView().getSelectionModel();
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertSame("sanity: view and choiceView are the same", getView(), getChoiceView());
    }

    protected abstract ChoiceInterface getChoiceView();

    protected abstract SingleSelectionModel createSimpleSelectionModel();

    @Override
    protected int getAnchorIndex(int index) {
        return index;
    }

    @Override
    protected FocusModel getFocusModel() {
        return null;
    }

    
    @Override
    protected void setSelectionModel(SingleSelectionModel model) {
        getChoiceView().setSelectionModel(model);
    }


    /**
     * Simply a tagging interface with methods needed for comparative testing
     * of ChoiceBoxX/ChoiceBox.
     * 
     */
    public static interface ChoiceInterface<T> {
     
        SingleSelectionModel<T> getSelectionModel();

        void setSelectionModel(SingleSelectionModel<T> model);
        
        T getValue();
        
        void setValue(T value);
        
        void setItems(ObservableList<T> items);
        ObservableList<T> getItems();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AbstractChoiceInterfaceSelectionIssues.class.getName());
}
