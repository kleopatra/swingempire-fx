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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

import com.sun.javafx.scene.control.skin.ComboBoxPopupControl;
import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.VirtualContainerBase;
import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;

import de.swingempire.fx.property.PathAdapter;


//import javafx.scene.accessibility.Attribute;

/**
 * C&P from core and cleaned.
 * 
 * Changes: 
 * <li> use stringconverter for null if prompt is empty 
 * <li> set ListView's selectionModel to
 * SingleMultipleSelectionModel which is coupled to combo's selectinModel -
 * <li> TODO: commented content of updateValue - needs to be solved cleanly 
 * <li> bound ListView's items to comboBox items 
 * <li> TODO: support editing (throws
 * classcastexception, probably need to c&p some more) 
 * <li> removed selection
 * update in layout 
 * <li> fixed keyboard navigation broken - slave must have access
 * to focusModel!
 * <li> outdated: encapsulate comboBoxItems (keep null substitution) 
 * <li> removed updateListViewItems/updateComboBoxItems 
 * <li> TODO: verify that updateListViewItems is replaced by listening to itemsProperty
 * <li> re-enabled popup layout by invoking super methods as needed 
 * 
 * PENDING:
 * adjust to pulled-up (into ComboBoxPopupControl/Base) methods 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboBoxXListViewSkin<T> extends ComboBoxPopupControl<T> {

    // By default we measure the width of all cells in the ListView. If this
    // is too burdensome, the developer may set a property in the ComboBox
    // properties map with this key to specify the number of rows to measure.
    // This may one day become a property on the ComboBox itself.
    private static final String COMBO_BOX_ROWS_TO_MEASURE_WIDTH_KEY = "comboBoxRowsToMeasureWidth";

    /***************************************************************************
     * * Private fields * *
     **************************************************************************/

    private final ComboBoxX<T> comboBox;

//    private ObservableList<T> comboBoxItems;

    private ListCell<T> buttonCell;

    private Callback<ListView<T>, ListCell<T>> cellFactory;

    private TextField textField;

    private final ListView<T> listView;

//    private ObservableList<T> listViewItems;

    // PENDING JW: these two shouldn't be necessary: handled by slave model
    private boolean listSelectionLock = false;

    private boolean listViewSelectionDirty = false;

    /***************************************************************************
     * * Listeners * *
     **************************************************************************/

    private boolean itemCountDirty;

    private final ListChangeListener<T> listViewItemsListener = new ListChangeListener<T>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends T> c) {
            itemCountDirty = true;
            getSkinnable().requestLayout();
            // RT-37622: popup width not updated
            // doesn't help
//            invokeReconfigurePopup();
            // doesn't help
//            getListView().requestLayout();
            // doesn't help
//            Platform.runLater(() -> {
//                
//                if (listView instanceof ComboBoxXListViewSkin.ComboListView) {
//                    ComboListView view = (ComboBoxXListViewSkin<T>.ComboListView) listView;
//                    view.invokeUpdateRowCount((ListViewSkin<?>) view.getSkin());
//                    view.setPrefWidth(view.computePrefWidth(-1));
//                    listView.requestLayout();
//                }
//            });
            
        }
    };

    private final WeakListChangeListener<T> weakListViewItemsListener = new WeakListChangeListener<T>(
            listViewItemsListener);

    private EventHandler<KeyEvent> textFieldKeyEventHandler = event -> {
        if (textField == null || !getSkinnable().isEditable())
            return;
        handleKeyEvent(event, true);
    };

    private EventHandler<MouseEvent> textFieldMouseEventHandler = event -> {
        ComboBoxBase<T> comboBox = getSkinnable();
        if (event.getTarget().equals(comboBox))
            return;
        comboBox.fireEvent(event.copyFor(comboBox, comboBox));
        event.consume();
    };

    private EventHandler<DragEvent> textFieldDragEventHandler = event -> {
        ComboBoxBase<T> comboBox = getSkinnable();
        if (event.getTarget().equals(comboBox))
            return;
        comboBox.fireEvent(event.copyFor(comboBox, comboBox));
        event.consume();
    };

    /***************************************************************************
     * * Constructors * *
     **************************************************************************/

    public ComboBoxXListViewSkin(final ComboBoxX<T> comboBox) {
        super(comboBox, new ComboBoxXListViewBehavior<T>(comboBox));
        this.comboBox = comboBox;
//        updateComboBoxItems();

        // editable input node
        this.textField = comboBox.isEditable() ? getEditableInputNode() : null;

        // Fix for RT-29565. Without this the textField does not have a correct
        // pref width at startup, as it is not part of the scenegraph (and
        // therefore
        // has no pref width until after the first measurements have been
        // taken).
        if (this.textField != null) {
            getChildren().add(textField);
        }

        // listview for popup
        this.listView = createListView();
        // add the listener to update row in list?
        comboBox.itemsListProperty().addListener(weakListViewItemsListener);
        
        // Fix for RT-21207. Additional code related to this bug is further
        // below.
        this.listView.setManaged(false);
        getChildren().add(listView);
        // -- end of fix

//        updateListViewItems();
        updateCellFactory();

        updateButtonCell();

        // move fake focus in to the textfield if the comboBox is editable
        comboBox.focusedProperty().addListener((ov, t, hasFocus) -> {
            if (comboBox.isEditable()) {
                // Note JW: must use FakeFocus type of core skin
                // Fix for the regression noted in a comment in RT-29885.
                ((FakeFocusTextField) textField).setFakeFocus(hasFocus);
            }
        });

        comboBox.addEventFilter(KeyEvent.ANY, ke -> {
            if (textField == null || !comboBox.isEditable()) {
                handleKeyEvent(ke, false);
            } else {
                // This prevents a stack overflow from our rebroadcasting of the
                // event to the textfield that occurs in the final else
                // statement
                // of the conditions below.
                if (ke.getTarget().equals(textField))
                    return;

                // Fix for the regression noted in a comment in RT-29885.
                // This forwards the event down into the TextField when
                // the key event is actually received by the ComboBox.
                textField.fireEvent(ke.copyFor(textField, textField));
                ke.consume();
            }
        });

        updateEditable();

        // Fix for RT-19431 (also tested via ComboBoxListViewSkinTest)
        updateValue();
        updateDisplayNode();

        // Fix for regression: not displaying the correct value on dynamic 
        // update of items
        selectedIndexPath = new PathAdapter<SingleSelectionModel<T>, Number>(
                comboBox.selectionModelProperty(),  SingleSelectionModel::selectedIndexProperty);
        selectedIndexPath.addListener((p, old, value) -> updateDisplayNode());
        // Fix for RT-36902, where focus traversal was getting stuck inside the
        // ComboBox
        comboBox.setImpl_traversalEngine(new ParentTraversalEngine(comboBox, new Algorithm() {
            @Override public Node select(Node owner, Direction dir, TraversalContext context) {
                return null;
            }

            @Override public Node selectFirst(TraversalContext context) {
                return null;
            }

            @Override public Node selectLast(TraversalContext context) {
                return null;
            }
        }));

        // registerChangeListener(comboBox.itemsProperty(), "ITEMS");
        registerChangeListener(comboBox.promptTextProperty(), "PROMPT_TEXT");
        registerChangeListener(comboBox.cellFactoryProperty(), "CELL_FACTORY");
        registerChangeListener(comboBox.visibleRowCountProperty(),
                "VISIBLE_ROW_COUNT");
        registerChangeListener(comboBox.converterProperty(), "CONVERTER");
        registerChangeListener(comboBox.buttonCellProperty(), "BUTTON_CELL");
        registerChangeListener(comboBox.valueProperty(), "VALUE");
        registerChangeListener(comboBox.editableProperty(), "EDITABLE");
        
        
    }

    private PathAdapter<SingleSelectionModel<T>, Number> selectedIndexPath;
    /***************************************************************************
     * * Public API * *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override
    protected void handleControlPropertyChanged(String p) {
        // Fix for RT-21207
        if ("SHOWING".equals(p)) {
            if (getSkinnable().isShowing()) {
                this.listView.setManaged(true);
            } else {
                this.listView.setManaged(false);
            }
        }
        // -- end of fix

        super.handleControlPropertyChanged(p);

        if ("ITEMS".equals(p)) {
            // CHANGED JW: update handled in listener to itemsList property
            // updateComboBoxItems();
            // updateListViewItems();
        } else if ("PROMPT_TEXT".equals(p)) {
            updateDisplayNode();
        } else if ("CELL_FACTORY".equals(p)) {
            updateCellFactory();
        } else if ("VISIBLE_ROW_COUNT".equals(p)) {
            if (listView == null)
                return;
//            listView.setPrefHeight(getListViewPrefHeight());
            listView.requestLayout();
        } else if ("CONVERTER".equals(p)) {
            // PENDING JW: test the effect of removing updateListViewItems
            // was no-op in this context anyway
//            updateListViewItems();
        } else if ("EDITOR".equals(p)) {
            getEditableInputNode();
        } else if ("BUTTON_CELL".equals(p)) {
            updateButtonCell();
        } else if ("VALUE".equals(p)) {
            updateValue();
            // PENDING JW: trying to fix regression on dynamic list update
            // doesn't help
//            updateDisplayNode();
        } else if ("EDITABLE".equals(p)) {
            updateEditable();
        }
    }

    /**
     * PENDING JW: formal fix of scope, untested
     */
    @Override
    protected void updateEditable() {
        if (comboBox == null) return;
        TextField newTextField = comboBox.getEditor();
//        if (newTextField == null) return;
        if (!comboBox.isEditable()) {
            // remove event filters
            if (textField != null) {
                textField.removeEventFilter(KeyEvent.ANY,
                        textFieldKeyEventHandler);
                textField.removeEventFilter(MouseEvent.DRAG_DETECTED,
                        textFieldMouseEventHandler);
                textField.removeEventFilter(DragEvent.ANY,
                        textFieldDragEventHandler);
            }
        } else if (newTextField != null) {
            // add event filters
            newTextField.addEventFilter(KeyEvent.ANY, textFieldKeyEventHandler);

            // Fix for RT-31093 - drag events from the textfield were not
            // surfacing
            // properly for the ComboBox.
            newTextField.addEventFilter(MouseEvent.DRAG_DETECTED,
                    textFieldMouseEventHandler);
            newTextField.addEventFilter(DragEvent.ANY,
                    textFieldDragEventHandler);
        }

        textField = newTextField;
    }

    /** {@inheritDoc} */
    @Override
    public Node getDisplayNode() {
        Node displayNode;
        if (comboBox.isEditable()) {
            displayNode = getEditableInputNode();
        } else {
            displayNode = buttonCell;
        }

        updateDisplayNode();

        return displayNode;
    }

    /**
     * local alias with null substitution (emptyObservableList) Null
     * substitution happens only if client code explicitly nulls the items:
     * combo installs a arrayObservableList in its parameterless constructor
     */
//    private void updateComboBoxItems() {
//        this.comboBoxItems = comboBox.getItems();
//        this.comboBoxItems = getComboBoxItems() == null ? FXCollections
//                .<T> emptyObservableList() : getComboBoxItems();
//    }

    /**
     * @return the comboBoxItems
     */
    private ObservableList<T> getComboBoxItems() {
        return comboBox.getItems(); //comboBoxItems;
    }

    /**
     * PENDING JW: now bound to same items alias to listViewItems to local
     * comboBoxItems, that is listView.getItems == this.comboBoxItems (different
     * from combo.getItems if null) Listening to listViewItems, and the only
     * place where listViewItems is used
     */
//    public void updateListViewItems() {
//         if (listViewItems != null) {
//         listViewItems.removeListener(weakListViewItemsListener);
//         }
//        
//         this.listViewItems = getComboBoxItems();
//         listView.setItems(listViewItems);
//        
//         if (listViewItems != null) {
//         listViewItems.addListener(weakListViewItemsListener);
//         }
//        
//         itemCountDirty = true;
//         getSkinnable().requestLayout();
//    }

    @Override
    public Node getPopupContent() {
        return listView;
    }

    @Override
    protected double computeMinWidth(double height, double topInset,
            double rightInset, double bottomInset, double leftInset) {
        invokeReconfigurePopup();
        return 50;
    }

    /**
     * PENDING JW Hack around not-visible method, no-op for now
     */
    protected void invokeReconfigurePopup() {
        Class clazz = ComboBoxPopupControl.class;
        try {
            Method method = clazz.getDeclaredMethod("reconfigurePopup");
            method.setAccessible(true);
            method.invoke(this);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//         reconfigurePopup();
    }

    @Override
    protected double computePrefWidth(double height, double topInset,
            double rightInset, double bottomInset, double leftInset) {
        double superPrefWidth = super.computePrefWidth(height, topInset,
                rightInset, bottomInset, leftInset);
        double listViewWidth = listView.prefWidth(height);
        double pw = Math.max(superPrefWidth, listViewWidth);
        // PENDING JW: really reconfig after querying the pref width? 
        invokeReconfigurePopup();

        return pw;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset,
            double rightInset, double bottomInset, double leftInset) {
        invokeReconfigurePopup();
        return super.computeMaxWidth(height, topInset, rightInset, bottomInset,
                leftInset);
    }

    @Override
    protected double computeMinHeight(double width, double topInset,
            double rightInset, double bottomInset, double leftInset) {
        invokeReconfigurePopup();
        return super.computeMinHeight(width, topInset, rightInset, bottomInset,
                leftInset);
    }

    @Override
    protected double computePrefHeight(double width, double topInset,
            double rightInset, double bottomInset, double leftInset) {
        invokeReconfigurePopup();
        return super.computePrefHeight(width, topInset, rightInset,
                bottomInset, leftInset);
    }

    @Override
    protected double computeMaxHeight(double width, double topInset,
            double rightInset, double bottomInset, double leftInset) {
        invokeReconfigurePopup();
        return super.computeMaxHeight(width, topInset, rightInset, bottomInset,
                leftInset);
    }

    @Override
    protected void layoutChildren(final double x, final double y,
            final double w, final double h) {
        if (listViewSelectionDirty) {
            // try {
            // listSelectionLock = true;
            // T item = comboBox.getSelectionModel().getSelectedItem();
            // listView.getSelectionModel().clearSelection();
            // listView.getSelectionModel().select(item);
            // } finally {
            // listSelectionLock = false;
            // listViewSelectionDirty = false;
            // }
        }

        super.layoutChildren(x, y, w, h);
    }

    // Added to allow subclasses to prevent the popup from hiding when the
    // ListView is clicked on (e.g when the list cells have checkboxes).
    protected boolean isHideOnClickEnabled() {
        return true;
    }

    /***************************************************************************
     * * Private methods * *
     **************************************************************************/

    private void handleKeyEvent(KeyEvent ke, boolean doConsume) {
        // When the user hits the enter or F4 keys, we respond before
        // ever giving the event to the TextField.
        if (ke.getCode() == KeyCode.ENTER) {
            setTextFromTextFieldIntoComboBoxValue();

            if (doConsume)
                ke.consume();
        } else if (ke.getCode() == KeyCode.F4) {
            if (ke.getEventType() == KeyEvent.KEY_RELEASED) {
                if (comboBox.isShowing())
                    comboBox.hide();
                else
                    comboBox.show();
            }
            ke.consume(); // we always do a consume here (otherwise unit tests
                          // fail)
        } else if (ke.getCode() == KeyCode.F10
                || ke.getCode() == KeyCode.ESCAPE) {
            // RT-23275: The TextField fires F10 and ESCAPE key events
            // up to the parent, which are then fired back at the
            // TextField, and this ends up in an infinite loop until
            // the stack overflows. So, here we consume these two
            // events and stop them from going any further.
            if (doConsume)
                ke.consume();
        }
    }

    public void hacking() {
//        Node node = getDisplayNode();
//        if (node instanceof ListCell) {
//            ((ListCell) node).getText();
//        } 
//        if (comboBox.getSelectionModel().getSelectedItem() !=
//                listView.getFocusModel().getFocusedItem()) {
//            LOG.info("not same item:" + listView.getFocusModel().getFocusedItem());
//        }
    }
    /**
     * PENDING JW: removed all for now - listView selectionModel is coupled to
     * combo selectionModel, all should be automatic
     */
    private void updateValue() {
        T newValue = comboBox.getValue();
//        LOG.info("update value " + newValue);
//         SelectionModel<T> listViewSM = listView.getSelectionModel();
//         listViewSM.getSelectedIndex();
//         listViewSM.getSelectedItem();
//
//         DebugUtils.printSelectionState(listView);
//         DebugUtils.getDisplayText(comboBox);
//         listViewSM.select(comboBox.getSelectionModel().getSelectedIndex());
//         listViewSM.select(comboBox.getSelectionModel().getSelectedItem());
        // if (newValue == null) {
        // listViewSM.clearSelection();
        // } else {
        // // RT-22386: We need to test to see if the value is in the comboBox
        // // items list. If it isn't, then we should clear the listview
        // // selection
        // int indexOfNewValue = getIndexOfComboBoxValueInItemsList();
        // if (indexOfNewValue == -1) {
        // listSelectionLock = true;
        // listViewSM.clearSelection();
        // listSelectionLock = false;
        // } else {
        // int index = comboBox.getSelectionModel().getSelectedIndex();
        // if (index >= 0 && index < getComboBoxItems().size()) {
        // T itemsObj = getComboBoxItems().get(index);
        // if (itemsObj != null && itemsObj.equals(newValue)) {
        // listViewSM.select(index);
        // } else {
        // listViewSM.select(newValue);
        // }
        // } else {
        // // just select the first instance of newValue in the list
        // int listViewIndex = getComboBoxItems().indexOf(newValue);
        // if (listViewIndex == -1) {
        // // RT-21336 Show the ComboBox value even though it doesn't
        // // exist in the ComboBox items list (part one of fix)
        // updateDisplayNode();
        // } else {
        // listViewSM.select(listViewIndex);
        // }
        // }
        // }
        // }
    }

    private String initialTextFieldValue = null;

    /**
     * PENDING JW: formal fix of scope, untested
     */
    @Override
    protected TextField getEditableInputNode() {
        if (textField != null)
            return textField;

        textField = comboBox.getEditor();
        textField.focusTraversableProperty().bindBidirectional(
                comboBox.focusTraversableProperty());
        textField.promptTextProperty().bind(comboBox.promptTextProperty());
        textField.tooltipProperty().bind(comboBox.tooltipProperty());

        // Fix for RT-21406: ComboBox do not show initial text value
        initialTextFieldValue = textField.getText();
        // End of fix (see updateDisplayNode below for the related code)

        textField.focusedProperty().addListener((ov, t, hasFocus) -> {
            if (!comboBox.isEditable())
                return;

            // Fix for RT-29885
                comboBox.getProperties().put("FOCUSED", hasFocus);
                // --- end of RT-29885

                // RT-21454 starts here
                if (!hasFocus) {
                    setTextFromTextFieldIntoComboBoxValue();
                    pseudoClassStateChanged(CONTAINS_FOCUS_PSEUDOCLASS_STATE,
                            false);
                } else {
                    pseudoClassStateChanged(CONTAINS_FOCUS_PSEUDOCLASS_STATE,
                            true);
                }
                // --- end of RT-21454
            });

        return textField;
    }

    /**
     * PENDING JW: formal fix of scope, untested
     */
    @Override
    protected void updateDisplayNode() {
        // PENDING JW: as of 8u40b7, this is may be called too early
        // that is before installing the buttoncell
        // hacking around by silently returning
        if (buttonCell == null)
            return;
        StringConverter<T> c = comboBox.getConverter();
        if (c == null)
            return;

        T value = comboBox.getValue();
        if (comboBox.isEditable()) {
            if (initialTextFieldValue != null
                    && !initialTextFieldValue.isEmpty()) {
                // Remainder of fix for RT-21406: ComboBox do not show initial
                // text value
                textField.setText(initialTextFieldValue);
                initialTextFieldValue = null;
                // end of fix
            } else {
                String stringValue = c.toString(value);
                if (value == null || stringValue == null) {
                    textField.setText("");
                } else if (!stringValue.equals(textField.getText())) {
                    textField.setText(stringValue);
                }
            }
        } else {
            int index = getIndexOfComboBoxValueInItemsList();
            if (index > -1) {
                buttonCell.setItem(null);
                buttonCell.updateIndex(index);
            } else {
                // RT-21336 Show the ComboBox value even though it doesn't
                // exist in the ComboBox items list (part two of fix)
                buttonCell.updateIndex(-1);
                boolean empty = updateDisplayText(buttonCell, value, false);

                // Note that empty boolean collected above. This is used to
                // resolve
                // RT-27834, where we were getting different styling based on
                // whether
                // the cell was updated via the updateIndex method above, or
                // just
                // by directly updating the text. We fake the pseudoclass state
                // for empty, filled, and selected here.
                buttonCell.pseudoClassStateChanged(PSEUDO_CLASS_EMPTY, empty);
                buttonCell.pseudoClassStateChanged(PSEUDO_CLASS_FILLED, !empty);
                buttonCell.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, true);
            }
        }
    }

    // return a boolean to indicate that the cell is empty (and therefore not
    // filled)
    private boolean updateDisplayText(ListCell<T> cell, T item, boolean empty) {
        if (empty) {
            if (cell == null)
                return true;
            cell.setGraphic(null);
            cell.setText(null);
            return true;
        } else if (item instanceof Node) {
            Node currentNode = cell.getGraphic();
            Node newNode = (Node) item;
            if (currentNode == null || !currentNode.equals(newNode)) {
                cell.setText(null);
                cell.setGraphic(newNode);
            }
            return newNode == null;
        } else {
            // run item through StringConverter if it isn't null
            StringConverter<T> c = comboBox.getConverter();
            String s;
            if (item == null) {
                // CHANGED JW: use converter if promptText is empty
                if (!isEmptyPrompt()) {
                    s = comboBox.getPromptText();
                } else if (c != null) {
                    s = c.toString(item);
                } else {
                    s = "";
                }
            } else {
                if (c == null) {
                    s = item.toString();
                } else {
                    s = c.toString(item);
                }
            }

            // String s = item == null ? comboBox.getPromptText() :
            // (c == null ? item.toString() : c.toString(item));
            cell.setText(s);
            cell.setGraphic(null);
            return s == null || s.isEmpty();
        }
    }

    protected boolean isEmptyPrompt() {
        return comboBox.getPromptText() == null
                || comboBox.getPromptText().isEmpty();
    }
    /**
     * PENDING JW: formal fix of scope, untested
     */
    @Override
    protected void setTextFromTextFieldIntoComboBoxValue() {
        if (!comboBox.isEditable())
            return;

        StringConverter<T> c = comboBox.getConverter();
        if (c == null)
            return;

        T oldValue = comboBox.getValue();
        String text = textField.getText();

        // conditional check here added due to RT-28245
        T value = oldValue == null && (text == null || text.isEmpty()) ? null
                : c.fromString(textField.getText());

        if ((value == null && oldValue == null)
                || (value != null && value.equals(oldValue))) {
            // no point updating values needlessly (as they are the same)
            return;
        }

        comboBox.setValue(value);
    }

    private int getIndexOfComboBoxValueInItemsList() {
        T value = comboBox.getValue();
        int index = getComboBoxItems().indexOf(value);
        return index;
    }

    private void updateButtonCell() {
        buttonCell = comboBox.getButtonCell() != null ? comboBox
                .getButtonCell() : getDefaultCellFactory().call(listView);
        buttonCell.setMouseTransparent(true);
        buttonCell.updateListView(listView);
    }

    private void updateCellFactory() {
        Callback<ListView<T>, ListCell<T>> cf = comboBox.getCellFactory();
        cellFactory = cf != null ? cf : getDefaultCellFactory();
        listView.setCellFactory(cellFactory);
    }

    private Callback<ListView<T>, ListCell<T>> getDefaultCellFactory() {
        return new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListCell<T> call(ListView<T> listView) {
                return new ListCell<T>() {
                    @Override
                    public void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        updateDisplayText(this, item, empty);
                    }
                };
            }
        };
    }

    // PENDING JW: extracted for digging 37622
    protected class ComboListView extends ListView<T> {
        
        public ComboListView() {
            SingleMultipleSelectionModel<T> adapter = new SingleMultipleSelectionModel<>(
                    comboBox.selectionModelProperty(), focusModelProperty());
            setSelectionModel(adapter);
            
            // PENDING JW: need to set the properties _before_
            // binding the items, otherwise we get the focus on first
            // disable selecting the first item on focus gain - this is
            // not what is expected in the ComboBox control (unlike the
            // ListView control, which does this).
            getProperties().put("selectOnFocusGain", false);
            // introduced between 8u20 and 8u40b7
            // with this, testfailures back to normal
            getProperties().put("selectFirstRowByDefault", false);
            
            // CHANGED JW
            // simply bind the items' properties
            itemsProperty().bind(comboBox.itemsListProperty());
            
            
        }
        

        @Override
        protected double computeMinHeight(double width) {
            return 30;
        }

        // PENDING JW: this is _not_ called in misbehaviour of RT_37622
        // how can selection lead to not calling
        @Override
        protected double computePrefWidth(double height) {
            double pw;
            if (getSkin() instanceof ListViewSkin) {
                ListViewSkin<?> skin = (ListViewSkin<?>) getSkin();
                if (itemCountDirty) {
                    invokeUpdateRowCount(skin);
                    itemCountDirty = false;
                }

                int rowsToMeasure = -1;
                if (comboBox.getProperties().containsKey(
                        COMBO_BOX_ROWS_TO_MEASURE_WIDTH_KEY)) {
                    rowsToMeasure = (Integer) comboBox.getProperties().get(
                            COMBO_BOX_ROWS_TO_MEASURE_WIDTH_KEY);
                }

                double calc = invokeGetMaxCellWidth(skin, rowsToMeasure);
                pw = Math.max(comboBox.getWidth(), calc + 30);
            } else {
                pw = Math.max(100, comboBox.getWidth());
            }

            // need to check the ListView pref height in the case that the
            // placeholder node is showing
            if (getItems().isEmpty() && getPlaceholder() != null) {
                pw = Math.max(super.computePrefWidth(height), pw);
            }

            return Math.max(50, pw);
        }

        /**
         * PENDING JW: hack around not visible super method, hard-coded for
         * now.
         */
        protected double invokeGetMaxCellWidth(ListViewSkin<?> skin,
                int rowsToMeasure) {
            Class clazz = VirtualContainerBase.class;
            try {
                Method method = clazz.getDeclaredMethod("getMaxCellWidth", int.class);
                method.setAccessible(true);
                return (double) method.invoke(skin, rowsToMeasure);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return 100;
            // return skin.getMaxCellWidth(rowsToMeasure);
        }

        /**
         * PENDING JW: hack around not visible super method, no-op for now.
         */
        protected void invokeUpdateRowCount(ListViewSkin<?> skin) {
            Class clazz = ListViewSkin.class;
            try {
                Method method = clazz.getDeclaredMethod("updateRowCount");
                method.setAccessible(true);
                method.invoke(skin);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // skin.updateRowCount();
        }

        @Override
        protected double computePrefHeight(double width) {
            return getListViewPrefHeight();
        }
    }
    
    private ListView<T> createListView() {
        final ListView<T> _listView = new ComboListView() {

            {
            }

        };

        _listView.setId("list-view");
        _listView.placeholderProperty().bind(comboBox.placeholderProperty());
        _listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        _listView.setFocusTraversable(false);

//        _listView
//                .getSelectionModel()
//                .selectedIndexProperty()
//                .addListener(
//                        o -> {
////                            if (listSelectionLock)
////                                return;
////                            int index = listView.getSelectionModel()
////                                    .getSelectedIndex();
////                            comboBox.getSelectionModel().select(index);
//                            updateDisplayNode();
//                            // comboBox.accSendNotification(Attribute.TITLE);
//                        });

//        comboBox.getSelectionModel().selectedItemProperty().addListener(o -> {
//            listViewSelectionDirty = true;
//        });

        _listView.addEventFilter(MouseEvent.MOUSE_RELEASED, t -> {
            // RT-18672: Without checking if the user is clicking in the
            // scrollbar area of the ListView, the comboBox will hide.
            // Therefore,
            // we add the check below to prevent this from happening.
                EventTarget target = t.getTarget();
                if (target instanceof Parent) {
                    List<String> s = ((Parent) target).getStyleClass();
                    if (s.contains("thumb") || s.contains("track")
                            || s.contains("decrement-arrow")
                            || s.contains("increment-arrow")) {
                        return;
                    }
                }

                if (isHideOnClickEnabled()) {
                    comboBox.hide();
                }
            });

        _listView.setOnKeyPressed(t -> {
            // TODO move to behavior, when (or if) this class becomes a SkinBase
                if (t.getCode() == KeyCode.ENTER
                        || t.getCode() == KeyCode.SPACE
                        || t.getCode() == KeyCode.ESCAPE) {
                    comboBox.hide();
                }
            });

        return _listView;
    }

    private double getListViewPrefHeight() {
        double ph;
        if (listView.getSkin() instanceof VirtualContainerBase) {
            int maxRows = comboBox.getVisibleRowCount();
            VirtualContainerBase<?, ?, ?> skin = (VirtualContainerBase<?, ?, ?>) listView
                    .getSkin();
            ph = invokeGetVirtualFlowPreferredHeight(maxRows, skin);
        } else {
            double ch = getComboBoxItems().size() * 25;
            ph = Math.min(ch, 200);
        }

        return ph;
    }

    /**
     * PENDING JW: Hack around invisibl method, hardcoded to maxRows * 20 for
     * now.
     * 
     * @param maxRows
     * @param skin
     * @return
     */
    protected double invokeGetVirtualFlowPreferredHeight(int maxRows,
            VirtualContainerBase<?, ?, ?> skin) {
        Class clazz = VirtualContainerBase.class;
        try {
            Method method = clazz.getDeclaredMethod("getVirtualFlowPreferredHeight", int.class);
            method.setAccessible(true);
            return (double) method.invoke(skin, maxRows);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return maxRows * 20f;
        // return skin.getVirtualFlowPreferredHeight(maxRows);
    }

    /**************************************************************************
     * 
     * API for testing
     * 
     *************************************************************************/

    public ListView<T> getListView() {
        return listView;
    }

    /***************************************************************************
     * * Stylesheet Handling * *
     **************************************************************************/

    private static PseudoClass CONTAINS_FOCUS_PSEUDOCLASS_STATE = PseudoClass
            .getPseudoClass("contains-focus");

    // These three pseudo class states are duplicated from Cell
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass
            .getPseudoClass("selected");

    private static final PseudoClass PSEUDO_CLASS_EMPTY = PseudoClass
            .getPseudoClass("empty");

    private static final PseudoClass PSEUDO_CLASS_FILLED = PseudoClass
            .getPseudoClass("filled");

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBoxXListViewSkin.class.getName());

    /**
     * PENDING JW: formal fix of scope, untested
     */
    @Override
    protected TextField getEditor() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * PENDING JW: formal fix of scope, untested
     */
    @Override
    protected StringConverter<T> getConverter() {
        // TODO Auto-generated method stub
        return null;
    }

    /***************************************************************************
     * * Support classes * *
     **************************************************************************/

//    public static final class FakeFocusTextField extends TextField {
//
//        public void setFakeFocus(boolean b) {
//            setFocused(b);
//        }
//
//        // @Override public Object accGetAttribute(Attribute attribute,
//        // Object... parameters) {
//        // switch (attribute) {
//        // case FOCUS_ITEM:
//        // /* Internally comboBox reassign its focus the text field.
//        // * For the accessibility perspective it is more meaningful
//        // * if the focus stays with the comboBox control.
//        // */
//        // return getParent();
//        // default: return super.accGetAttribute(attribute, parameters);
//        // }
//        // }
//    }
//
    // @Override public Object accGetAttribute(Attribute attribute, Object...
    // parameters) {
    // switch (attribute) {
    // case FOCUS_ITEM: {
    // if (comboBox.isShowing()) {
    // /* On Mac, for some reason, changing the selection on the list is not
    // * reported by VoiceOver the first time it shows.
    // * Note that this fix returns a child of the PopupWindow back to the main
    // * Stage, which doesn't seem to cause problems.
    // */
    // return listView.accGetAttribute(attribute, parameters);
    // }
    // return null;
    // }
    // case TITLE: {
    // String title = comboBox.isEditable() ? textField.getText() :
    // buttonCell.getText();
    // if (title == null || title.isEmpty()) {
    // title = comboBox.getPromptText();
    // }
    // return title;
    // }
    // case SELECTION_START: return textField.getSelection().getStart();
    // case SELECTION_END: return textField.getSelection().getEnd();
    //
    // //fall through
    // default: return super.accGetAttribute(attribute, parameters);
    // }
    // }

}
