/*
 * Created on 11.02.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.collections.FXCollections;
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
 * This is the exact sample code as posted on SO
 * https://stackoverflow.com/q/54670425/203657
 * 
 * @see BugTableViewSampleVisualGlitchBorder
 * 
 * @author Jeanette Winzenburg, Berlin
 * 
 */
public class PlainTableViewDebugRowLayoutWithOverlaySO extends Application {
    
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
            overlay.relocate(hbarValue, 0);
            if (!getChildren().contains(overlay)) {
                // children are cleared on change notification from columns, re-add
                getChildren().add(overlay);
            }
            overlay.toFront();
        }
        
        protected ScrollBar getHorizontalScrollBar() {
            if (hbar == null) {
                VirtualFlow flow = getVirtualFlow();
                if (flow != null) {
                    hbar = (ScrollBar) invokeGetMethodValue(VirtualFlow.class, flow, "getHbar");
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
        TableView<Locale> table = createPlainTable();
        table.setRowFactory(c -> {
            return new TableRow<>() {

                @Override
                protected Skin<?> createDefaultSkin() {
                    return new MyTableRowSkin<>(this);
                }
                
            };
        });
        BorderPane content = new BorderPane(table);
        return content;
    }

    private TableView<Locale> createPlainTable() {
        List<Locale> locales = Arrays.stream(Locale.getAvailableLocales())
                // want to have visible content
                .filter(l -> l.getDisplayCountry() != null && !l.getDisplayCountry().trim().isEmpty())
                // just a few
                .limit(10)
                .collect(Collectors.toList());
        TableView<Locale> table =  new TableView<>(FXCollections.observableList(locales));
        table.getColumns().addAll(
                createColumn("country"),
                createColumn("displayLanguage"), 
                createColumn("displayLanguage"), 
                createColumn("language"), 
                createColumn("displayCountry")
                ); 
        return table;
    }
    
    private TableColumn<Locale, String> createColumn(String property) {
        TableColumn<Locale, String> column = new TableColumn<>(property);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        URL uri = getClass().getResource("overlaycell.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());

        //stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static Object invokeGetMethodValue(Class declaringClass, Object target, String name) {
        try {
            Method field = declaringClass.getDeclaredMethod(name);
            field.setAccessible(true);
            return field.invoke(target);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(PlainTableViewDebugRowLayoutWithOverlaySO.class.getName());

}
