/*
 * Created on 22.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Reported:
 * https://javafx-jira.kenai.com/browse/RT-38731
 * 
 * This is based on
 * https://javafx-jira.kenai.com/browse/RT-15793 
 * 
 * That issue is "fixed" by hacking, the deeper
 * issue (might be reported in RT-16072 which I'm not allowed to see)
 * is that there are use-cases where change events must be fired
 * based on identity of values (vs. based on equality which is 
 * default here).
 * 
 * This issue report is yet another example where it blows. Run the
 * example and see two variants of the same underlying bug
 * 
 * variant 1:
 * 
 * - choicebox with initially empty list
 * - click the "set empty items and add" button
 * - click drop-down arrow
 * - expected: popup contains added item
 * - actual: no popup shown (because it is still empty)
 * 
 * variant 2:
 * 
 * - click on "set not empty items"
 * - click drop-down arrow
 * - expected and actual: popup contains the items of the list
 * - click on "set not empty items and add"
 * - click drop-down arrow 
 * - expected: popup contains the added item
 * - actual: popup only contains the two initial items of the list
 * 
 */
public class ItemsListenerRT_38731 extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
       Pane ap = new VBox();
       // empty list
       ChoiceBox choice = new ChoiceBox();
       // same with comboBox
//       ComboBox choice = new ComboBox();
       Button choiceAdd = new Button("set empty items and add choice");
       choiceAdd.setOnAction(ev -> {
           ObservableList cl = FXCollections.observableArrayList();
           // reset items to new empty list
           choice.setItems(cl);
           // add item to new list
           cl.add("choiceToto");}
       );
       // list with data
       String[] data = {"one", "two"};
       Button choiceSetOnly = new Button("set not empty items");
       choiceSetOnly.setOnAction(ev -> {
           ObservableList cl = FXCollections.observableArrayList(data);
           // set items to not-empty list
           choice.setItems(cl);
       });
       Button choiceSetNotEmpty = new Button("set not empty items and add");
       choiceSetNotEmpty.setOnAction(ev -> {
           ObservableList cl = FXCollections.observableArrayList(data);
           // set items to new not-empty list with same items
           choice.setItems(cl);
           // add new item to new list
           cl.add("another");
           
       });
       ap.getChildren().addAll(choice, choiceAdd, choiceSetOnly, choiceSetNotEmpty);
       
       Scene scene = new Scene(ap);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
}