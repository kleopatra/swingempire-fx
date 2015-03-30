/*
 * Created on 29.03.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
//import javafx.scene.control.cell.CellUtils;
import javafx.util.Callback;
import javafx.util.StringConverter;

import com.sun.javafx.scene.control.skin.TreeTableRowSkin;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class CheckBoxTreeTableRow<T> extends TreeTableRow<T> {

    private CheckBox checkBox;
    
    private ObservableValue<Boolean> booleanProperty;
    
    private BooleanProperty indeterminateProperty;
    
    public CheckBoxTreeTableRow() {
        // getSelectedProperty as anonymous inner class to deal with situation
        // where the user is using CheckBoxTreeItem instances in their tree
        this(item -> {
            if (item instanceof CheckBoxTreeItem<?>) {
                return ((CheckBoxTreeItem<?>)item).selectedProperty();
            }
            return null;
        });
    }
    
    public CheckBoxTreeTableRow(
            final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedProperty) {
        this(getSelectedProperty, null, null);
    }

    public CheckBoxTreeTableRow(
            final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedProperty, 
            final StringConverter<TreeItem<T>> converter) {
        this(getSelectedProperty, converter, null);
    }

    private CheckBoxTreeTableRow(
            final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedProperty, 
            final StringConverter<TreeItem<T>> converter, 
            final Callback<TreeItem<T>, ObservableValue<Boolean>> getIndeterminateProperty) {
        this.getStyleClass().add("check-box-tree-cell");
        setSelectedStateCallback(getSelectedProperty);
        setConverter(converter);
        
        this.checkBox = new CheckBox();
        checkBox.setAlignment(Pos.TOP_LEFT);
        this.checkBox.setAllowIndeterminate(false);

        // by default the graphic is null until the cell stops being empty
        setGraphic(null);
    }
    
    // --- converter
    private ObjectProperty<StringConverter<TreeItem<T>>> converter = 
            new SimpleObjectProperty<StringConverter<TreeItem<T>>>(this, "converter");

    /**
     * The {@link StringConverter} property.
     */
    public final ObjectProperty<StringConverter<TreeItem<T>>> converterProperty() { 
        return converter; 
    }
    
    /** 
     * Sets the {@link StringConverter} to be used in this cell.
     */
    public final void setConverter(StringConverter<TreeItem<T>> value) { 
        converterProperty().set(value); 
    }
    
    /**
     * Returns the {@link StringConverter} used in this cell.
     */
    public final StringConverter<TreeItem<T>> getConverter() { 
        return converterProperty().get(); 
    }
    
    
    
    // --- selected state callback property
    private ObjectProperty<Callback<TreeItem<T>, ObservableValue<Boolean>>> 
            selectedStateCallback = 
            new SimpleObjectProperty<Callback<TreeItem<T>, ObservableValue<Boolean>>>(
            this, "selectedStateCallback");

    /**
     * Property representing the {@link Callback} that is bound to by the 
     * CheckBox shown on screen.
     */
    public final ObjectProperty<Callback<TreeItem<T>, ObservableValue<Boolean>>> selectedStateCallbackProperty() { 
        return selectedStateCallback; 
    }
    
    /** 
     * Sets the {@link Callback} that is bound to by the CheckBox shown on screen.
     */
    public final void setSelectedStateCallback(Callback<TreeItem<T>, ObservableValue<Boolean>> value) { 
        selectedStateCallbackProperty().set(value); 
    }
    
    /**
     * Returns the {@link Callback} that is bound to by the CheckBox shown on screen.
     */
    public final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedStateCallback() { 
        return selectedStateCallbackProperty().get(); 
    }
    
    /** {@inheritDoc} */
    @Override public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
//            StringConverter<TreeItem<T>> c = getConverter();
//            
            TreeItem<T> treeItem = getTreeItem();
            // PENDING JW: this is nuts .. certainly will pose problems
            // when re-using the cell
//            treeItem.setGraphic(checkBox);
//            
//            // update the node content
////            setText(c != null ? c.toString(treeItem) : (treeItem == null ? "" : treeItem.toString()));
//            checkBox.setGraphic(treeItem == null ? null : treeItem.getGraphic());
            setGraphic(checkBox);
//            
            // uninstall bindings
            if (booleanProperty != null) {
                checkBox.selectedProperty().unbindBidirectional((BooleanProperty)booleanProperty);
            }
            if (indeterminateProperty != null) {
                checkBox.indeterminateProperty().unbindBidirectional(indeterminateProperty);
            }

            // install new bindings.
            // We special case things when the TreeItem is a CheckBoxTreeItem
            if (treeItem instanceof CheckBoxTreeItem) {
                CheckBoxTreeItem<T> cbti = (CheckBoxTreeItem<T>) treeItem;
                booleanProperty = cbti.selectedProperty();
                checkBox.selectedProperty().bindBidirectional((BooleanProperty)booleanProperty);
                
                indeterminateProperty = cbti.indeterminateProperty();
                checkBox.indeterminateProperty().bindBidirectional(indeterminateProperty);
            } else {
                throw new IllegalStateException("item must be CheckBoxTreeItem");
//                Callback<TreeItem<T>, ObservableValue<Boolean>> callback = getSelectedStateCallback();
//                if (callback == null) {
//                    throw new NullPointerException(
//                            "The CheckBoxTreeCell selectedStateCallbackProperty can not be null");
//                }
//                
//                booleanProperty = callback.call(treeItem);
//                if (booleanProperty != null) {
//                    checkBox.selectedProperty().bindBidirectional((BooleanProperty)booleanProperty);
//                }
            }
        }
        
        
    }


    @Override
    protected Skin<?> createDefaultSkin() {
        return new CheckBoxTreeTableRowSkin(this);
    }


    public static class CheckBoxTreeTableRowSkin extends TreeTableRowSkinX {
        protected ObjectProperty<Node> checkGraphic;

        /**
         * @param control
         */
        public CheckBoxTreeTableRowSkin(TreeTableRow control) {
            super(control);
//            updateChildren();
        }

        
        @Override
        protected ObjectProperty<Node> graphicProperty() {
//            super.graphicProperty();
            boolean checkCreated = false;
            if (checkGraphic == null) {
                checkCreated = true;
                checkGraphic = new SimpleObjectProperty<Node>(this, "checkGraphic");
            }
            CheckBoxTreeTableRow treeTableRow = getTableRow();
            if (treeTableRow.getTreeItem() == null) {
                checkGraphic.set(null);   
            } else {
                TreeItem item = getTableRow().getTreeItem();
                if (checkCreated) {
                    
                }
//                LOG.info("created check for " + checkCreated + "  " +((item != null) ? item.getValue() : null));
                checkGraphic.set(treeTableRow.checkBox);
            }
            LOG.info("isIgnoreGraphic" + isIgnoreGraphic());
            return checkGraphic;
        }

        protected void updateChildren() {
            super.updateChildren();
        }
        protected CheckBoxTreeTableRow getTableRow() {
            return (CheckBoxTreeTableRow) super.getSkinnable();
        }
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(CheckBoxTreeTableRow.class.getName());
}
