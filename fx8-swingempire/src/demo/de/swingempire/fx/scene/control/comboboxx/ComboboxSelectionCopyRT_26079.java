package de.swingempire.fx.scene.control.comboboxx;


import java.lang.reflect.Field;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-26079
 * selectedIndex != -1 for empty list is intentional!
 * 
 * same: 
 * https://javafx-jira.kenai.com/browse/RT-24898
 * 
 * happens if newList.equals(oldList) and newList != oldList
 * 
 * 
 * Part might be a usage error: using an invalidation listener, that's
 * notified "early" in notification sequence, might be _before_ 
 * the combo fully updated itself? Don't trust the output!
 * 
 * Setup: combo with initial items, nothing selected
 * Steps:
 * - press selectFirst to select first item in list: expected/actual "E1" is shown
 * - press reset to replace the items with a list that is equal but not the same:
 *   expected: selection (both index and item) cleared [*]
 *   actual: selectedItem still "E1"
 *   
 * [*]
 * Expectation based on (assumed?) implementation intention of reaction to clear/replace items:
 * 
 * <code><pre>
 * if (oldItems.contains(oldSelectedItem)) {
 *     // both index and item cleared
 *     assertEquals(-1, selectedIndex);
 *     assertEquals(null, selectedItem);
 * } else { // oldSelected had not been in items
 *     // old item will remain selected 
 *     assertEquals(oldSelectedItem, selectedItem);
 *     // selectedIndex depends on whether or not the item is in list
 *     if (newItems.contains(oldSelectedItem) {
 *        assertEquals(items.indexOf(oldSelectedItem), selectedIndex); 
 *     } else {
 *        assertEquals(-1, selectedIndex); 
 *     }
 * }
 * </pre></code>
 * 
 * Inconsistencies:
 * 
 * On startup we have three items, first E1, empty selection
 * - press selectFirst to select E1
 * - press resetSame to replace items with same elements: selectedItem/index cleared, value old
 * - press clear: nothing happens (selection _is_ empty without item)
 * - press selectFirst: all sync'ed again
 * - press resetSameFirst: all remain synched on old value (unexpected)
 * 
 * Run again
 * - press select first to select E1 - all sync'ed
 * - press resetSameFirst: selection remains E1, all sync'ed (fishy)
 * - press select first to select E1 again - all sync'ed, no detectable change
 * - press resetSameFirst again - selection cleared, all sync'ed (expected)
 * 
 */
public class ComboboxSelectionCopyRT_26079 extends Application 
//    implements InvalidationListener
    implements ChangeListener
{
  private Label state;
  private ToggleGroup tg;
  private ComboBox<String> cb = new ComboBox<>();
//  private ComboBoxX<String> cb = new ComboBoxX<>();

  public static void main(String[] args)
  {
    Application.launch(args);
  }
  
  @Override
  public void start(Stage primaryStage) throws Exception
  {
    state = new Label();
    
    tg = new ToggleGroup();
    tg.selectedToggleProperty().addListener(this);
    
    RadioButton rb1 = new RadioButton("one");
    rb1.setSelected(true);
    rb1.setToggleGroup(tg);
    
    RadioButton rb2 = new RadioButton("second");
    rb2.setToggleGroup(tg);
            
    String[] threeStartE1 = {"E1", "E2", "E3"};
    String[] threeStartE0 = {"E0", "E2", "E3"};
    String[] fourStartE1 = {"E1", "E2", "E3", "E4"};
    String[] fourStartE0 = {"E0", "E2", "E3", "E4"};
    // not using builder: combo value is empty
    ObservableList<String> items = createList(threeStartE1);
    cb.setItems(items);
    cb.setEditable(false);

    cb.getSelectionModel().selectFirst();
    cb.disableProperty().bind(rb1.selectedProperty());
    cb.getSelectionModel().selectedItemProperty().addListener(this);
    // clearSelection here, that is before showing: item/index cleared as expected
    cb.getSelectionModel().clearSelection();
    
    Button resetToSame = new Button("reset items to same");
    resetToSame.setOnAction(e -> {
        cb.getItems().setAll(createList(cb.getItems().toArray(new String[0])));
        LOG.info("prevCount/setAllCalled? " + getPreviousItemCount() + " / " + getWasSetAll());
    });
    Button resetToSameFirst = new Button("reset items with same first item");
    resetToSameFirst.setOnAction(e -> {
        // here we reset the list to one with the same first item, but different size
        String firstItem = cb.getItems().get(0);
        String[] newItems;
        if ("E1".equals(firstItem)) {
            newItems = cb.getItems().size() == 3 ? fourStartE1 : threeStartE1;
        } else {
            newItems = cb.getItems().size() == 3 ? fourStartE0 : threeStartE0;
        }
        cb.getItems().setAll(newItems);
        LOG.info("prevCount/setAllCalled? " + getPreviousItemCount() + " / " + getWasSetAll());
    });
    Button selectFirst = new Button("select first");
    selectFirst.setOnAction(e -> {
        // selects first as expected
        cb.getSelectionModel().selectFirst();
        LOG.info("prevCount/setAllCalled? " + getPreviousItemCount() + " / " + getWasSetAll());
        
    });
    Button clear = new Button("clear");
    clear.setOnAction(e -> {
        cb.getSelectionModel().clearSelection();
        LOG.info("prevCount/setAllCalled? " + getPreviousItemCount() + " / " + getWasSetAll());
    });
    
    Parent hbox = new FlowPane(rb1, rb2, cb, selectFirst, resetToSame, resetToSameFirst, clear);
    Parent root = new VBox(state, hbox);
    Scene scene = new Scene(root);
    primaryStage.setScene(scene);
    primaryStage.setMinWidth(600);
    primaryStage.show();
    updateState();

    // Works: view and selection model in sync
    //    cb.getSelectionModel().selectFirst();
    //    cb.getSelectionModel().clearSelection();
    //    cb.getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E3"));
    // original not working: view and selection model not in sync
    //    cb.getSelectionModel().selectFirst();
    //    cb.getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E3"));
    //    cb.getSelectionModel().clearSelection();
  }

    protected ObservableList<String> createList(String[] items) {
        return FXCollections.observableArrayList(items);
    }

  public void invalidated(Observable paramObservable)
  {
    updateState();
  }

  int count;
  private void updateState()
  {
    if (tg != null && cb != null)
      state.setText(String.format(
          "radiobutton: %s, wasSetAll: %s, previousItemCount: %s, selectedItem: %s, selectedIndex: %s, comboValue: %s", 
          ((RadioButton) tg.getSelectedToggle()).getText(),
          getWasSetAll(),
          getPreviousItemCount(),
          cb.getSelectionModel().getSelectedItem(),
          cb.getSelectionModel().getSelectedIndex(),
          cb.getValue()));
  }
  
  protected int getPreviousItemCount() {
      Class clazz = ComboBox.class;
      try {
          Field field = clazz.getDeclaredField("previousItemCount");
          field.setAccessible(true);
          return field.getInt(cb);
      } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
          e.printStackTrace();
      }
      return -1;  
  }
  protected boolean getWasSetAll() {
      Class clazz = ComboBox.class;
      try {
        Field field = clazz.getDeclaredField("wasSetAllCalled");
        field.setAccessible(true);
        return field.getBoolean(cb);
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
        e.printStackTrace();
    }
    return false;  
  }
  @SuppressWarnings("unused")
  private static final Logger LOG = Logger
        .getLogger(ComboboxSelectionCopyRT_26079.class.getName());

    @Override
    public void changed(ObservableValue observable, Object oldValue,
            Object newValue) {
        updateState();
    }
}
