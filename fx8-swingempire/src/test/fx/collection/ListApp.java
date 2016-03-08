/*
 * Created on 02.06.2013
 *
 */
package fx.collection;

import java.text.Collator;
import java.util.Comparator;

import de.swingempire.fx.demobean.Person;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ListView;
import javafx.scene.control.ListViewBuilder;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.stage.Stage;

/**
 * fx-9: looks very old ... defer
 * 
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
public class ListApp extends Application {

    /**
     * @return
     */
    protected Parent createContent() {
//        final ListView table = createTableView();
        final ListView table = createSimpleTableView();
        Button print = ButtonBuilder.create()
                .text("Print selected")
                .onAction(new EventHandler<ActionEvent>() {
                    
                    @Override
                    public void handle(ActionEvent event) {
                        int sel = table.getSelectionModel().getSelectedIndex();
                        Object person = table.getSelectionModel().getSelectedItem();
                        System.out.println("sel/item: " + sel + "/" + person);
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
                    }
                })
                .build();
        Button clear = ButtonBuilder.create()
                .text("Clear selected")
                .onAction(new EventHandler<ActionEvent>() {
                    
                    @Override
                    public void handle(ActionEvent event) {
                        table.getSelectionModel().clearSelection();
                    }
                })
                .build();
        Button minusOne = ButtonBuilder.create()
                .text("Select -1")
                // has no effect
                .onAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        table.getSelectionModel().select(-1);
                    }
                })
                .build();
        Button nullItem = ButtonBuilder.create()
            .text("Select null")
            // has no effect
            .onAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    table.getSelectionModel().select(null);
                }
            })
            .build();
        Button toggleMode = ButtonBuilder.create()
                .text("Toggle Mode")
                .onAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        SelectionMode mode = table.getSelectionModel().getSelectionMode();
                        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE == mode ? 
                                SelectionMode.MULTIPLE : SelectionMode.SINGLE);
                    }
                })
                .build();
        Button sort = ButtonBuilder.create()
                .text("Sort")
                .onAction(new EventHandler<ActionEvent>() {
                    boolean sorted = false;
                    Comparator reverse = new ReverseComparator(Collator.getInstance());
                    @Override
                    public void handle(ActionEvent event) {
                        if (sorted) {
                          // PENDING JW: reverse seems to fire a different
                          // event than sort?
                          // reverse clears the selection 
                          // yeah, reverse is implemented with a setAll
                            // aka: new model
                            // vs. ObservableListwrapper which sets
                            // the values at the new position in an iterator
                            // and fires the permutation
                          FXCollections.sort(table.getItems(), reverse);   
                        } else {
                          FXCollections.sort(table.getItems());
                        }
                        sorted = !sorted;
                    }
                })
                .disable(!(table.getItems().get(0) instanceof String))
                .build();
        HBox buttonPane = HBoxBuilder.create()
            .children(print, printAll, clear, minusOne, nullItem, toggleMode,
                    sort)
            .build();
        BorderPane tablePane = BorderPaneBuilder.create() 
           .center(table)     
           .bottom(buttonPane)
           .build();
        return tablePane;
    }

    public static class ReverseComparator implements Comparator {
        private Comparator delegate;

        public ReverseComparator(Comparator delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(Object o1, Object o2) {
            int result = delegate.compare(o1, o2);
            return -result;
        }
    }
    private ListView createSimpleTableView() {
        ObservableList items = FXCollections.observableArrayList(
                "5-item", "4-item", "3-item", "2-item", "1-item");
        ListView table = ListViewBuilder.create()
                .items(items)
                .build();
        return table;
    }
    /**
     * @return
     */
    private ListView createTableView() {
        ListView table = ListViewBuilder.create()
                .items(getTeamMembers())
                .build();
//              table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

           
              table.getSelectionModel().selectedItemProperty()
                                          .addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue,
                                    Object newValue) {
                  Object selectedPerson = newValue;
                  System.out.println(selectedPerson + " chosen in ListView");
                }
              });
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
