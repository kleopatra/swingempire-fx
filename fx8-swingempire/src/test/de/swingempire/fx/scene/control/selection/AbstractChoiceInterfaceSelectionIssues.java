/*
 * Created on 16.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.lang.reflect.Field;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import static org.junit.Assert.*;

import de.swingempire.fx.scene.control.comboboxx.ComboSelectionRT_19433;
import de.swingempire.fx.scene.control.comboboxx.ComboboxSelectionCopyRT_26079;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreDynamicItemsInPopup;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreExternalError;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreSeparatorSelect;
import de.swingempire.fx.scene.control.selection.SelectionIgnores.IgnoreSetSelectionModel;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SkinBase;
import javafx.util.StringConverter;

/**
 * Extracted common ancestor for ChoiceBox/ChoiceBoxX testing.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public abstract class AbstractChoiceInterfaceSelectionIssues<V extends Control, W extends SelectionModel> 
    extends SelectionIssues<V, W>{

    
    
    // ---------- test combo issues

    /**
     * Base testing of an issue seen in ComboBoxTableCell:
     * have an edit commit listener and remove the selected item
     * (aka: new value)
     */
    @Test
    public void testRemoveSelectedIndexThatIsLast() {
        items.remove(1, items.size());
        assertEquals("", 1, items.size());
        // select uncontained
        getSelectionModel().select("dummy");
        ChangeListener l = (source, ov, nv) -> {
            LOG.info("items: " + items + ov + nv);
//            assertSame("removing last item", items.get(0), nv);
            items.remove(nv);
        };
        getSelectionModel().selectedItemProperty().addListener(l);
        getSelectionModel().select(0);
    }
    
    /**
     * RT_39908: replacing item before selected shifts 
     * selection towards the end. 
     * 
     * marked as fixed, doing regression testing here for custom models
     * 
     * PENDING JW: 
     * - the fix (sometime around 4. Feb. 2015) ignores all replace
     *   changes: check what happens on replacing all items
     * - selection is shifted on each subChange, check what happens
     *   on disjoint changes: must not fire more than a single event,
     *   looks like it might  
     */
    @Test
    public void testSelectedIndexOnReplacedAbove_rt_39908() {
//        initSkin();
        getSelectionModel().clearAndSelect(1);
        assertEquals(items.get(1), getSelectionModel().getSelectedItem());
        assertEquals(1, getSelectionModel().getSelectedIndex());
        
        items.set(0, "a");
        assertEquals("selectedIndex unchanged on replaced above", 1, 
                getSelectionModel().getSelectedIndex());
    }
    @Test
    public void testSelectedItemOnReplacedAbove_rt_39908() {
//        initSkin();
        getSelectionModel().clearAndSelect(1);
        assertEquals(items.get(1), getSelectionModel().getSelectedItem());
        assertEquals(1, getSelectionModel().getSelectedIndex());

        items.set(0, "a");
        assertEquals("selectedItem unchanged on replaced above", 
                items.get(1), getSelectionModel().getSelectedItem());
    }
    /**
     * Trying to catch RT_19227: support duplicate items.
     * Here we select in combo and test listView's sync
     * 
     * None of the two tests fail, despite RT_38927: list selected == 0 
     * on opening popup again.
     * 
     */
    @Test
    public void testSelectedIndexWithDuplicateItems() {
        if (!hasDependendSelectionModel()) return;
        initSkin();
        int index = 2;
        // duplicate
        items.add(index, items.get(index));
        int second = index +1;
        getSelectionModel().select(second);
        assertEquals("sanity: ", second, getSelectionModel().getSelectedIndex());
        assertEquals("dependent selectedIndex must be same", second, getDependentSelectionModel().getSelectedIndex());
    }
    
    /**
     * Here we select in dependent and test selected in combo.
     */
    @Test
    public void testSelectedIndexWithDuplicateItemsInvers() {
        if (!hasDependendSelectionModel()) return;
        initSkin();
        int index = 2;
        // duplicate
        items.add(index, items.get(index));
        int second = index +1;
        getDependentSelectionModel().select(second);
        assertEquals("sanity: ", second, getDependentSelectionModel().getSelectedIndex());
        assertEquals("dependent selectedIndex must be same", second, getSelectionModel().getSelectedIndex());
    }
    
    protected boolean hasDependendSelectionModel() {
        return false;
    }

    protected SelectionModel getDependentSelectionModel() {
        return null;
    }
    
    @Test
    public void testSetValueUpdatesSelectedItem() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index );
        Object uncontained = "uncontained";
        getChoiceView().setValue(uncontained);
        assertEquals("selectedItem must be synced to value", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("display must be synced to value", uncontained, getDisplayText());
    }
    
    /**
     * Test against SO: 
     * 
     * sequence: 
     * - start unselected
     * - setValue to uncontained
     * - select (via select(index))
     * - setValue to uncontained again
     */
    @Test
    public void testSetValueUpdatesSelectedItemAgain() {
        initSkin();
        int index = 2;
        Object uncontained = "uncontained";
        getChoiceView().setValue(uncontained);
        assertEquals("selectedItem must be synced to value", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("display must be synced to value", uncontained, getDisplayText());
        getSelectionModel().select(index);
        assertEquals(items.get(index), getSelectionModel().getSelectedItem());
        getChoiceView().setValue(uncontained);
        assertEquals("selectedItem must be synced to value", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("display must be synced to value", uncontained, getDisplayText());
    }
    
    /**
     * Test against SO: 
     * 
     * sequence: 
     * - start unselected
     * - setValue to uncontained
     * - select (via select(item))
     * - setValue to uncontained again
     */
    @Test
    public void testSetValueUpdatesSelectedItemAgain2() {
        initSkin();
        int index = 2;
        Object uncontained = "uncontained";
        getChoiceView().setValue(uncontained);
        assertEquals("selectedItem must be synced to value", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("display must be synced to value", uncontained, getDisplayText());
        Object item = items.get(index);
        getSelectionModel().select(item);
        assertEquals(item, getSelectionModel().getSelectedItem());
        getChoiceView().setValue(uncontained);
        assertEquals("selectedItem must be synced to value", uncontained, getSelectionModel().getSelectedItem());
        assertEquals("display must be synced to value", uncontained, getDisplayText());
    }
    
    /**
     * Regression guard: combo value must not be changed on setAll 
     * https://javafx-jira.kenai.com/browse/RT-20945
     */
    @Test
    @ConditionalIgnore (condition = IgnoreDynamicItemsInPopup.class)
    public void testUpdateOnShowingSetAllItems() {
        initSkin();
        getChoiceView().showingProperty().addListener((o, old, value) -> {
            if (!value) return;
            Object selected = getSelectionModel().getSelectedItem();
            getChoiceView().getItems().setAll("replaced");
            assertEquals(-1, getSelectionModel().getSelectedIndex());
            assertEquals(selected, getChoiceView().getValue());
            fail("just to see it fail");
        });
        int index = 2;
        getSelectionModel().select(index);
        getChoiceView().show();
    }
    
    /**
     * Regression guard: combo value must not be changed on setAll 
     * https://javafx-jira.kenai.com/browse/RT-20945
     */
    @Test
    @ConditionalIgnore (condition = IgnoreDynamicItemsInPopup.class)
    public void testUpdateOnShowingSetItems() {
        initSkin();
        getChoiceView().showingProperty().addListener((o, old, value) -> {
            if (!value) return;
            Object selected = getSelectionModel().getSelectedItem();
            getChoiceView().setItems(FXCollections.observableArrayList( "replaced"));
            assertEquals(-1, getSelectionModel().getSelectedIndex());
            assertEquals(selected, getChoiceView().getValue());
//            fail("just to see it fail");
        });
        int index = 2;
        getSelectionModel().select(index);
        getChoiceView().show();
    }
    
    /**
     * https://javafx-jira.kenai.com/browse/RT-19433
     * SelectedIndex not updated when committin an edit
     * 
     * Example of broken class invariant:
     * 
     * if (selectedIndex >= 0) {
     *    assertEquals(items.get(selectedIndex), selectedItem);
     * }
     * 
     * @see ComboSelectionRT_19433
     */
    @Test
    public void testSelectedIndexOnCommitEdit() {
        // skin or not doesn't make a differnce
//        initSkin();
        // editable or not doesn't make a difference
//        getChoiceView().setEditable(true);
        int index = 2;
        getSelectionModel().select(index);
        Object selected = getSelectionModel().getSelectedItem();
        Object uncontained = "uncontained";
        getChoiceView().setValue(uncontained);
        assertEquals("value must be updated", uncontained, getChoiceView().getValue());
        assertEquals("selectedItem must be updated", uncontained, getSelectionModel().getSelectedItem());
        if (!items.contains(uncontained))
            assertEquals("selectedIndex must be cleared for uncontained", -1, getSelectionModel().getSelectedIndex());
    }
    
    /**
     * https://javafx-jira.kenai.com/browse/RT-32919
     * setConverter must not clear selection
     * 
     * hmm .. can't reproduce here: was reported against
     * 2.2, looks fixed in 8u20
     */
    @Test
    public void testConverterEffectOnSelection() {
        initSkin();
        int index = 2;
        getSelectionModel().select(index);
        Object selected = getSelectionModel().getSelectedItem();
        getChoiceView().setConverter(getConverter());
        assertEquals(index, getSelectionModel().getSelectedIndex());
        assertEquals(selected, getSelectionModel().getSelectedItem());
        assertEquals(selected, getChoiceView().getValue());
    }
    
//------------ start tests around RT-26079    
    /**
     * In AnchoredSelectionModel testing, we decided to  
     * live with the regression, since it will be solved eventually
     * in core.
     * <p>
     * Note, RT-26079 it's not related (at least not directly: the example
     * uses setAll(....) which is special-cased
     * <p>
     * Note: this might not be exactly 15793: behaviour of remove is different anyway
     * (though buggy) - though passes with core combo, fails with comboXX. 
     * <p>
     * Good 
     * enough, though basically we have to test whether the wiring to 
     * listChangeEvents is updated on 
     * 
     * <code><pre>
     * newList.equals(oldList) && newList != oldList
     * </pre></code>
     * 
     * 
     * @see ComboboxSelectionCopyRT_26079
     * @see AbstractListMultipleSelectionIssues#testRT15793()
     */
    @Test
    public void testSelectFirstRT_15793RemoveItem() {
        view = createView(FXCollections.observableArrayList());
//        getChoiceView().itemsProperty().addListener(o -> {LOG.info("notified");});
//        getChoiceView().itemsProperty().addListener((o, old, value) -> {LOG.info("notified");});
        ObservableList<String> emptyList = FXCollections.observableArrayList();
        // listView is instantiated with an empty list, so following assumption 
        // is incorrect
//        assertEquals(null, view.getItems());
        assertSame(view, getChoiceView());
        getChoiceView().setItems(emptyList);
        emptyList.add("something");
        getSelectionModel().selectFirst();
        assertEquals(0, getSelectionModel().getSelectedIndex());
        emptyList.remove(0);
        assertEquals(-1, getSelectionModel().getSelectedIndex());
    }
    
    /**
     * Here we test with replace.<p>
     * 
     * PENDING JW: need to add at least two items to the replaced list,
     * otherwise the set(0, something) can't be distinguished from 
     * setAll(something). Behaviour is (should be?) different
     * - first is replace ==> update selectedItem to new value at selectedIndex
     * - second is allChanged ==> clear selection if old selectedItem had been in list  
     */
    @Test
    public void testSelectFirstRT_15793ReplaceItem() {
        view = createView(FXCollections.observableArrayList());
//        getChoiceView().itemsProperty().addListener(o -> {LOG.info("notified");});
//        getChoiceView().itemsProperty().addListener((o, old, value) -> {LOG.info("notified");});
        ObservableList<String> emptyList = FXCollections.observableArrayList();
        // listView is instantiated with an empty list, so following assumption 
        // is incorrect
//        assertEquals(null, view.getItems());
        assertSame(view, getChoiceView());
        getChoiceView().setItems(emptyList);
        String first = "something";
        emptyList.addAll(first, "something else");
        getSelectionModel().selectFirst();
        assertEquals(0, getSelectionModel().getSelectedIndex());
        String other = "other";
        emptyList.set(0, other);
        assertEquals(0, getSelectionModel().getSelectedIndex());
        assertEquals(other, getSelectionModel().getSelectedItem());
    }

    /**
     * PENDING JW: give up for now, don't understand why
     * behvaiour different with/out skin
     * 
     * Still don't understand the reason - but the tests for ComboBoxX pass for 
     * 8u40b7 (but don't for core combo)
     * 
     * @see ComboboxSelectionCopyRT_26079
     */
    @Test 
    @ConditionalIgnore(condition = SelectionIgnores.IgnoreRT26079.class)
    public void testSelectFirstMemoryEqualsListWithSkin() {
        // prepare needed - to reproduce same behaviour as with builder
        // select before setting items
        getSelectionModel().selectFirst();
        // replace all items and clear selection before skin
        getChoiceView().getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E3"));
        getSelectionModel().clearSelection();
        assertEquals("sanity initial: selectedIndex must be cleared", -1, getSelectionModel().getSelectedIndex());
        assertEquals("sanity initial: selectedItem must be cleared", null, getSelectionModel().getSelectedItem());
        assertEquals("sanity initial: value must be cleared", null, getChoiceView().getValue());
        // force skin
        initSkin();
        // re-select
        getSelectionModel().selectFirst();
        // re-set a list that's equal but not the same
        // seems - not the issue here: the issue is special-casing setAll
//        getChoiceView().setItems(FXCollections.observableArrayList("E1", "E2", "E3"));
        getChoiceView().getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E3"));
        // clearing again
        getSelectionModel().clearSelection();
        assertEquals("selectedIndex must be cleared", -1, getSelectionModel().getSelectedIndex());
        assertEquals("selectedItem must be cleared", null, getSelectionModel().getSelectedItem());
        // here we fail
        assertEquals("value must be cleared", null, getChoiceView().getValue());
//        assertEquals("", getDisplayText());
    }
    
    @Test
    @ConditionalIgnore(condition = SelectionIgnores.IgnoreRT26079.class)
    public void testSelectFirstMemorySimilarListWithSkin() {
        // prepare needed - to reproduce same behaviour as with builder
        // select before setting items
        getSelectionModel().selectFirst();
        // replace all items and clear selection before skin
        getChoiceView().getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E3"));
        getSelectionModel().clearSelection();
        assertEquals("sanity initial: selectedIndex must be cleared", -1, getSelectionModel().getSelectedIndex());
        assertEquals("sanity initial: selectedItem must be cleared", null, getSelectionModel().getSelectedItem());
        assertEquals("sanity initial: value must be cleared", null, getChoiceView().getValue());
        // force skin
        initSkin();
        // re-select
        getSelectionModel().selectFirst();
        // re-set a list that's equal but not the same
        // seems - not the issue here: the issue is special-casing setAll
//        getChoiceView().setItems(FXCollections.observableArrayList("E1", "E2", "E3"));
        getChoiceView().getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E5"));
        // clearing again
        getSelectionModel().clearSelection();
        assertEquals("selectedIndex must be cleared", -1, getSelectionModel().getSelectedIndex());
        assertEquals("selectedItem must be cleared", null, getSelectionModel().getSelectedItem());
        // here we fail
        assertEquals("value must be cleared", null, getChoiceView().getValue());
//        assertEquals("", getDisplayText());
    }
    
    @Test
    @ConditionalIgnore(condition = SelectionIgnores.IgnoreRT26079.class)
    public void testSelectFirstMemorySimilarLongerListWithSkin() {
        // prepare needed - to reproduce same behaviour as with builder
        // select before setting items
        getSelectionModel().selectFirst();
        // replace all items and clear selection before skin
        getChoiceView().getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E3"));
        getSelectionModel().clearSelection();
        assertEquals("sanity initial: selectedIndex must be cleared", -1, getSelectionModel().getSelectedIndex());
        assertEquals("sanity initial: selectedItem must be cleared", null, getSelectionModel().getSelectedItem());
        assertEquals("sanity initial: value must be cleared", null, getChoiceView().getValue());
        // force skin
        initSkin();
        // re-select
        getSelectionModel().selectFirst();
        // re-set a list that's equal but not the same
        // seems - not the issue here: the issue is special-casing setAll
//        getChoiceView().setItems(FXCollections.observableArrayList("E1", "E2", "E3"));
        getChoiceView().getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E3", "E5"));
        // clearing again
        getSelectionModel().clearSelection();
        assertEquals("selectedIndex must be cleared", -1, getSelectionModel().getSelectedIndex());
        assertEquals("selectedItem must be cleared", null, getSelectionModel().getSelectedItem());
        // here we fail
        assertEquals("value must be cleared", null, getChoiceView().getValue());
//        assertEquals("", getDisplayText());
    }
//------------ end tests around RT-26079    
    
//---------- test unselectable (Separator)
    @Test
    @ConditionalIgnore(condition = IgnoreSeparatorSelect.class)
    public void testSeparatorNotSelectedItem() {
        if (!supportsSeparators()) return;
        getSelectionModel().select(new Separator());
        assertEquals("selecting index with unselectable item must not change selected index",
                -1, getSelectionModel().getSelectedIndex());
        assertEquals(null, getSelectionModel().getSelectedItem());
    }
    
    @Test
    @ConditionalIgnore(condition = IgnoreSeparatorSelect.class)
    public void testSeparatorNotSelected() {
        if (!supportsSeparators()) return;
        int index = 2;
        items.set(index, new Separator());
        getSelectionModel().select(index);
        assertEquals("selecting index with unselectable item must not change selected index",
                -1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    @ConditionalIgnore(condition = IgnoreSeparatorSelect.class)
    public void testSeparatorSelectNext() {
        if (!supportsSeparators()) return;
        
        int index = 2;
        items.set(index, new Separator());
        getSelectionModel().select(index - 1);
        getSelectionModel().selectNext();
        assertEquals("selecting next must move over separator",
                index + 1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    @ConditionalIgnore(condition = IgnoreSeparatorSelect.class)
    public void testSeparatorSelectPrevious() {
        if (!supportsSeparators()) return;
        int index = 2;
        items.set(index, new Separator());
        getSelectionModel().select(index + 1);
        getSelectionModel().selectPrevious();
        assertEquals("selecting next must move over separator",
                index - 1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    @ConditionalIgnore(condition = IgnoreSeparatorSelect.class)
    public void testSeparatorSelectFirst() {
        if (!supportsSeparators()) return;
        int index = 0;
        items.set(index, new Separator());
        getSelectionModel().selectFirst();
        assertEquals("selecting first must move over separator",
                index + 1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    @ConditionalIgnore(condition = IgnoreSeparatorSelect.class)
    public void testSeparatorSelectLast() {
        if (!supportsSeparators()) return;
        int index = items.size() - 1;
        items.set(index, new Separator());
        getSelectionModel().selectLast();
        assertEquals("selecting first must move over separator",
                index - 1, getSelectionModel().getSelectedIndex());
    }
    
    @Test
    @ConditionalIgnore(condition = IgnoreSeparatorSelect.class)
    public void testSeparatorInPopup() {
        if (!supportsSeparators()) return;
        if (!hasPopup()) return;
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
        if (!hasPopup()) return;
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
        if (!hasPopup()) return;
        initSkin();
        ContextMenu popup = getPopup();
        assertEquals("size same as items", items.size(), popup.getItems().size());
        items.remove(0);
        assertEquals("size same as items", items.size(), popup.getItems().size());
    }
    
    @Test
    public void testPopupItemsOnAddItem() {
        if (!hasPopup()) return;
        initSkin();
        ContextMenu popup = getPopup();
        assertEquals("size same as items", items.size(), popup.getItems().size());
        items.add(0, "added");
        assertEquals("size same as items", items.size(), popup.getItems().size());
    }
    
    @Test
    public void testPopupItemsOnSetItem() {
        if (!hasPopup()) return;
        initSkin();
        ContextMenu popup = getPopup();
        assertEquals("size same as items", items.size(), popup.getItems().size());
        items.set(0, items.get(0) + "changed");
        assertEquals("size same as items", items.size(), popup.getItems().size());
    }
    
    @Test
    public void testPopupItemsOnSetItemAtSelected() {
        if (!hasPopup()) return;
        initSkin();
        ContextMenu popup = getPopup();
        getSelectionModel().select(0);
        assertEquals("size same as items", items.size(), popup.getItems().size());
        items.set(0, items.get(0) + "changed");
        assertEquals("size same as items", items.size(), popup.getItems().size());
    }
    
    /**
     * selecting 
     */
    @Test
    public void testPopupSelectedOnSelectUncontainedItem() {
        if (!hasPopup()) return;
        initSkin();
        ContextMenu popup = getPopup();
        int index = 0;
        getSelectionModel().select(index);
        RadioMenuItem radio = (RadioMenuItem) getPopup().getItems().get(index);
        assertEquals("sanity: radio must be selected", true, radio.isSelected());
        getSelectionModel().select("uncontained");
        assertEquals("popup must be unselected", false, radio.isSelected());
        
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
        assertEquals(value, getDisplayText());
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
     * see for combo: https://javafx-jira.kenai.com/browse/RT-38886
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
        assertEquals(getChoiceView().getValue(), getSelectionModel().getSelectedItem());
        assertEquals(null, getSelectionModel().getSelectedItem());
        assertEquals(-1, getSelectionModel().getSelectedIndex());
        assertTrue("displayText must be empty, but was: " + getDisplayText() , isEmptyDisplayText());
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
        assertEquals("choicebox must show value", uncontained, getDisplayText());
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
     *     
     * PENDING JW: this test passes for ComboBoxX, but behaviour in view example
     * is different. 
     * 
     * - passes if subList(index, items.size() - 1); // accidentally last not included
     * - fails if subList(index, items.size())
     * 
     * what's the difference? 
     * Bug: incorrect handling of replace 
     * - with the former the new selectedItem is accidentally null because newIndex > length? 
     * 
     * @see ComboboxSelectionCopyRT_26079
     */
    @Test
    public void testSetItemsIfSelectedItemContained() {
        initSkin();
        int index = items.size() - 5;
        getSelectionModel().select(index);
        Object oldSelected = getSelectionModel().getSelectedItem();
        ObservableList subList = FXCollections.observableList(items.subList(index, items.size()));
        assertTrue("sanity: oldSelected contained in sublist", subList.contains(oldSelected));
        getChoiceView().setItems(subList);
        assertTrue("sanity: oldSelected contained in choice items", getChoiceView().getItems().contains(oldSelected));
        if (getSelectionModel().getSelectedItem() != null) {
            assertEquals("if not null, selectedItem must be unchanged", 
                    oldSelected, getSelectionModel().getSelectedItem());
            assertEquals("if not cleared, selectedIndex must be", 0, getSelectionModel().getSelectedIndex());
        }
        assertEquals("selectedItem cleared if contained before setting", 
                null, getSelectionModel().getSelectedItem());
        assertTrue("displayText must be empty, but was: " + getDisplayText() , isEmptyDisplayText());
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
        ObservableList subList = FXCollections.observableList(items.subList(index, items.size()));
        getChoiceView().setItems(subList);
        assertEquals("selectedItem unchanged if not in new list", selectedItem, getSelectionModel().getSelectedItem());
        assertEquals("choicebox must show value", selectedItem, getDisplayText());
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
        assertEquals("label must show value", uncontained, getDisplayText());
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
        assertEquals("label must show value", uncontained, getDisplayText());
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
        assertTrue("displayText must be empty, but was: " + getDisplayText() , isEmptyDisplayText());
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
        assertTrue("displayText must be empty, but was: " + getDisplayText() , isEmptyDisplayText());
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
        assertTrue("displayText must be empty, but was: " + getDisplayText() , isEmptyDisplayText());
    }
    
    /**
     * @return
     */
    protected boolean isEmptyDisplayText() {
        String text = getDisplayText();
        return text == null || text.isEmpty();
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
        assertEquals(converter.toString(items.get(3)), getDisplayText());
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
        assertEquals(converter.toString(null), getDisplayText());
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
        assertEquals(converter.toString(null), getDisplayText());
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
        assertEquals("choice must show uncontained item", uncontained, getDisplayText());
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
        assertEquals("choice must show uncontained item", uncontained, getDisplayText());
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
        assertEquals("choice must show uncontained item", uncontained, getDisplayText());
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
        assertEquals("choice must show uncontained item", uncontained, getDisplayText());
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
     * A valid alternative is to detect the replace/update in the model, keep 
     * selectedIndex as is and update value/selectedItem to the new item.
     * Updated expectaction to conditional.
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
        int newIndex = getSelectionModel().getSelectedIndex();
        if (isClearSelectionOnSetItem()) {
            assertEquals("selected index must be cleared", -1, newIndex);
        } else {
            assertEquals("selected index must be unchanged", index, newIndex);
        }
        
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
        if (isClearSelectionOnSetItem()) {
            assertEquals("value must cleared", null, getChoiceView().getValue());
        } else {
            assertEquals("value must be modified item", modified, getChoiceView().getValue());
        }
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
        if (isClearSelectionOnSetItem()) {
            assertEquals("selectedItem must be cleared", null, getSelectionModel().getSelectedItem());
        } else {
            assertEquals("selectedItem must be modified item", modified, getSelectionModel().getSelectedItem());
        }
    }
    
    /**
     * Overridden to with conditional assert, outcome depends on 
     * isClearSelectionOnSetItem. 
     * Also covered by more specified tests setItemAtSelectedXX
     * which depend on whether or not the selection should be cleared.
     * 
     * @see #testSetItemAtSelectedIndexEffectOnSelectedIndex()
     * @see #testSetItemAtSelectedIndexEffectOnSelectedItem()
     * @see #testSetItemAtSelectedIndexEffectOnValue()
     * @see #isClearSelectionOnSetItem()
     */
    @Override @Test 
    public void testSelectedOnSetItemAtSelectedFocused() {
        int index = 2;
        getSelectionModel().select(index);
        Object selected = items.get(index);
        Object modified = selected + "xx";
        items.set(index, modified);
        if (isClearSelectionOnSetItem()) {
            assertEquals("selectedIndex must be cleared", -1, getSelectionModel().getSelectedIndex());
            assertEquals("selectedItem must be cleared", null, getSelectionModel().getSelectedItem());
        } else {
            assertEquals("selectedIndex must be unchanged", index, getSelectionModel().getSelectedIndex());
            assertEquals("selectedItem must be modified", modified, getSelectionModel().getSelectedItem());
        }
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
     * Sanity testing: 
     * ChoiceBoxSkin is listening to the selectedItem (and updating radio items)
     * That listener is never re-wired to a new model. Doesn't matter, because
     * the radio state is based on the selectedIndex of the current model.
     * 
     * The other way round: radio state is update on being notified by selectedIndex
     * (which is updated with model change) and on showing (which is a bit weird ...)
     * 
     * So most probably no macroscopic effect, just internal confusion. 
     */
    @Test
    public void testSetSelectionModelListeningToOldItemProperty() {
        initSkin();
        W model = createSimpleSelectionModel();
        int index = 2;
        model.select(index);
        
        W old = getSelectionModel();
        setSelectionModel(model);
        assertEquals("here it's sanity, issue fixed: ", items.get(index), getChoiceView().getValue());
        
        old.select(0);
        assertEquals("value not effected by changes of old model ", 
                items.get(index), getChoiceView().getValue());
    }
    
    /**
     * Sanity testing fix of RT-38724:
     * invers: if value is bound, the model mut be update 
     */
    @Test
    @ConditionalIgnore (condition = IgnoreSetSelectionModel.class)
    public void testSetSelectionModelWithSelectionBoundValue() {
        W model = createSimpleSelectionModel();
        int index = 2;
        model.select(index);
        ObjectProperty property = new SimpleObjectProperty(items.get(1));
        getChoiceView().valueProperty().bind(property);
        setSelectionModel(model);
        assertEquals(property.get(), model.getSelectedItem());
    }
    
    /**
     * RT-38724
     * ChoiceBox.setSelectionModel must update box' value
     * To test we set a model which has a selection - doesn't update choiceBox value.
     * Here we test the skinless box
     * 
     * Fixed in core, jdk9
     */
    @Test
    @ConditionalIgnore (condition = IgnoreSetSelectionModel.class)
    public void testSetSelectionModelWithSelectionNoSkin() {
        W model = createSimpleSelectionModel();
        int index = 2;
        model.select(index);
        assertEquals("sanity: model is selecting index and item", items.get(index), 
                model.getSelectedItem());
        /*getChoiceView().*/setSelectionModel(model);
        assertEquals("box value must be same as selected item", items.get(index), 
                getChoiceView().getValue());
    }

    /**
     * RT-38724
     * ChoiceBox.setSelectionModel must update box' value
     * To test we set a model which has a selection - doesn't update choiceBox value.
     * Here we force the skin, just in case
     * 
     * Fixed in core, jdk9
     * PENDING JW: commented the call to displayValue - still not updated!
     */
    @Test 
    @ConditionalIgnore (condition = IgnoreSetSelectionModel.class)
    public void testSetSelectionModelWithSelectionWithSkin() {
        initSkin();
        W model = createSimpleSelectionModel();
        int index = 2;
        model.select(index);
        assertEquals("sanity: model is selecting index and item", items.get(index), 
                model.getSelectedItem());
        setSelectionModel(model);
        assertEquals("box value must be same as selected item", items.get(index), 
                getChoiceView().getValue());
 //       assertEquals("label must be updated", items.get(index), getDisplayText());
    }

    /**
     * ChoiceBoxBehavior has some weird code around selection/model/changes.
     * trying to find macroscopic behaviour failure.
     * 
     * Here: sanity testing, selecting an item in the model updates box value
     */
    @ConditionalIgnore (condition = IgnoreSetSelectionModel.class)
    @Test
    public void testSetSelectionModelSelectAfterSetting() {
        initSkin();
        W model = createSimpleSelectionModel();
        int index = 2;
        setSelectionModel(model);
        model.select(index);
        assertEquals("box value must be same as selected item", items.get(index), getChoiceView().getValue());
    }

//------------------- Null selectionModel

    /**
     * Test that combo can cope with null selectionModel.
     * Fails but the failure doesn't show up as error (on console only)
     * Ignoring for now to not pollute.
     */
    @Test 
    public void testNullSelectionModelValueShown() {
        initSkin();
        setSelectionModel(null);
        Object uncontained = "uncontained";
        getChoiceView().setValue(uncontained);
        assertEquals(uncontained, getChoiceView().getValue());
    }

    /**
     * Logs exception stacktrace that doesn't show up as error (actually not at all)
     * in test log - why not? Thread issue? 
     */
    @Test 
    @ConditionalIgnore (condition = IgnoreExternalError.class)
    public void testNullSelectionModelOnSetValue() {
        setSelectionModel(null);
        getChoiceView().setValue(items.get(3));
        assertEquals(items.get(3), getChoiceView().getValue());
    }
    
    @Test 
    @ConditionalIgnore (condition = IgnoreExternalError.class)
    public void testNullSelectionModelPreviouslySelectedOnSetValue() {
        getSelectionModel().select(2);
        setSelectionModel(null);
        assertEquals(null, getChoiceView().getValue());
        getChoiceView().setValue(items.get(3));
        assertEquals(items.get(3), getChoiceView().getValue());
    }
    
    /**
     * Overfidden to ignore: sorting not explicitly supported
     */
    @Test @Ignore
    @Override
    public void testSelectedIndexAfterSort() {
        super.testSelectedIndexAfterSort();
    }
    
// ------------------ infrastructure
    
    

    @Override
    protected V createView(ObservableList items) {
        return null;
    }

    @Override
    protected abstract W getSelectionModel();

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertSame("sanity: view and choiceView are the same", getView(), getChoiceView());
    }

    /**
     * PENDING JW: here we expect look for a label, that's specific to choiceBox.
     * ComboBox test must override to implement differently.
     * Refactor test hierarchy to abstract away the dependency.
     * @return
     */
    protected String getDisplayText() {
        SkinBase skin = (SkinBase) getView().getSkin();
        Label label = (Label) skin.getChildren().get(0);
        return label.getText();
    }

    /**
     * PENDING JW: refactor test hierarchy
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
     * Returns true if selection is cleared on setItem at selected index.
     * 
     * Depends on type of control: ChoiceBox is implemented to yes, ComboBox
     * is implemented to false (though buggy!)
     * 
     * @return
     */
    protected abstract boolean isClearSelectionOnSetItem();
    /**
     * PENDING JW: refactor test hierarchy
     */
    protected abstract boolean supportsSeparators();

    /**
     * PENDING JW: refactor test hierarchy
     */
    protected abstract boolean hasPopup();
    protected abstract ChoiceControl getChoiceView();

    protected abstract W createSimpleSelectionModel();

    @Override
    protected int getAnchorIndex(int index) {
        return index;
    }

    @Override
    protected FocusModel getFocusModel() {
        return null;
    }
    
    @Override
    protected abstract void setSelectionModel(W model);


    @Override
    protected void resetItems(ObservableList other) {
        getChoiceView().setItems(other);
    }
    /**
     * Simply a tagging interface with methods needed for comparative testing
     * of ChoiceBoxX/ChoiceBox.
     * 
     */
    public static interface ChoiceControl<T, W extends SelectionModel<T>> {
     
        ObjectProperty<ObservableList<T>> itemsProperty();

        void setItems(ObservableList<T> items);

        ObservableList<T> getItems();

        T getValue();

        void setValue(T value);
        
        ObjectProperty<T> valueProperty();

        // removed because of typing issues when testing the adapter
//        W getSelectionModel();
//
//        void setSelectionModel(W model);

        void show();

        ReadOnlyBooleanProperty showingProperty();

        void setConverter(StringConverter<T> converter);

        void setEditable(boolean editable);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AbstractChoiceInterfaceSelectionIssues.class.getName());
}
