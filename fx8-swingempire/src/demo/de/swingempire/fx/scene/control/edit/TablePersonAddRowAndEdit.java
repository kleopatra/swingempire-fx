/*
 * Created on 05.02.2016
 *
 */
package de.swingempire.fx.scene.control.edit;

import java.util.logging.Logger;

import com.sun.javafx.tk.Toolkit;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.scene.control.cell.DebugTextFieldTableCell;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
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
 * Currently the only way is to introduce a real delay, f.i. by a timeline.
 * There must be something else.
 * 
 * <p>
 * 
 * Update: table.layout _is_ working, provided the table's skin has registered its
 * listener to the items _before_ we do so.
 * 
 * <p>
 * 
 * Setup here is:
 * add(item)
 * in itemsListener do layout and edit newly added item
 * 
 * if added by button -> okay, we need the layout so that focus
 *   is in the textfield
 * if added in commitHandler -> crazy behaviour, a bit less crazy 
 *   if removing call to layout 
 * 
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TablePersonAddRowAndEdit extends Application {

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

   
    private Parent getContent() {
//        InputMap m;
//        ListViewSkin s;
        TableView<Person> table = new TableView<>();
        table.setItems(data);
        table.setEditable(true);
        
        TableColumn<Person, String> firstName = new TableColumn<>("First Name");
        firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstName.addEventHandler(TableColumn.editCommitEvent(), e -> {
            int index = e.getTablePosition().getRow();
            if (index == table.getItems().size() - 1) {//getInsertIndex(table) - 1) {
                p("index in commithandler" + index);
                Person person = createNewItem("edit", index);
                table.getItems().add(person);
//                table.edit(table.getItems().size(), firstName);
            }
        });
        
//        firstName.setCellFactory(v -> new MyTextFieldCell<>());
        firstName.setCellFactory(TextFieldTableCell.forTableColumn());
        ListChangeListener l = c -> {
            while (c.next()) {
                // true added only
                if (c.wasAdded() && ! c.wasRemoved()) {
                    p("in itemslistener: " + c.getFrom());
                    // force the re-layout before starting the edit
                    // moved into table override below
                    // leads to weird effects if item added in commitHandler
//                    table.layout();
//                    Toolkit.getToolkit().firePulse();
                    table.getSelectionModel().select(c.getFrom());
                    table.scrollTo(c.getFrom());
                    table.requestFocus();
                    table.edit(c.getFrom(), firstName);
                    return;
                }
            };
        };
        // install the listener to the items after the skin has registered
        // its own
        ChangeListener skinListener = (src, ov, nv) -> {
            table.getItems().removeListener(l);
            table.getItems().addListener(l);
        };
        table.skinProperty().addListener(skinListener);
        table.getColumns().addAll(firstName);
        
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
            int index = getInsertIndex(table);
//            int index = 1;
            table.scrollTo(index);
            table.requestFocus();
            table.edit(index - 1, firstName);
        });
        HBox buttons = new HBox(10, add, edit);
        BorderPane content = new BorderPane(table);
        content.setBottom(buttons);
        return content;
    }
    
    protected int getInsertIndex(TableView table) {
        int standInIndex = table.getItems().indexOf(standIn);
        return  standInIndex < 0 ? table.getItems().size() : standInIndex;
    }
    public static class TTableView<S> extends TableView<S> {

        /**
         * Overridden to force a layout before calling super.
         */
        @Override
        public void edit(int row, TableColumn<S, ?> column) {
            layout();
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
        primaryStage.setScene(new Scene(getContent()));
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
