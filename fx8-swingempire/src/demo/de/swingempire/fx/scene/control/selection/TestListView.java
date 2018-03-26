/*
 * Created on 08.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * UnsupportedOperation when deleting on receiving the change
 * http://stackoverflow.com/q/27350618/203657
 * 
 * It's the list itself (vs. any part of the selectionModel) that throws:
 * removing again while in notification round.
 * 
 * With the flag, we get an inconsistent selection state: selection
 * is on item above the clicked, focus one below
 */
public class TestListView extends Application {
    @Override
    public void start (Stage stage) {
      List<String> orderList = new ArrayList<String> ();
      ObservableList<String> orderOvList =
        FXCollections.observableList (orderList);
      ListView<String> order = new ListView<String> (orderOvList);

      orderOvList.add ("abc");
      orderOvList.add ("def");
      orderOvList.add ("ghi");
      orderOvList.add ("jkl");

      VBox orderBoxPane = new VBox (6);
      BooleanProperty removing = new SimpleBooleanProperty();
      order.getSelectionModel().selectedItemProperty().addListener (
        ov -> {
            // we get here again during 
            // list's notification about the remove
            // (selectionModel is updating itself)
            // without the flag, we would violate
            // the no-modification-in-notification rule
          int i = order.getSelectionModel ().getSelectedIndex ();
          if (removing.get()) {
              removing.set(false);
              return;
          }
          if (orderOvList.size () >= 0) {
            removing.set(true);  
            System.out.println (i);
            orderOvList.remove (i, i + 1);
          }
          removing.set(false);
        });
      
//      order.setCellFactory(lv -> {
//          ListCell<String> cell = new ListCell<String>() {
//              @Override
//              public void updateItem(String item, boolean empty) {
//                  super.updateItem(item, empty);
//                  setText(item);
//              }
//          };
//          cell.setOnMouseClicked(event -> {
//              String item = cell.getItem();
//              if (item != null) {
//                  orderOvList.remove(item);
//
//                  // ensure nothing selected after removal:
//                  order.getSelectionModel().clearSelection();
//              }
//          });
//          return cell ;
//      });


      orderBoxPane.getChildren ().add (order);

      Scene scene = new Scene (orderBoxPane);
      stage.setTitle ("TestListView");
      stage.setScene (scene);
      stage.show ();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
  }

