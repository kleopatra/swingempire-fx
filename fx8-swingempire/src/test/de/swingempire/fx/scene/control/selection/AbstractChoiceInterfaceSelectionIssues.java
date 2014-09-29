/*
 * Created on 16.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.lang.reflect.Field;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.SkinBase;
import javafx.util.StringConverter;

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

//---------- test unselectable (Separator)
    
    
    @Test
    public void testSeparatorNotSelectedItem() {
        getSelectionModel().select(new Separator());
        assertEquals("selecting index with unselectable item must not change selected index",
                -1, getSelectionModel().getSelectedIndex());
        assertEquals(null, getSelectionModel().getSelectedItem());
    }
    
    @Test
    public void testSeparatorNotSelected() {
        int index = 2;
        items.set(index, new Separator());
        getSelectionModel().select(index);
        assertEquals("selecting index with unselectable item must not change selected index",
                -1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testSeparatorSelectNext() {
        int index = 2;
        items.set(index, new Separator());
        getSelectionModel().select(index - 1);
        getSelectionModel().selectNext();
        assertEquals("selecting next must move over separator",
                index + 1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testSeparatorSelectPrevious() {
        int index = 2;
        items.set(index, new Separator());
        getSelectionModel().select(index + 1);
        getSelectionModel().selectPrevious();
        assertEquals("selecting next must move over separator",
                index - 1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testSeparatorSelectFirst() {
        int index = 0;
        items.set(index, new Separator());
        getSelectionModel().selectFirst();
        assertEquals("selecting first must move over separator",
                index + 1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testSeparatorSelectLast() {
        int index = items.size() - 1;
        items.set(index, new Separator());
        getSelectionModel().selectLast();
        assertEquals("selecting first must move over separator",
                index - 1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    public void testSeparatorInPopup() {
        initSkin();
        int index = 2;
        items.set(index, new Separator());
        MenuItem menuItem = getPopup().getItems().get(index);
        assertTrue("expected separatorMenuItem but was " + menuItem.getClass(),
                menuItem instanceof SeparatorMenuItem);
    }

    
    
// --------- internals testing: popup/Menu/items

    /**
     * https://javafx-jira.kenai.com/browse/RT-38394
     * <p>
     * Choicebox misbehaving on updating elements in items
     * 
     * Note: for now, this is fixed in ChoiceBoxXSkin by brute force!
     */
    @Test
    public void testPopupItemsOnUpdateItem() {
        initSkin();
        ObservableList<Item> items = FXCollections
                .observableArrayList(item -> new Observable[] { item
                        .nameProperty() }); // the extractor
        items.addAll(IntStream.rangeClosed(1, 10)
                .mapToObj(i -> new Item("Item " + i))
                .collect(Collectors.toList()));
        getChoiceView().setItems(items);
        getSelectionModel().select(0);
        items.get(0).setName("newName");
        assertEquals(items.size(), getPopup().getItems().size());
    }

    @Test
    public void testPopupItemsOnRemoveItem() {
        initSkin();
        ContextMenu popup = getPopup();
        assertEquals("size same as items", items.size(), popup.getItems().size());
        items.remove(0);
        assertEquals("size same as items", items.size(), popup.getItems().size());
    }
    
    @Test
    public void testPopupItemsOnAddItem() {
        initSkin();
        ContextMenu popup = getPopup();
        assertEquals("size same as items", items.size(), popup.getItems().size());
        items.add(0, "added");
        assertEquals("size same as items", items.size(), popup.getItems().size());
    }
    
    @Test
    public void testPopupItemsOnSetItem() {
        initSkin();
        ContextMenu popup = getPopup();
        assertEquals("size same as items", items.size(), popup.getItems().size());
        items.set(0, items.get(0) + "changed");
        assertEquals("size same as items", items.size(), popup.getItems().size());
    }
    
    @Test
    public void testPopupItemsOnSetItemAtSelected() {
        initSkin();
        ContextMenu popup = getPopup();
        getSelectionModel().select(0);
        assertEquals("size same as items", items.size(), popup.getItems().size());
        items.set(0, items.get(0) + "changed");
        assertEquals("size same as items", items.size(), popup.getItems().size());
    }
    
    /**
     * @return the context menu controlled by skin
     */
    protected ContextMenu getPopup() {
        Object skin = getView().getSkin();
        Class clazz = skin.getClass();
        try {
            Field field = clazz.getDeclaredField("popup");
            field.setAccessible(true);
            return (ContextMenu) field.get(skin);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Simple bean to test update notification
     */
    public static class Item {
        public final StringProperty name = new SimpleStringProperty();

        public StringProperty nameProperty() {
            return name;
        }

        public final String getName() {
            return nameProperty().get();
        }

        public final void setName(String name) {
            nameProperty().set(name);
        }

        public Item(String name) {
            setName(name);
        }

        @Override
        public String toString() {
            return getName();
        }
    }

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
        initSkin();
        Object value = items.get(3);
        getChoiceView().setValue(value);
        assertEquals(value, getSelectionModel().getSelectedItem());
        assertEquals(value, getLabel().getText());
    }
    
    /**
     * Here we test the selectedIndex and selectedItem - logic seems inconsistent
     * for contained items
     * 
     * <li> on clear, selectedItem is nulled (expected)
     * <li> on remove, it is kept (bug)
     * <p>
     * On remove, the internal selectedItem is intended to be removed (see code comment
     * in itemsContentListener, the while block) - but implementation of 
     * SingleSelectionModel.clearSelection prevents the clear of an uncontained
     * selectedItem (which it is after removal).
     *
     * @see #testClearItemsResetsSelection()
     * @See #testSelectedOnRemoveItemAtSelectedFocused()
     */
    @Test
    public void testRemoveSelectedItemIfSelectedItemContained() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        Object item = items.get(index);
        items.remove(index);
        assertEquals(-1, getSelectionModel().getSelectedIndex());
        assertEquals(null, getSelectionModel().getSelectedItem());
        assertEquals(getChoiceView().getValue(), getSelectionModel().getSelectedItem());
        assertEquals("", getLabel().getText());
    }
    
    /**
     * can't really test: which item to remove? All?
     * 
     * @see #testClearItemsResetsSelection()
     */
    @Test
    public void testRemoveSelectedItemIfSelectedItemUncontained() {
        initSkin();
        String uncontained = "uncontained";
        getSelectionModel().select(uncontained);
        items.clear();
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals("choicebox must show value", uncontained, getLabel().getText());
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
        initSkin();
        int index = items.size() - 5;
        getSelectionModel().select(index);
        ObservableList subList = FXCollections.observableList(items.subList(index, items.size() - 1));
        getChoiceView().setItems(subList);
        assertEquals("selectedItem cleared if contained before setting", 
                null, getSelectionModel().getSelectedItem());
        assertEquals("", getLabel().getText());
    }
    
    /**
     * Implementation of itemsProperty in choiceBox vs. itemsChangeListener in 
     * model are inconclusive:
     * - former clears selection except if uncontained selectedItem
     * - latter clears only on empty
     */
    @Test
    public void testSetItemsIfSelectedItemUncontained() {
        initSkin();
        int index = items.size() - 5;
        getSelectionModel().select("uncontained");
        Object selectedItem = getSelectionModel().getSelectedItem();
        ObservableList subList = FXCollections.observableList(items.subList(index, items.size() - 1));
        getChoiceView().setItems(subList);
        assertEquals("selectedItem unchanged if not in new list", selectedItem, getSelectionModel().getSelectedItem());
        assertEquals("choicebox must show value", selectedItem, getLabel().getText());
    }
    
    /**
     * Regression testing: https://javafx-jira.kenai.com/browse/RT-29433
     */
    @Test
    public void testSetEmptyItemsResetsSelectedItemUncontained() {
        initSkin();
        String uncontained = "uncontained";
        getSelectionModel().select(uncontained);
        getChoiceView().setItems(FXCollections.emptyObservableList());
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals(uncontained, getChoiceView().getValue());
        assertEquals("label must show value", uncontained, getLabel().getText());
    }
    
    /**
     * Regression testing: https://javafx-jira.kenai.com/browse/RT-29433 .
     * 
     * Here we test the effect of clearing the selection.
     * 
     * PENDING JW: Logic somehwat inconsistent with removing? Or are we again on 
     * contained vs uncontained selectedItem?
     * 
     * Conclusion to Pending above
     * - it's not unconsistent in intention, just appears to by not showing the uncontained
     * 
     */
    @Test
    public void testClearItemsNotResetsSelectedItemUncontained() {
        initSkin();
        String uncontained = "uncontained";
        getSelectionModel().select(uncontained);
        getChoiceView().getItems().clear();
        assertEquals(uncontained, getSelectionModel().getSelectedItem());
        assertEquals(uncontained, getChoiceView().getValue());
        assertEquals("label must show value", uncontained, getLabel().getText());
    }
    
    
    /**
     * Regression testing: https://javafx-jira.kenai.com/browse/RT-29433
     */
    @Test
    public void testSetEmptyItemsResetsSelection() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        getChoiceView().setItems(FXCollections.emptyObservableList());
        assertEquals(null, getSelectionModel().getSelectedItem());
        assertEquals(null, getChoiceView().getValue());
        assertEquals("", getLabel().getText());
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
        initSkin();
        int index = 2;
        getSelectionModel().select(items.get(index));
        getChoiceView().getItems().clear();
        assertEquals(null, getSelectionModel().getSelectedItem());
        assertEquals(null, getChoiceView().getValue());
        assertEquals("", getLabel().getText());
    }
    
    /**
     * Regression testing: https://javafx-jira.kenai.com/browse/RT-29433
     * 
     * Here we test the effect of clearing the selection.
     * 
     * @see #testRemoveSelectedItemIfSelectedItemContained()
     * 
     */
    @Test
    public void testClearItemsResetsSelection() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        getChoiceView().getItems().clear();
        assertEquals(null, getSelectionModel().getSelectedItem());
        assertEquals(null, getChoiceView().getValue());
        assertEquals("", getLabel().getText());
    }
    
    /**
     * 
     * Here we test the synch of selectedItem/value, irrespective
     * wheter or not the actual value is correct.
     * 
     * @see #testSetItemAtSelectedIndexEffectOnSelectedIndex()
     */
    @Test
    public void testSetItemAtSelectedIndexEffectSynchValueSelectedItem() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        Object selected = getSelectionModel().getSelectedItem();
        Object modified = selected + "dummy";
        getChoiceView().getItems().set(index, modified);
        assertEquals(getSelectionModel().getSelectedItem(), getChoiceView().getValue());
    }
    
    
    /**
     * Test that empty value is shown with converter.
     * Here we set the converter after creating skin.
     */
    @Test
    public void testValueIsShownUsingConverterAfterSkin() {
        StringConverter converter = getConverter();
        initSkin();
        getSelectionModel().select(items.get(3));
        getChoiceView().setConverter(converter);
        assertEquals(converter.toString(items.get(3)), getLabel().getText());
    }
    
    /**
     * Test that empty value is shown with converter.
     * Here we set the converter after creating skin.
     */
    @Test
    public void testEmptyValueIsShownUsingConverterAfterSkin() {
        StringConverter converter = getConverter();
        initSkin();
        getChoiceView().setConverter(converter);
        assertEquals("sanity: guarantee null value", null, getSelectionModel().getSelectedItem());
        assertEquals(converter.toString(null), getLabel().getText());
    }
    
    /**
     * Test that empty value is shown with converter.
     * Here we set the converter before creating skin.
     */
    @Test
    public void testEmptyValueIsShownUsingConverter() {
        StringConverter converter = getConverter();
        getChoiceView().setConverter(converter);
        initSkin();
        assertEquals("sanity: guarantee null value", null, getSelectionModel().getSelectedItem());
        assertEquals(converter.toString(null), getLabel().getText());
    }
    
    @Test
    public void testValueShownWithoutSelectionModel() {
        initSkin();
        getChoiceView().setSelectionModel(null);
        Object uncontained = "uncontained";
        getChoiceView().setValue(uncontained);
        assertEquals(uncontained, getChoiceView().getValue());
    }
    /**
     * Returns a converter that:
     * - converts null/empty to a fixed value
     * - converts objects by toString + x
     * @return
     */
    protected StringConverter getConverter() {
        StringConverter converter = new StringConverter() {

            @Override
            public String toString(Object object) {
                return object != null ? object.toString() + "X" : "None";
            }

            @Override
            public Object fromString(String string) {
                throw new UnsupportedOperationException("converter is unidirectional");
            }};
        return converter;
        
    }
    /**
     * Issue: uncontained selectedItem allowed but not shown in choicebox
     * Beware: bowel testing of skin!
     * Here we select uncontained after creating skin
     */
    @Test
    public void testUncontainedSelectedItemShownInitialEmptySelection() {
        initSkin();
        String uncontained = "here we go with something";
        getSelectionModel().select(uncontained);
        Label label = getLabel();
        assertEquals("choice must show uncontained item", uncontained, label.getText());
    }
    
    /**
     * Issue: uncontained selectedItem allowed but not shown in choicebox
     * Beware: bowel testing of skin!
     * Here we select a contained item then select uncontained, both after creating skin
     */
    @Test
    public void testUncontainedSelectedItemShownInitialNotEmptySelection() {
        initSkin();
        getSelectionModel().select(items.get(3));
        String uncontained = "here we go with something";
        getSelectionModel().select(uncontained);
        Label label = getLabel();
        assertEquals("choice must show uncontained item", uncontained, label.getText());
    }
    
    /**
     * Issue: uncontained selectedItem allowed but not shown in choicebox
     * Beware: bowel testing of skin!
     * Here we select the uncontained before creating the skin
     */
    @Test
    public void testUncontainedSelectedItemShownInitialUncontainedSelection() {
        String uncontained = "here we go with something";
        getSelectionModel().select(uncontained);
        initSkin();
        Label label = getLabel();
        assertEquals("choice must show uncontained item", uncontained, label.getText());
    }
    
    /**
     * Issue: uncontained selectedItem allowed but not shown in choicebox
     * Beware: bowel testing of skin!
     * Here we select a contained item before creating skin
     */
    @Test
    public void testUncontainedSelectedItemShownInitialContainedSelection() {
        String uncontained = "here we go with something";
        getSelectionModel().select(items.get(3));
        initSkin();
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
     * <li> selectedIndex is cleared on modifying the selectedItem
     * <li> selectedItem is old selectedItem
     * <li> value is oldValue (aka: synched to selectedItem) but not showing
     * <p>
     * the latter two are bugs as per rejection of RT-19820, but not
     * seen due to RT-38826
     *  <p>
     * 
     * Note: api doc of ChoiceBox explicitly allows programmatic (vs. user
     * induced) selection of uncontained elements!
     * <p>
     * Conclusion: (see issues-2014) behaviour is different for internal vs
     * external selectedItem 
     * <p>
     * Changed expectation here and in related test on modifying a internal selectedItem: 
     * selected index and selectedItem must be cleared
     * <p>
     * skin or not doesn't make a difference!
     * <p>
     * 
     * @see #testUncontainedSelectedItemShownInitialNotEmptySelection()
     */
    @Test
    public void testSetItemAtSelectedIndexEffectOnSelectedIndex() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        Object selected = getSelectionModel().getSelectedItem();
        Object modified = selected + "dummy";
        getChoiceView().getItems().set(index, modified);
        assertEquals("selected index must be cleared", -1, getSelectionModel().getSelectedIndex());
    }
    
    /**
     * @see #testSetItemAtSelectedIndexEffectOnSelectedIndex()
     */
    @Test
    public void testSetItemAtSelectedIndexEffectOnValue() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        Object selected = getSelectionModel().getSelectedItem();
        Object modified = selected + "dummy";
        getChoiceView().getItems().set(index, modified);
        assertEquals("selectedItem must cleared", null, getChoiceView().getValue());
    }
    
    /**
     * @see #testSetItemAtSelectedIndexEffectOnSelectedIndex()
     * 
     */
    @Test
    public void testSetItemAtSelectedIndexEffectOnSelectedItem() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        Object selected = getSelectionModel().getSelectedItem();
        Object modified = selected + "dummy";
        getChoiceView().getItems().set(index, modified);
        assertEquals("selectedItem must cleared", null, getSelectionModel().getSelectedItem());
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

    protected void initSkin() {
        loader = new StageLoader(getView());
        // doesn't make a difference: still spurious RejectedExecutionException ..
        // triggered by task PaintRenderJob
//        PlatformImpl.runAndWait(() -> loader = new StageLoader(getView()));
    }
    
    /**
     * RT-38724
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
        assertEquals("box value must be same as selected item", items.get(index), 
                getChoiceView().getValue());
    }

    /**
     * RT-38724
     * ChoiceBox.setSelectionModel must update box' value
     * To test we set a model which has a selection - doesn't update choiceBox value.
     * Here we force the skin, just in case
     */
    @Test
    public void testSetSelectionModelWithSelectionWithSkin() {
        initSkin();
        SingleSelectionModel model = createSimpleSelectionModel();
        int index = 2;
        model.select(index);
        assertEquals("sanity: model is selecting index and item", items.get(index), 
                model.getSelectedItem());
        getChoiceView().setSelectionModel(model);
        assertEquals("box value must be same as selected item", items.get(index), 
                getChoiceView().getValue());
        assertEquals("label must be updated", items.get(index), getLabel().getText());
    }

    /**
     * ChoiceBoxBehavior has some weird code around selection/model/changes.
     * trying to find macroscopic behaviour failure.
     * 
     * Here: sanity testing, selecting an item in the model updates box value
     */
    @Test
    public void testSetSelectionModelSelectAfterSetting() {
        initSkin();
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

        void setConverter(StringConverter<T> converter);

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
