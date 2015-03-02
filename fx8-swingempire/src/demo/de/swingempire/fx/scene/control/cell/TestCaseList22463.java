/*
 * Created on 21.10.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Here we have a list that's equal to the new list because its items are equals.
 * Should be covered by 15793? Why not?
 * 
 * Original setup:
 * - start with getPersons1
 * - in refresh: setItems(getPersons2)
 * 
 * added fields to try and track why the item is updated, but not the whole list
 * 
 * Since 8u40b12, this can be handled by a custom ListCell that overrides
 * isItemChanged to return false if new/old item aren't identical.
 * 
 * https://javafx-jira.kenai.com/browse/RT-39094
 * 
 * @author dosiennik
 */
public class TestCaseList22463 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

//    private ObservableList<Person22463> persons1;
//    private ObservableList<Person22463> persons2;

    int idCount;
    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Test case");
        primaryStage.setX(0);
        primaryStage.setY(0);

        Button refreshButton = new Button("Refresh list");

        final ListView<Person22463> listView = new ListView<>();

        // prior to 8u40b12
//        listView.setCellFactory(lv -> {
//            System.out.println("created");
//            ListCell cell = new IdentityCheckingListCell<Person22463>() {
//                @Override
//                protected void updateItem(Person22463 item, boolean empty) {
//                    super.updateItem(item, empty);
//                    setText(item != null ? item.getName() : "");
//                    if (!empty) {
//                        System.out.println("item updated to '" + item + "'"  + "for " + getId());
//                    }
//                }
//            };
//            cell.setId("cell-id: " + idCount++);
//            return cell;
//         });

        // with new api, since 8u40b12
        listView.setCellFactory(lv -> {
            ListCell<Person22463> cell = new ListCell<Person22463>() {
                
                
                @Override
                protected boolean isItemChanged(Person22463 oldItem,
                        Person22463 newItem) {
                    return oldItem != newItem;
                }
    
                // boiler-plate to show anything
                @Override
                protected void updateItem(Person22463 item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item != null ? item.getName() : "");
                    if (!empty) {
                        System.out.println("item updated to '" + item + "'"  + "for " + getId());
                    }
                }
            };    
            return cell;
                
        });
        Button init = new Button("init for setItem");
        init.setOnAction(e -> {
            listView.getItems().clear();
//            listView.setItems(persons2);
            listView.setItems(FXCollections.observableArrayList(getPerson1()));
        });
        // original
        refreshButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                // Without clearing, the table isn't refreshed.
//                table.getItems().clear();
                List<Person22463> person = getPerson2();
                listView.setItems(FXCollections.observableList(person));
            }
        });

        Button setItem = new Button("refresh first item");
        setItem.setOnAction(e -> {
            // this is updating
            List<Person22463> persons = getPerson2();
            listView.getItems().set(0, persons.get(0));
        });
        BorderPane pane = new BorderPane();
        pane.setCenter(listView);
        
        Parent buttons = new HBox(init, refreshButton, setItem);
        pane.setBottom(buttons);

        primaryStage.setScene(new Scene(pane, 400, 400));
        primaryStage.show();

//        persons1 = FXCollections.observableArrayList(getPerson1());
//        persons2 = FXCollections.observableArrayList(getPerson2());
        listView.setItems(FXCollections.observableArrayList(getPerson1()));

    }

    private List<Person22463> getPerson1() {
        List<Person22463> p = new ArrayList<>();
        Person22463 p1 = new Person22463();
        p1.setId(1l);
        p1.setName("name1");
        Person22463 p2 = new Person22463();
        p2.setId(2l);
        p2.setName("name2");
        p.add(p1);
        p.add(p2);
        return p;
    }

    private List<Person22463> getPerson2() {
        List<Person22463> p = new ArrayList<>();
        Person22463 p1 = new Person22463();
        p1.setId(1l);
        p1.setName("updated name1");
        Person22463 p2 = new Person22463();
        p2.setId(2l);
        p2.setName("updated name2");
        p.add(p1);
        p.add(p2);
        return p;
    }
}