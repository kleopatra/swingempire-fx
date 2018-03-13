/*
 * Created on 06.03.2018
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
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
            if (getSortIconDisplay() == ContentDisplay.RIGHT) return;
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
        
        // --------------- make sort icon location styleable
        
        // use StyleablePropertyFactory to simplify styling-related code
        private static final StyleablePropertyFactory<MyTableColumnHeader> FACTORY = 
                new StyleablePropertyFactory<>(TableColumnHeader.getClassCssMetaData());

        // default value (strictly speaking: an implementation detail)
        // PENDING: what about RtoL orientation? Is it handled correctly in
        // core?
        private static final ContentDisplay DEFAULT_SORT_ICON_DISPLAY = ContentDisplay.RIGHT;

        private static CssMetaData<MyTableColumnHeader, ContentDisplay> CSS_SORT_ICON_DISPLAY = 
                FACTORY.createEnumCssMetaData(ContentDisplay.class,
                        "-fx-sort-icon-display",
                        header -> header.sortIconDisplayProperty(),
                        DEFAULT_SORT_ICON_DISPLAY);

        // property with lazy instantiation
        private StyleableObjectProperty<ContentDisplay> sortIconDisplay;

        protected StyleableObjectProperty<ContentDisplay> sortIconDisplayProperty() {
            if (sortIconDisplay == null) {
                sortIconDisplay = new SimpleStyleableObjectProperty<>(
                        CSS_SORT_ICON_DISPLAY, this, "sortIconDisplay",
                        DEFAULT_SORT_ICON_DISPLAY);

            }
            return sortIconDisplay;
        }

        protected ContentDisplay getSortIconDisplay() {
            return sortIconDisplay != null ? sortIconDisplay.get()
                    : DEFAULT_SORT_ICON_DISPLAY;
        }

        protected void setSortIconDisplay(ContentDisplay display) {
            sortIconDisplayProperty().set(display);
        }

        /**
         * Returnst the CssMetaData associated with this class, which may
         * include the CssMetaData of its superclasses.
         * 
         * @return the CssMetaData associated with this class, which may include
         *         the CssMetaData of its superclasses
         */
        public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
            return FACTORY.getCssMetaData();
        }

        /** {@inheritDoc} */
        @Override
        public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
            return getClassCssMetaData();
        }

        // below is old-style styling (before 8u40) - keep for reference?
        // not really needed because its copied from analogous handling in TableColumnHeader
//        // default value (strictly speaking: an implementation detail)
//        // PENDING: what about RtoL orientation? Is it handled correctly in core?
//        private static final ContentDisplay DEFAULT_SORT_ICON_DISPLAY = ContentDisplay.RIGHT;
//        private ObjectProperty<ContentDisplay> sortIconDisplay; 
//        
//        private ContentDisplay getSortIconDisplay() {
//            return sortIconDisplay == null ? DEFAULT_SORT_ICON_DISPLAY : sortIconDisplay.get();
//        }
//        
//        private ObjectProperty<ContentDisplay> sortIconDisplayProperty() {
//            if (sortIconDisplay == null) {
//                sortIconDisplay = new StyleableObjectProperty<>(DEFAULT_SORT_ICON_DISPLAY) {
//
//                    @Override
//                    public CssMetaData<? extends Styleable, ContentDisplay> getCssMetaData() {
//                        return StyleableProperties.SORT_ICON_DISPLAY;
//                    }
//
//                    @Override
//                    public Object getBean() {
//                        return MyTableColumnHeader.this;
//                    }
//
//                    @Override
//                    public String getName() {
//                        return "sortIconDisplay";
//                    }
//                    
//                };
//                
//            }
//            return sortIconDisplay;
//        }
//        
//        private static class StyleableProperties {
//            private static final CssMetaData<MyTableColumnHeader, ContentDisplay> SORT_ICON_DISPLAY = new CssMetaData<MyTableColumnHeader, ContentDisplay>(
//                    "-fx-sort-icon-display",
//                    new EnumConverter<ContentDisplay>(ContentDisplay.class),
//                    DEFAULT_SORT_ICON_DISPLAY) {
//
//                @Override
//                public boolean isSettable(MyTableColumnHeader columnHeader) {
//                    return columnHeader.sortIconDisplay == null
//                            || !columnHeader.sortIconDisplay.isBound();
//                }
//
//                @Override
//                public StyleableProperty<ContentDisplay> getStyleableProperty(
//                        MyTableColumnHeader n) {
//                    return (StyleableProperty<ContentDisplay>) n
//                            .sortIconDisplayProperty();
//                }
//            };
//
//            private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
//            static {
//
//                final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<CssMetaData<? extends Styleable, ?>>(
//                        TableColumnHeader.getClassCssMetaData());
//                styleables.add(SORT_ICON_DISPLAY);
//                STYLEABLES = Collections.unmodifiableList(styleables);
//
//            }
//
//        }
//
//        /**
//         * Returnst the CssMetaData associated with this class, which may include the
//         * CssMetaData of its superclasses.
//         * @return the CssMetaData associated with this class, which may include the
//         * CssMetaData of its superclasses
//         */
//        public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
//            return StyleableProperties.STYLEABLES;
//        }
//
//        /** {@inheritDoc} */
//        @Override 
//        public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
//            return getClassCssMetaData();
//        }

        
//-------- reflection acrobatics .. might use lookup and/or keeping aliases around
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
        
        
        // dynamic change of stylesheet
        Button button = new Button("switch stylesheet");
        button.setOnAction(e -> {
            Scene scene = table.getScene();
            URL uri = getClass().getResource("columnheader.css");
            scene.getStylesheets().add(uri.toExternalForm());
            
        });
        
        BorderPane pane = new BorderPane(table);
        pane.setBottom(button);
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
//        URL uri = getClass().getResource("columnheader.css");
//        stage.getScene().getStylesheets().add(uri.toExternalForm());
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
