/*
 * Created on 11.02.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.net.URL;
import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.DebugUtils;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.IndexedCell;
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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Here: trying to dig why there are single pixels of underscrolled column 
 * are visible on the left if having some fixed overlay.
 * 
 * Here using a plain StackPane (vs. a Cell)
 * 
 * @see BugTableViewSampleVisualGlitchBorder
 * 
 * @author Jeanette Winzenburg, Berlin
 * 
 */
public class PlainTableViewDebugRowLayoutWithOverlay extends Application {
    
    public static class MyTableRowSkin<T> extends TableRowSkin<T> {

        ScrollBar hbar;
        StackPane overlay;
        public MyTableRowSkin(TableRow<T> control) {
            super(control);
            overlay = new StackPane();
            overlay.getStyleClass().setAll("overlay");
            getChildren().add(overlay);
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

            double hbarValue = scrollBar.getValue();
            TableColumnBase<T, ?> firstColumn = getVisibleLeafColumns().get(0);
            overlay.resize(firstColumn.getWidth(), h);
            overlay.relocate(snapSizeX(hbarValue), 0);
            if (!getChildren().contains(overlay)) {
                // children are cleared on change notification from columns, re-add
                getChildren().add(overlay);
            }
            overlay.toFront();
            if (getSkinnable().getIndex() == 0) {
                LOG.info("index of overlay: " + getChildren().contains(overlay));
                DebugUtils.printBounds(overlay);
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
            Parent p = getSkinnable();
            while (p != null) {
                if (p instanceof VirtualFlow) {
                    return (VirtualFlow) p;
                }
                p = p.getParent();
            }
            return null;
        }

    }

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
        table.getColumns().get(0).getStyleClass().add("overlay-column");
        BorderPane content = new BorderPane(table);
        return content;
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
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(PlainTableViewDebugRowLayoutWithOverlay.class.getName());

}
