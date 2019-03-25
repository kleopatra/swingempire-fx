/*
 * Created on 14.03.2019
 *
 */
package de.swingempire.fx.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.util.Callback;

/**
 * Factory methods to create TableViews.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableFactory {

    /**
     * 
     * @param table the table to create the skin for
     * @param tableHeaderFactory the function to create the table header, may be null to denote
     *    using super
     * @param flowFactory the supplier to create the virtual flow, may be null to denote using super
     * @return a tableViewSkin for the given table that uses the header/flow as created by the
     *   given factories
     */
    public static <T> TableViewSkin<T> createTableViewSkin(TableView<T> table, 
            Function<TableViewSkin<T>, TableHeaderRow> tableHeaderFactory,
            Supplier<VirtualFlow<TableRow<T>>> flowFactory) {
        TableViewSkin<T> skin = new TableViewSkin<>(table) {
    
            @Override
            protected TableHeaderRow createTableHeaderRow() {
                return tableHeaderFactory == null ? 
                        super.createTableHeaderRow() : tableHeaderFactory.apply(this);
            }
    
            @Override
            protected VirtualFlow<TableRow<T>> createVirtualFlow() {
                return flowFactory == null ? 
                        super.createVirtualFlow() : flowFactory.get();
            }
            
        };
        return skin;
    }

    /**
     * Creates and returns a TableView with a default skin that injects custom TableHeaderRow and
     * custom VirtualFlow as provided by the given factories. 
     * 
     * @param tableHeaderFactory the function to create the table header, may be null to denote
     *    using super
     * @param flowFactory the supplier to create the virtual flow, may be null to denote using super
     * @return a table with default skin which uses the header/flow as created by the factories 
     */
    public static <T> TableView<T> createTable(Function<TableViewSkin<T>, TableHeaderRow> tableHeaderFactory,
            Supplier<VirtualFlow<TableRow<T>>> flowFactory) {
        return createTable(table -> createTableViewSkin(table, tableHeaderFactory, flowFactory));
    }

    public static <T> TableView<T> createTable(Function<TableView<T>, TableViewSkin<T>> skinFactory) {
        TableView<T> table = new TableView<T>() {
    
            @Override
            protected Skin<?> createDefaultSkin() {
                return skinFactory == null ? 
                        super.createDefaultSkin() : skinFactory.apply(this);
            }
            
        };
        return table;
    }

    /**
     * Creates and returns a TableColumn for given property with initial pref width.
     * 
     * @param property the property name, used as title as well.
     * @param pref the initial pref width, -1 denoting default
     * @return
     */
    public static <T> TableColumn<T, ?> createTableColumn(String property, double pref) {
        TableColumn<T, ?> column = new TableColumn<>(property);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        if (pref > 0)
            column.setPrefWidth(pref);
        return column;
    }

    /**
     * Creates and returns a TableColumn for given property with initial pref width.
     * 
     * @param property the property name, used as title as well.
     * @return
     */
    public static <T> TableColumn<T, ?> createTableColumn(String property) {
        return createTableColumn(property, -1);
    }

    public static <T> void createAndAddTableColumns(TableView<T> table, 
            Function<String, TableColumn<T, ?>> factory, String... properties) {
    }
    
    public static <T> List<TableColumn<T, ?>> createTableColumns(
            Function<String, TableColumn<T, ?>> factory, 
            String... properties) {
        List<TableColumn<T, ?>> result = Arrays.stream(properties)
                .map(factory::apply)
                .collect(Collectors.toList());
        return result;
    }
    
    public static <T> List<TableColumn<T, ?>> createTableColumns(String... properties ) {
        List<TableColumn<T, ?>> result = Arrays.stream(properties)
                .map(((Function<String, TableColumn<T, ?>>) TableColumn::new)::apply)
//                .map(col -> { 
//                    col.setCellValueFactory(new PropertyValueFactory(col.getText());
//                    return (TableColumn<T, ?>)col;
//                            }
//                ))
                .collect(Collectors.toList());
        Consumer<? super TableColumn<T, ?>> config = 
                col -> col.setCellValueFactory(new PropertyValueFactory<>(col.getText()));
        Function<TableColumn<?, ?>, String> text = TableColumn::getText;
        
        
        result.forEach(config);
        return result;        
    }
    
    public static <T> TableColumn<T, ?> createColumn(String property) {
        Function<String, TableColumn<T, ?>> factory = TableColumn::new;
        Function<String, Callback<CellDataFeatures<T, Object>, ObservableValue<Object>>> 
            cellFactory = PropertyValueFactory::new;
        
        
        return createTableColumn(TableColumn::new, property);
    }
   
    public static <T> TableColumn<T, ?> createTableColumn(Function<String, TableColumn<T, ?>> factory, String property) {
        return factory.apply(property);
    }
}
