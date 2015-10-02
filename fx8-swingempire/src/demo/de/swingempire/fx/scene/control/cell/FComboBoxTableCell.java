/*
 * Created on 01.10.2015
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Cell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.StringConverter;


/**
 * @author Jeanette Winzenburg, Berlin
 */
public class FComboBoxTableCell<S, T> extends TableCell<S, T> {

    private final ObservableList<T> items;

    private ComboBox<T> comboBox;

    private boolean ignoreCommit;

    private ChangeListener<T> selectionListener;
    /**
     * Creates a default {@link ComboBoxTableCell} instance with the given items
     * being used to populate the {@link ComboBox} when it is shown.
     * 
     * @param items The items to show in the ComboBox popup menu when selected 
     *      by the user.
     */
    @SafeVarargs
    public FComboBoxTableCell(T... items) {
        this(FXCollections.observableArrayList(items));
    }

    /**
     * Creates a default {@link ComboBoxTableCell} instance with the given items
     * being used to populate the {@link ComboBox} when it is shown.
     * 
     * @param items The items to show in the ComboBox popup menu when selected 
     *      by the user.
     */
    public FComboBoxTableCell(ObservableList<T> items) {
        this(null, items);
    }

    /**
     * Creates a {@link ComboBoxTableCell} instance with the given items
     * being used to populate the {@link ComboBox} when it is shown, and the 
     * {@link StringConverter} being used to convert the item in to a 
     * user-readable form.
     * 
     * @param converter A {@link StringConverter} that can convert an item of type T 
     *      into a user-readable string so that it may then be shown in the 
     *      ComboBox popup menu.
     * @param items The items to show in the ComboBox popup menu when selected 
     *      by the user.
     */
    public FComboBoxTableCell(StringConverter<T> converter, ObservableList<T> items) {
        this.getStyleClass().add("combo-box-table-cell");
        this.items = items;
        setConverter(converter != null ? converter : CellUtils.<T>defaultStringConverter());
        selectionListener = (source, ov, nv) -> {
            if (isEditing()) {
                source.removeListener(selectionListener);
                commitEdit(nv);
            }
        };
    }
    
    /**
     * Returns the items to be displayed in the ChoiceBox when it is showing.
     */
    public ObservableList<T> getItems() {
        return items;
    }    
    
    /** {@inheritDoc} */
    @Override 
    public void startEdit() {
        if (! isEditable() || ! getTableView().isEditable() || ! getTableColumn().isEditable()) {
            return;
        }
        
        if (comboBox == null) {
            comboBox = createComboBox(this, items, converterProperty());
//            comboBox.editableProperty().bind(comboBoxEditableProperty());
        }
        
        comboBox.getSelectionModel().select(getItem());
        
        super.startEdit();
        setText(null);
        setGraphic(comboBox);
        comboBox.getSelectionModel().selectedItemProperty().addListener(selectionListener);
        /* listening to selectedItem: stacktrace on remove last item in 
         Exception in thread "JavaFX Application Thread" java.lang.IndexOutOfBoundsException
        at com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList.subList(ReadOnlyUnbackedObservableList.java:136)
        at javafx.collections.ListChangeListener$Change.getAddedSubList(ListChangeListener.java:242)
        at com.sun.javafx.scene.control.behavior.ListViewBehavior.lambda$new$177(ListViewBehavior.java:269)
        at javafx.collections.WeakListChangeListener.onChanged(WeakListChangeListener.java:88)
        at com.sun.javafx.collections.ListListenerHelper$Generic.fireValueChangedEvent(ListListenerHelper.java:329)
        at com.sun.javafx.collections.ListListenerHelper.fireValueChangedEvent(ListListenerHelper.java:73)
        at com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList.callObservers(ReadOnlyUnbackedObservableList.java:75)
        at javafx.scene.control.MultipleSelectionModelBase.clearAndSelect(MultipleSelectionModelBase.java:378)
        at javafx.scene.control.ListView$ListViewBitSetSelectionModel.clearAndSelect(ListView.java:1403)
        at com.sun.javafx.scene.control.behavior.CellBehaviorBase.simpleSelect(CellBehaviorBase.java:256)
        at com.sun.javafx.scene.control.behavior.CellBehaviorBase.doSelect(CellBehaviorBase.java:220)
        at com.sun.javafx.scene.control.behavior.CellBehaviorBase.mousePressed(CellBehaviorBase.java:150)
        at com.sun.javafx.scene.control.skin.BehaviorSkinBase$1.handle(BehaviorSkinBase.java:95)
        at com.sun.javafx.scene.control.skin.BehaviorSkinBase$1.handle(BehaviorSkinBase.java:89)
 
         */
//        comboBox.valueProperty().addListener(selectionListener);
        /* listening to value: Stacktrace on removing last element in edit commit handler
         Exception in thread "JavaFX Application Thread" java.lang.IndexOutOfBoundsException
        at com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList.subList(ReadOnlyUnbackedObservableList.java:136)
        at javafx.collections.ListChangeListener$Change.getAddedSubList(ListChangeListener.java:242)
        at com.sun.javafx.scene.control.behavior.ListViewBehavior.lambda$new$177(ListViewBehavior.java:269)
        at javafx.collections.WeakListChangeListener.onChanged(WeakListChangeListener.java:88)
        at com.sun.javafx.collections.ListListenerHelper$Generic.fireValueChangedEvent(ListListenerHelper.java:329)
        at com.sun.javafx.collections.ListListenerHelper.fireValueChangedEvent(ListListenerHelper.java:73)
        at com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList.callObservers(ReadOnlyUnbackedObservableList.java:75)
        at javafx.scene.control.MultipleSelectionModelBase.clearAndSelect(MultipleSelectionModelBase.java:378)
        at javafx.scene.control.ListView$ListViewBitSetSelectionModel.clearAndSelect(ListView.java:1403)
        at com.sun.javafx.scene.control.behavior.CellBehaviorBase.simpleSelect(CellBehaviorBase.java:256)
        at com.sun.javafx.scene.control.behavior.CellBehaviorBase.doSelect(CellBehaviorBase.java:220)
        at com.sun.javafx.scene.control.behavior.CellBehaviorBase.mousePressed(CellBehaviorBase.java:150)
        at com.sun.javafx.scene.control.skin.BehaviorSkinBase$1.handle(BehaviorSkinBase.java:95)
        at com.sun.javafx.scene.control.skin.BehaviorSkinBase$1.handle(BehaviorSkinBase.java:89)

         */
    }

    /** {@inheritDoc} */
    @Override public void cancelEdit() {
        super.cancelEdit();
        
        setText(getConverter().toString(getItem()));
        setGraphic(null);
    }
    
    /** {@inheritDoc} */
    @Override public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        CellUtils.updateItem(this, getConverter(), null, null, comboBox);
    }

    private <T> ComboBox<T> createComboBox(final Cell<T> cell,
            final ObservableList<T> items,
            final ObjectProperty<StringConverter<T>> converter) {
        ComboBox<T> comboBox = new ComboBox<T>(items);
        comboBox.converterProperty().bind(converter);
        comboBox.setMaxWidth(Double.MAX_VALUE);
//        comboBox.getSelectionModel().selectedItemProperty()
//        comboBox.valueProperty()
//                .addListener((ov, oldValue, newValue) -> {
//                    if (cell.isEditing()) {
//                        if (ignoreCommit) {
//                            return;
//                        }
//                        ignoreCommit = true;
//                        cell.commitEdit(newValue);
//                        ignoreCommit = false;
//                    }
//                });
        return comboBox;
    }


    // --- converter
    private ObjectProperty<StringConverter<T>> converter = 
            new SimpleObjectProperty<StringConverter<T>>(this, "converter");

    /**
     * The {@link StringConverter} property.
     */
    public final ObjectProperty<StringConverter<T>> converterProperty() { 
        return converter; 
    }
    
    /** 
     * Sets the {@link StringConverter} to be used in this cell.
     */
    public final void setConverter(StringConverter<T> value) { 
        converterProperty().set(value); 
    }
    
    /**
     * Returns the {@link StringConverter} used in this cell.
     */
    public final StringConverter<T> getConverter() { 
        return converterProperty().get(); 
    }
    
    

}
