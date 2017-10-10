/*
 * Created on 14.09.2016
 *
 */
package de.swingempire.fx.scene.control.skin;

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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;

/**
 * Note: fixed in fx9, changed overall setup, will no longer compile!
 * NestedTableColumnHeader must use factory method for creating
 * TableColumns always.
 * 
 * Does not in its constructor.
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BugNestedColumnHeaderFactory extends Application {

    public static class MyColumnHeader extends TableColumnHeader {

        public MyColumnHeader(TableViewSkinBase skin, TableColumnBase tc) {
            super(skin, tc);
            setShape(new Ellipse(10, 10));
        }
        
    }
    public static class MyNestedColumnHeader extends NestedTableColumnHeader {
        
        @Override
        protected TableColumnHeader createTableColumnHeader(
                TableColumnBase col) {
            return col.getColumns().isEmpty() ?
                    new MyColumnHeader(getTableViewSkin(), col) : 
                    new MyNestedColumnHeader(getTableViewSkin(), col);    
        }

        public MyNestedColumnHeader(TableViewSkinBase skin,
                TableColumnBase tc) {
            super(skin, tc);
            // background just to see better
            setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, null, null)));
        }
        
    }
    
    public static class MyTableHeader extends TableHeaderRow {

        
        @Override
        protected NestedTableColumnHeader createRootHeader() {
            return new MyNestedColumnHeader(getTableSkin(), null);
        }

        public MyTableHeader(TableViewSkinBase skin) {
            super(skin);
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
}
