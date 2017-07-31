/*
 * Created on 14.09.2016
 *
 */
package de.swingempire.lang;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;

/**
 * NestedTableColumnHeader must use factory method for creating
 * TableColumns always.
 * 
 * Does not in its constructor.
 * 
 * Changed for fx9
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BugNestedColumnHeaderFactory9 extends Application {

    public static class MyColumnHeader extends TableColumnHeader {

        public MyColumnHeader(TableViewSkinBase skin, TableColumnBase tc) {
            super(tc);
            setShape(new Ellipse(10, 10));
        }
        
    }
    public static class MyNestedColumnHeader extends NestedTableColumnHeader {
        
        @Override
        protected TableColumnHeader createTableColumnHeader(
                TableColumnBase col) {
            TableColumnHeader header = col == null || col.getColumns().isEmpty() || col == getTableColumn() ?
                    new MyColumnHeader(null, col) : 
                    new MyNestedColumnHeader(null, col);  
                    LOG.info("col: " + (col != null ? col.getText() : "null") + " header: " + header);
            return header;
        }

        public MyNestedColumnHeader(TableViewSkinBase skin,
                TableColumnBase tc) {
            super(/*skin, */tc);
            // background just to see better
            setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, null, null)));
        }
        
    }
    
    public static class MyTableHeader extends TableHeaderRow {

        
        @Override
        protected NestedTableColumnHeader createRootHeader() {
            LOG.info("" + getTableSkin());
//            return new MyNestedColumnHeader(null, null);
            return new MyNestedColumnHeader(getTableSkin(), null);
        }

        public MyTableHeader(TableViewSkinBase skin) {
            super(skin);
        }
        
        protected TableViewSkinBase getTableSkin() {
            return (TableViewSkinBase) FXUtils.invokeGetFieldValue(TableHeaderRow.class, this, "tableSkin");
        }
        
        protected VirtualFlow getVirtualFlow() {
            return (VirtualFlow) FXUtils.invokeGetFieldValue(TableHeaderRow.class, this, "flow");
        }
        protected double getScrollX() {
            return (double) FXUtils.invokeGetFieldValue(TableHeaderRow.class, this, "scrollX");
        }
    }
    
    public static class MyTableViewSkin extends TableViewSkin {

        @Override
        protected TableHeaderRow createTableHeaderRow() {
            return new MyTableHeader(this);
        }

        public MyTableViewSkin(TableView tableView) {
            super(tableView);
        }
        
    }
    private Parent getContent() {
        TableView table = new TableView() {

            @Override
            protected Skin createDefaultSkin() {
                return new MyTableViewSkin(this);
            }
            
        };
        TableColumn single = new TableColumn("Single");
        TableColumn nested = new TableColumn("Nested");
        TableColumn childOne = new TableColumn("Child One");
        TableColumn childTwo = new TableColumn("Child Two");
        nested.getColumns().addAll(childOne, childTwo);
        table.getColumns().addAll(single, nested);
        BorderPane pane = new BorderPane(table);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 600, 400));
//        URL uri = getClass().getResource("headers.css");
//        primaryStage.getScene().getStylesheets().add(uri.toExternalForm());
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(BugNestedColumnHeaderFactory9.class.getName());
}
