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
import de.swingempire.fx.util.FXUtils;

/**
 * 
 * C&P'ed core to experiment with cleanup ideas.
 * 
 * Experimentations
 * - add and make use of ListProperty for items
 * - cleanup selection model: make it obey its class invariant always,
 *   react to items changes correctly and make it extendable 
 * - let the model be in full control about its own state, remove
 *   all external bowel interference
 * - fix the fix for dynamic items while opening popup: ignore
 *   selection changes while in process of opening
 * - use SingleMultipleSelection (slave of combo's selectionModel)
 *   in list in popup
 * - bind list's itemProperty to combo's itemProperty         
 * 
 * Changes:
 * - replaced manual wiring to items property by itemsListProperty
 *   (kept itemsProperty, bound bidi to itemsListProperty)
 * - replaced manual wiring to selectionModel.selectedItemProperty by PathAdapter
 * - fixed regression RT-22572/22937 (orig hacked in selectedItemListener)
 *   is: keep selectedItem while opening popup 
 * - solved 22572 in show: don't update value if selection changed in
 *   the course of showing, instead revert selectedItem to value at its end   
 * - TODO regression testing RT-19227 (orig hacked in listener to valueProperty
 *   is: multiple instances in list (what's the use-case?)
 * - TODO regression testing RT-15793 (orig hacked in itemsContentListener)
 *   is: missing notification on setting equals but not same list
 *   waiting for core fix of listProperty notification
 * - replaced list change handling, doc'ed behaviour 
 * - removed interference of ComboBox into inner bowels of selectionModel
 * - fixed selectionModel select(Object) to not break class invariant
 * - TODO fix editing (broken during re-implement of skin)
 * - TODO fully cleanup skin
 * - use converter for null/empty selected item (if there's not prompt)
 * 
 * ------------------------ original api doc below
 * An implementation of the {@link ComboBoxBase} abstract class for the most common
 * form of ComboBox, where a popup list is shown to users providing them with
 * a choice that they may select from. For more information around the general
 * concepts and API of ComboBox, refer to the {@link ComboBoxBase} class 
 * documentation.
 * 
 * <p>On top of ComboBoxBase, the ComboBox class introduces additional API. Most
 * importantly, it adds an {@link #itemsProperty() items} property that works in
 * much the same way as the ListView {@link ListView#itemsProperty() items}
 * property. In other words, it is the content of the items list that is displayed
 * to users when they click on the ComboBox button.
 *
 * <p>The ComboBox exposes the {@link #valueProperty()} from
 * {@link javafx.scene.control.ComboBoxBase}, but there are some important points
 * of the value property that need to be understood in relation to ComboBox.
 * These include:
 *
 * <ol>
 *     <li>The value property <strong>is not</strong> constrained to items contained
 *     within the items list - it can be anything as long as it is a valid value
 *     of type T.</li>
 *     <li>If the value property is set to a non-null object, and subsequently the
 *     items list is cleared, the value property <strong>is not</strong> nulled out.</li>
 *     <li>Clearing the {@link javafx.scene.control.SelectionModel#clearSelection()
 *     selection} in the selection model <strong>does not</strong> null the value
 *     property - it remains the same as before.</li>
 *     <li>It is valid for the selection model to have a selection set to a given
 *     index even if there is no items in the list (or less items in the list than
 *     the given index). Once the items list is further populated, such that the
 *     list contains enough items to have an item in the given index, both the
 *     selection model {@link SelectionModel#selectedItemProperty()} and
 *     value property will be updated to have this value. This is inconsistent with
 *     other controls that use a selection model, but done intentionally for ComboBox.</li>
 * </ol>
 * 
 * <p>By default, when the popup list is showing, the maximum number of rows
 * visible is 10, but this can be changed by modifying the 
 * {@link #visibleRowCountProperty() visibleRowCount} property. If the number of
 * items in the ComboBox is less than the value of <code>visibleRowCount</code>,
 * then the items size will be used instead so that the popup list is not
 * exceedingly long.
 * 
 * <p>As with ListView, it is possible to modify the 
 * {@link javafx.scene.control.SelectionModel selection model} that is used, 
 * although this is likely to be rarely changed. This is because the ComboBox
 * enforces the need for a {@link javafx.scene.control.SingleSelectionModel} 
 * instance, and it is not likely that there is much need for alternate 
 * implementations. Nonetheless, the option is there should use cases be found 
 * for switching the selection model.
 * 
 * <p>As the ComboBox internally renders content with a ListView, API exists in
 * the ComboBox class to allow for a custom cell factory to be set. For more
 * information on cell factories, refer to the {@link Cell} and {@link ListCell}
 * classes. It is important to note that if a cell factory is set on a ComboBox,
 * cells will only be used in the ListView that shows when the ComboBox is 
 * clicked. If you also want to customize the rendering of the 'button' area
 * of the ComboBox, you can set a custom {@link ListCell} instance in the 
 * {@link #buttonCellProperty() button cell} property. One way of doing this
 * is with the following code (note the use of {@code setButtonCell}:
 * 
 * <pre>
 * {@code
 * Callback<ListView<String>, ListCell<String>> cellFactory = ...;
 * ComboBox comboBox = new ComboBox();
 * comboBox.setItems(items);
 * comboBox.setButtonCell(cellFactory.call(null));
 * comboBox.setCellFactory(cellFactory);}</pre>
 * 
 * <p>Because a ComboBox can be {@link #editableProperty() editable}, and the
 * default means of allowing user input is via a {@link TextField}, a 
 * {@link #converterProperty() string converter} property is provided to allow
 * for developers to specify how to translate a users string into an object of
 * type T, such that the {@link #valueProperty() value} property may contain it.
 * By default the converter simply returns the String input as the user typed it,
 * which therefore assumes that the type of the editable ComboBox is String. If 
 * a different type is specified and the ComboBox is to be editable, it is 
 * necessary to specify a custom {@link StringConverter}.
 * 
 * <h3>A warning about inserting Nodes into the ComboBox items list</h3>
 * ComboBox allows for the items list to contain elements of any type, including 
 * {@link Node} instances. Putting nodes into 
 * the items list is <strong>strongly not recommended</strong>. This is because 
 * the default {@link #cellFactoryProperty() cell factory} simply inserts Node 
 * items directly into the cell, including in the ComboBox 'button' area too. 
 * Because the scenegraph only allows for Nodes to be in one place at a time, 
 * this means that when an item is selected it becomes removed from the ComboBox
 * list, and becomes visible in the button area. When selection changes the 
 * previously selected item returns to the list and the new selection is removed.
 * 
 * <p>The recommended approach, rather than inserting Node instances into the 
 * items list, is to put the relevant information into the ComboBox, and then
 * provide a custom {@link #cellFactoryProperty() cell factory}. For example,
 * rather than use the following code:
 * 
 * <pre>
 * {@code
 * ComboBox<Rectangle> cmb = new ComboBox<Rectangle>();
 * cmb.getItems().addAll(
 *     new Rectangle(10, 10, Color.RED), 
 *     new Rectangle(10, 10, Color.GREEN), 
 *     new Rectangle(10, 10, Color.BLUE));}</pre>
 * 
 * <p>You should do the following:</p>
 * 
 * <pre><code>
 * ComboBox&lt;Color&gt; cmb = new ComboBox&lt;Color&gt;();
 * cmb.getItems().addAll(
 *     Color.RED,
 *     Color.GREEN,
 *     Color.BLUE);
 *
 * cmb.setCellFactory(new Callback&lt;ListView&lt;Color&gt;, ListCell&lt;Color&gt;&gt;() {
 *     &#064;Override public ListCell&lt;Color&gt; call(ListView&lt;Color&gt; p) {
 *         return new ListCell&lt;Color&gt;() {
 *             private final Rectangle rectangle;
 *             { 
 *                 setContentDisplay(ContentDisplay.GRAPHIC_ONLY); 
 *                 rectangle = new Rectangle(10, 10);
 *             }
 *             
 *             &#064;Override protected void updateItem(Color item, boolean empty) {
 *                 super.updateItem(item, empty);
 *                 
 *                 if (item == null || empty) {
 *                     setGraphic(null);
 *                 } else {
 *                     rectangle.setFill(item);
 *                     setGraphic(rectangle);
 *                 }
 *            }
 *       };
 *   }
 *});</code></pre>
 * 
 * <p>Admittedly the above approach is far more verbose, but it offers the 
 * required functionality without encountering the scenegraph constraints.
 * 
 * @see ComboBoxBase
 * @see Cell
 * @see ListCell
 * @see StringConverter
 * @since JavaFX 2.1
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

        setItems(items);
        setSelectionModel(new ComboBoxXSelectionModel<T>(this));
        // KEEP JW: original comment
        // listen to the value property input by the user, and if the value is
        // set to something that exists in the items list, we should update the
        // selection model to indicate that this is the selected item
        valueProperty().addListener((ov, t, t1) -> {
            // CHANGED JW: copied code from ChoiceBoxX
            final SingleSelectionModel<T> sm = getSelectionModel();
            if (sm != null) {
                sm.select(t1);
            }
            // KEEP until regression testing done
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
            getSelectionModel().clearSelection();
        });
    }
    
    /**
     * Overridden to fix RT-22572: keep the fix localized at
     * the use-case where it is needed, that is only 
     * when populating the items when showing.
     * 
     */
    @Override
    public void show() {
        beforeShown();
        super.show();
        afterShown();
    }

    /**
     * Data dump of selection state.
     * PENDING JW: Storing all, just in case. Re-visit.
     */
    protected static class ItemsSelectionState<T> {

        private int selectedIndex;
        private T selectedItem;
        private ObservableList<T> items;

        public ItemsSelectionState(int selectedIndex, T selectedItem,
                ObservableList<T> items) {
            this.selectedIndex = selectedIndex;
            this.selectedItem = selectedItem;
            this.items = items != null ? FXCollections.observableArrayList(items) : null;
        }

        @Override
        public String toString() {
            return "[index: " + selectedIndex + " item: " + selectedItem + " items: " + items;
        }
    }

    private ItemsSelectionState<T> selectionState;
    
    /**
     * Updates value to newValue if selectionState is null and
     * value not bound. Does nothing otherwise.
     * @param newValue
     */
    private void updateValue(T newValue) {
        if (selectionState != null) return;
        if (! valueProperty().isBound()) {
            setValue(newValue);
        }
    }

    /**
     * Stores selectionState.
     */
    protected void beforeShown() {
        if (getSelectionModel() == null) return;
        selectionState = new ItemsSelectionState<>(getSelectionModel().getSelectedIndex(), 
                getSelectionModel().getSelectedItem(), getItems());
    }

    /**
     * Re-selects old value if selectedItem had been changed while
     * opening popup. Clears selectionState.
     */
    protected void afterShown() {
        if (selectionState != null) {
            T oldSelected = selectionState.selectedItem;
            selectionState = null;
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
    private ListProperty<T> itemsList = new SimpleListProperty<>(this, "itemsList");
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
        updateValue(t1);
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
