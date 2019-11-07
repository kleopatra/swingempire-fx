/*
 * Created on 05.01.2019
 *
 */
package de.swingempire.fx.scene.control;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.sun.javafx.scene.control.Properties;
import com.sun.javafx.scene.control.TableColumnBaseHelper;
import com.sun.javafx.scene.control.TreeTableViewBackingList;
import com.sun.javafx.scene.control.skin.Utils;
import com.sun.javafx.tk.Toolkit;

import static org.junit.Assert.*;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.util.FXUtils;
import de.swingempire.fx.util.StageLoader;
import de.swingempire.testfx.table.TableAutoSizeSam.MyTableColumnHeader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Modified test to dig into test failure (in openjfx, experiment-sam) when
 * changing content from initial to smaller and back to initial again.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class TableColumnHeaderTest2 {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    private MyTableColumnHeader tableColumnHeader;

    private TableView<Person> tableView;

    private TableColumn<Person, String> column;

    private ObservableList<Person> model;

    @Before
    public void beforeTest() {
        tableColumnHeader = null;
//        model = Person.persons();
        model = FXCollections.observableArrayList(
                new Person("Justice Caldwell or", null),
                new Person("Humphrey McPhee", null),
                new Person("Orrin Davies", null),
                new Person("Emma Wilson", null)
        );

        column = new TableColumn<>("Col ");
        column.setCellValueFactory(new PropertyValueFactory<Person, String>("firstName"));
        tableView = new TableView<>(model) {
            @Override
            protected Skin<?> createDefaultSkin() {
                return new TableViewSkin(this) {
                    @Override
                    protected TableHeaderRow createTableHeaderRow() {
                        return new TableHeaderRow(this) {
                            @Override
                            protected NestedTableColumnHeader createRootHeader() {
                                return new NestedTableColumnHeader(null) {
                                    @Override
                                    protected TableColumnHeader createTableColumnHeader(
                                            TableColumnBase col) {
                                        tableColumnHeader = new MyTableColumnHeader(
                                                column);
                                        return col == null
                                                || col.getColumns().isEmpty()
                                                || col == getTableColumn()
                                                        ? tableColumnHeader
                                                        : new NestedTableColumnHeader(
                                                                col);

                                    }
                                };
                            }
                        };
                    }
                };
            }
        };

        tableView.getColumns().add(column);
    }

    /**
     * Initial sizing bug (todo: check for issue)
     * Note: this behaves as expected in application, sizing is just fine ..
     */
    @Test
    public void testDecreaseContent() {
        initStage();
        double width = column.getWidth();
        String initial = model.get(0).getFirstName();
        model.stream().forEach(p -> p.setFirstName("small"));
        tableColumnHeader.resizeCol();
        assertTrue("Column width " + column.getWidth() + " must be smaller than initial " + width,
                width > column.getWidth());
        model.get(0).setFirstName(initial);
        tableColumnHeader.resizeCol();
        assertEquals(width, column.getWidth(), 0.001);
    }
    

    @Test
    public void test_resizeColumnToFitContent() {
        initStage();

        double width = column.getWidth();
        tableColumnHeader.resizeCol();
        assertEquals("Width must be the same", width, column.getWidth(), 0.001);

        EventType<TableColumn.CellEditEvent<Person, String>> eventType = TableColumn
                .editCommitEvent();
        column.getOnEditCommit()
                .handle(new TableColumn.CellEditEvent<Person, String>(tableView,
                        new TablePosition<Person, String>(tableView, 0, column),
                        (EventType) eventType,
                        "This is a big text inside that column"));
        tableColumnHeader.resizeCol();
        assertTrue("Column width " + column.getWidth() + " must be smaller " + width, 
                width <= column.getWidth());

        column.getOnEditCommit()
                .handle(new TableColumn.CellEditEvent<Person, String>(tableView,
                        new TablePosition<Person, String>(tableView, 0, column),
                        (EventType) eventType, "small"));
        column.getOnEditCommit()
                .handle(new TableColumn.CellEditEvent<Person, String>(tableView,
                        new TablePosition<Person, String>(tableView, 1, column),
                        (EventType) eventType, "small"));
        column.getOnEditCommit()
                .handle(new TableColumn.CellEditEvent<Person, String>(tableView,
                        new TablePosition<Person, String>(tableView, 2, column),
                        (EventType) eventType, "small"));
        column.getOnEditCommit()
                .handle(new TableColumn.CellEditEvent<Person, String>(tableView,
                        new TablePosition<Person, String>(tableView, 3, column),
                        (EventType) eventType, "small"));

        tableColumnHeader.resizeCol();
        assertTrue("Column width " + column.getWidth() + " must be smaller " + width, 
                width >= column.getWidth());
    }

    /**
     * 
     */
    protected void initStage() {
        Toolkit tk = Toolkit.getToolkit();

        show();

        tk.firePulse();
    }

    /**
     * 
     */
    protected void show() {
        Scene scene = new Scene(tableView);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setWidth(500);
        stage.setHeight(400);
        stage.centerOnScreen();
        stage.show();
    }

    public static class MyTableColumnHeader extends TableColumnHeader {

        public MyTableColumnHeader(final TableColumnBase tc) {
            super(tc);
        }

        public void resizeCol() {
            /*
             * Note: this compiles only with a tweaked TableColumnHeader in
             * experiment-tableheader-sam-test!
             */
//            doColumnAutoSize(getTableColumn(), -1);
            resizeColumnToFitContent(-1);
        }

        private void resizeColumnToFitContent(int rows) {
            FXUtils.invokeGetMethodValue(TableColumnHeader.class, this,
                    "doColumnAutoSize",
                    new Class[] { TableColumnBase.class, Integer.TYPE },
                    new Object[] { getTableColumn(), rows });
        }
    }
}