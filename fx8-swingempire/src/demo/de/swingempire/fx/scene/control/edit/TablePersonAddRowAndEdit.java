/*
 * Created on 05.02.2016
 *
 */
package de.swingempire.fx.scene.control.edit;

import java.util.List;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.TableViewBehaviorBase;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.scene.control.cell.DebugTextFieldTableCell;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

/**
 * http://stackoverflow.com/q/35279377/203657
 * 
 * Problem: starting edit of newly added item - not started. Looks like the table
 * is not-yet ready when listening to changes. At that time the cells are not yet
 * updated to new content, making the target cell (still old!) appear not editable
 * thus not starting.
 * 
 * What doesn't work:
 * 
 * - table.layout();
 * - table.requestLayout();
 * - toolkit.firePulse
 * - Platform.runlater
 * 
 * <p>
 * 
 * Setup here is:
 * - add(item) in commitHandler
 * - in itemsListener (do layout for core textFieldTableCell) and edit newly added item
 * - "working" (newly added item is editing) except that focus is not in textField
 * - "not working" (newly added item not editing) if cellSelectionEnabled: suspect that then
 *   the editCancel from Cell focusListener is jumping in
 * 
 * if added by button -> okay, we need the layout so that focus
 *   is in the textfield
 * if added in commitHandler -> crazy behaviour, a bit less crazy 
 *   if removing call to layout 
 * 
 * --> can't call layout
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TablePersonAddRowAndEdit extends Application {

    private TableView<Person> table;
    private TableColumn<Person, String> firstName;
    private ChangeListener skinListener = (src, ov, nv) -> skinChanged();
   
    private boolean setData;
    
    private void skinChanged() {
        table.skinProperty().removeListener(skinListener);
        installItemsListener();
    }

    /**
     * @param table
     * @param firstName
     * @return
     */
    protected void installItemsListener() {
        ListChangeListener l = c -> {
            while (c.next()) {
                // true added only
                if (c.wasAdded() && !c.wasRemoved()) {
                    p("in itemslistener: " + c.getFrom());
                    // force the re-layout before starting the edit
                    // leads to weird effects if item added in commitHandler
//                     table.layout();
                    table.scrollTo(c.getFrom());
                    table.requestFocus();
                    table.edit(c.getFrom(), firstName);
                    return;
                }
            }
            ;
        };
        table.getItems().addListener(l);
    }

    protected void commitEdit(CellEditEvent<Person, String> t) {
        if (setData) {
            int index = t.getTablePosition().getRow();
            List<Person> list = t.getTableView().getItems();
            if (list == null || index < 0 || index >= list.size()) return;
            Person rowData = list.get(index);
            ObservableValue ov = t.getTableColumn().getCellObservableValue(rowData);

            if (ov instanceof WritableValue) {
                ((WritableValue)ov).setValue(t.getNewValue());
            }

        }
        int index = t.getTablePosition().getRow();
        if (index == table.getItems().size() - 1) {//getInsertIndex(table) - 1) {
            p("index in commithandler" + index);
            Person person = createNewItem("edit", index +1);
            table.getItems().add(person);
            table.getSelectionModel().select(index + 1);
            table.getFocusModel().focus(index + 1, firstName);
//                   table.edit(table.getItems().size(), firstName);
        }
    }

    private Parent getContent() {
//        InputMap m;
//        ListViewSkin s;
        table = new TTableView<>();
        table.setItems(data);
        table.setEditable(true);
        TableViewBehaviorBase b;
         firstName = new TableColumn<>("First Name");
        firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
//        firstName.setCellFactory(v -> new MyTextFieldCell<>());
//        firstName.setCellFactory(TextFieldTableCell.forTableColumn());
        firstName.setCellFactory(DebugTextFieldTableCell.forTableColumn());
        table.getColumns().addAll(firstName);
        
        // tablecell is focused only if cellSelectionEnabled
        table.getSelectionModel().setCellSelectionEnabled(true);
        firstName.addEventHandler(TableColumn.editCommitEvent(), this::commitEdit);
        
//        setData = true;
//        firstName.setOnEditCommit(this::commitEdit);
        
        table.skinProperty().addListener(skinListener);
        
        Button add = new Button("AddAndEdit");
        add.setOnAction(e -> {
            int standInIndex = table.getItems().indexOf(standIn);
            int index =  getInsertIndex(table);
//                    standInIndex < 0 ? table.getItems().size() : standInIndex;
//            index =1;
            Person person = createNewItem("edit", index);
            table.getItems().add(index, person);
            
        });
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            int index = getInsertIndex(table) -1;
//            int index = 1;
            table.scrollTo(index);
            table.getSelectionModel().select(index);
//            table.requestFocus();
            table.edit(index, firstName);
        });
        
        MenuBar bar = new MenuBar();
        Menu menu = new Menu("Select/Focus");
        bar.getMenus().add(menu);
        MenuItem select = new MenuItem("select second last");
        select.setOnAction(e -> {
            table.getSelectionModel().select(table.getItems().size()-2);
        });
        select.setAccelerator(KeyCombination.valueOf("F6"));
        MenuItem focus = new MenuItem("focus second last");
        focus.setOnAction(e -> {
            table.getFocusModel().focus(table.getItems().size()-2, firstName);
        });
        focus.setAccelerator(KeyCombination.valueOf("F7"));
        
        menu.getItems().addAll(select, focus);
        HBox buttons = new HBox(10, add, edit);
        BorderPane content = new BorderPane(table);
        content.setBottom(buttons);
        content.setTop(bar);
        return content;
    }

    protected int getInsertIndex(TableView table) {
        int standInIndex = table.getItems().indexOf(standIn);
        return  standInIndex < 0 ? table.getItems().size() : standInIndex;
    }
    
    public static class TTableView<S> extends TableView<S> {

        /**
         * was: Overridden to force a layout before calling super.
         * can't, see class doc of example
         */
        @Override
        public void edit(int row, TableColumn<S, ?> column) {
            Exception ex = new RuntimeException("dummy");
            StackTraceElement[] stackTrace = ex.getStackTrace();
            String caller = "CALLER-OF-EDIT " + row + " / " + column + "\n";
            int max = Math.min(3, stackTrace.length);
            for (int i = 1; i < max; i++) { // first is this method
                    caller+= stackTrace[i].getClassName() + 
                    " / "+  stackTrace[i].getMethodName() + " / " + stackTrace[i].getLineNumber() + "\n";
            }
            LOG.info(caller);
//            layout();
            super.edit(row, column);
        }
        
    }
    
    /**
     * A cell that can handle not-editable items. Has to update its
     * editability based on the rowItem. Must be done in updateItem
     * (tried a listener to the tableRow's item, wasn't good enough - doesn't
     * get notified reliably)
     * 
     */
    public static class MyTextFieldCell<S> extends TextFieldTableCell<S, String> {

        private Button button;
        
        public MyTextFieldCell() {
            super(new DefaultStringConverter());
            ContextMenu menu = new ContextMenu();
            menu.getItems().add(createMenuItem());
            setContextMenu(menu);
        }
        
        private boolean isStandIn() {
            return getTableRow() != null && getTableRow().getItem() instanceof StandIn;
        }
        
        /**
         * Update cell's editable based on the rowItem.
         */
        private void doUpdateEditable() {
            if (isEmpty() || isStandIn()) {
                setEditable(false);
            } else {
                setEditable(true);
            }
        }
        
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            doUpdateEditable();
            if (isStandIn()) {
                if (isEditing()) {
                    LOG.info("shouldn't be editing - has StandIn");
                    
                }
                if (button == null) {
                    button = createButton();
                }
                setText(null);
                setGraphic(button);
            } 
        }
        
        private Button createButton() {
            Button b = new Button("Add");
            b.setOnAction(e -> {
                int index = getTableView().getItems().size() -1;
                getTableView().getItems().add(index, createNewItem("button", index));
            });
            return b;
        }
        
        private MenuItem createMenuItem() {
            MenuItem item = new MenuItem("Add");
            item.setOnAction(e -> {
                if (isStandIn()) return;
                int index = getIndex();
                getTableView().getItems().add(index, createNewItem("menu", index));
            });
            return item;
        }

        
        private S createNewItem(String text, int index) {
            return (S) new Person(text + index, text + index, text);
        }

    }

    private Person createNewItem(String text, int index) {
        return new Person(text + index, text + index, text);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 400, 230));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    /**
     * Dirty: Marker-Interface to denote a class as not mutable.
     */
    public static interface StandIn {
    }
    
    public static class PersonStandIn extends Person implements StandIn{

         public PersonStandIn() {
            super("standIn", "", "");
        }
        
    }

    private PersonStandIn standIn = new PersonStandIn();
    private final ObservableList<Person> data =
            // Person from Tutorial - with Properties exposed!
            FXCollections.observableArrayList(
                    new Person("Jacob", "Smith", "jacob.smith@example.com"),
                    new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
                    new Person("Ethan", "Williams", "ethan.williams@example.com"),
                    new Person("Emma", "Jones", "emma.jones@example.com"),
                    new Person("Michael", "Brown", "michael.brown@example.com")
//                    , standIn
                    );


    public static void main(String[] args) {
        launch(args);
    }

    private static void p(String text) {
        System.out.println(text);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TablePersonAddRowAndEdit.class.getName());
}
