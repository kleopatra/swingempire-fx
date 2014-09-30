package de.swingempire.fx.scene.control.comboboxx;


import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.scene.SceneBuilder;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioButtonBuilder;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-26079
 * selectedIndex != -1 for empty list is intentional!
 * 
 * same: 
 * https://javafx-jira.kenai.com/browse/RT-24898
 * 
 * Crazy ...
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboboxSelectionTest extends Application 
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
    
    RadioButton rb1 = RadioButtonBuilder.create()
        .text("one")
        .selected(true)
        .toggleGroup(tg)
        .build();
    
    RadioButton rb2 = RadioButtonBuilder.create()
        .text("two")
        .toggleGroup(tg)
        .build();
    
    cb = ComboBoxBuilder.<String>create()
        .items(FXCollections.observableArrayList("E1", "E2", "E3"))
        .editable(false)
        .build();
    cb.getSelectionModel().selectFirst();
    cb.disableProperty().bind(rb1.selectedProperty());
    cb.getSelectionModel().selectedItemProperty().addListener(this);
    cb.getSelectionModel().clearSelection();
    
    primaryStage.setScene(
      SceneBuilder.create()
        .root(
            VBoxBuilder.create()
              .spacing(8)
              .children(
                state,
                HBoxBuilder.create()
                  .spacing(8)
                  .children(rb1, rb2, cb)
                  .build())
              .build())
        .build());
    primaryStage.setMinWidth(300);
    primaryStage.show();
    updateState();
    
    // Does not work: combobox shows selection but selectionModel.selectedItem is null
    cb.getSelectionModel().selectFirst();
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
}
