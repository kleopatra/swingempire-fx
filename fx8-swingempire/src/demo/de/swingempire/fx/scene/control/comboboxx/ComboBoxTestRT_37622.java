/*
 * Created on 26.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-37622
 * ComboBox size of popup not updated
 * 
 * orig:
 * - open popup to see three items
 * - click change content 
 * - open popup to see four items
 * - expected: seeing four items without scrollbars (aka: size adjusted in both dimensions)
 * - actual: three items with vertical scrollbar, long item with ellipse
 * 
 * happens in report (Mac) always, can be reproduced in win7 machines only
 * if selecting "three" on first opening 
 */
public class ComboBoxTestRT_37622 extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
       
       primaryStage.centerOnScreen();
       primaryStage.setHeight(200);
       primaryStage.setWidth(300);
       List<String> list1 = new ArrayList<>();
       list1.add("one");
       list1.add("two");
       list1.add("three");
       
       List<String> list2 = new ArrayList<>();
       list2.add("one");
       list2.add("two");
       list2.add("three three");
       list2.add("four");
       
//       final ComboBox<String> combo = new ComboBox<String>();
       // layout invoke added, same issue as core
       final ComboBoxX<String> combo = new ComboBoxX<String>();
       combo.getItems().setAll(list1);
       
       Button button = new Button("Change combo contents");
       button.setOnAction(event -> {
          if ( combo.getItems().size() == 3 ) {
             combo.getItems().setAll(list2);
          } else {
             combo.getItems().setAll(list1);
          }
       });
       
       VBox box = new VBox(20, combo, button );
       box.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
       primaryStage.setScene(new Scene( new StackPane(box) ));

       primaryStage.show();

    }

    public static void main(String[] args) throws Exception {
       launch(args);
    }

 }

