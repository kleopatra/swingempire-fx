/*
 * Created on 11.02.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.net.URL;
import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableRowSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Here: trying to dig why there are single pixels of underscrolled column 
 * are visible on the left if having some fixed overlay.
 * 
 * Here using a plain StackPane (vs. a Cell)
 * 
 * bug: https://bugs.openjdk.java.net/browse/JDK-8146406
 * resize/relocate the overlay with +/- a single pixel to hack around 
 * 
 * @see BugTableViewSampleVisualGlitchBorder
 * 
 * @author Jeanette Winzenburg, Berlin
 * 
 */
public class PlainTableViewDebugRowLayoutWithOverlay extends Application {
    
    public static class MyTableRowSkin<T> extends TableRowSkin<T> {

        static boolean hasClippedListener; 
        
        ScrollBar hbar;
        StackPane overlay;
        public MyTableRowSkin(TableRow<T> control) {
            super(control);
            overlay = new StackPane();
            overlay.getStyleClass().setAll("overlay");
            getChildren().add(overlay);
            if (!hasClippedListener) {
                Region clip =  (Region) getVirtualFlow().lookup(".clipped-container");
                TableView<T> table = (TableView<T>) getVirtualFlow().getParent();
                if (clip != null) {
                    Group sheet = (Group) clip.lookup(".sheet");
                    Node clipRect = clip.getClip();
                    clip.layoutXProperty().addListener((src, ov, nv) -> {
                        double horPadding = table.snappedRightInset() + table.snappedLeftInset();
                        if (table.getWidth() - horPadding != clip.getWidth()) {
                            LOG.info("\nlayoutClipListener - table/flow: " + table.getWidth() + " / " + clip.getWidth());
                        }
//                        LOG.info("\nlayoutX listener: hbar value: " + getHorizontalScrollBar().getValue() + clipRect);
//                        DebugUtils.printBounds(clipRect);
////                        LOG.info("hbarValue " + ov + " / " + nv);
                    });
                    hasClippedListener = true;
                    getHorizontalScrollBar().valueProperty().addListener((src, ov, nv) ->{
                        double horPadding = table.snappedRightInset() + table.snappedLeftInset();
                        if (table.getWidth() - horPadding != clip.getWidth()) {
                            LOG.info("\nhbar listener - table/flow: " + table.getWidth() + " / " + clip.getWidth());
                        }
//                        LOG.info("\nhbar value listener " + getHorizontalScrollBar().getValue() + clipRect);
//                        DebugUtils.printBounds(clipRect);
//                        LOG.info("contentWidth: " + table.getProperties().get("TableView.contentWidth"));
                    });
                }
            }
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            super.layoutChildren(x, y, w, h);
            layoutOverlay(x, y, w, h);
        }

        private void layoutOverlay(double x, double y, double w, double h) {
            if (getVisibleLeafColumns().isEmpty()) return;
            ScrollBar scrollBar = getHorizontalScrollBar();
            if (scrollBar == null)  return;

            Point2D xParent = getSkinnable().localToParent(x, y);
//            Point2D 
            
            double hbarValue = scrollBar.getValue();
            Point2D hbarParent = getSkinnable().localToParent(hbarValue, 0);
            TableColumnBase<T, ?> firstColumn = getVisibleLeafColumns().get(0);
            if (!getChildren().contains(overlay)) {
                // children are cleared on change notification from columns, re-add
                getChildren().add(overlay);
            }
            overlay.toFront();
            // resize/relocate with +/- a single pixel to hack around 
//            overlay.resize(firstColumn.getWidth() + 1, h);
//            overlay.relocate(hbarValue - 1, 0);
            overlay.resize(10, h);
            overlay.relocate(hbarValue, 0);
            if (getSkinnable().getIndex() == 0) {
                LOG.info("flow pref: " + getVirtualFlow().prefWidth(-1));
//                DebugUtils.printBounds(overlay);
//                DebugUtils.printBounds(hbar);
//                DebugUtils.printBounds(getVirtualFlow());
//                DebugUtils.printBounds(getSkinnable().getParent());
                //                new RuntimeException("who is calling? \n").printStackTrace();
            }
        }
        
        protected ScrollBar getHorizontalScrollBar() {
            if (hbar == null) {
                VirtualFlow flow = getVirtualFlow();
                if (flow != null) {
                    hbar = (ScrollBar) FXUtils.invokeGetMethodValue(VirtualFlow.class, flow, "getHbar");
                    registerChangeListener(hbar.valueProperty(), e -> getSkinnable().requestLayout());
                }
            }
            return hbar;
        }
        
        // copied from TableRowSkinBase
        protected VirtualFlow getVirtualFlow() {
            Parent node = getSkinnable();
            while (node != null) {
                if (node instanceof VirtualFlow) {
                    return (VirtualFlow) node;
                }
                node = node.getParent();
            }
            return null;
        }

    }

    public static ScrollBar getHorizontalScrollBar(VirtualFlow flow) {
        return (ScrollBar) FXUtils.invokeGetMethodValue(VirtualFlow.class, flow, "getHbar");
    }
    
    public static VirtualFlow getVirtualFlow(Node node) {
        return (VirtualFlow) node.lookup("VirtualFlow");
//        while (node != null) {
//            if (node instanceof VirtualFlow) {
//                return (VirtualFlow) node;
//            }
//            node = node.getParent();
//        }
//        return null;
    }
    
    public static ScrollBar getHbar(Node node) {
        VirtualFlow flow = getVirtualFlow(node);
        return getHorizontalScrollBar(flow);
    }
    
    private ScrollBar hbar;
    private Label valueLabel = new Label();
    
    private Parent createContent() {
        TableView<Person> table = createPlainTable();
        table.setRowFactory(c -> {
            return new TableRow<>() {

                @Override
                protected Skin<?> createDefaultSkin() {
                    return new MyTableRowSkin<>(this);
                }
                
            };
        });
//        table.getColumns().get(0).getStyleClass().add("overlay-column");
        
        double delta = 0.05;
        Button up = new Button("+");
        up.setOnAction(e -> {
            installHbar(table);
            double value = hbar.getValue();
            hbar.setValue(value + delta);
        });
        Button down = new Button("-");
        down.setOnAction(e -> {
            installHbar(table);
            double value = hbar.getValue();
            hbar.setValue(value - delta);
        });
        
        BorderPane content = new BorderPane(table);
        content.setBottom(new HBox(10, up, valueLabel, down));
        return content;
    }

    /**
     * @param table
     */
    protected void installHbar(Node table) {
        if (hbar == null) {
            hbar = getHbar(table);
            valueLabel.textProperty().bind(hbar.valueProperty().asString());
        }
    }

    private TableView<Person> createPlainTable() {
        TableView<Person> table =  new TableView<>(Person.persons());
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getColumns().addAll(createColumn("firstName"), 
                createColumn("lastName"), createColumn("email"), createColumn("secondaryMail"));
        return table;
    }
    
    private TableColumn<Person, String> createColumn(String property) {
        TableColumn<Person, String> column = new TableColumn<>(property);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        URL uri = getClass().getResource("overlaycell.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());

        stage.setTitle(FXUtils.version());
        
//        Screen primary = Screen.getPrimary();
//        Rectangle2D vb = primary.getVisualBounds();
//        Rectangle2D raw = primary.getBounds();
//        Rectangle2D visuals = raw;
//        stage.setX(visuals.getMinX());
//        stage.setY(visuals.getMinY());
//        stage.setWidth(visuals.getWidth());
//        stage.setHeight(visuals.getHeight());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(PlainTableViewDebugRowLayoutWithOverlay.class.getName());

}
