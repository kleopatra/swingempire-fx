/*
 * Created on 16.06.2016
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * Multi-purpose test driver.
 * <p>
 * auto-size columns programmatically (fx9 version)
 * https://stackoverflow.com/a/48665254/203657
 * <p>
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TableViewDemo extends Application {

    private TableView<Person> table;
    private TableColumn emailHeader;
    private TableColumn emailColumn;
    
    public static class PlainTableCell<S, T> extends TableCell<S, T> {
        
        public PlainTableCell() {
        }
        
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

    }

    private Parent getContent() {
        table = createTableWithColumns();
        table.setTableMenuButtonVisible(true);
        
        table.getColumns().addListener((ListChangeListener) c -> {
            LOG.info("from columns: ");
            FXUtils.prettyPrint(c);
        });
        
        // quick check: we get a change from visible columns because
        // the old content is replaced with the same new content
        // strictly speaking, that's an implementation detail!!
        // can't rely on it, but will have to ...
        table.getVisibleLeafColumns().addListener((ListChangeListener) c -> {
            LOG.info("from visibleLeafs: ");
            FXUtils.prettyPrint(c);
        });
        
        BorderPane pane = new BorderPane(table);
        Button button = new Button("debug");
        button.setOnAction(e -> debugHeaders(table));
        
        
        Button addNested = new Button("add hidden column to nested");
        addNested.setOnAction(e -> {
            TableColumn column = new TableColumn("hidden");
            column.setVisible(false);
            emailHeader.getColumns().add(column);
        });
        
        Button addNormal = new Button("add hidden normal column");
        addNormal.setOnAction(e -> {
            TableColumn column = new TableColumn("normal");
            column.setVisible(false);
            table.getColumns().add(column);
        });
        
        Button autoSizeEmail = new Button("autoSize email column");
        autoSizeEmail.setOnAction(e -> {
            doAutoSize(table, emailColumn);
        });
        Button prefEmail =  new Button("toggle pref of email column ");
        prefEmail.setOnAction(e -> {
            boolean isDefault = emailColumn.getPrefWidth() == 80;
            // toggling pref width of a column does resize it to its pref
            // this is done in the tableColumn itself, which updates its width
            // in prefWidth.invalidated
            // columnHeader is listening to width and updates itself in next layout pass
            emailColumn.setPrefWidth(isDefault? 200 : 80);
        });
        
        Button autoSizeAll = new Button("autoSize all columns");
        autoSizeAll.setOnAction(e -> {
            doAutoSize(table);
        });
        FlowPane buttons = new FlowPane();
        buttons.getChildren().addAll(button, addNested, addNormal, autoSizeEmail, prefEmail, autoSizeAll);
        buttons.setHgap(10);
        buttons.setVgap(10);
        pane.setBottom(buttons);
        // select first row (it's focused only)
//        table.getSelectionModel().select(0);
        // table always has initial focus (aka: focusOwner) due to being in the center?
//        pane.setTop(new TextField("some dummy to focus"));
        return pane;
    }

    /**
     * Resizes the email column such that column width fits content.
     * The (reflective) access changed considerably from fx8 -> fx9.
     * This implementation is fx9.
     */
    private void doAutoSize() {
//        TableViewSkin skin = (TableViewSkin) table.getSkin();
//        TableHeaderRow tableHeader = getTableHeaderRow(skin);
//        TableColumnHeader columnHeader = tableHeader.getColumnHeaderFor(emailColumn);
//        if (columnHeader != null) {
//            columnHeader.doColumnAutoSize(emailColumn, -1);
//        }
    }

    private void doAutoSizeWithLookup(TableView table, TableColumn column) {
        // good enough to find an arbitrary column header
        // due to sub-optimal api
        TableColumnHeader header = (TableColumnHeader) table.lookup(".column-header");
        if (header != null) {
            // not accessible, need reflection
            //  header.doColumnAutoSize(emailColumn, -1);
            // works only if prefWidth == default == 80 (hard-coded)
            // resetting here has no effect, might be due to lazy layout
            double oldPref = column.getPrefWidth();
            column.setPrefWidth(80);
            doColumnAutoSize(header, column);
            column.setPrefWidth(oldPref);
        }
    }
    
    /**
     * Resizes column to fit its content. Note that this does nothing if the column's 
     * prefWidth is != 80.
     * 
     * @param table
     * @param column
     */
    public static void doAutoSize(TableView table, TableColumn column) {
        // good enough to find an arbitrary column header
        // due to sub-optimal api
        TableColumnHeader header = (TableColumnHeader) table.lookup(".column-header");
        if (header != null) {
            doColumnAutoSize(header, column);
        }
    }
    
    /**
     * Resizes all visible columns to fit its content. Note that this does nothing if a column's 
     * prefWidth is != 80.
     * 
     * @param table
     */
    public static void doAutoSize(TableView<?> table) {
        // good enough to find an arbitrary column header
        // due to sub-optimal api
        TableColumnHeader header = (TableColumnHeader) table.lookup(".column-header");
        if (header != null) {
            table.getVisibleLeafColumns().stream().forEach(column -> doColumnAutoSize(header, column));
        }
    }
    
    public static void doColumnAutoSize(TableColumnHeader columnHeader, TableColumn column) {
        // use your preferred reflection utility method 
        FXUtils.invokeGetMethodValue(TableColumnHeader.class, columnHeader, "doColumnAutoSize", 
                new Class[] {TableColumnBase.class, Integer.TYPE}, 
                new Object[] {column, -1});
    }
    
    /**
     * @param skin
     * @return
     */
    private TableHeaderRow getTableHeaderRow(TableViewSkin skin) {
        return (TableHeaderRow) FXUtils.invokeGetMethodValue(TableViewSkinBase.class, skin, "getTableHeaderRow");
    }

    /**
     * @param table
     */
    private void debugHeaders(TableView table) {
//        TableHeaderRow row = ((TableViewSkin) table.getSkin()).getTableHeader();
//        NestedTableColumnHeader root = row.getRootHeader();
//        TableColumnHeader first = (TableColumnHeader) root.getColumnHeaders().get(0);
    }
    
    
    protected TableView<Person> createTableWithColumns() {
        // can't sort a filtered list?
//        FilteredList<Person> filtered = new FilteredList(Person.persons());
        TableView table = new TableView(Person.persons());
        TableColumn first = new TableColumn("First Name");
        first.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        first.setCellFactory(e -> new PlainTableCell());
        table.setEditable(true);
        table.getColumns().addAll(first);
        emailHeader = new TableColumn("Emails");
        table.getColumns().addAll(emailHeader);
        TableColumn nestedPrimary = new TableColumn("Nested Primary");
        nestedPrimary.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn nestedSecondary = new TableColumn("Secondary");
        nestedSecondary.setCellValueFactory(new PropertyValueFactory<>("secondaryMail"));
        emailHeader.getColumns().addAll(nestedPrimary, nestedSecondary);
        
        emailColumn = new TableColumn("Primary");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn secondary = new TableColumn("Secondary");
        secondary.setCellValueFactory(new PropertyValueFactory<>("secondaryMail"));
        
//        emailHeader.getColumns().addAll(email, secondary);
        table.getColumns().addAll(emailColumn, secondary);
        TableColumn last = new TableColumn("Last Name");
        last.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        table.getColumns().addAll(last);
        return table;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 1000, 400));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }
    

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewDemo.class.getName());
}
