/*
 * Created on 14.10.2014
 *
 */
package de.swingempire.fx.control;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.javafx.event.EventDispatchChainImpl;
import com.sun.javafx.scene.control.behavior.TableCellBehavior;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Cell;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableCellSkinBase;
import javafx.scene.control.skin.TableRowSkin;
import javafx.scene.control.skin.TableRowSkinBase;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * fx-9: wait for minimum of fix to abstract-private methods...
 * ------
 * 
 * per-cell contextMenu must be triggered by keyboard 
 * (f.i. shift-f10 on win) 
 * reported: https://javafx-jira.kenai.com/browse/RT-40071
 * 
 * plain example is in report, here trying to route keyEvents to
 * the focused cell
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableViewContextMenu extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        TableView<Person> table = new TableView<Person>() {

            @Override
            public EventDispatchChain buildEventDispatchChain(
                    EventDispatchChain tail) {
                if (getSkin() instanceof EventTarget) {
                    ((EventTarget) getSkin()).buildEventDispatchChain(tail);
                }
                return super.buildEventDispatchChain(tail);
            }

            @Override
            protected Skin<?> createDefaultSkin() {
                return new MyTableViewSkin(this);
//                return super.createDefaultSkin();
            }
            
            

        };
//        table.addEventHandler(KeyEvent.ANY, e -> {
//            LOG.info("got key on table? " + e);
//        });
        table.addEventHandler(ContextMenuEvent.ANY, e -> {
            LOG.info("got context on table? " + e);
        });
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Person,String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
//        firstNameCol.addEventHandler(KeyEvent.ANY, e -> {
//            LOG.info("got key on column? " + firstNameCol.getText());
//        });
        firstNameCol.setContextMenu(new ContextMenu(new MenuItem("columnItem")));
        ContextMenu cellMenu = new ContextMenu(new MenuItem("cellItem"));
        firstNameCol.setCellFactory(p -> new PlainTableCell(cellMenu));
        
        
        TableColumn<Person,String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
//        lastNameCol.addEventHandler(KeyEvent.ANY, e -> {
//            LOG.info("got key on column? " + lastNameCol.getText());
//        });
        lastNameCol.setContextMenu(new ContextMenu(new MenuItem("lastcolumnItem")));
        ContextMenu lastcellMenu = new ContextMenu(new MenuItem("lastcellItem"));
        lastNameCol.setCellFactory(p -> new PlainTableCell(lastcellMenu));
        
        
        table.getColumns().addAll(firstNameCol, lastNameCol); 
        table.setItems(data);

        Button button = new Button("dummy for comparison");
        button.setOnContextMenuRequested(e -> {
            LOG.info("contextMenuEvent on button " + e);
        });
        ContextMenu context = new ContextMenu(new MenuItem("buttonItem"));
        context.setOnShowing(e -> {
//            new RuntimeException("who dunnit? ").printStackTrace();
        });
        button.setContextMenu(context);
        VBox pane = new VBox(table, button);
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
        stage.setTitle(FXUtils.version(true));
    }

    private static class MySpecialCellBehavior extends TableCellBehavior {

        EventHandler contextHandler = e -> LOG.info("got context in behaviour? " + e); 
        public MySpecialCellBehavior(TableCell control) {
            super(control);
            control.addEventHandler(KeyEvent.ANY, e -> {
                LOG.info("got key in behaviour? " + e);
            });
//            control.addEventHandler(ContextMenuEvent.ANY, e -> {
//                LOG.info("got context in behaviour? " + e);
//            });
            control.addEventHandler(ContextMenuEvent.ANY, contextHandler);
//            LOG.info("sanity ....:" + control);
        }



//        @Override
//        public void contextMenuRequested(ContextMenuEvent e) {
//            LOG.info("contextMenu requested in behaviour?" + e);
//            super.contextMenuRequested(e);
//        }
//

        @Override
        protected void doSelect(double x, double y, MouseButton button,
                int clickCount, boolean shiftDown, boolean shortcutDown) {
//            if (button == MouseButton.SECONDARY) return;
            super.doSelect(x, y, button, clickCount, shiftDown, shortcutDown);
        }

    }

    private static class MySpecialCellSkin extends TableCellSkinBase {
        private final TableColumn tableColumn;

        public MySpecialCellSkin(TableCell tableCell) {
            super(tableCell, new MySpecialCellBehavior(tableCell));
            this.tableColumn = tableCell.getTableColumn();
            super.init(tableCell);
        }

        @Override protected BooleanProperty columnVisibleProperty() {
            return tableColumn.visibleProperty();
        }

        @Override protected ReadOnlyDoubleProperty columnWidthProperty() {
            return tableColumn.widthProperty();
        }
        
    }

    
    private static class MyTableViewSkin extends TableViewSkin implements EventTarget {

        /**
         * @param tableView
         */
        public MyTableViewSkin(TableView tableView) {
            super(tableView);
        }

        @Override
        public EventDispatchChain buildEventDispatchChain(
                EventDispatchChain tail) {
            EventDispatchChainImpl chain = (EventDispatchChainImpl) tail;
            TablePosition focused = getFocusModel().getFocusedCell();
            if (focused != null) {
                TableColumn column = focused.getTableColumn();
                if (column != null) {
                    column.buildEventDispatchChain(tail);
                    int row = focused.getRow();
                    if (row > -1) {
                        IndexedCell rowCell = flow.getCell(row);
                        if (rowCell instanceof TableRow) {
                            TableRow tableRow = (TableRow) rowCell;
                            TableRowSkin skin = (TableRowSkin) tableRow
                                    .getSkin();
                            IndexedCell cell = (IndexedCell) invokeGetCellFromCellsMap(skin, column);
                            LOG.info("building chain: " + column.getText() + cell.getItem() + cell.getIndex());
                            cell.buildEventDispatchChain(tail);
                        }
                    }
                }
            }
//            LOG.info("chain: " + invokeActiveCount(chain));
            return tail;
        }

        private Cell invokeGetCellFromCellsMap(TableRowSkin skin, TableColumn column) {
            Class clazz = TableRowSkinBase.class;
            try {
                Field field = clazz.getDeclaredField("cellsMap");
                field.setAccessible(true);
                Map map = (Map) field.get(skin);
                return (Cell) map.get(column);
            } catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
        private Cell invokeGetCell(TableRowSkin skin, TableColumn column) {
            Class clazz = TableRowSkinBase.class;
            try {
                Method method = clazz.getDeclaredMethod("getCell", TableColumnBase.class);
                method.setAccessible(true);
                return (Cell) method.invoke(skin, column);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }
        private int invokeActiveCount(EventDispatchChainImpl chain) {
            Class clazz = chain.getClass();
            try {
                Field field = clazz.getDeclaredField("activeCount");
                field.setAccessible(true);
                return (int) field.get(chain);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return -1;
        }
        
        
    }
    /**
     * C&P of default tableCell in TableColumn + contextMenu
     */
    private static class PlainTableCell<S, T> extends TableCell<S, T> {
        
        public PlainTableCell(ContextMenu menu) {
            addEventHandler(KeyEvent.ANY, e -> {
                LOG.info("got key on plaincell? " + getItem() + getIndex());
            });
            addEventHandler(ContextMenuEvent.ANY, e -> {
                
                LOG.info("got context on plaincell? " + getItem() + getIndex());
            });

            setContextMenu(menu);
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
        @Override
        protected Skin<?> createDefaultSkin() {
            return new MySpecialCellSkin(this);
//            return super.createDefaultSkin();
        }
        
    }

    public static class Person {
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private Person(String fName, String lName) {
            this.firstName = new SimpleStringProperty(fName);
            this.lastName = new SimpleStringProperty(lName);
        }
        public String getFirstName() {return firstName.get();}
        public void setFirstName(String fName) {firstName.set(fName);}
        public Property<String> firstNameProperty() { return firstName; }
        public String getLastName() {return lastName.get();}
        public void setLastName(String fName) {lastName.set(fName);}
        public Property<String> lastNameProperty() { return lastName; }
    }

    ObservableList<Person> data = FXCollections.observableArrayList(
            new Person("Jacob", "Smith"),
            new Person("Isabella", "JohnsoJn"),
            new Person("Ethan", "Williams"),
            new Person("Emma", "Jones"),
            new Person("Michael", "Brown")
    );


    public static void main(String[] args) {
        Application.launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewContextMenu.class.getName());
}
