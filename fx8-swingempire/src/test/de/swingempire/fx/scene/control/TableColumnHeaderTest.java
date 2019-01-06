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
 * Quick check for a https://github.com/javafxports/openjdk-jfx/issues/338
 * 
 * No problem if createTableColumnHeader is valid.
 * Commented.
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class TableColumnHeaderTest {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();


    private MyTableColumnHeader tableColumnHeader;

    @Before
    public void beforeTest() {
        tableColumnHeader = null;
    }

    @Test
    public void testNestedColumnRemove() {
        TableView<Person> tableView = new TableView<>(Person.persons());
        TableColumn<Person, String> nested = new TableColumn<>("Nested ");
        TableColumn<Person, String> first = new TableColumn<>("first ");
        nested.getColumns().add(first);
        first.setCellValueFactory(cc -> cc.getValue().firstNameProperty());
        tableView.getColumns().addAll(nested);
        
        Toolkit tk = Toolkit.getToolkit();

        Scene scene = new Scene(tableView);
        new StageLoader(scene);
//        Stage stage = new Stage();
//        stage.setScene(scene);
//        stage.setWidth(500);
//        stage.setHeight(400);
//        stage.centerOnScreen();
//        stage.show();

        tk.firePulse();
        assertEquals(1, nested.getColumns().size());
        nested.getColumns().clear();
        tk.firePulse();
        assertTrue(nested.getColumns().isEmpty());
       
    }
    
    @Test @Ignore
    public void test_resizeColumnToFitContent() {
        ObservableList<Person> model = Person.persons();
//                FXCollections.observableArrayList(
//                new Person("Humphrey McPhee", 76),
//                new Person("Justice Caldwell", 30),
//                new Person("Orrin Davies", 30),
//                new Person("Emma Wilson", 8)
//        );
        TableColumn<Person, String> column = new TableColumn<>("Col ");
        column.setCellValueFactory(new PropertyValueFactory<Person, String>("firstName"));
        TableView<Person> tableView = new TableView<>(model) {
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
                                    protected TableColumnHeader createTableColumnHeader(TableColumnBase col) {
                                        // this is correct override
//                                        if (col == null || col.getColumns().isEmpty() || col == getTableColumn()) {
//                                            tableColumnHeader = new MyTableColumnHeader(col);
//                                            return tableColumnHeader;
//                                        }
                                        // this is contract violating override: basically returns a 
                                        // nested header always
                                        tableColumnHeader = new MyTableColumnHeader(column);
                                        return new NestedTableColumnHeader(col);
                                    }
                                };
                            }
                        };
                    }
                };
            }
        };

        tableView.getColumns().add(column);

        Toolkit tk = Toolkit.getToolkit();

        Scene scene = new Scene(tableView);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setWidth(500);
        stage.setHeight(400);
        stage.centerOnScreen();
        stage.show();

        tk.firePulse();

        double width = column.getWidth();
        tableColumnHeader.resizeCol();
        assertEquals("Width must be the same",
                width, column.getWidth(), 0.001);

        EventType<TableColumn.CellEditEvent<Person, String>> eventType = TableColumn.editCommitEvent();
        column.getOnEditCommit().handle(new TableColumn.CellEditEvent<Person, String>(
                tableView, new TablePosition<Person, String>(tableView, 0, column), (EventType) eventType, "This is a big text inside that column"));
        tableColumnHeader.resizeCol();
        assertTrue("Column width must be greater",
                width < column.getWidth());

        column.getOnEditCommit().handle(new TableColumn.CellEditEvent<Person, String>(
                tableView, new TablePosition<Person, String>(tableView, 0, column), (EventType) eventType, "small"));
        column.getOnEditCommit().handle(new TableColumn.CellEditEvent<Person, String>(
                tableView, new TablePosition<Person, String>(tableView, 1, column), (EventType) eventType, "small"));
        column.getOnEditCommit().handle(new TableColumn.CellEditEvent<Person, String>(
                tableView, new TablePosition<Person, String>(tableView, 2, column), (EventType) eventType, "small"));
        column.getOnEditCommit().handle(new TableColumn.CellEditEvent<Person, String>(
                tableView, new TablePosition<Person, String>(tableView, 3, column), (EventType) eventType, "small"));

        tableColumnHeader.resizeCol();
        assertTrue("Column width must be smaller",
                width > column.getWidth());
    }

    private static class MyTableColumnHeader extends TableColumnHeader {

        public MyTableColumnHeader(final TableColumnBase tc) {
            super(tc);
        }

        public void resizeCol() {
            resizeColumnToFitContent(getTableColumn(), -1);
        }
        
        
        /**
         * Resizes the given column based on the preferred width of all items contained in it. This can be potentially very
         * expensive if the number of rows is large. Subclass can either call this method or override it (no need to call
         * {@code super()}) to provide their custom algorithm.
         *
         * @param tc      the column to resize
         * @param maxRows the number of rows considered when resizing. If -1 is given, all rows are considered.
         * @since 12
         */
        protected void resizeColumnToFitContent(TableColumnBase<?, ?> tc, int maxRows) {
            if (!tc.isResizable()) return;

            Object control = this.getTableSkin().getSkinnable();
            if (control instanceof TableView) {
                resizeColumnToFitContent((TableView)control, (TableColumn)tc, this.getTableSkin(), maxRows);
            } else if (control instanceof TreeTableView) {
                resizeColumnToFitContent((TreeTableView)control, (TreeTableColumn)tc, this.getTableSkin(), maxRows);
            }
        }

        private <T,S> void resizeColumnToFitContent(TableView<T> tv, TableColumn<T, S> tc, TableViewSkinBase tableSkin, int maxRows) {
            List<?> items = tv.getItems();
            if (items == null || items.isEmpty()) return;

            Callback/*<TableColumn<T, ?>, TableCell<T,?>>*/ cellFactory = tc.getCellFactory();
            if (cellFactory == null) return;

            TableCell<T,?> cell = (TableCell<T, ?>) cellFactory.call(tc);
            if (cell == null) return;

            // set this property to tell the TableCell we want to know its actual
            // preferred width, not the width of the associated TableColumnBase
            cell.getProperties().put(Properties.DEFER_TO_PARENT_PREF_WIDTH, Boolean.TRUE);

            // determine cell padding
            double padding = 10;
            Node n = cell.getSkin() == null ? null : cell.getSkin().getNode();
            if (n instanceof Region) {
                Region r = (Region) n;
                padding = r.snappedLeftInset() + r.snappedRightInset();
            }

            int rows = maxRows == -1 ? items.size() : Math.min(items.size(), maxRows);
            double maxWidth = 0;
            for (int row = 0; row < rows; row++) {
                cell.updateTableColumn(tc);
                cell.updateTableView(tv);
                cell.updateIndex(row);

                if ((cell.getText() != null && !cell.getText().isEmpty()) || cell.getGraphic() != null) {
                    tableSkin.getChildren().add(cell);
                    cell.applyCss();
                    maxWidth = Math.max(maxWidth, cell.prefWidth(-1));
                    tableSkin.getChildren().remove(cell);
                }
            }

            // dispose of the cell to prevent it retaining listeners (see RT-31015)
            cell.updateIndex(-1);

            // RT-36855 - take into account the column header text / graphic widths.
            // Magic 10 is to allow for sort arrow to appear without text truncation.
            TableColumnHeader header = //tableSkin.getTableHeaderRow().getColumnHeaderFor(tc);
                    getColumnHeaderFor(tc);
            Label label = getLabel(header);
            double headerTextWidth = Utils.computeTextWidth(label.getFont(), tc.getText(), -1);
            Node graphic = label.getGraphic();
            double headerGraphicWidth = graphic == null ? 0 : graphic.prefWidth(-1) + label.getGraphicTextGap();
            double headerWidth = headerTextWidth + headerGraphicWidth + 10 + header.snappedLeftInset() + header.snappedRightInset();
            maxWidth = Math.max(maxWidth, headerWidth);

            // RT-23486
            maxWidth += padding;
            if (tv.getColumnResizePolicy() == TableView.CONSTRAINED_RESIZE_POLICY && tv.getWidth() > 0) {
                if (maxWidth > tc.getMaxWidth()) {
                    maxWidth = tc.getMaxWidth();
                }

                int size = tc.getColumns().size();
                if (size > 0) {
                    resizeColumnToFitContent(tc.getColumns().get(size - 1), maxRows);
                    return;
                }

//                TableSkinUtils.resizeColumn(tableSkin, tc, Math.round(maxWidth - tc.getWidth()));
            } else {
                TableColumnBaseHelper.setWidth(tc, maxWidth);
            }
        }

        private <T,S> void resizeColumnToFitContent(TreeTableView<T> ttv, TreeTableColumn<T, S> tc, TableViewSkinBase tableSkin, int maxRows) {
            List<?> items = new TreeTableViewBackingList(ttv);
            if (items == null || items.isEmpty()) return;

            Callback cellFactory = tc.getCellFactory();
            if (cellFactory == null) return;

            TreeTableCell<T,S> cell = (TreeTableCell) cellFactory.call(tc);
            if (cell == null) return;

            // set this property to tell the TableCell we want to know its actual
            // preferred width, not the width of the associated TableColumnBase
            cell.getProperties().put(Properties.DEFER_TO_PARENT_PREF_WIDTH, Boolean.TRUE);

            // determine cell padding
            double padding = 10;
            Node n = cell.getSkin() == null ? null : cell.getSkin().getNode();
            if (n instanceof Region) {
                Region r = (Region) n;
                padding = r.snappedLeftInset() + r.snappedRightInset();
            }

            TreeTableRow<T> treeTableRow = new TreeTableRow<>();
            treeTableRow.updateTreeTableView(ttv);

            int rows = maxRows == -1 ? items.size() : Math.min(items.size(), maxRows);
            double maxWidth = 0;
            for (int row = 0; row < rows; row++) {
                treeTableRow.updateIndex(row);
                treeTableRow.updateTreeItem(ttv.getTreeItem(row));

                cell.updateTreeTableColumn(tc);
                cell.updateTreeTableView(ttv);
                cell.updateTreeTableRow(treeTableRow);
                cell.updateIndex(row);

                if ((cell.getText() != null && !cell.getText().isEmpty()) || cell.getGraphic() != null) {
                    tableSkin.getChildren().add(cell);
                    cell.applyCss();

                    double w = cell.prefWidth(-1);

                    maxWidth = Math.max(maxWidth, w);
                    tableSkin.getChildren().remove(cell);
                }
            }

            // dispose of the cell to prevent it retaining listeners (see RT-31015)
            cell.updateIndex(-1);

            // RT-36855 - take into account the column header text / graphic widths.
            // Magic 10 is to allow for sort arrow to appear without text truncation.
            TableColumnHeader header = //tableSkin.getTableHeaderRow().getColumnHeaderFor(tc);
                    getColumnHeaderFor(tc);
            Label label = getLabel(header);
            double headerTextWidth = Utils.computeTextWidth(label.getFont(), tc.getText(), -1);
            Node graphic = label.getGraphic();
            double headerGraphicWidth = graphic == null ? 0 : graphic.prefWidth(-1) + label.getGraphicTextGap();
            double headerWidth = headerTextWidth + headerGraphicWidth + 10 + header.snappedLeftInset() + header.snappedRightInset();
            maxWidth = Math.max(maxWidth, headerWidth);

            // RT-23486
            maxWidth += padding;
            if (ttv.getColumnResizePolicy() == TreeTableView.CONSTRAINED_RESIZE_POLICY && ttv.getWidth() > 0) {

                if (maxWidth > tc.getMaxWidth()) {
                    maxWidth = tc.getMaxWidth();
                }

                int size = tc.getColumns().size();
                if (size > 0) {
                    resizeColumnToFitContent(tc.getColumns().get(size - 1), maxRows);
                    return;
                }

//                TableSkinUtils.resizeColumn(tableSkin, tc, Math.round(maxWidth - tc.getWidth()));
            } else {
                TableColumnBaseHelper.setWidth(tc, maxWidth);
            }
        }
        
        protected TableViewSkinBase getTableSkin() {
            return (TableViewSkinBase) FXUtils.invokeGetMethodValue(TableColumnHeader.class, this, "getTableSkin");
        }
        
        protected Label getLabel(TableColumnHeader header) {
            return (Label) FXUtils.invokeGetFieldValue(TableColumnHeader.class, header, "label");
        }
        
        protected TableColumnHeader getColumnHeaderFor(TableColumnBase column) {
            TableViewSkinBase skin = getTableSkin();
            TableHeaderRow tableHeader = (TableHeaderRow) FXUtils.invokeGetMethodValue(TableViewSkinBase.class, skin, "getTableHeaderRow");
            return (TableColumnHeader) FXUtils.invokeGetMethodValue(TableHeaderRow.class, tableHeader, "getColumnHeaderFor", TableColumnBase.class, column);
        }

    }
}