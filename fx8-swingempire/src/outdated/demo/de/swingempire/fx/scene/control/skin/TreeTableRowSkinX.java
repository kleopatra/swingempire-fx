package de.swingempire.fx.scene.control.skin;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.javafx.scene.control.behavior.TreeTableRowBehavior;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.control.skin.TableRowSkinBase;
import javafx.scene.control.skin.TreeTableViewSkin;

/**
 * PENDING JW: unused? as copy, too old anyway, start again if need be.
 * --------
 * Copy of core TreeTableRowSkin, 8u60b5
 */
public class TreeTableRowSkinX<T> extends TableRowSkinBase<TreeItem<T>, TreeTableRow<T>, TreeTableRowBehavior<T>, TreeTableCell<T,?>> {

    /***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/

    // maps into the TreeTableViewSkin items property via
    // TreeTableViewSkin.treeItemToListMap
    private SimpleObjectProperty<ObservableList<TreeItem<T>>> itemsProperty;
    private TreeItem<?> treeItem;
    private boolean disclosureNodeDirty = true;
    private Node graphicX;

    private TreeTableViewSkin treeTableViewSkin;

    private boolean childrenDirty = false;



    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/
    
    public TreeTableRowSkinX(TreeTableRow<T> control) {
        super(control, new TreeTableRowBehavior<T>(control));
        
        super.init(control);
        
        updateTreeItem();
        updateTableViewSkin();

        registerChangeListener(control.treeTableViewProperty(), "TREE_TABLE_VIEW");
        registerChangeListener(control.indexProperty(), "INDEX");
        registerChangeListener(control.treeItemProperty(), "TREE_ITEM");
        registerChangeListener(control.getTreeTableView().treeColumnProperty(), "TREE_COLUMN");
    }



    /***************************************************************************
     *                                                                         *
     * Listeners                                                               *
     *                                                                         *
     **************************************************************************/

    private MultiplePropertyChangeListenerHandler treeItemListener = new MultiplePropertyChangeListenerHandler(p -> {
        if ("GRAPHIC".equals(p)) {
            disclosureNodeDirty = true;
            getSkinnable().requestLayout();
        }
        return null;
    });



    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * The amount of space to multiply by the treeItem.level to get the left
     * margin for this tree cell. This is settable from CSS
     */
    private DoubleProperty indent = null;
    public final void setIndent(double value) { indentProperty().set(value); }
    public final double getIndent() { return indent == null ? 10.0 : indent.get(); }
    public final DoubleProperty indentProperty() { 
        if (indent == null) {
            indent = new StyleableDoubleProperty(10.0) {
                @Override public Object getBean() {
                    return TreeTableRowSkinX.this;
                }

                @Override public String getName() {
                    return "indent";
                }

                @Override public CssMetaData<TreeTableRow<?>,Number> getCssMetaData() {
                    return TreeTableRowSkinX.StyleableProperties.INDENT;
                }
            };
        }
        return indent; 
    }



    /***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/

    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);

        if ("TREE_ABLE_VIEW".equals(p)) {
            updateTableViewSkin();
        } else if ("INDEX".equals(p)) {
            updateCells = true;
        } else if ("TREE_ITEM".equals(p)) {
            updateTreeItem();
            isDirty = true;
        } else if ("TREE_COLUMN".equals(p)) {
            // Fix for RT-27782: Need to set isDirty to true, rather than the
            // cheaper updateCells, as otherwise the text indentation will not
            // be recalculated in TreeTableCellSkin.leftLabelPadding()
            isDirty = true;
            getSkinnable().requestLayout();
        }
    }

    @Override protected void updateChildren() {
        super.updateChildren();

        updateDisclosureNodeAndGraphic();

        if (childrenDirty) {
            childrenDirty = false;
            if (cells.isEmpty()) {
                getChildren().clear();
            } else {
                // TODO we can optimise this by only showing cells that are
                // visible based on the table width and the amount of horizontal
                // scrolling.
                getChildren().addAll(cells);
            }
        }
    }

    @Override protected void layoutChildren(double x, double y, double w, double h) {
        if (disclosureNodeDirty) {
            updateDisclosureNodeAndGraphic();
            disclosureNodeDirty = false;
        }

        Node disclosureNode = getDisclosureNode();
        if (disclosureNode != null && disclosureNode.getScene() == null) {
            updateDisclosureNodeAndGraphic();
        }

        super.layoutChildren(x, y, w, h);
    }

    @Override protected TreeTableCell<T, ?> getCell(TableColumnBase tcb) {
        TreeTableColumn tableColumn = (TreeTableColumn<T,?>) tcb;
        TreeTableCell cell = (TreeTableCell) tableColumn.getCellFactory().call(tableColumn);

        cell.updateTreeTableColumn(tableColumn);
        cell.updateTreeTableView(tableColumn.getTreeTableView());

        return cell;
    }

    @Override protected void updateCells(boolean resetChildren) {
        super.updateCells(resetChildren);

        if (resetChildren) {
            childrenDirty = true;
            updateChildren();
        }
    }

    @Override protected boolean isIndentationRequired() {
        return true;
    }

    @Override protected TableColumnBase getTreeColumn() {
        return getSkinnable().getTreeTableView().getTreeColumn();
    }

    @Override protected int getIndentationLevel(TreeTableRow<T> control) {
        return control.getTreeTableView().getTreeItemLevel(control.getTreeItem());
    }

    @Override protected double getIndentationPerLevel() {
        return getIndent();
    }

    @Override protected Node getDisclosureNode() {
        return getSkinnable().getDisclosureNode();
    }

    @Override protected boolean isDisclosureNodeVisible() {
        return getDisclosureNode() != null && treeItem != null && ! treeItem.isLeaf();
    }

    @Override protected boolean isShowRoot() {
        return getSkinnable().getTreeTableView().isShowRoot();
    }

    @Override protected ObservableList<TreeTableColumn<T, ?>> getVisibleLeafColumns() {
        return getSkinnable().getTreeTableView().getVisibleLeafColumns();
    }

    @Override protected void updateCell(TreeTableCell<T, ?> cell, TreeTableRow<T> row) {
        cell.updateTreeTableRow(row);
    }

    @Override protected boolean isColumnPartiallyOrFullyVisible(TableColumnBase tc) {
        return false; //treeTableViewSkin == null ? false : treeTableViewSkin.isColumnPartiallyOrFullyVisible(tc);
    }

    @Override protected TreeTableColumn<T, ?> getTableColumnBase(TreeTableCell cell) {
        return cell.getTableColumn();
    }

    @Override protected ObjectProperty<Node> graphicProperty() {
        TreeTableRow<T> treeTableRow = getSkinnable();
        if (treeTableRow == null) return null;
        if (treeItem == null) return null;

        return treeItem.graphicProperty();
    }

    @Override protected Control getVirtualFlowOwner() {
        return getSkinnable().getTreeTableView();
    }

    @Override protected DoubleProperty fixedCellSizeProperty() {
        return getSkinnable().getTreeTableView().fixedCellSizeProperty();
    }



    /***************************************************************************
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     **************************************************************************/

    private void updateTreeItem() {
        if (treeItem != null) {
            treeItemListener.unregisterChangeListener(treeItem.expandedProperty());
            treeItemListener.unregisterChangeListener(treeItem.graphicProperty());
        }
        treeItem = getSkinnable().getTreeItem();
        if (treeItem != null) {
            treeItemListener.registerChangeListener(treeItem.graphicProperty(), "GRAPHIC");
        }
    }
    
    private void updateDisclosureNodeAndGraphic() {
        if (getSkinnable().isEmpty()) return;
        
        // check for graphic missing
        ObjectProperty<Node> graphicProperty = graphicProperty();
        Node newGraphic = graphicProperty == null ? null : graphicProperty.get();
        if (newGraphic != null) {
            // RT-30466: remove the old graphic
            if (newGraphic != graphicX) {
                getChildren().remove(graphicX);
            }

            if (! getChildren().contains(newGraphic)) {
                getChildren().add(newGraphic);
            }
        }
        graphicX = newGraphic;
        
        // check disclosure node
        Node disclosureNode = getSkinnable().getDisclosureNode();
        if (disclosureNode != null) {
            boolean disclosureVisible = treeItem != null && ! treeItem.isLeaf();
            disclosureNode.setVisible(disclosureVisible);
                
            if (! disclosureVisible) {
                getChildren().remove(disclosureNode);
            } else if (disclosureNode.getParent() == null) {
                getChildren().add(disclosureNode);
                disclosureNode.toFront();
            } else {
                disclosureNode.toBack();
            }
            
            // RT-26625: [TreeView, TreeTableView] can lose arrows while scrolling
            // RT-28668: Ensemble tree arrow disappears
            if (disclosureNode.getScene() != null) {
                disclosureNode.applyCss();
            }
        }
    }

    private void updateTableViewSkin() {
        TreeTableView<T> tableView = getSkinnable().getTreeTableView();
        if (tableView.getSkin() instanceof TreeTableViewSkin) {
            treeTableViewSkin = (TreeTableViewSkin)tableView.getSkin();
        }
    }
    
    
    /***************************************************************************
     *                                                                         *
     *                         Stylesheet Handling                             *
     *                                                                         *
     **************************************************************************/

    /** @treatAsPrivate */
    private static class StyleableProperties {
        
        private static final CssMetaData<TreeTableRow<?>,Number> INDENT = 
            new CssMetaData<TreeTableRow<?>,Number>("-fx-indent",
                SizeConverter.getInstance(), 10.0) {
                    
            @Override public boolean isSettable(TreeTableRow<?> n) {
                DoubleProperty p = ((TreeTableRowSkinX<?>) n.getSkin()).indentProperty();
                return p == null || !p.isBound();
            }

            @Override public StyleableProperty<Number> getStyleableProperty(TreeTableRow<?> n) {
                final TreeTableRowSkinX<?> skin = (TreeTableRowSkinX<?>) n.getSkin();
                return (StyleableProperty<Number>)(WritableValue<Number>)skin.indentProperty();
            }
        };
        
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<CssMetaData<? extends Styleable, ?>>(CellSkinBase.getClassCssMetaData());
            styleables.add(INDENT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
    
    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }



    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        final TreeTableView<T> treeTableView = getSkinnable().getTreeTableView();
        switch (attribute) {
            case SELECTED_ITEMS: {
                // FIXME this could be optimised to iterate over cellsMap only
                // (selectedCells could be big, cellsMap is much smaller)
                List<Node> selection = new ArrayList<>();
                int index = getSkinnable().getIndex();
                for (TreeTablePosition<T,?> pos : treeTableView.getSelectionModel().getSelectedCells()) {
                    if (pos.getRow() == index) {
                        TreeTableColumn<T,?> column = pos.getTableColumn();
                        if (column == null) {
                            /* This is the row-based case */
                            column = treeTableView.getVisibleLeafColumn(0);
                        }
                        TreeTableCell<T,?> cell = cellsMap.get(column).get();
                        if (cell != null) selection.add(cell);
                    }
                    return FXCollections.observableArrayList(selection);
                }
            }
            case CELL_AT_ROW_COLUMN: {
                int colIndex = (Integer)parameters[1];
                TreeTableColumn<T,?> column = treeTableView.getVisibleLeafColumn(colIndex);
                if (cellsMap.containsKey(column)) {
                    return cellsMap.get(column).get();
                }
                return null;
            }
            case FOCUS_ITEM: {
                TreeTableView.TreeTableViewFocusModel<T> fm = treeTableView.getFocusModel();
                TreeTablePosition<T,?> focusedCell = fm.getFocusedCell();
                TreeTableColumn<T,?> column = focusedCell.getTableColumn();
                if (column == null) {
                    /* This is the row-based case */
                    column = treeTableView.getVisibleLeafColumn(0);
                }
                if (cellsMap.containsKey(column)) {
                    return cellsMap.get(column).get();
                }
                return null;
            }
            default: return super.queryAccessibleAttribute(attribute, parameters);
        }
    }
}
