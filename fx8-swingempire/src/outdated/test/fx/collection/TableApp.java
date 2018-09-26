/*
 * Created on 02.06.2013
 *
 */
package fx.collection;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
//import javafx.scene.control.MyTableViewSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * fx-9: looks very old, defer.
 * ------
 * Issue: tableView selection appears as selected after sorting.
 *  when actually, the selection is cleared. The visual details of the
 *  misbehaviour depend on selectionModel
 * - singleSelection: the same absolute index 
 * - multipleSelection: jumps to arbitrary index
 * 
 * Reason seems to be a not correctly updated selectedIndices/items (the
 * lists storing the multiple selection) - those lists seem to be what
 * controls the selection visuals
 * 
 * Issue: usability - how to unselect via keyboard/mouse?
 * That's a feature/bug (?) of singleSelectionMode, behaves just normal in
 * multiple
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableApp extends Application {

    private TableView createTable() {
//            final TableView table = createSimpleTableView();
            final TableView table = createTableView();
//            TableViewSelectionModel original = table.getSelectionModel();
            // adding a new instance of the exact same model 
            // (just extended, new constructor, no other change)
            // makes the selection-on-sort behave as expected
//            TableViewSelectionModel copied = new CopiedTableViewSelectionModel<>(table);
//            TableViewSelectionModel my = new MyTableViewSelectionModel(table);
//            table.setSelectionModel(copied);
//            MyTableViewSelectionModel.installSuperSelection(table);
            // resetting to original instance reverts to misbehaviour
//            table.setSelectionModel(original);
            InvalidationListener invalidationListener = new InvalidationListener() {

                @Override
                public void invalidated(Observable observable) {
                    SelectionModel bean = (SelectionModel) ((ReadOnlyProperty) observable).getBean();
                    System.out.println("got invalidation from selectedIndex: " + bean.getSelectedItem());
                }
                
            };
            ChangeListener changeListener = new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue,
                        Object newValue) {
                    SelectionModel bean = (SelectionModel) ((ReadOnlyProperty) observable).getBean();
                    System.out.println("got change from selectedIndex: " + bean.getSelectedItem());
                    System.out.println(newValue + " chosen in TableView");
                    RuntimeException error = new RuntimeException("who is calling?");
    //                  error.printStackTrace();
                }
            };
//            table.setFocusModel(new TableView.TableViewFocusModel<>(table));
            TableViewSelectionModel installed = table.getSelectionModel();
            installed.selectedIndexProperty().addListener(changeListener);
            installed.selectedIndexProperty().addListener(invalidationListener);
            // setting new items doesn't help
//            table.setItems(getTeamMembers());
            table.getItems().addListener(new FXUtils.PrintingListChangeListener());
            ListChangeListener.Change change = null;
            return table;
        }

    /**
     * @return
     */
    protected Parent createContent() {
        final TableView table = createTable();
        Button print = ButtonBuilder.create()
                .text("Print selected")
                .onAction(new EventHandler<ActionEvent>() {
                    
                    @Override
                    public void handle(ActionEvent event) {
                        int sel = table.getSelectionModel().getSelectedIndex();
                        Object person = table.getSelectionModel().getSelectedItem();
                        System.out.println("sel/item: " + sel + "/" + person);
                        table.requestFocus();
                    }
                })
                .build();
        Button printAll = ButtonBuilder.create()
                .text("Print all selected")
                .onAction(new EventHandler<ActionEvent>() {
                    
                    @Override
                    public void handle(ActionEvent event) {
                        ObservableList sel = table.getSelectionModel().getSelectedIndices();
                        ObservableList person = table.getSelectionModel().getSelectedItems();
                        System.out.println("sel/item: " + sel + "/" + person);
                        System.out.println("cells: " + table.getSelectionModel().getSelectedCells());
                        table.requestFocus();
                    }
                })
                .build();
        Button clear = ButtonBuilder.create()
                .text("Clear selected")
                .onAction(new EventHandler<ActionEvent>() {
                    
                    @Override
                    public void handle(ActionEvent event) {
                        table.getSelectionModel().clearSelection();
                        table.requestFocus();
                    }
                })
                .build();
        Button minusOne = ButtonBuilder.create()
                .text("Select -1")
                // has no effect
                .onAction((ActionEvent event) -> {
                    table.getSelectionModel().select(-1);
                    table.requestFocus();
        })
                .build();
        Button nullItem = ButtonBuilder.create()
                .text("Select null")
                // has no effect
                .onAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        table.getSelectionModel().select(null);
                        table.requestFocus();
                    }
                })
                .build();
        
        Button toggleMode = ButtonBuilder.create()
                .text("Toggle Mode")
                // has no effect
                .onAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        SelectionMode mode = table.getSelectionModel().getSelectionMode();
                        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE == mode ? 
                                SelectionMode.MULTIPLE : SelectionMode.SINGLE);
                        table.requestFocus();
                    }
                })
                .build();
        
        Button clearLast = ButtonBuilder.create()
                .text("Clear Lead")
                // has no effect
                .onAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        int lead = table.getSelectionModel().getSelectedIndex();
                        System.out.println("lead to clear: " + lead);
                        table.getSelectionModel().clearSelection(lead);
                        table.requestFocus();
                    }
                })
                .build();
        Button reselectFirst = ButtonBuilder.create()
            .text("Reselect First")
            // has no effect
            .onAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (table.getSelectionModel().getSelectedIndices().size() == 0) return;
                    int first = (int) table.getSelectionModel().getSelectedIndices().get(0);
                    table.getSelectionModel().select(first);
                    table.requestFocus();
                }
            })
            .build();
        
        
        HBox buttonPane = HBoxBuilder.create()
            .children(print, printAll, clear, minusOne, 
                    nullItem, toggleMode, clearLast, reselectFirst)
            .build();
        BorderPane tablePane = BorderPaneBuilder.create() 
           .center(table)     
           .bottom(buttonPane)
           .build();
        return tablePane;
    }

    private TableView createSimpleTableView() {
        ObservableList items = createSimpleItems();
        Callback<CellDataFeatures<Object, Object>, ObservableValue<Object>> factory = new Callback<CellDataFeatures<Object, Object>, ObservableValue<Object>>() {
            @Override
            public ObservableValue<Object> call(CellDataFeatures<Object, Object> param) {
                return new ReadOnlyObjectWrapper(param.getValue());
            }
        };
        TableView table = TableViewBuilder.create()
//                .items(items)
                .columns(TableColumnBuilder.create()
                        .text("numberedItems")
                        .cellValueFactory(factory)       
                        .build()
                 )
                .build();
        table.setItems(items);
        return table;

    }

    /**
     * @return
     */
    protected ObservableList createSimpleItems() {
        ObservableList items = FXCollections.observableArrayList(
                "5-item", "4-item", "3-item", "2-item", "1-item");
        return items;
    }
    /**
     * @return
     */
    private TableView createTableView() {
        TableView table = TableViewBuilder.create()
                .columns(
                  TableColumnBuilder.create()
                    .text("First Name")
                    .cellValueFactory(new PropertyValueFactory("firstName"))
                    .prefWidth(180)
                    .build(),
                  TableColumnBuilder.create()
                    .text("Last Name")
                    .cellValueFactory(new PropertyValueFactory("lastName"))
                    .prefWidth(180)
                    .build(),
                  TableColumnBuilder.create()
                    .text("Phone Number")
                    .cellValueFactory(new PropertyValueFactory("phone"))
                    .prefWidth(150)
                    .build()
                )
                .items(getTeamMembers())
                .build();
          // sequence of setting items vs selection doesn't matter
//        table.setItems(getTeamMembers());
              return table;
    }

    public ObservableList getTeamMembers() {
        ObservableList teamMembers = FXCollections.observableArrayList();
        for (int i = 1; i <= 10; i++) {
          teamMembers.add(new Person("FirstName" + i,
                                     "LastName" + i,
                                     "Phone" + i));
        }
        return teamMembers;
      }

    @Override
    public void start(Stage stage) throws Exception {
        Parent tablePane = createContent();
        Scene scene = SceneBuilder.create()
           .root(tablePane)     
           .build();  
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
