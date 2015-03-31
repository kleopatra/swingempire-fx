/*
 * Created on 31.03.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.Locale;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.StyleOrigin;
import javafx.css.StyleableObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import com.sun.javafx.runtime.VersionInfo;
import com.sun.javafx.scene.control.skin.TableRowSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableRowGraphicSample extends Application {

    
    public static class TableRowGraphic extends TableRow {
        private Node shape;

        public TableRowGraphic() {
//            shape = new Circle(10, Color.ROSYBROWN);
            shape = new CheckBox();
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(shape);
            }
        }

        @Override
        protected Skin createDefaultSkin() {
            return new TableRowGraphicSkin(this);
        }
        
        
    }

    public static class TableRowGraphicSkin extends TableRowSkin {

        ObjectProperty<Node> shapeP;
        
        public TableRowGraphicSkin(TableRowGraphic tableRow) {
            super(tableRow);
            shapeP = new SimpleObjectProperty(this, "shape");
            
//            updateChildren();
        }

        @Override
        protected ObjectProperty<Node> graphicProperty() {
            if (shapeP == null) return shapeP;
            shapeP.set(getTableRow().getGraphic());
            return shapeP;
        }
        
        
        @Override
        protected boolean isIndentationRequired() {
            return true;
        }

        @Override
        protected int getIndentationLevel(IndexedCell control) {
            return 1;
        }

        @Override
        protected double getIndentationPerLevel() {
            return 20;
        }

        
//        @Override
//        protected void updateChildren() {
//            super.updateChildren();
//            LOG.info("graphic " + graphicProperty());
//            if (graphicProperty() == null) return; 
//            Node g = graphicProperty().get();
//            if (g != null) {
////                getChildren().remove(g);
////                getChildren().add(g);
//            }
//        }

        protected TableRowGraphic getTableRow() {
            return (TableRowGraphic) getSkinnable();
        }
        @Override protected void layoutChildren(double x, final double y, final double w, final double h) {
            checkState();
            if (cellsMap.isEmpty()) return;

            ObservableList<? extends TableColumnBase> visibleLeafColumns = getVisibleLeafColumns();
            if (visibleLeafColumns.isEmpty()) {
                super.layoutChildren(x,y,w,h);
                return;
            }

            TableRow control = (TableRow) getSkinnable();

            ///////////////////////////////////////////
            // indentation code starts here
            ///////////////////////////////////////////
            double leftMargin = 0;
            double disclosureWidth = 0;
            double graphicWidth = 0;
            boolean indentationRequired = isIndentationRequired();
            boolean disclosureVisible = isDisclosureNodeVisible();
            int indentationColumnIndex = 0;
            Node disclosureNode = null;
            if (indentationRequired) {
                // Determine the column in which we want to put the disclosure node.
                // By default it is null, which means the 0th column should be
                // where the indentation occurs.
                TableColumnBase<?,?> treeColumn = getTreeColumn();
                indentationColumnIndex = treeColumn == null ? 0 : visibleLeafColumns.indexOf(treeColumn);
                indentationColumnIndex = indentationColumnIndex < 0 ? 0 : indentationColumnIndex;

                int indentationLevel = getIndentationLevel(control);
                if (! isShowRoot()) indentationLevel--;
                final double indentationPerLevel = getIndentationPerLevel();
                leftMargin = indentationLevel * indentationPerLevel;

                // position the disclosure node so that it is at the proper indent
                Control c = getVirtualFlowOwner();
                final double defaultDisclosureWidth = 0;
//                        maxDisclosureWidthMap.containsKey(c) ?
//                    maxDisclosureWidthMap.get(c) : 0;
                disclosureWidth = defaultDisclosureWidth;

                disclosureNode = getDisclosureNode();
                if (disclosureNode != null) {
                    disclosureNode.setVisible(disclosureVisible);

                    if (disclosureVisible) {
                        disclosureWidth = disclosureNode.prefWidth(h);
                        if (disclosureWidth > defaultDisclosureWidth) {
//                            maxDisclosureWidthMap.put(c, disclosureWidth);

                            // RT-36359: The recorded max width of the disclosure node
                            // has increased. We need to go back and request all
                            // earlier rows to update themselves to take into account
                            // this increased indentation.
                            final VirtualFlow<?> flow = getVirtualFlow();
//                            final int thisIndex = getSkinnable().getIndex();
//                            for (int i = 0; i < flow.cells.size(); i++) {
//                                C cell = flow.cells.get(i);
//                                if (cell == null || cell.isEmpty()) continue;
//                                cell.requestLayout();
//                                cell.layout();
//                            }
                        }
                    }
                }
            }
            ///////////////////////////////////////////
            // indentation code ends here
            ///////////////////////////////////////////

            // layout the individual column cells
            double width;
            double height;

            final double verticalPadding = snappedTopInset() + snappedBottomInset();
            final double horizontalPadding = snappedLeftInset() + snappedRightInset();
            final double controlHeight = control.getHeight();

            /**
             * RT-26743:TreeTableView: Vertical Line looks unfinished.
             * We used to not do layout on cells whose row exceeded the number
             * of items, but now we do so as to ensure we get vertical lines
             * where expected in cases where the vertical height exceeds the
             * number of items.
             */
            int index = control.getIndex();
            if (index < 0/* || row >= itemsProperty().get().size()*/) return;

            for (int column = 0, max = cells.size(); column < max; column++) {
                TableCell tableCell = (TableCell) cells.get(column);
                TableColumnBase<?, ?> tableColumn = getTableColumnBase(tableCell);

                boolean isVisible = true;
                if (false) {
                    // we determine if the cell is visible, and if not we have the
                    // ability to take it out of the scenegraph to help improve
                    // performance. However, we only do this when there is a
                    // fixed cell length specified in the TableView. This is because
                    // when we have a fixed cell length it is possible to know with
                    // certainty the height of each TableCell - it is the fixed value
                    // provided by the developer, and this means that we do not have
                    // to concern ourselves with the possibility that the height
                    // may be variable and / or dynamic.
                    isVisible = isColumnPartiallyOrFullyVisible(tableColumn);

//                    height = fixedCellSize;
                } else {
                    height = Math.max(controlHeight, tableCell.prefHeight(-1));
                    height = snapSize(height) - snapSize(verticalPadding);
                }

                if (isVisible) {
//                    if (fixedCellSizeEnabled && tableCell.getParent() == null) {
//                        getChildren().add(tableCell);
//                    }

                    width = snapSize(tableCell.prefWidth(-1)) - snapSize(horizontalPadding);

                    // Added for RT-32700, and then updated for RT-34074.
                    // We change the alignment from CENTER_LEFT to TOP_LEFT if the
                    // height of the row is greater than the default size, and if
                    // the alignment is the default alignment.
                    // What I would rather do is only change the alignment if the
                    // alignment has not been manually changed, but for now this will
                    // do.
                    final boolean centreContent = h <= 24.0;

                    // if the style origin is null then the property has not been
                    // set (or it has been reset to its default), which means that
                    // we can set it without overwriting someone elses settings.
                    final StyleOrigin origin = ((StyleableObjectProperty<?>) tableCell.alignmentProperty()).getStyleOrigin();
                    if (! centreContent && origin == null) {
                        tableCell.setAlignment(Pos.TOP_LEFT);
                    }
                    // --- end of RT-32700 fix

                    ///////////////////////////////////////////
                    // further indentation code starts here
                    ///////////////////////////////////////////
                    if (indentationRequired && column == indentationColumnIndex) {
                        if (disclosureVisible) {
                            double ph = disclosureNode.prefHeight(disclosureWidth);

                            if (width < (disclosureWidth + leftMargin)) {
//                                fadeOut(disclosureNode);
                            } else {
//                                fadeIn(disclosureNode);
                                disclosureNode.resize(disclosureWidth, ph);

                                disclosureNode.relocate(x + leftMargin,
                                        centreContent ? (h / 2.0 - ph / 2.0) :
                                                (y + tableCell.getPadding().getTop()));
                                disclosureNode.toFront();
                            }
                        }

                        // determine starting point of the graphic or cell node, and the
                        // remaining width available to them
                        ObjectProperty<Node> graphicProperty = graphicProperty();
                        Node graphic = graphicProperty == null ? null : graphicProperty.get();

                        if (graphic != null) {
                            graphicWidth = graphic.prefWidth(-1) + 3;
                            double ph = graphic.prefHeight(graphicWidth);

                            if (width < disclosureWidth + leftMargin + graphicWidth) {
//                                fadeOut(graphic);
                            } else {
//                                fadeIn(graphic);
                                getChildren().remove(graphic);
                                getChildren().add(graphic);
                                double newX = x + leftMargin + disclosureWidth;
                                graphic.relocate(newX,
                                        centreContent ? (h / 2.0 - ph / 2.0) :
                                                (y + tableCell.getPadding().getTop()));

                                graphic.toFront();
                                x += leftMargin + disclosureWidth + graphicWidth;
                                width += disclosureWidth + graphicWidth;
                            }
                        }
                    }
                    ///////////////////////////////////////////
                    // further indentation code ends here
                    ///////////////////////////////////////////

                    tableCell.resize(width, height);
                    tableCell.relocate(x, snappedTopInset());

                    // Request layout is here as (partial) fix for RT-28684.
                    // This does not appear to impact performance...
                    tableCell.requestLayout();
                } else {
//                    if (fixedCellSizeEnabled) {
//                        // we only add/remove to the scenegraph if the fixed cell
//                        // length support is enabled - otherwise we keep all
//                        // TableCells in the scenegraph
//                        getChildren().remove(tableCell);
//                    }

                    width = snapSize(tableCell.prefWidth(-1)) - snapSize(horizontalPadding);
                }

                x += width;
            }
        }
        
        private VirtualFlow<?> getVirtualFlow() {
            Parent p = getSkinnable();
            while (p != null) {
                if (p instanceof VirtualFlow) {
                    return (VirtualFlow<?>) p;
                }
                p = p.getParent();
            }
            return null;
        }

    }
    
    private Parent getContent() {
        ObservableList data = FXCollections.observableArrayList(Locale.getAvailableLocales());
        data.remove(0); // empty displayName ...
        TableView table = new TableView(data);
        table.setRowFactory(item -> new TableRowGraphic());
        
        TableColumn display = new TableColumn("Display");
        display.setCellValueFactory(new PropertyValueFactory("displayName"));
        table.getColumns().addAll(display);
        BorderPane pane = new BorderPane(table);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent(), 400, 100);
        primaryStage.setScene(scene);
        primaryStage.setTitle(VersionInfo.getRuntimeVersion());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableRowGraphicSample.class.getName());
}
