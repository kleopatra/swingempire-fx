/*
 * Created on 30.09.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */


import java.util.logging.Logger;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
//import javafx.scene.accessibility.Attribute;
//import javafx.scene.accessibility.Role;
import javafx.util.Callback;
import javafx.util.StringConverter;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

import de.swingempire.fx.property.PathAdapter;

/**
 * 
 * C&P'ed core 8u20 to experiment with cleanup ideas.
 * 
 * Experimentations
 * <li> add and make use of ListProperty for items
 * <li> cleanup selection model: make it obey its class invariant always,
 *   react to items changes correctly and make it extendable 
 * <li> let the model be in full control about its own state, remove
 *   all external bowel interference
 * <li> fix the fix for dynamic items while opening popup: ignore
 *   selection changes while in process of opening
 * <li> use SingleMultipleSelection (slave of combo's selectionModel)
 *   in list in popup
 * <li> bind list's itemProperty to combo's itemProperty         
 * 
 * <li>
 * Changes:
 * <li> replaced manual wiring to items property by itemsListProperty
 *   (kept itemsProperty, bound bidi to itemsListProperty)
 * <li> replaced manual wiring to selectionModel.selectedItemProperty by PathAdapter
 * <li> fixed regression RT-22572/22937 (orig hacked in selectedItemListener)
 *   is: keep selectedItem while opening popup 
 * <li> solved 22572 in show: don't update value if selection changed in
 *   the course of showing, instead revert selectedItem to value at its end   
 * <li> checked if fix in show is good enough, what if dynamic data update is done
 *   on Event.ON_SHOWING no, it's fired in setShowing which is called from 
 *   show/hide. Hooking into show is just fine.  
 * <li> DEFERRED JW regression testing RT-19227 (orig hacked in listener to valueProperty
 *   is: multiple instances in list (what's the use-case?)
 *   core fix is incomplete - RT-38927 - wait for fix-all until support here
 * <li> regression testing RT-15793 (orig hacked in itemsContentListener)
 *   is: missing notification on setting equals but not same list
 *   hacked with InvalidationListener on itemsProperty that forces a set on 
 *   the itemsListProperty (see code comment in constructor)
 *   (no, see below) waiting for core fix of listProperty notification
 * <li> Note: RT-15793 is about missing config option of ObjectProperty (can't configure
 *   to fire on identity check vs. equality check) so listProperty is not the culprit 
 * <li> PENDING JW: use BugPropertyAdapters.listProperty instead of manual hack here  
 * <li> replaced list change handling, doc'ed behaviour 
 * <li> removed interference of ComboBox into inner bowels of selectionModel
 * <li> fixed selectionModel select(Object) to not break class invariant
 * <li> fixed editing (broken during re-implement of skin)
 * <li> TODO fully cleanup skin
 * <li> use converter for null/empty selected item (if there's not prompt)
 * 
 * 
 * 
 * ------------------------ most of original api doc removed - see core ComboBox
 * 
 * Except violation of selectionModel class invariant:
 * 
 *     <li>It is valid for the selection model to have a selection set to a given
 *     index even if there is no items in the list (or less items in the list than
 *     the given index). Once the items list is further populated, such that the
 *     list contains enough items to have an item in the given index, both the
 *     selection model {@link SelectionModel#selectedItemProperty()} and
 *     value property will be updated to have this value. This is inconsistent with
 *     other controls that use a selection model, but done intentionally for ComboBox.</li>
 *
 * It's not only inconsistent with other controls, it's breaking class invariant of 
 * SelectionModel which is something like
 * 
 * <code><pre>
 * if (selectedIndex >= 0) {
 *     assertEquals(selectedItem, getItems(selectedIndex);
 * }
 * if (!getItems().contains(selectedItem)) {
 *      assertEquals(-1, selectedIndex);
 * }
 * </pre> </code>
 * 
 * not respecting that (which isn't a viable option anyway, basic OO principles <b>MUST</b>
 * be respected) will lead to breaking valid code that relies on it:
 * 
 * if (getSelectedIndex() >= 0) {
 *     Object interestingItem = getItems(getSelectedIndex));
 * }
 * 
 * Also, the value should be the selectedItem at all stable combo states: unstable are
 * <li> while opening the popup (client code might populate items)
 * <li> uncommitted edits in the textField
 * (maybe: alternatively could be regarded as "ouside" of combo responsibily anyway) 
 * 
 * <p> 
 * 
 * Forturnately, that breakage isn't really happening too often: the driving issue RT-26079
 * is hard to reproduce: happens when using a builder, which is deprecated nowadays anyway, or
 * re-setting a list that is equal but not the same in some (not really understood by me)
 * circumstances.   
 * 
 * @see ComboBox
 * @see ComboBoxBase
 * @see ComboBoxXListViewSkin
 * @see de.swingempire.fx.property.BugPropertyAdapters#listProperty(javafx.beans.property.Property)
 * 
 */
public class ComboBoxX<T> extends ComboBoxBase<T> {
    
    /***************************************************************************
     *                                                                         *
     * Static properties and methods                                           *
     *                                                                         *
     **************************************************************************/
    
    private static <T> StringConverter<T> defaultStringConverter() {
        return new StringConverter<T>() {
            @Override public String toString(T t) {
                return t == null ? null : t.toString();
            }

            @Override public T fromString(String string) {
                return (T) string;
            }
        };
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a default ComboBox instance with an empty 
     * {@link #itemsProperty() items} list and default 
     * {@link #selectionModelProperty() selection model}.
     */
    public ComboBoxX() {
        this(FXCollections.<T>observableArrayList());
    }
    
    /**
     * Creates a default ComboBox instance with the provided items list and
     * a default {@link #selectionModelProperty() selection model}.
     */
    public ComboBoxX(ObservableList<T> items) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        
        // CHANGED JW: replaced manual wiring by PathAdapter
        selectedItemPath = new PathAdapter<>(selectionModelProperty(), p -> p.selectedItemProperty());
        selectedItemPath.addListener(selectedItemListener);
        // CHANDED JW: bidi-bind to new ListProperty
        itemsProperty().bindBidirectional(itemsList);

        /*
         * Hacking around RT_15793: ObjectProperty doesn't fire changeEvent for
         * equals but not same list. Need identity check.
         * 
         * Not really better than core, just more localized: all collaborators
         * that are interested in items' changes are listening/binding
         * to itemsListProperty. 
         * 
         * Think about formalizing into an adapter with general usefulness.
         */
        InvalidationListener hack15793 = o -> {
            ObservableList<T> newItems = ((ObjectProperty<ObservableList<T>>) o).get();
            ObservableList<T> oldItems = itemsList.get();
            boolean changedEquals = (newItems != null) && (oldItems != null) && newItems.equals(oldItems);
            if (changedEquals) {
                itemsList.set(newItems);
            }
        };
        
        itemsProperty().addListener(hack15793);
        setItems(items);
        setSelectionModel(new ComboBoxXSelectionModel<T>(this));
        // KEEP JW: original comment
        // listen to the value property input by the user, and if the value is
        // set to something that exists in the items list, we should update the
        // selection model to indicate that this is the selected item
        valueProperty().addListener((ov, t, t1) -> {
//            new RuntimeException("who's calling? " + t + " / " + t1).printStackTrace();
            // CHANGED JW: copied code from ChoiceBoxX
            final SingleSelectionModel<T> sm = getSelectionModel();
            if (sm != null) {
                sm.select(t1);
            }
            // KEEP until regression testing done
            // PENDING JW: the part in the else-block is about supporting duplicates RT-19227
            // which is incomplete: reported as RT-38927
            // CHANGED JW: removed all special casing, let the selectionModel handle it
//            if (getItems() == null) return;
//
//            SelectionModel<T> sm = getSelectionModel();
//            int index = getItems().indexOf(t1);
//
//            if (index == -1) {
//                // PENDING JW: selectItem vs. setSelectedItem
////                sm.setSelectedItem(t1);
//                sm.select(t1);
//            } else {
//                // we must compare the value here with the currently selected
//                // item. If they are different, we overwrite the selection
//                // properties to reflect the new value.
//                // We do this as there can be circumstances where there are
//                // multiple instances of a value in the ComboBox items list,
//                // and if we don't check here we may change the selection
//                // mistakenly because the indexOf above will return the first
//                // instance always, and selection may be on the second or
//                // later instances. This is RT-19227.
//                T selectedItem = sm.getSelectedItem();
//                if (selectedItem == null || ! selectedItem.equals(getValue())) {
//                    sm.clearAndSelect(index);
//                }
//            }
        });
        
        editableProperty().addListener(o -> {
            // when editable changes, we reset the selection / value states
            // PENDING JW: why?
            getSelectionModel().clearSelection();
        });
    }
    
    private BeforeShowingState<T> beforeShowingState;

    /**
     * Overridden to fix RT-22572: keep the fix localized at
     * the use-case where it is needed, that is only 
     * when populating the items while opening the popup.
     * 
     */
    @Override
    public void show() {
        beforeShown();
        super.show();
        afterShown();
    }

    /**
     * Updates value to newValue if selectionState is null and
     * value not bound. Does nothing otherwise.
     * 
     * This method is called from change listener to 
     * <code>selectedItemProperty()</code>
     * 
     * @param newValue the new value of selectedItem.
     */
    protected void selectedItemChanged(T newValue) {
        if (beforeShowingState != null) return;
        if (! valueProperty().isBound()) {
            setValue(newValue);
        }
    }

    /**
     * Stores selectionState.
     */
    protected void beforeShown() {
        if (getSelectionModel() == null) return;
        beforeShowingState = new BeforeShowingState<>(getSelectionModel().getSelectedIndex(), 
                getSelectionModel().getSelectedItem(), getItems());
    }

    /**
     * Re-selects old value if selectedItem had been changed while
     * opening popup. Clears selectionState.
     * 
     * PENDING JW: 
     * <li> initial (after startup) selection in the list, 
     * <li> update that item dynamically while opening
     * <li> without the (debug) access to listCellText: value shows changed item
     *   and changed item selected in dropdown
     * <li> with (debug) access to listCellText: value shows old item (correct)
     *   and changed item selected in dropdown
     *   
     * at any later time, the selection is cleared as expected   
     * 
     * as of 8u40b7, all fine without debug access, except for focus rect
     * being on first item  
     *
     */
    protected void afterShown() {
        if (beforeShowingState != null) {
            T oldSelected = beforeShowingState.selectedItem;
            beforeShowingState = null;
            if (oldSelected != getSelectionModel().getSelectedItem()) {
                getSelectionModel().select(getValue());
            }
        }
    }
    
    
    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/
    



    /**
     * Data dump of selection state.
     * PENDING JW: Storing all, just in case. Re-visit.
     */
    protected static class BeforeShowingState<T> {
    
        private final int selectedIndex;
        private final T selectedItem;
        private final ObservableList<T> items;
    
        public BeforeShowingState(int selectedIndex, T selectedItem,
                ObservableList<T> items) {
            this.selectedIndex = selectedIndex;
            this.selectedItem = selectedItem;
            this.items = items != null ? FXCollections.unmodifiableObservableList(items) : null;
        }
    
        @Override
        public String toString() {
            return "[index: " + selectedIndex + " item: " + selectedItem + " items: " + items;
        }
    }

    // --- items
    /**
     * The list of items to show within the ComboBox popup.
     */
    private ObjectProperty<ObservableList<T>> items = new SimpleObjectProperty<ObservableList<T>>(this, "items") {
        @Override protected void invalidated() {
            // KEEP until regression testing done
            // CHANGED JW: removed hack
            // FIXME temporary fix for RT-15793. This will need to be
            // properly fixed when time permits
//            if (getSelectionModel() instanceof ComboBoxSelectionModel) {
//                ((ComboBoxSelectionModel<T>)getSelectionModel()).updateItemsObserver(null, getItems());
//            }
//            if (getSkin() instanceof ComboBoxListViewSkin) {
//                ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) getSkin();
//                skin.updateListViewItems();
//            }
        }
    };

    public final void setItems(ObservableList<T> value) { itemsProperty().set(value); }
    public final ObservableList<T> getItems() {return items.get(); }
    public ObjectProperty<ObservableList<T>> itemsProperty() { return items; }
    
    // CHANGED JW: added itmesListProperty
    // PENDING JW: hacking around 15793 - currently done in constructor, use adapter 
    // insttead
    private ListProperty<T> itemsList = new SimpleListProperty<T>(this, "itemsList") {
        {
//            bindBidirectional(items);
//            // PENDING JW: here the hack is working - not in BugPropertyAdapter: why not? 
//            // the other is an adapter, here we do it ourselves?
//            InvalidationListener hack15793 = o -> {
//                ObservableList<T> newItems = ((ObjectProperty<ObservableList<T>>) o).get();
//                ObservableList<T> oldItems = get();
//                boolean changedEquals = (newItems != null) && (oldItems != null) && newItems.equals(oldItems);
//                if (changedEquals) {
//                    set(newItems);
//                }
//            };
//            items.addListener(hack15793);
        }
    };
    public final ListProperty<T> itemsListProperty() {return itemsList;}; 

    // --- string converter
    /**
     * Converts the user-typed input (when the ComboBox is 
     * {@link #editableProperty() editable}) to an object of type T, such that 
     * the input may be retrieved via the  {@link #valueProperty() value} property.
     */
    public ObjectProperty<StringConverter<T>> converterProperty() { return converter; }
    private ObjectProperty<StringConverter<T>> converter = 
            new SimpleObjectProperty<StringConverter<T>>(this, "converter", ComboBoxX.<T>defaultStringConverter());
    public final void setConverter(StringConverter<T> value) { converterProperty().set(value); }
    public final StringConverter<T> getConverter() {return converterProperty().get(); }
    
    
    // --- cell factory
    /**
     * Providing a custom cell factory allows for complete customization of the
     * rendering of items in the ComboBox. Refer to the {@link Cell} javadoc
     * for more information on cell factories.
     */
    private ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactory = 
            new SimpleObjectProperty<Callback<ListView<T>, ListCell<T>>>(this, "cellFactory");
    public final void setCellFactory(Callback<ListView<T>, ListCell<T>> value) { cellFactoryProperty().set(value); }
    public final Callback<ListView<T>, ListCell<T>> getCellFactory() {return cellFactoryProperty().get(); }
    public ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactoryProperty() { return cellFactory; }
    
    
    // --- button cell
    /**
     * The button cell is used to render what is shown in the ComboBox 'button'
     * area. If a cell is set here, it does not change the rendering of the
     * ComboBox popup list - that rendering is controlled via the 
     * {@link #cellFactoryProperty() cell factory} API.
     * @since JavaFX 2.2
     */
    public ObjectProperty<ListCell<T>> buttonCellProperty() { return buttonCell; }
    private ObjectProperty<ListCell<T>> buttonCell = 
            new SimpleObjectProperty<ListCell<T>>(this, "buttonCell");
    public final void setButtonCell(ListCell<T> value) { buttonCellProperty().set(value); }
    public final ListCell<T> getButtonCell() {return buttonCellProperty().get(); }
    
    
    // --- Selection Model
    /**
     * The selection model for the ComboBox. A ComboBox only supports
     * single selection.
     */
    private ObjectProperty<SingleSelectionModel<T>> selectionModel = 
            new SimpleObjectProperty<SingleSelectionModel<T>>(this, "selectionModel");
    public final void setSelectionModel(SingleSelectionModel<T> value) { selectionModel.set(value); }
    public final SingleSelectionModel<T> getSelectionModel() { return selectionModel.get(); }
    public final ObjectProperty<SingleSelectionModel<T>> selectionModelProperty() { return selectionModel; }
    

    /**
     * The selection model for the ChoiceBox. Only a single choice can be made,
     * hence, the ChoiceBox supports only a SingleSelectionModel. Generally, the
     * main interaction with the selection model is to explicitly set which item
     * in the items list should be selected, or to listen to changes in the
     * selection to know which item has been chosen.
     * 
     * CHANGED JW: just a simple property, re-wiring listeners is done in PathAdapter.
     */
    
    private PathAdapter<SingleSelectionModel<T>, T> selectedItemPath;
    
    // CHANGED JW: listener registered on path
    private ChangeListener<T> selectedItemListener = (ov, t, t1) -> {
        selectedItemChanged(t1);
    };



    // --- Visible Row Count
    /**
     * The maximum number of rows to be visible in the ComboBox popup when it is
     * showing. By default this value is 10, but this can be changed to increase
     * or decrease the height of the popup.
     */
    private IntegerProperty visibleRowCount
            = new SimpleIntegerProperty(this, "visibleRowCount", 10);
    public final void setVisibleRowCount(int value) { visibleRowCount.set(value); }
    public final int getVisibleRowCount() { return visibleRowCount.get(); }
    public final IntegerProperty visibleRowCountProperty() { return visibleRowCount; }
    
    
    // --- Editor
    private TextField textField;
    /**
     * The editor for the ComboBox. The editor is null if the ComboBox is not
     * {@link #editableProperty() editable}.
     * @since JavaFX 2.2
     */
    private ReadOnlyObjectWrapper<TextField> editor;
    public final TextField getEditor() { 
        return editorProperty().get(); 
    }
    public final ReadOnlyObjectProperty<TextField> editorProperty() { 
        if (editor == null) {
            editor = new ReadOnlyObjectWrapper<TextField>(this, "editor");
            textField = new ComboBoxListViewSkin.FakeFocusTextField();
            editor.set(textField);
        }
        return editor.getReadOnlyProperty(); 
    }

    
    // --- Placeholder Node
    private ObjectProperty<Node> placeholder;
    /**
     * This Node is shown to the user when the ComboBox has no content to show.
     * The placeholder node is shown in the ComboBox popup area
     * when the items list is null or empty.
     * @since JavaFX 8.0
     */
    public final ObjectProperty<Node> placeholderProperty() {
        if (placeholder == null) {
            placeholder = new SimpleObjectProperty<Node>(this, "placeholder");
        }
        return placeholder;
    }
    public final void setPlaceholder(Node value) {
        placeholderProperty().set(value);
    }
    public final Node getPlaceholder() {
        return placeholder == null ? null : placeholder.get();
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * Methods                                                                 *
     *                                                                         *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new ComboBoxXListViewSkin<T>(this);
    }
     
    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    private static final String DEFAULT_STYLE_CLASS = "combo-box";
    
    /***************************************************************************
     *                                                                         *
     * Accessibility handling                                                  *
     *                                                                         *
     **************************************************************************/

//    /** @treatAsPrivate */
//    @Override
//    public Object accGetAttribute(Attribute attribute, Object... parameters) {
//        switch(attribute) {
//            case ROLE: return Role.COMBOBOX;
//            case TITLE:
//                //let the skin first.
//                Object title = super.accGetAttribute(attribute, parameters);
//                if (title != null) return title;
//                StringConverter<T> converter = getConverter();
//                if (converter == null) {
//                    return getValue() != null ? getValue().toString() : "";
//                }
//                return converter.toString(getValue());
//            default: return super.accGetAttribute(attribute, parameters);
//        }
//    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ComboBoxX.class
            .getName());
}
