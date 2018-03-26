/*
 * Created on 31.03.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.Locale;
import java.util.logging.Logger;

import com.sun.javafx.runtime.VersionInfo;

import de.swingempire.fx.scene.control.tree.TableRowGraphicSample.DefaultTableCell;
import de.swingempire.fx.scene.control.tree.TableRowGraphicSample.TableRowGraphic;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableCellSkin;
import javafx.scene.control.skin.TableRowSkin;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * PENDING JW:
 * fx-9: indentation-related methods moved to package-private
 * -------
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableRowGraphicSample extends Application {

    
    public static class TableRowGraphic<S> extends TableRow<S> {
        private Node shape;

        public TableRowGraphic() {
//            shape = new Circle(10, Color.ROSYBROWN);
            shape = new CheckBox();
            ((CheckBox)shape).setAlignment(Pos.TOP_LEFT);
        }

        @Override
        protected void updateItem(S item, boolean empty) {
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

    public static class TableRowGraphicSkin<S> extends TableRowSkin<S> {

        ObjectProperty<Node> shapeP;
        
        public TableRowGraphicSkin(TableRowGraphic<S> tableRow) {
            super(tableRow);
//            shapeP = new SimpleObjectProperty(this, "shape");
            
//            updateChildren();
        }

        @Override
        protected ObjectProperty<Node> graphicProperty() {
            return getSkinnable().graphicProperty();
//            if (shapeP == null) {
//                shapeP = new SimpleObjectProperty(this, "shape");
//            }
//            shapeP.set(getTableRow().getGraphic());
//            return shapeP;
        }
        
        protected void updateGraphic() {
            if (getSkinnable().isEmpty()) return;
            ObjectProperty<Node> graphicProperty = graphicProperty();
            Node newGraphic = graphicProperty == null ? null : graphicProperty.get();
            if (newGraphic != null) {
//                // RT-30466: remove the old graphic
//                if (newGraphic != graphic) {
//                    getChildren().remove(graphic);
//                }
//
                if (! getChildren().contains(newGraphic)) {
                    getChildren().add(newGraphic);
//                    graphic = newGraphic;
                }
            }
        }
        
        
        @Override
        protected boolean isIndentationRequired() {
            return true;
        }

        @Override
        protected int getIndentationLevel(TableRow control) {
            return 1;
        }

        @Override
        protected double getIndentationPerLevel() {
            return 10;
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            updateGraphic();
            super.layoutChildren(x, y, w, h);
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

    }

    /**
     * TableCellSkin that can cope with row graphics.
     */
    public static class DefaultTableCellSkin<S, T> extends TableCellSkin<S, T> {

        public DefaultTableCellSkin(TableCell<S, T> tableCell) {
            super(tableCell);
        }

        
        @Override
        protected double leftLabelPadding() {
            double padding = super.leftLabelPadding();
            padding += getRowGraphicPatch();
            return padding;
        }

        protected double getRowGraphicPatch() {
            if (!isTreeColumn()) return 0;
            double indent = 0;
            Skin skin = getSkinnable().getTableRow().getSkin();
            if (skin instanceof TableRowGraphicSkin) {
                TableRowGraphicSkin rowSkin = (TableRowGraphicSkin) skin;
                indent += rowSkin.getIndentationPerLevel();
            }
            Node graphic = getSkinnable().getTableRow().getGraphic();
            if (graphic != null) {
                // start with row's graphic
                indent += graphic.prefWidth(getCellSize());
            }
            return indent;
        }



        /**
         * Checks and returns whether our cell is attached to a treeTableView/column
         * and actually has a TreeItem.
         * @return
         */
        protected boolean isTreeColumn() {
            if (getSkinnable().isEmpty()) return false;
            if (getSkinnable().getTableRow() == null) return false;
            TableColumn<S, T> column = getSkinnable().getTableColumn();
            TableView<S> view = getSkinnable().getTableView();
            return view.getVisibleLeafColumns().indexOf(column) == 0;
        }
        
    }
    
    /**
     * C&p of default cell as defined in TableColumn. Plus using custom skin.
     */
    public static class DefaultTableCell<S, T> extends TableCell<S, T> {
        
        @Override protected void updateItem(T item, boolean empty) {
            if (item == getItem()) return;

            super.updateItem(item, empty);

            if (item == null) {
                super.setText(null);
                super.setGraphic(null);
            } else if (item instanceof Node) {
                super.setText(null);
                super.setGraphic((Node)item);
            } else {
                super.setText(item.toString());
                super.setGraphic(null);
            }
        }

        @Override
        protected Skin<?> createDefaultSkin() {
//            return super.createDefaultSkin();
            return new DefaultTableCellSkin(this);
        }
        
    }
    
    private Parent getContent() {
        ObservableList data = FXCollections.observableArrayList(Locale.getAvailableLocales());
        data.remove(0); // empty displayName ...
        TableView table = new TableView(data);
        table.setRowFactory(item -> new TableRowGraphic());
        
        TableColumn display = new TableColumn("Display");
        display.setCellFactory(item -> new DefaultTableCell());
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
