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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TableViewDemo extends Application {

    private TableView<Person> table;
    private TableColumn emailHeader;

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
        
        Button addNested = new Button("add nested column");
        addNested.setOnAction(e -> {
            TableColumn column = new TableColumn("hidden");
            column.setVisible(false);
            emailHeader.getColumns().add(column);
        });
        
        Button addNormal = new Button("add normal column");
        addNormal.setOnAction(e -> {
            TableColumn column = new TableColumn("normal");
            column.setVisible(false);
            table.getColumns().add(column);
        });
        
        HBox buttons = new HBox(10, button, addNested, addNormal);
        pane.setBottom(buttons);
        return pane;
    }

    /**
     * @param table
     */
    private void debugHeaders(TableView table) {
//        TableHeaderRow row = ((TableViewSkin) table.getSkin()).getTableHeader();
//        NestedTableColumnHeader root = row.getRootHeader();
//        TableColumnHeader first = (TableColumnHeader) root.getColumnHeaders().get(0);
    }
    
    /**
     * @param first
     */
    private void installColumnFilter(TableColumn<Person, String> first) {
//        Function<Person, String> f = p -> first.getCellObservableValue(p).getValue();
//        BiPredicate<String, String> pred = (value, match) -> value.startsWith(match);
//        SimpleFilterModel model = new SimpleFilterModel(f, pred);
//        ColumnFilter filter = new ColumnFilter(model);
//        first.setColumnFilter(filter);
    }
    
    protected TableView<Person> createTableWithColumns() {
        // can't sort a filtered list?
//        FilteredList<Person> filtered = new FilteredList(Person.persons());
        TableView table = new TableView(Person.persons());
        TableColumn first = new TableColumn("First Name");
        first.setCellValueFactory(new PropertyValueFactory<>("firstName"));
//        installColumnFilter(first);
//        FilterModel model = first.getColumnFilter().getFilterModel();
//        filtered.predicateProperty().bind(model.predicateProperty());
        
        table.setEditable(true);
        table.getColumns().addAll(first);
        emailHeader = new TableColumn("Emails");
        table.getColumns().addAll(emailHeader);
        
        TableColumn email = new TableColumn("Primary");
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
//        installColumnFilter(email);
        
        TableColumn secondary = new TableColumn("Secondary");
        secondary.setCellValueFactory(new PropertyValueFactory<>("secondaryEmail"));
//        installColumnFilter(secondary);
        
//        emailHeader.getColumns().addAll(email, secondary);
        table.getColumns().addAll(email, secondary);
        // not filterable column
        TableColumn last = new TableColumn("Last Name");
        last.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        table.getColumns().addAll(last);
        return table;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 600, 400));
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
