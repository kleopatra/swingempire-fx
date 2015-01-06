/*
 * Created on 11.10.2014
 *
 */
package de.swingempire.fx.property;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.ConditionalIgnore;

import de.swingempire.fx.property.PropertyIgnores.IgnoreEqualsNotFire;
import de.swingempire.fx.property.PropertyIgnores.IgnoreReported;
import de.swingempire.fx.scene.control.cell.Person22463;
import de.swingempire.fx.util.ChangeReport;
import de.swingempire.fx.util.FXUtils;
import de.swingempire.fx.util.InvalidationReport;
import de.swingempire.fx.util.ListChangeReport;
import static de.swingempire.fx.property.BugPropertyAdapters.*;
import static de.swingempire.fx.util.FXUtils.*;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ObservableListTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    /**
     * Sanity ... removing last implies that getFrom == size!! that is
     * invalid index altogether.
     */
    @Test
    public void testRemovedLast() {
        ObservableList<String> items = createObservableList(true);
        ListChangeReport report = new ListChangeReport(items);
        int oldSize = items.size();
        int lastIndex = oldSize - 1;
        items.remove(lastIndex);
        assertEquals(oldSize - 1, items.size());
        Change c = report.getLastChange();
        c.next();
        assertEquals(lastIndex, c.getFrom());
        report.prettyPrint();
    }
    /**
     * Sanity ...
     */
    @Test
    public void testRemovedFrom() {
        ObservableList<String> items = createObservableList(true);
        ListChangeReport report = new ListChangeReport(items);
        int index = 2;
        items.remove(index);
        Change c = report.getLastChange();
        c.next();
        assertEquals(index, c.getFrom());
    }
    @Test
    public void testSetSameItem() {
        ObservableList<String> items = createObservableList(true);
        ListChangeReport report = new ListChangeReport(items);
        String first = items.get(0);
        items.set(0, first);
        assertEquals(1, report.getEventCount());
        assertTrue(wasSingleReplaced(report.getLastChange()));
        Change c = report.getLastChange();
        c.reset();
        c.next();
        assertSame(first, c.getAddedSubList().get(0));
        assertSame(first, c.getRemoved().get(0));
    }
    
    /**
     * Simulate indirect modifications:
     * 
     * - selectedIndices keeps indices into the base list
     * - needs to keep the indices up-to-date when the base list is modified
     * - is observable and fires notifications as expected
     * - listeners to selectedIndices _must not_ change the base list during
     *   processing the change on indices! 
     *  
     */
    @Test
    public void testCoupledLists() {
        ObservableList<String> list = createObservableList(true);
        // simulate selection into list above
        ObservableList<Integer> selectedIndices = FXCollections.observableArrayList(2, 3);
        // simulate selectedItems
        ObservableList<String> selectedItems = FXCollections.observableArrayList();
        for (Integer index : selectedIndices) {
            selectedItems.add(list.get(index));
        }
        FilteredList t;
        // this listener is responsible for updating the indices on modifications
        // of the base llist
        ListChangeListener selectedIndexUpdater = c -> {
            // PENDING JW: this is _not_ the pattern used in FilteredList, 
            // think again!
            while (c.next()) {
                if (c.wasReplaced()) {
                    // weed out synthetic adds/removes
                    // ignore for now 
                } else if (c.wasAdded()) {
                    // real adds
                    // position where this change starts
                    int pos = c.getFrom();
                    int firstAffected = -1;
                    for (int i = 0; i < selectedIndices.size(); i++) {
                        if (selectedIndices.get(i) >= pos) {
                            firstAffected = i;
                            break;
                        }
                    }
                    if (firstAffected >= 0) {
                        int addedSize = c.getAddedSize();
                        for (int i = firstAffected; i < selectedIndices.size(); i++) {
                            selectedIndices.set(i, selectedIndices.get(i) + addedSize);
                        }
                    }
                } else if (c.wasRemoved()) {
                    // real removes
                }
            }
        };
        
        list.addListener(selectedIndexUpdater);
        // this is the listener that updates the backing list while
        // receiving notifications from the updater
        // this is _wrong_ if the notifications from the selected
        // where fired during processing of the change
        // and blows somewhere alöng thelines
        ListChangeListener back = c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    List<Integer> added = c.getAddedSubList();
                    for (int i = added.size() - 1; i >= 0; i--) {
                        int index = added.get(i);
                        list.remove(index);
                    }
                }
            }
        };
//        selectedIndices.addListener(back);
        list.add(3, "item-added");
//        LOG.info(selectedIndices + "");
    }
    
    @Test
    public void testSanityPermutation() {
        ObservableList<Integer> list = FXCollections.observableArrayList(1, 2, 3, 4);
        ObservableList<Integer> copy = FXCollections.observableArrayList(list);
        ListChangeReport report = new ListChangeReport(list);
        FXCollections.sort(list);
        Change c = report.getLastChange();
        assertTrue(wasSinglePermutated(c));
        c.reset();
        c.next();
        for (int i = c.getFrom(); i < c.getTo(); i++) {
            int newIndex = c.getPermutation(i);
            assertEquals(copy.get(i), list.get(newIndex));
        }
        
    }
    

    @Test
    public void testSubListClear() {
        ObservableList list = createObservableList(true);
        int from = 2;
        int to = 6;
        List subList = list.subList(from, to);
        int subSize = subList.size();
        ListChangeReport report = new ListChangeReport(list);
        subList.clear();
        assertEquals(1, report.getEventCount());
        Change c = report.getLastChange();
        assertEquals(subSize, getRemovedSize(c));
        assertEquals(0, getAddedSize(c));
    }
    
    /**
     * subList is of type List and there's no setAll for List
     * 
     * So we can't get a replaced by a sublist?
     */
    @Test @Ignore
    public void testSubListSetAll() {
        ObservableList list = createObservableList(true);
        int from = 2;
        int to = 6;
        List subList = list.subList(from, to);
        int subSize = subList.size();
        List itemsOfSubList = new ArrayList(subList);
//        itemsOfSubList.remove(0);
        ListChangeReport report = new ListChangeReport(list);
        subList.retainAll(itemsOfSubList);
        assertEquals("wrong assumption: implementation is clever enough to detect retain same",
                1, report.getEventCount());
        Change c = report.getLastChange();
        LOG.info("changed list: " + list);
        prettyPrint(c);
        assertEquals("single change" , 1, getChangeCount(c));
        assertEquals("removed", subSize, getRemovedSize(c));
        assertEquals("added" , subSize, getAddedSize(c));
        assertTrue("single replace" + c, wasSingleReplaced(c));
    }
    
    @Test
    public void testSetAllEqualList() {
        ObservableList list = createObservableList(true);
        int size = list.size();
        ObservableList other = createObservableList(true);
        ListChangeReport report = new ListChangeReport(list);
        list.setAll(other);
        assertEquals(1, report.getEventCount());
        Change c = report.getLastChange();
        assertEquals(size, getAddedSize(c));
        assertEquals(size, getRemovedSize(c));
        assertEquals(1, getChangeCount(c));
        assertTrue(wasAllChanged(c));
        assertTrue(wasSingleReplaced(c));
    }
    
    @Test
    public void testSetAllSmallerList() {
        ObservableList list = createObservableList(true);
        int size = list.size();
        ObservableList other = createObservableList(true);
        other.remove(1);
        int otherSize = other.size();
        ListChangeReport report = new ListChangeReport(list);
        list.setAll(other);
        assertEquals(1, report.getEventCount());
        Change c = report.getLastChange();
        assertEquals(otherSize, getAddedSize(c));
        assertEquals(size, getRemovedSize(c));
        assertEquals(1, getChangeCount(c));
        assertTrue(wasAllChanged(c));
        assertTrue(wasSingleReplaced(c));
    }
    
    @Test
    public void testSetAllLargerList() {
        ObservableList list = createObservableList(true);
        int size = list.size();
        ObservableList other = createObservableList(true);
        other.add("additional");
        int otherSize = other.size();
        ListChangeReport report = new ListChangeReport(list);
        list.setAll(other);
        assertEquals(1, report.getEventCount());
        Change c = report.getLastChange();
        assertEquals(otherSize, getAddedSize(c));
        assertEquals(size, getRemovedSize(c));
        assertEquals(1, getChangeCount(c));
        assertTrue(wasAllChanged(c));
        assertTrue(wasSingleReplaced(c));
    }
    
    @Test
    public void testSetAllWithEmptyList() {
        ObservableList list = createObservableList(true);
        int size = list.size();
        ObservableList other = createObservableList(false);
        int otherSize = other.size();
        ListChangeReport report = new ListChangeReport(list);
        list.setAll(other);
        assertEquals(1, report.getEventCount());
        Change c = report.getLastChange();
        assertEquals(otherSize, getAddedSize(c));
        assertEquals(size, getRemovedSize(c));
        assertEquals(1, getChangeCount(c));
        assertTrue(wasAllChanged(c));
    }
    
    @Test
    public void testSetAllToEmptyList() {
        ObservableList list = createObservableList(false);
        int size = list.size();
        ObservableList other = createObservableList(true);
        int otherSize = other.size();
        ListChangeReport report = new ListChangeReport(list);
        list.setAll(other);
        assertEquals(1, report.getEventCount());
        Change c = report.getLastChange();
        assertEquals(otherSize, getAddedSize(c));
        assertEquals(size, getRemovedSize(c));
        assertEquals(1, getChangeCount(c));
        assertTrue(wasAllChanged(c));
    }
    
//------------- compile test: replace ObjectProperty<List> by ListProperty?
    
    @Test
    public void testAPIChange() {
        WithListValue<String> p = new WithListValue<>();
        Property<ObservableList<String>> property = p.itemsProperty();
        ChangeListener<? super ObservableList<String>> listener = new ChangeListener<ObservableList<String>>() {

            @Override
            public void changed(
                    ObservableValue<? extends ObservableList<String>> observable,
                    ObservableList<String> oldValue,
                    ObservableList<String> newValue) {
                LOG.info("dummy dooo!");
                
            }
        } ;
        
        property.addListener(listener);
//        ListProperty items = table.itemsProperty();
    }

    public static class WithListValue<T> {
        
        ObjectProperty<ObservableList<T>> items = new SimpleObjectProperty<>();
        ListProperty<T> itemsList = new SimpleListProperty();
        
        Property<ObservableList<T>> itemsProperty() {
            return itemsList;
        }
    }

//------------- testing list notification if elements are equal: RT_22463    
    
    @Test
    public void testNotificationSetEqualElement() {
        ObservableList first = FXCollections.observableArrayList(getPerson1());
        ObservableList second = FXCollections.observableArrayList(getPerson2());
        int index = 0;
        assertEquals("sanity: ", first.get(index), second.get(index));
        ListChangeReport report = new ListChangeReport(first);
        first.set(index, second.get(index));
        assertEquals("list must have fired on setting equal item", 1, report.getEventCount());
        
    }
    @Test
    public void testNotificationSetAllEqualList() {
        ObservableList first = FXCollections.observableArrayList(getPerson1());
        ObservableList second = FXCollections.observableArrayList(getPerson2());
        assertEquals("sanity: ", first, second);
        ListChangeReport report = new ListChangeReport(first);
        first.setAll(second);
        assertEquals("list must have fired on setting equal item", 1, report.getEventCount());
        
    }
    private List<Person22463> getPerson1() {
        List<Person22463> p = new ArrayList<>();
        Person22463 p1 = new Person22463();
        p1.setId(1l);
        p1.setName("name1");
        Person22463 p2 = new Person22463();
        p2.setId(2l);
        p2.setName("name2");
        p.add(p1);
        p.add(p2);
        return p;
    }

    private List<Person22463> getPerson2() {
        List<Person22463> p = new ArrayList<>();
        Person22463 p1 = new Person22463();
        p1.setId(1l);
        p1.setName("updated name1");
        Person22463 p2 = new Person22463();
        p2.setId(2l);
        p2.setName("updated name2");
        p.add(p1);
        p.add(p2);
        return p;
    }

//---------- end testing 22463    
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
        objectProperty.set(otherList);
        ListChangeReport report = new ListChangeReport(listProperty);
        otherList.remove(0);
        assertEquals("must fire list change after modification on new list", 1, report.getEventCount());
    }
    
    /**
     * Testing modification on list modifications after setting an equals list.
     * Arguable, the set itself may (or not) fire a change (that's RT-38770),
     * but subsequent modifications on the new list must to taken.
     * 
     * Consequence for usage of list-valued properties: 
     * - don't use a ChangeListener on a listValued ObjectProperty if you
     *   are interested in content-listening, instead use an invalidationListener
     * - don't use listValued ObjectProperties at all, instead use ListProperty   
     * 
     * Consequences for usage of ListProperty 
     * - don't raw bidi-bind listProperty to a listValued ObjectProperty, instead
     * - either: bind to listValued property (if the listProperty can be read-only)
     * - or: additionally add an InvalidationListener that updates the value
     *   of the listProperty in case the new value is equals  
     */
    @Test
    @ConditionalIgnore(condition = IgnoreEqualsNotFire.class)
    public void testListPropertySetEqualListListChangeEventAfter() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> objectProperty = new SimpleObjectProperty<>(list);
        ListProperty<String> listProperty = new SimpleListProperty<>();
        listProperty.bindBidirectional(objectProperty);
        ObservableList<String> otherList = createObservableList(true);
        objectProperty.set(otherList);
        ListChangeReport report = new ListChangeReport(listProperty);
        otherList.remove(0);
        assertEquals("Culprit is bidi-bound ObjectProperty \n listProperty must fire list change after modification on new list", 
                1, report.getEventCount());
    }
    
    
  //--------------------  
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
     *   
     * sequence, direction, or where-to-set list doesn't make a difference ..
     * NO - can't! but probably will once RT-35214 is fixed (bubbles up to public preview)
     * 
     */
    @Test
    @ConditionalIgnore(condition = IgnoreEqualsNotFire.class)
    public void testListValuedObjectPropertyChange() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> property = new SimpleObjectProperty<>(list);
        ChangeReport report = new ChangeReport(property);
        property.set(createObservableList(true));
        assertEquals("not supported: \n list-valued objectProperty must fire on not-same list", 
                1, report.getEventCount());
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
        assertEquals("supported: list-valued objectProperty must fire on not-same list", 
                1, report.getEventCount());
    }
    
    /**
     * Invalidation events are fired.
     */
    @Test
    public void testListValuedObjectPropertyBoundTo() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> property = new SimpleObjectProperty<>(list);
        ObjectProperty<ObservableList<String>> otherProperty =
                new SimpleObjectProperty(createObservableList(true));
        assertFalse("sanity: two properties with equal list are not equal", 
                property.equals(otherProperty));
        ListProperty listProperty = new SimpleListProperty();
        listProperty.bind(property);
        
        ChangeReport report = new ChangeReport(listProperty);
        property.set(createObservableList(true));
        assertEquals("supported: listProperty bound to listValued property fires change event", 
                1, report.getEventCount());
        ListChangeReport lr = new ListChangeReport(listProperty);
        property.get().remove(0);
        assertEquals(1, lr.getEventCount());
    }
    
    
    /**
     * Issue RT-38828 ListProperty fires ChangeEvent on modifications to the list
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
     */
    @Test
    @ConditionalIgnore(condition = IgnoreReported.class)
    public void testListPropertyChangeNotificationOnRemoveItem() {
        ObservableList<String> list = createObservableList(true);
        ListProperty<String> listProperty = new SimpleListProperty<>(list);
        ChangeReport report = new ChangeReport(listProperty);
        list.remove(0);
        assertSame("sanity: value didn't change", list, listProperty.get());
        assertEquals("RT-38828: listProperty must not fire changeEvent on removing item", 
                0, report.getEventCount());
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
        Change<String> change = report.getLastChange();
        assertSame("source of listChange must be list property", listProperty, report.getLastValue());
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
     * Testing bidi-binding between 2 listProperty.
     */
    @Test
    public void testListPropertyBidiBindingToListProperty() {
        ObservableList<String> list = createObservableList(true);
        ListProperty<String> p1 = new SimpleListProperty<>(list);
        ListProperty<String> p2 = new SimpleListProperty<>(list);
        p2.bindBidirectional(p1);
        assertSame("sanity, same list bidi-bound", list, p2.get());
        ObservableList<String> other = createObservableList(true);
        ChangeReport report = new ChangeReport(p2);
        p1.set(other);
        assertEquals("RT-38770 - bidi-binding between two ListProperties", 1, report.getEventCount());
    }
    
    /**
     * Testing binding between 2 listProperty.
     */
    @Test
    public void testListPropertyBindingToListProperty() {
        ObservableList<String> list = createObservableList(true);
        ListProperty<String> p1 = new SimpleListProperty<>(list);
        ListProperty<String> p2 = new SimpleListProperty<>(list);
        p2.bind(p1);
        assertSame("sanity, same list bidi-bound", list, p2.get());
        ObservableList<String> other = createObservableList(true);
        ChangeReport report = new ChangeReport(p2);
        p1.set(other);
        assertEquals("RT-38770 - bind 2 ListProperties", 1, report.getEventCount());
    }
    
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
        FXUtils.prettyPrint(report.getLastChange());
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
