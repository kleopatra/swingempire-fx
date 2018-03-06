/*
 * Created on 06.03.2018
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/49121560/203657
 * position sort indicator at leading edge of column header
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableHeaderLeadingSortArrow extends Application {

    /**
     * Custom TableColumnHeader that lays out the sort icon at its leading edge.
     */
    public static class MyTableColumnHeader extends TableColumnHeader {

        public MyTableColumnHeader(TableColumnBase column) {
            super(column);
        }

        @Override
        protected void layoutChildren() {
            // call super to ensure that all children are created and installed
            super.layoutChildren();
            Node sortArrow = getSortArrow();
            // no sort indicator, nothing to do
            if (sortArrow == null || !sortArrow.isVisible()) return;
            // re-arrange label and sort indicator
            double sortWidth = sortArrow.prefWidth(-1);
            double headerWidth = snapSizeX(getWidth()) - (snappedLeftInset() + snappedRightInset());
            double headerHeight = getHeight() - (snappedTopInset() + snappedBottomInset());
            
            // position sort indicator at leading edge
            sortArrow.resize(sortWidth, sortArrow.prefHeight(-1));
            positionInArea(sortArrow, snappedLeftInset(), snappedTopInset(),
                    sortWidth, headerHeight, 0, HPos.CENTER, VPos.CENTER);
            // resize label to fill remaining space
            getLabel().resizeRelocate(sortWidth, 0, headerWidth - sortWidth, getHeight());
        }
        
        private Node getSortArrow() {
            return (Node) FXUtils.invokeGetFieldValue(TableColumnHeader.class, this, "sortArrow");
        }
        
        private Label getLabel() {
            return (Label) FXUtils.invokeGetFieldValue(TableColumnHeader.class, this, "label");
        }
        
    }
    
    private Parent createContent() {
        // instantiate the tableView with the custom default skin
        TableView<Locale> table = new TableView<>(FXCollections.observableArrayList(
                Locale.getAvailableLocales())) {

                    @Override
                    protected Skin<?> createDefaultSkin() {
                        return new MyTableViewSkin<>(this);
                    }
            
        };
        TableColumn<Locale, String> countryCode = new TableColumn<>("CountryCode");
        countryCode.setCellValueFactory(new PropertyValueFactory<>("country"));
        TableColumn<Locale, String> language = new TableColumn<>("Language");
        language.setCellValueFactory(new PropertyValueFactory<>("language"));
        TableColumn<Locale, String> variant = new TableColumn<>("Variant");
        variant.setCellValueFactory(new PropertyValueFactory<>("variant"));
        table.getColumns().addAll(countryCode, language, variant);
        
        BorderPane pane = new BorderPane(table);
        return pane;
    }

    /**
     * Custom nested columnHeader, headerRow und skin only needed to 
     * inject the custom columnHeader in their factory methods.
     */
    public static class MyNestedTableColumnHeader extends NestedTableColumnHeader {
    
        public MyNestedTableColumnHeader(TableColumnBase column) {
            super(column);
        }
    
        @Override
        protected TableColumnHeader createTableColumnHeader(
                TableColumnBase col) {
            return col == null || col.getColumns().isEmpty() || col == getTableColumn() ?
                    new MyTableColumnHeader(col) :
                    new MyNestedTableColumnHeader(col);
        }
    }

    public static class MyTableHeaderRow extends TableHeaderRow {
    
        public MyTableHeaderRow(TableViewSkinBase tableSkin) {
            super(tableSkin);
        }
    
        @Override
        protected NestedTableColumnHeader createRootHeader() {
            return new MyNestedTableColumnHeader(null);
        }
    }

    public static class MyTableViewSkin<T> extends TableViewSkin<T> {
    
        public MyTableViewSkin(TableView<T> table) {
            super(table);
        }
    
        @Override
        protected TableHeaderRow createTableHeaderRow() {
            return new MyTableHeaderRow(this);
        }
        
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableHeaderLeadingSortArrow.class.getName());

}
