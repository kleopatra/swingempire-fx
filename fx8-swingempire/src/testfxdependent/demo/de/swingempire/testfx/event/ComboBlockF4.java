/*
 * Created on 26.10.2019
 *
 */
package de.swingempire.testfx.event;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Disable F4 for combo.
 * https://stackoverflow.com/q/58550028/203657
 * answer by slaw
 * 
 */
public class ComboBlockF4 extends Application {

    @Override
    public void start(Stage primaryStage) {
      var comboBox = new ComboBox<String>();
      for (int i = 0; i < 20; i++) {
        comboBox.getItems().add("Item #" + i);
      }
      comboBox.getSelectionModel().select(0);

      var oldDispatcher = comboBox.getEventDispatcher();
      comboBox.setEventDispatcher((event, tail) -> {
        if (event.getEventType() == KeyEvent.KEY_RELEASED
                && ((KeyEvent) event).getCode() == KeyCode.F4) {
//            System.out.println(event);
          return null; // returning null indicates the event was consumed
        }
        return oldDispatcher.dispatchEvent(event, tail);
      });

      comboBox.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
          if (e.getCode() == KeyCode.F4) {
              System.out.println("in keypressed");
          }
      });
      Scene scene = new Scene(new StackPane(comboBox), 500, 300);
      
      scene.getAccelerators().put(KeyCombination.keyCombination(KeyCode.F4.getName()), 
              () -> System.out.println("accelerator"));
      primaryStage.setScene(scene);
      
      primaryStage.show();
      primaryStage.setX(50);
    }

    public static void main(String[] args) {
        launch(args);
    }

  }