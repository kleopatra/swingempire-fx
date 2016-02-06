/*
 * Created on 05.02.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.property.PathAdapter;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
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
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TablePersonAddRowAndEdit extends Application {

    private PersonStandIn standIn = new PersonStandIn();
    private final ObservableList<Person> data =
            FXCollections.observableArrayList(
                    new Person("Jacob", "Smith", "jacob.smith@example.com"),
                    new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
                    new Person("Ethan", "Williams", "ethan.williams@example.com"),
                    new Person("Emma", "Jones", "emma.jones@example.com"),
                    new Person("Michael", "Brown", "michael.brown@example.com")
                    , standIn
                    );

    private static boolean startEditInAdder = true;
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Parent getContent() {

//        TableView<Person> table = new XTableView<>();
        TableView<Person> table = new TableView<>();
        table.setItems(data);
        table.setEditable(true);
//        table.getSelectionModel().setCellSelectionEnabled(true);
        
        TableColumn<Person, String> firstName = new TableColumn<>("First Name");
        firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        firstName.setCellFactory(v -> new MyTextFieldCell<>());
        ListChangeListener l = c -> {
            while (c.next()) {
                // true added only
                if (c.wasAdded() && ! c.wasRemoved()) {
//                    Toolkit.getToolkit().firePulse();
                    Platform.runLater(() -> {
                        System.out.println("added: " + c.getList().get(c.getFrom()));
                        if (!startEditInAdder)
                            table.edit(c.getFrom(), firstName);
                    });
                    return;
                }
            }
        };
        table.getItems().addListener(l);
        
        table.getColumns().addAll(firstName);
        
        Button add = new Button("AddAndEdit");
        add.setOnAction(e -> {
            int standInIndex = table.getItems().indexOf(standIn);
            int index = standInIndex < 0 ? table.getItems().size() : standInIndex;
            Person person = createNewItem("edit", index);
//            table.getSelectionModel().clearAndSelect(lastReal);
//            table.getFocusModel().focus(index);
//            table.requestFocus();
            table.getItems().add(index, person);
//            table.refresh();
//            System.out.println("lastReal/size " + index + " / " +table.getItems().size());
//            System.out.println("lastRealItem/before " + table.getItems().get(index) + " / " + table.getItems().get(index -1));
            // this looks like a bug: need to pass-in the index _before_ the index of 
            // the newly created
            if (startEditInAdder) {
                table.edit(index, firstName);
            }
            
        });
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            int lastReal = table.getItems().size() -2;
            table.edit(lastReal, firstName);
        });
        HBox buttons = new HBox(10, add, edit);
        BorderPane content = new BorderPane(table);
        content.setBottom(buttons);
        return content;
    }
    
    public static class MyTextFieldCell<S> extends TextFieldTableCell<S, String> {
//    public static class MyTextFieldCell<S> extends XTextFieldTableCell<S, String> {

        private Button button;
        
        private PathAdapter adapter;
        
        
        
        private ChangeListener pathListener = (src, ov, nv) -> {
            if (nv instanceof StandIn) {
                LOG.info(" standin at " + getIndex() + isEmpty());
                if (getIndex() >= 0) {
//                    new RuntimeException("whoiscalling \n").printStackTrace();
                    System.out.println("index not yet updated: cell/items " + getIndex() + " / " + getTableView().getItems().indexOf(nv));
                }
            }
            if (isEmpty() || nv instanceof StandIn) {
                setEditable(false);
            } else {
                setEditable(true);
            }
//            setEditable(!(nv instanceof StandIn) );
        };
        
        public MyTextFieldCell() {
            super(new DefaultStringConverter());
            ContextMenu menu = new ContextMenu();
            menu.getItems().add(createMenuItem());
            setContextMenu(menu);
            ReadOnlyProperty<TableRow<S>> row = tableRowProperty();
            Callback<TableRow<S>, ObjectProperty<S>> factory = r -> r.itemProperty();
            adapter = new PathAdapter(factory);
            adapter.setRoot(tableRowProperty());
            adapter.addListener(pathListener);
        }
        
        private boolean isStandIn() {
            return getTableRow() != null && getTableRow().getItem() instanceof StandIn;
        }

        @Override
        public void updateItem(String item, boolean empty) {
            setGraphic(null);
            super.updateItem(item, empty);
            if (isStandIn()) {
                if (isEditing()) {
                    LOG.info("shouldn't be editing - has StandIn");
                    
                }
                if (button == null) {
                    button = createButton();
                }
//                setText(null);
//                setGraphic(button);
            } 
        }

        private Button createButton() {
            Button b = new Button("Add");
            b.setOnAction(e -> {
                int lastReal = getTableView().getItems().size() -1;
                S person = createNewItem("edit", lastReal);
                getTableView().getItems().add(lastReal, person);
                getTableView().refresh();
                System.out.println("lastReal/size " + lastReal + " / " +getTableView().getItems().size());
                System.out.println("lastRealItem/before " + getTableView().getItems().get(lastReal) + " / " + getTableView().getItems().get(lastReal -1));
                // this looks like a bug: need to pass-in the index _before_ the index of 
                // the newly created
                if (startEditInAdder) {
                    getTableView().edit(lastReal-1, getTableColumn());
                }
            });
            return b;
        }
        private MenuItem createMenuItem() {
            // TODO Auto-generated method stub
            MenuItem item = new MenuItem("Add");
            item.setOnAction(e -> {
                if (isStandIn()) return;
                int lastReal = getIndex();
                S person = createNewItem("menu", lastReal);
                getTableView().getItems().add(lastReal, person);
                System.out.println("lastReal/size " + lastReal + " / " +getTableView().getItems().size());
                System.out.println("lastRealItem/before " + getTableView().getItems().get(lastReal) + " / " + getTableView().getItems().get(lastReal -1));
                // this looks like a bug: need to pass-in the index _before_ the index of 
                // the newly created
                if (startEditInAdder) {
                    getTableView().edit(lastReal-1, getTableColumn());
                }
            });
            return item;
        }


        /**
         * @param string
         * @return
         */
        private S createNewItem(String string, int lastReal) {
            return (S) new Person("free" + lastReal, "free" + lastReal, "free");
        }

        
        
    }

    private Person createNewItem(String string, int lastReal) {
        return new Person("free" + lastReal, "free" + lastReal, "free");
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

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TablePersonAddRowAndEdit.class.getName());
}
