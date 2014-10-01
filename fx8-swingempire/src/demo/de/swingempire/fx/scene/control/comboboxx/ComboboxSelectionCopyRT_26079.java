package de.swingempire.fx.scene.control.comboboxx;


import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
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
 */
public class ComboboxSelectionCopyRT_26079 extends Application 
    implements InvalidationListener
{
  private Label state;
  private ToggleGroup tg;
  private ComboBox<String> cb;

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
            
   
    // not using builder: combo value is empty
    ObservableList<String> items = FXCollections.observableArrayList("E1", "E2", "E3");
    cb = new ComboBox<String>();
    cb.setItems(items);
    cb.setEditable(false);

    cb.getSelectionModel().selectFirst();
    cb.disableProperty().bind(rb1.selectedProperty());
    cb.getSelectionModel().selectedItemProperty().addListener(this);
    cb.getSelectionModel().clearSelection();
    
    Button button = new Button("reset items to same");
    button.setOnAction(e -> {
        
    });
    HBox hbox = new HBox(rb1, rb2, cb, button);
    Parent root = new VBox(state, hbox);
    Scene scene = new Scene(root);
    primaryStage.setScene(scene);
    primaryStage.setMinWidth(600);
    primaryStage.show();
    updateState();

    // Does not work: combobox shows selection but selectionModel.selectedItem is null
    cb.getSelectionModel().selectFirst();
    // here we replace the cb's items with a list that is equal but not the same
    cb.getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E3"));
    cb.getSelectionModel().clearSelection();
    // Works: view and selection model in sync
    //    cb.getSelectionModel().selectFirst();
    //    cb.getSelectionModel().clearSelection();
    //    cb.getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E3"));
  }

  @Override
  public void invalidated(Observable paramObservable)
  {
    updateState();
  }

  private void updateState()
  {
    if (tg != null && cb != null)
      state.setText(String.format(
          "radiobutton: %s, combobox: %s", 
          ((RadioButton) tg.getSelectedToggle()).getText(),
          cb.getSelectionModel().getSelectedItem()));
  }
  
  @SuppressWarnings("unused")
  private static final Logger LOG = Logger
        .getLogger(ComboboxSelectionCopyRT_26079.class.getName());
}
