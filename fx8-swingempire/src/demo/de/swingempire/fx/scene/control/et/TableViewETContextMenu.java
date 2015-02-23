/*
 * Created on 14.10.2014
 *
 */
package de.swingempire.fx.scene.control.et;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.sun.javafx.scene.control.behavior.TableCellBehavior;
import com.sun.javafx.scene.control.skin.TableCellSkinBase;

import de.swingempire.fx.util.FXUtils;

/**
 * per-cell contextMenu must be triggered by keyboard 
 * (f.i. shift-f10 on win) 
 * reported: https://javafx-jira.kenai.com/browse/RT-40071
 * 
 * plain example is in report, here trying to route keyEvents to
 * the focused cell
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableViewETContextMenu extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        TableViewET<Person> table = new TableViewET<Person>();
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

    /**
     * PENDING: need to position the contextMenu relative to
     * the cell (currently is relative to tableView)
     * 
     * ContextMenuEvent has a copyFor(newSource, ...) method - where to apply?
     * Once the event reaches the cell/behaviour it's too late: the handler
     * in control is already installed, sees it before us, no way to 
     * switch to another.
     * 
     * C&P of default tableCell in TableColumn + contextMenu
     */
    private static class PlainTableCell<S, T> extends TableCell<S, T> {
        public PlainTableCell(ContextMenu menu) {
//            addEventHandler(KeyEvent.ANY, e -> {
//                LOG.info("got key on plaincell? " + getItem() + getIndex());
//            });
            addEventHandler(ContextMenuEvent.ANY, e -> {
                e.consume();
                LOG.info("got context on plaincell? " + getItem() + getIndex() + "\n   " +e);
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
    private static class MySpecialCellBehavior extends TableCellBehavior {

        public MySpecialCellBehavior(TableCell control) {
            super(control);
        }

        @Override
        public void contextMenuRequested(ContextMenuEvent e) {
            LOG.info("contextMenu requested in behaviour?" + e);
            e.consume();
            super.contextMenuRequested(e);
        }

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
            .getLogger(TableViewETContextMenu.class.getName());
}
