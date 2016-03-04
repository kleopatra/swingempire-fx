/*
 * Created on 29.03.2015
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import de.swingempire.fx.scene.control.cell.DefaultTreeTableCell.DefaultTreeTableCellSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.skin.TableRowSkinBase;
import javafx.scene.control.skin.TreeTableRowSkin;
//import javafx.scene.control.cell.CellUtils;
import javafx.util.Callback;

/**
 * Support custom graphic for Tree/TableRow. Here in particular a checkBox.
 * http://stackoverflow.com/q/29300551/203657
 * <p>
 * Basic idea: implement custom TreeTableRow that set's its graphic to the 
 * graphic/checkBox. Doesn't work: layout is broken, graphic appears
 * over the text. All fine if we set the graphic to the TreeItem that's
 * shown. Possible as long as the treeItem doesn't have a graphic of
 * its own.
 * <p>
 * Basic problem:
 * <li> TableRowSkinBase seems to be able to cope: has protected method
 *   graphicsProperty that should be implemented to return the graphic 
 *   if any. That graphic is added to the children list and sized/located
 *   in layoutChildren. 
 * <li> are added the graphic/disclosureNode as needed before
 *   calling super.layoutChildren,  
 * <li> graphic/disclosure are placed inside the leftPadding of the tableCell
 *   that is the treeColumn
 * <li> TreeTableCellSkin must cooperate in taking into account the graphic/disclosure 
 *   when calculating its leftPadding
 * <li> cellSkin is hard-coded to use the TreeItem's graphic (vs. the rowCell's)   
 *
 * PENDING JW: 
 * <li>- would expect to not alter the scenegraph during layout (might lead to
 *   endless loops or not) but done frequently in core code   
 * <p> 
 *  
 * Outline of the solution as implemented:
 * <li> need a TreeTableCell with a custom skin
 * <li> override leftPadding in skin to add row graphic if available
 * <li> need CheckBoxTreeTableRow that sets its graphic to checkBox (or a combination
 *   of checkBox and treeItem's)
 * <li> need custom rowSkin that implements graphicProperty to return the row graphic  
 *    
 * PENDING JW: doesn't compile due to package-private abstract methods - WAIT for fix 
 * (is work-in-progress)   
 *    
 * @author Jeanette Winzenburg, Berlin
 * 
 * @see DefaultTreeTableCell
 * @see DefaultTreeTableCellSkin
 * 
 */
public class CheckBoxTreeTableRow<T> extends TreeTableRow<T> {

    private CheckBox checkBox;
    
    private ObservableValue<Boolean> booleanProperty;
    
    private BooleanProperty indeterminateProperty;
    
    public CheckBoxTreeTableRow() {
        this(item -> {
            if (item instanceof CheckBoxTreeItem<?>) {
                return ((CheckBoxTreeItem<?>)item).selectedProperty();
            }
            return null;
        });
    }
    
    public CheckBoxTreeTableRow(
            final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedProperty) {
        this.getStyleClass().add("check-box-tree-cell");
        setSelectedStateCallback(getSelectedProperty);
        checkBox = new CheckBox();
        checkBox.setAlignment(Pos.TOP_LEFT);
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
    @Override 
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            TreeItem<T> treeItem = getTreeItem();
            checkBox.setGraphic(treeItem == null ? null : treeItem.getGraphic());
            setGraphic(checkBox);
            // uninstall bindings
            if (booleanProperty != null) {
                checkBox.selectedProperty().unbindBidirectional((BooleanProperty)booleanProperty);
            }
            if (indeterminateProperty != null) {
                checkBox.indeterminateProperty().unbindBidirectional(indeterminateProperty);
            }

            // install new bindings.
            // this can only handle TreeItems of type CheckBoxTreeItem
            if (treeItem instanceof CheckBoxTreeItem) {
                CheckBoxTreeItem<T> cbti = (CheckBoxTreeItem<T>) treeItem;
                booleanProperty = cbti.selectedProperty();
                checkBox.selectedProperty().bindBidirectional((BooleanProperty)booleanProperty);
                
                indeterminateProperty = cbti.indeterminateProperty();
                checkBox.indeterminateProperty().bindBidirectional(indeterminateProperty);
            } else {
                throw new IllegalStateException("item must be CheckBoxTreeItem");
            }
        }
        
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CheckBoxTreeTableRowSkin<>(this);
    }

    public static class CheckBoxTreeTableRowSkin<S> extends TreeTableRowSkin<S> {

        public CheckBoxTreeTableRowSkin(TreeTableRow<S> control) {
            super(control);
        }

        /**
         * Returns the tableRow graphicProperty.
         * 
         * Note: While this is implicitly called from the constructor 
         * of LabeledSkinBase, it doesn't matter as we don't alias the
         * property but return it directly.
         * 
         * PENDING JW: as of jdk9, this property is package-private, no 
         * clean way to inject our own! Will be made protected, ongoing work in
         * https://bugs.openjdk.java.net/browse/JDK-8148573
         */
        @Override
        protected ObjectProperty<Node> graphicProperty() {
            return getSkinnable().graphicProperty();
        }

        protected CheckBoxTreeTableRow<S> getTableRow() {
            return (CheckBoxTreeTableRow<S>) super.getSkinnable();
        }
    }
    
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(CheckBoxTreeTableRow.class.getName());
}
