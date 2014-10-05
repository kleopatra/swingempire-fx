/*
 * Created on 18.06.2014
 *
 */
package de.swingempire.fx.property;

import java.util.logging.Logger;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.util.ChangeReport;
import de.swingempire.fx.util.ListChangeReport;
import static de.swingempire.fx.property.BugPropertyAdapters.*;
import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class ObservableTest {

//--------------------  
    /**
     * Testing notification of setup in comboX/choiceX
     * 
     * sequence, direction, or where-to-set list doesn't make a difference ..
     * but probably will once RT-35214 is fixed (bubbles up to public preview)
     * not yet in 8u40b7
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
     * but not the same.
     */
    @Test
    public void testListProperty() {
        ObservableList<String> list = createObservableList(true);
        ListProperty<String> property = new SimpleListProperty<>(list);
        ChangeReport report = new ChangeReport(property);
        property.set(createObservableList(true));
        assertEquals("listProperty must fire on not-same list", 1, report.getEventCount());
    }
    
    @Test
    public void testListValuedObjectProperty() {
        ObservableList<String> list = createObservableList(true);
        ObjectProperty<ObservableList<String>> property = new SimpleObjectProperty<>(list);
        ChangeReport report = new ChangeReport(property);
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
        LOG.info("" + report.getLastListChange()); 
    }
    
    
    @Test
    public void testListPropertyListChangeNotificationOnSetList() {
        ObservableList<String> list = createObservableList(true);
        ListProperty<String> listProperty = new SimpleListProperty<>();
        ListChangeReport report = new ListChangeReport(listProperty);
        listProperty.set(list);
        assertSame("sanity: value didn't change", list, listProperty.get());
        assertEquals("listProperty must fire changeEvent on setList", 1, report.getEventCount());
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
    
//-------------------- end test related to https://javafx-jira.kenai.com/browse/RT-38770
    
    @Test
    public void testDynamicBooleanBinding() {
        DynamicBooleanBinding binding = new DynamicBooleanBinding() {

            @Override
            protected boolean computeValue() {
                return false;
            }
            
        };
        assertNotNull(binding.marker);
        binding.get();
        assertTrue(binding.isValid());
        BooleanProperty p = new SimpleBooleanProperty(true);
        binding.addDependencies(p);
        assertFalse(binding.isValid());
    }

    /**
     * Bug: the observable in notifications must be the instance the 
     * listener was added to.
     */
    @Test
    public void testReadOnlyWrapperInvalidation() {
        ReadOnlyObjectWrapper<String> wrapper = new ReadOnlyObjectWrapper<>();
        wrapper.addListener(o -> {
            assertSame(wrapper, o);
        });
        wrapper.setValue("dummy");
    }
    
    @Test
    public void testReadOnlyWrapperChange() {
        ReadOnlyObjectWrapper<String> wrapper = new ReadOnlyObjectWrapper<>();
        wrapper.addListener((o, oldValue, newValue) -> {
            assertEquals(o.getValue(), newValue);
            assertSame(wrapper, o);
        });
        wrapper.setValue("dummy");
    }
    @Test
    public void testNumberTyping() {
        int initial = 10;
        IntegerProperty base = new SimpleIntegerProperty(initial);
        base.setValue(10.5);
        assertEquals(Integer.class, base.getValue().getClass());
    }
    
    /**
     * Bug - wrapper changes the source value.
     * https://javafx-jira.kenai.com/browse/RT-37523
     * 
     * Fixed as of 8u20
     */
    @Test
    public void testBooleanCore() {
        Boolean initial = true;
        ObjectProperty<Boolean> source = new SimpleObjectProperty<>(initial); 
        BooleanProperty wrapper = BooleanProperty.booleanProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testBooleanFix() {
        Boolean initial = true;
        ObjectProperty<Boolean> source = new SimpleObjectProperty<>(initial); 
        BooleanProperty wrapper = booleanProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testLongCore() {
        Long initial = 10l;
        ObjectProperty<Long> source = new SimpleObjectProperty<>(initial); 
        LongProperty wrapper = LongProperty.longProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testLongFix() {
        Long initial = 10l;
        ObjectProperty<Long> source = new SimpleObjectProperty<>(initial); 
        LongProperty wrapper = longProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testFloatCore() {
        Float initial = 10.f;
        ObjectProperty<Float> source = new SimpleObjectProperty<>(initial); 
        FloatProperty wrapper = FloatProperty.floatProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testFloatFix() {
        Float initial = 10.f;
        ObjectProperty<Float> source = new SimpleObjectProperty<>(initial); 
        FloatProperty wrapper = floatProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testDoubleCore() {
        Double initial = 10.;
        ObjectProperty<Double> source = new SimpleObjectProperty<>(initial); 
        DoubleProperty wrapper = DoubleProperty.doubleProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    @Test
    public void testDoubleFix() {
        Double initial = 10.;
        ObjectProperty<Double> source = new SimpleObjectProperty<>(initial); 
        DoubleProperty wrapper = doubleProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    /**
     * Issue: core adapter changes state of source property
     */
    @Test
    public void testIntegerCore() {
        Integer initial = 10;
        ObjectProperty<Integer> source = new SimpleObjectProperty<>(initial); 
        IntegerProperty wrapper = IntegerProperty.integerProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
    }
    
    /**
     * Issue: core adapter changes state of source property
     * 
     * fix by c&p ... has generic typing issues in bindNumber, method signature
     * 
     *     bindNumber(Property<T>, IntegerProperty);
     *     
     * aka: 
     *     bindNumber(Property<T>, Property<Number>)    
     *     
     * reverse can't be done:
     * 
     *     bindNumber(Property<Number>, Property<T>)
     *     
     * fix in u20 has a whole lot of methods with swapped parameter types, which at
     * the end swap the parameter when creating the TypedNumberBidirectionalBinding
     * (which is only the listener, registered after the binding synchs the values    
     * 
     */
    @Test
    public void testIntegerFix() {
        Integer initial = 10;
        ObjectProperty<Integer> source = new SimpleObjectProperty<>(initial); 
        IntegerProperty wrapper = integerProperty(source);
        assertEquals(initial, source.getValue());
        assertEquals(initial, wrapper.getValue());
        wrapper.setValue(new Double(10.5));
    }
    
    /**
     * Reverting state in changeListener triggers a StackoverflowException
     * 
     * Which is to be expected: the general rule is to never-ever change the
     * state of the caller in a notification method.
     * 
     * Crap: was error in test code - there's no inherent reason for the
     * framework code to throw up, application might suffer, though (think
     * of different listeners reverting to different default values)
     * 
     * https://javafx-jira.kenai.com/browse/RT-32139
     * 
     */
    @Test 
    public void testStateChangeInChangeListener() {
        String sourceValue = "original";
        StringProperty source = new SimpleStringProperty(sourceValue);
        ChangeListener<String> l = new ChangeListener<String>() {
            
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                source.setValue(sourceValue);
            }
        };
        source.addListener(l);
        source.setValue("something else");
    }
    
    /**
     * Reverting state in Invalidation is fine, it's simply marked as invalid.
     * Crap, see above
     */
    @Test
    public void testStateChangeInvalidationListener() {
        String sourceValue = "original";
        StringProperty source = new SimpleStringProperty(sourceValue);
        InvalidationListener l = new InvalidationListener() {
            
            @Override
            public void invalidated(Observable observable) {
                if (!sourceValue.equals(source.getValue() )) {
                    source.setValue(sourceValue);
                }
            }
        };
        source.addListener(l);
        source.setValue("something else");
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ObservableTest.class
            .getName());
}
