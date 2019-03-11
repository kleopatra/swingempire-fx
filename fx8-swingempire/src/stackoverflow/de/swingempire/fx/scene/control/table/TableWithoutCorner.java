/*
 * Created on 11.03.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Table: adjust vbar to write over corner.
 * https://stackoverflow.com/q/55100873/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableWithoutCorner extends Application {

    /**
     * Custom TableHeaderRow that requests a larger vbar height
     * if needed.
     */
    private static class MyTableHeader extends TableHeaderRow {

        private Region cornerAlias;
        private ScrollBar vBar;
        private TableViewSkinBase skin;
        
        public MyTableHeader(TableViewSkinBase skin) {
            super(skin);
            this.skin = skin;
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            adjustCornerLayout();
        }

        private void adjustCornerLayout() {
            checkAlias();
            // tbd: check also if corner is visible
            if (!vBar.isVisible()) {
                vBar.getProperties().remove("DELTA");
            } else { 
                vBar.getProperties().put("DELTA", getHeight());
            }
        }
        
        private void checkAlias() {
            if (cornerAlias == null) {
                cornerAlias = (Region) lookup(".show-hide-columns-button");
            }
            if (vBar == null) {
                vBar = (ScrollBar) skin.getSkinnable().lookup(".scroll-bar:vertical");
            }
        }

    }
    
    /**
     * Custom VirtualFlow that respects additinal height for its 
     * vertical ScrollBar.
     */
    private static class MyFlow extends VirtualFlow {

        private ScrollBar vBar;
        private Region clip;
        
        public MyFlow() {
            // the scrollbar to adjust
            vBar = (ScrollBar) lookup(".scroll-bar:vertical");
            // the clipped container to use for accessing viewport dimensions
            clip = (Region) lookup(".clipped-container");

        }
        
        /**
         * Overridden to adjust vertical scrollbar's height and y-location
         * after calling super.
         */
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            adjustVBar();
        }
        
        /**
         * Adjusts vBar height and y-location by the height as
         * requested by the table header.
         */
        protected void adjustVBar() {
            if (vBar.getProperties().get("DELTA") == null) return;
            double delta = (double) vBar.getProperties().get("DELTA");
            vBar.relocate(clip.getWidth(), - delta);
            vBar.resize(vBar.getWidth(), clip.getHeight() + delta);
        }
        
    }
    
    /**
     * Boilerplate: need custom TableViewSkin to inject a custom TableHeaderRow and
     * custom VirtualFlow.
     */
    private static class MyTableViewSkin<T> extends TableViewSkin<T> {

        public MyTableViewSkin(TableView<T> control) {
            super(control);
        }

        @Override
        protected TableHeaderRow createTableHeaderRow() {
            return new MyTableHeader(this);
        }

        @Override
        protected VirtualFlow<TableRow<T>> createVirtualFlow() {
            return new MyFlow();
        }
        
    }
    
    private Parent createContent() {
        TableView<Locale> table = new TableView<>(FXCollections.observableArrayList(Locale.getAvailableLocales())) {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new MyTableViewSkin(this);
            }
            
        }; 
        TableColumn<Locale, String> col = new TableColumn<>("Name");
        col.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        table.getColumns().addAll(col);
        return table;
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
            .getLogger(TableWithoutCorner.class.getName());

}
