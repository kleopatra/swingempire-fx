/*
 * Created on 11.10.2014
 *
 */
package de.swingempire.fx.property;

import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.util.ChangeReport;
import de.swingempire.fx.util.FXUtils;
import de.swingempire.fx.util.InvalidationReport;
import de.swingempire.fx.util.ListChangeReport;

import static de.swingempire.fx.property.BugPropertyAdapters.*;

import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class ObservableListTest {

    @Test
    public void testListPropertyAdapterInitial() {
        ObservableList<String> list = createObservableList(true);
        String listName = "dummy";
        ObjectProperty<ObservableList<String>> objectProperty = new SimpleObjectProperty<>(this, listName , list);
        ListProperty<String> listProperty = listProperty(objectProperty);
        assertNull("bean must be null", listProperty.getBean());
        assertEquals("name must be same as adapted property", listName, listProperty.getName());
        assertSame("listProperty value must be sync'ed", list, listProperty.get());
    }
    
    @Test
    public void testListPropertyAdapterModifyList() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> objectProperty = new SimpleObjectProperty<>(list);
        ListProperty<String> listProperty = listProperty(objectProperty);
        
        ListChangeReport report = new ListChangeReport(listProperty);
        list.remove(0);
        assertEquals(1, report.getEventCount());
    }

    /**
     * Sanity testing: setting !list.equals(oldList) is working
     */
    @Test
    public void testListPropertyAdapterSetListOnObjectProperty() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> objectProperty = new SimpleObjectProperty<>(list);
        ListProperty<String> listProperty = listProperty(objectProperty);
        ObservableList<String> otherList = createObservableList(true);
        otherList.remove(0);
        ChangeReport objectReport = new ChangeReport(objectProperty);
        ChangeReport report = new ChangeReport(listProperty);
        objectProperty.set(otherList);
        assertEquals("sanity: change event from objectProperty", 1, objectReport.getEventCount());
        assertEquals("must fire change on setting list to objectProperty", 1, report.getEventCount());
    }
    
    /**
     * Real test: setting list.equals(oldList) AND list != oldList must fire
     * This is what the adapter is meant to hack around.
     * 
     */
    @Test
    public void testListPropertyAdapterSetEqualListOnObjectProperty() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> objectProperty = new SimpleObjectProperty<>(list);
        ListProperty<String> listProperty = listProperty(objectProperty);
        ObservableList<String> otherList = createObservableList(true);
        ChangeReport report = new ChangeReport(listProperty);
        objectProperty.set(otherList);
        assertEquals("must fire change on setting list to objectProperty", 1, report.getEventCount());
    }
    
    @Test
    public void testListPropertyAdapterSetEqualListListChangeEvent() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> objectProperty = new SimpleObjectProperty<>(list);
        ListProperty<String> listProperty = listProperty(objectProperty);
        ObservableList<String> otherList = createObservableList(true);
        ListChangeReport report = new ListChangeReport(listProperty);
        objectProperty.set(otherList);
//        otherList.remove(0);
        assertEquals("must fire change on setting list to objectProperty", 1, report.getEventCount());
    }
    
    /**
     * Testing modification on list modifications after setting an equals list.
     * Arguable, the set itself may (or not) fire a change (that's RT-38770),
     * but subsequent modifications on the new list must fire.
     * 
     * Here we test the adapter.
     * 
     * @see BugPropertyAdapters#listProperty(javafx.beans.property.Property)
     */
    @Test
    public void testListPropertyAdapterSetEqualListListChangeEventAfter() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> objectProperty = new SimpleObjectProperty<>(list);
        ListProperty<String> listProperty = listProperty(objectProperty);
        ObservableList<String> otherList = createObservableList(true);
        ListChangeReport report = new ListChangeReport(listProperty);
        objectProperty.set(otherList);
        otherList.remove(0);
        assertEquals("must fire list change after modification on new list", 1, report.getEventCount());
    }
    
    /**
     * Testing modification on list modifications after setting an equals list.
     * Arguable, the set itself may (or not) fire a change (that's RT-38770),
     * but subsequent modifications on the new list must to taken.
     * 
     * Here we test the adapter
     */
    @Test
    public void testListPropertySetEqualListListChangeEventAfter() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> objectProperty = new SimpleObjectProperty<>(list);
        ListProperty<String> listProperty = new SimpleListProperty<>();
        listProperty.bindBidirectional(objectProperty);
        ObservableList<String> otherList = createObservableList(true);
        ListChangeReport report = new ListChangeReport(listProperty);
        objectProperty.set(otherList);
        otherList.remove(0);
        assertEquals("must fire list change after modification on new list", 1, report.getEventCount());
    }
    
    
  //--------------------  
    /**
     * Testing notification of setup in comboX/choiceX
     * 
     * sequence, direction, or where-to-set list doesn't make a difference ..
     * but probably will once RT-35214 is fixed (bubbles up to public preview)
     * not yet in 8u40b7
     * 
     * 
     */
    @Test
    public void testObjectPropertyBoundToListProperty() {
        ListProperty<String> listProperty = new SimpleListProperty<>();
        ObjectProperty<ObservableList<String>> objectProperty = new SimpleObjectProperty<>();
        objectProperty.bindBidirectional(listProperty);
        ObservableList<String> list = createObservableList(true);
        objectProperty.set(list);
        ChangeReport listPropertyReport = new ChangeReport(listProperty);
        ChangeReport objectPropertyReport = new ChangeReport(objectProperty);
        listProperty.set(createObservableList(true));
        assertEquals(1, listPropertyReport.getEventCount());
        assertEquals(1, objectPropertyReport.getEventCount());
    }
    /**
     * underlying issue with RT-15793: no notification on list that is equals
     * but not the same. Fires fine for ListPropery, but not for list-valued
     * ObjectProperty.
     */
    @Test
    public void testListProperty() {
        ObservableList<String> list = createObservableList(true);
        ListProperty<String> property = new SimpleListProperty<>(list);
        ChangeReport report = new ChangeReport(property);
        property.set(createObservableList(true));
        assertEquals("listProperty must fire on not-same list", 1, report.getEventCount());
    }
    
    /**
     * ObjectProperty doesn't fire if setting a list that's equals
     * but not the same. 
     * 
     * The not-firing happens in ExpressionHelpter:
     * - in set(value), bjectPropertyBase marks itself as invalid if value != oldValue
     *   and fires
     * - expressionHelper.fire check against value.equals(oldValue) and
     *   doesn't fire 
     */
    @Test
    public void testListValuedObjectPropertyChange() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> property = new SimpleObjectProperty<>(list);
        ChangeReport report = new ChangeReport(property);
        property.set(createObservableList(true));
        assertEquals("list-valued objectProperty must fire on not-same list", 1, report.getEventCount());
    }
    
    /**
     * Invalidation events are fired.
     */
    @Test
    public void testListValuedObjectPropertyInvalidation() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> property = new SimpleObjectProperty<>(list);
        InvalidationReport report = new InvalidationReport(property);
        property.set(createObservableList(true));
        assertEquals("list-valued objectProperty must fire on not-same list", 1, report.getEventCount());
    }
    
    /**
     * Reported: https://javafx-jira.kenai.com/browse/RT-38828
     * 
     * Confused: listProperty fires changeEvent if items in underlying list
     * removed?
     * 
     * Notes:
     * - seems to be intentional: ListExpressionHelper notifies changeListeners
     *   with oldValue = currentValue = newValue
     * - might be covered by doc if "value changed" is interpreted as !oldValue.equals(newValue)
     * - listeners need to be aware of the fact
     * - ObjectProperty.set test against identity, so no notification explosion in bidi-binding    
     * 
     * Here we have an additional bidi-binding
     */
    @Test
    public void testListPropertyBidiBindingChangeNotificationOnSetList() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> property = new SimpleObjectProperty<>();
        ListProperty<String> listProperty = new SimpleListProperty<>();
        property.bindBidirectional(listProperty);
        ChangeReport report = new ChangeReport(listProperty);
        property.set(list);
        assertEquals("listProperty must fire changeEvent on setting list", 1, report.getEventCount());
        report.clear();
        list.remove(0);
        assertEquals("listProperty must not fire changeEvent on removing item", 0, report.getEventCount());
    }

    
    /**
     * Issue RT-38828 ListProperty fires ChangeEvent on modifications to the list
     */
    @Test
    public void testListPropertyChangeNotificationOnRemoveItem() {
        ObservableList<String> list = createObservableList(true);
        ListProperty<String> listProperty = new SimpleListProperty<>(list);
        ChangeReport report = new ChangeReport(listProperty);
        list.remove(0);
        assertSame("sanity: value didn't change", list, listProperty.get());
        assertEquals("listProperty must not fire changeEvent on removing item", 0, report.getEventCount());
    }
    
    @Test
    public void testListPropertyListChangeNotificationOnRemoveItem() {
        ObservableList<String> list = createObservableList(true);
        ListProperty<String> listProperty = new SimpleListProperty<>(list);
        ListChangeReport report = new ListChangeReport(listProperty);
        list.remove(0);
        assertSame("sanity: value didn't change", list, listProperty.get());
        assertEquals("listProperty must fire listChangeEvent on removing item", 1, report.getEventCount());
//        LOG.info("" + report.getLastListChange()); 
    }
    
    
    @Test
    public void testListPropertyListChangeNotificationOnSetList() {
        ObservableList<String> list = createObservableList(true);
        ListProperty<String> listProperty = new SimpleListProperty<>();
        ListChangeReport report = new ListChangeReport(listProperty);
        listProperty.set(list);
        assertEquals("listProperty must fire changeEvent on setList", 
                1, report.getEventCount());
        Change<String> change = report.getLastListChange();
        assertSame("source of listChange must be list property", listProperty, report.getLastListValue());
    }
    
    
    /**
     * Sanity: plain ObjectProperty<ObservableList>.
     */
    @Test
    public void testPlainPropertyChangeNotificationOnRemoveItem() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> property = new SimpleObjectProperty<>(list);
        ChangeReport report = new ChangeReport(property);
        list.remove(0);
        assertSame(list, property.get());
        assertEquals("plain property must not fire changeEvent on removing item", 0, report.getEventCount());
    }
    
    
//----------------- tests related to RT-38770: changeListeners not notified 
//----------------- https://javafx-jira.kenai.com/browse/RT-38770    
    
    /**
     * Test notification of changeListeners on setting a list that equals the old
     * value but is a different instance.
     * 
     * Passes without bidi-binding.
     */
    @Test
    public void testListPropertyNotificationSingleListener() {
        ObservableList<String> initialValue = createObservableList(false);
        ListProperty<String> property = new SimpleListProperty<>(initialValue);
        ChangeReport report = new ChangeReport(property);
        ObservableList<String> otherValue = createObservableList(false);
        assertTrue("sanity: FXCollections returns a new instance", otherValue != initialValue);
        property.set(otherValue);
        assertEquals(1, report.getEventCount());
        assertSame(initialValue, report.getLastOldValue());
        assertSame(otherValue, report.getLastNewValue());
    }
    
    /**
     * Bug: ListPropertyBase fails to notify change listeners if multiple
     * listeners registered
     */
    @Test
    public void testListPropertyNotificationMultipleListener() {
        ObservableList<String> initialValue = createObservableList(false);
        ListProperty<String> property = new SimpleListProperty<>(initialValue);
        ChangeReport report = new ChangeReport(property);
        ChangeReport otherListener = new ChangeReport(property);
        ObservableList<String> otherValue = createObservableList(false);
        assertTrue("sanity: FXCollections returns a new instance", otherValue != initialValue);
        property.set(otherValue);
        assertEquals(1, report.getEventCount());
        assertSame(initialValue, report.getLastOldValue());
        assertSame(otherValue, report.getLastNewValue());
    }
    
    /**
     * Asserts bidi binding between source/target ListPropery
     * (too much for a decent test, but then ..)
     * - value of target is synched to source
     * - ChangeListener on target is notified on binding
     * 
     * @param withData
     */
    protected void assertListPropertyBidiBinding(boolean withData) {
        ObservableList<String> initialValue = createObservableList(withData);
        ListProperty<String> target = new SimpleListProperty<>(initialValue);
        ObservableList<String> sourceValue = createObservableList(withData);
        ListProperty<String> source = new SimpleListProperty<>(sourceValue);
        ChangeReport targetReport = new ChangeReport(target);

        target.bindBidirectional(source);
        
        assertSame("bidi binding updates", target.get(), source.get());
        assertSame("property is target of bidi", sourceValue, target.get());
        assertSame("other is unchanged", sourceValue, source.get());
        // this passes: listeners on target are notified installing the bidibinding
        assertEquals("change listener on property must be notified", 1, targetReport.getEventCount());
        
        targetReport.clear();
        ChangeReport sourceReport = new ChangeReport(source);
        ObservableList<String> thirdValue = createObservableList(withData);
        source.set(thirdValue);
        assertSame("source value taken", thirdValue, source.get());
        // was meant as sanity testing .. but the bidibinding prevents
        // the notification that is just fine if unbound ... see test above
        // assertEquals("sanity: source listener notified", 1, sourceReport.getEventCount());
        assertSame("property must be updated to new value of other", thirdValue, target.get());
        // this fails: listeners on target are not notified after updating the source of the bidibinding 
        assertEquals("change listener on property must be notified", 1, targetReport.getEventCount());
    }

    @Test
    public void testListPropertyBidiBindingNotificationListEmpty() {
        assertListPropertyBidiBinding(false);
    }
    
    @Test
    public void testListPropertyBidiBindingNotificationListWithData() {
        assertListPropertyBidiBinding(true);
    }
    
    static final String[] DATA = {
            "9-item", "8-item", "7-item", "6-item", 
            "5-item", "4-item", "3-item", "2-item", "1-item"}; 
    protected ObservableList<String> createObservableList(boolean withData) {
        return withData ? FXCollections.observableArrayList(DATA) : FXCollections.observableArrayList();
    }
    
    /**
     * Sanity: double-check notifications on Object properties
     */
    @Test
    public void testPropertyBidiBindingNotification() {
        String initialValue = "initial";
        ObjectProperty<String> property = new SimpleObjectProperty<>(initialValue);
        String otherValue = "other";
        ObjectProperty<String> otherProperty = new SimpleObjectProperty<>(otherValue);
        ChangeReport report = new ChangeReport(property);
        property.bindBidirectional(otherProperty);
        
        assertSame("bidi binding updates", property.get(), otherProperty.get());
        assertSame("property is target of bidi", otherValue, property.get());
        assertSame("other is unchanged", otherValue, otherProperty.get());
        assertEquals(1, report.getEventCount());
        
        report.clear();
        String thirdValue = "something else";
        otherProperty.set(thirdValue);
        assertSame("property must be updated to new value of other", thirdValue, property.get());
        assertEquals(1, report.getEventCount());
    }

    //------------- logging
    @Test @Ignore
    public void testNotificationSubList() {
        ObservableList<String> list = createObservableList(true);
        int index = list.size() - 5;
        ObservableList<String> subList = FXCollections.observableArrayList(list.subList(index, list.size()));
        ObjectProperty<ObservableList<String>> objectProperty = new SimpleObjectProperty<>();
        ListProperty<String> listProperty = new SimpleListProperty<>();
        objectProperty.bindBidirectional(listProperty);
        objectProperty.set(list);
        ListChangeReport report = new ListChangeReport(listProperty);
        objectProperty.set(subList);
        FXUtils.prettyPrint(report.getLastListChange());
    }

    @Test @Ignore
    public void testNotificationObservableList() {
        ObservableList<String> list = createObservableList(true);
        ListChangeListener l = c -> FXUtils.prettyPrint(c);
        list.addListener(l);
        modifyListSetAll(list);
    }
    
    @Test @Ignore
    public void testNotificationListProperty() {
        ObservableList<String> list = createObservableList(true);
        ListChangeListener l = c -> FXUtils.prettyPrint(c);
        ListProperty<String> property = new SimpleListProperty<>(list);
        property.addListener(l);
        modifyListSetAll(list);
        LOG.info("property: set equal list");
        property.set(createObservableList(true));
        LOG.info("set subList");
        property.set(FXCollections.observableArrayList(list.subList(2, 5)));
        LOG.info("set empty list");
        property.set(createObservableList(false));
        LOG.info("set empty list on empty list");
        property.set(createObservableList(false));
        LOG.info("set sub on empty");
        property.set(FXCollections.observableArrayList(list.subList(2, 5)));
        
        
    }

    protected void modifyListSetAll(ObservableList<String> list) {
        LOG.info("setAll with newSize < oldSize");
        list.setAll("one", "two");
        LOG.info("setAll with newSize == 0");
        list.setAll();
        LOG.info("setAll on empty list");
        list.setAll("one", "two");
        LOG.info("setAll with newSize = oldSize");
        list.setAll("one", "two");
        LOG.info("setAll with newSize > oldSize");
        list.setAll("one", "two", "three");
        LOG.info("clear");
        list.clear();
        LOG.info("setAll with collection");
        list.setAll(createObservableList(true));
        LOG.info("setAll with equals list");
        list.setAll(createObservableList(true));
    }
    //--------------------    
    
    /**
     * which change is fired in ListProperty on list.setAll vs. set(list)
     * 
     * fires a replaced (aka added && removed) - detectable via
     * list.size = change.addedSize?
     */
    @Test @Ignore
    public void testListPropertyNotificationDetails() {
        ObservableList<String> list = createObservableList(true);
        ListProperty<String> lp = new SimpleListProperty<>(list);
        ListChangeListener l = c -> FXUtils.prettyPrint(c);
        LOG.info("change from listProperty: modifying list");
        lp.addListener(l);
        LOG.info("set same element");
        list.set(0, list.get(0));
        LOG.info("set same list");
        list.setAll(createObservableList(true));
        // fires one event with two changes
        LOG.info("remove two not subsequent");
        list.removeAll(list.get(1), list.get(5)); 
        LOG.info("retain all with complete list");
        // fires nothing 
        list.retainAll(createObservableList(true));
        LOG.info("setAll to initial");
        list.setAll(createObservableList(true));
        // fires one event with 3 changes of type remove
        list.retainAll(list.get(0), list.get(3), list.get(7));
        LOG.info("setall with new list");
        list.setAll("one", "twoorwhat");
        // fires one replaced event for each element 
        UnaryOperator<String> op = p -> {
            if (p.length() > 3) return p;
            return p + 3;
         }; 
         list.replaceAll(op);               
        // fires setAll
        FXCollections.replaceAll(list, "one", "other");
        list.removeAll("one", "two");
        LOG.info("reset ListProperty to initial list");
        lp.set(createObservableList(true));
    }
    
    @Test @Ignore
    public void testObservableListNotificationDetails() {
        ObservableList<String> list = createObservableList(true);
        ListChangeListener l = c -> FXUtils.prettyPrint(c);
        LOG.info("change from list: ");
        list.addListener(l);
        list.setAll("one", "two");
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ObservableListTest.class
            .getName());
}
