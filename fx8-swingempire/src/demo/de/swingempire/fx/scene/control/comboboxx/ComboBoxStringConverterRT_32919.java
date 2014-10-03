/*
 * Created on 02.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * https://javafx-jira.kenai.com/browse/RT-32919
 * setConverter must not clear selection
 * 
 * seems to be fixed in 8u40
 */
public class ComboBoxStringConverterRT_32919 extends Application
{
  public static void main(String[] args)
  {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception
  {
    final ComboBox<Integer> cb = new ComboBox<Integer>();
    cb.getItems().setAll(1, 2, 3);
    cb.getSelectionModel().select(0);
    
    Button b = new Button("set string converter");
    b.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0)
      {
        // this should not clear the selection but it does...
        cb.setConverter(new StringConverter<Integer>() {
          @Override public String toString(Integer arg0) { return "item " + arg0; }
          @Override public Integer fromString(String arg0) { throw new UnsupportedOperationException(); }
        });
      }
    });
    
    VBox c = new VBox();
    c.getChildren().addAll(cb, b);
    stage.setScene(new Scene(c, 500, 400));
    stage.show();
  }
}
