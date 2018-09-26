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

import de.swingempire.fx.scene.control.selection.ListViewAnchored;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
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
 * indexedCell.updateIndex(newIndex); // called always
 * indexedCell.indexChanged(oldIndex, newIndex); // package private, implemented by
 *    concrete cells, called always
 * listCell.updateItem(newIndex); // private, called from indexChanged on listChangeListener
 *   // decides about calling or not updateItem(newValue, empty)
 * does not call if newValue.equals(oldValue)  
 * 
 * @author dosiennik
 */
public class TestCaseListX22463 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    int idCount;
    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Test case");
        primaryStage.setX(0);
        primaryStage.setY(0);

        Button refreshButton = new Button("Refresh list");

        final ListViewAnchored<Person22463> listView = new ListViewAnchored<>();
        
        listView.setCellFactory(lv -> {
            System.out.println("created");
            ListCell cell = new IdentityCheckingListCell<Person22463>() {
                @Override
                protected void updateItem(Person22463 item, boolean empty) {
                    // not calling super updates as expected
                    super.updateItem(item, empty);
                    setText(item != null ? item.getName() : "empty");
                    if (!empty) {
                        System.out.println("item updated to '" + item + "'"  + "for " + getId());
                    }
                }
            };
            cell.setId("cell-id: " + idCount++);
            return cell;
         });

        Button init = new Button("init for setItem");
        init.setOnAction(e -> {
//            listView.getItems().clear();
//            listView.setItems(persons2);
//            listView.setItems(persons1);
        });
        // original
        refreshButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                // Without clearing, the table isn't refreshed.
//                table.getItems().clear();
                listView.setItems(getList(listView.getItems()));
            }
        });

        Button setItem = new Button("refresh first item");
        setItem.setOnAction(e -> {
            // this is updating
//            List<Person22463> persons = getPerson2();
            listView.getItems().set(0, createEqualPerson(listView.getItems().get(0)));
        });
        BorderPane pane = new BorderPane();
        pane.setCenter(listView);
        
        Parent buttons = new HBox(init, refreshButton, setItem);
        pane.setBottom(buttons);

        primaryStage.setScene(new Scene(pane, 400, 120));
        primaryStage.show();

        listView.setItems(FXCollections.observableArrayList(getPersonsOrig1()));

    }

    /**
     * Creates a list that's equal to the given.
     * @param persons
     * @return
     */
    private ObservableList<Person22463> getList(List<Person22463> persons) {
        ObservableList<Person22463> list = FXCollections.observableArrayList();
        for (Person22463 object : persons) {
            list.add(createEqualPerson(object));
        }
        return list;
    }
    
    int count;
    
    /**
     * Creates person that's equal to the given. Same id, extended name.
     * @param object
     * @return
     */
    private Person22463 createEqualPerson(Person22463 person) {
        Person22463 p = new Person22463();
        p.setId(person.getId());
        p.setName(person.getName() + count++);
        return p;
    }

    private List<Person22463> getPersonsOrig1() {
        List<Person22463> p = new ArrayList<>();
        Person22463 p1 = new Person22463();
        p1.setId(1l);
        p1.setName("nameA");
        Person22463 p2 = new Person22463();
        p2.setId(2l);
        p2.setName("nameB");
        p.add(p1);
        p.add(p2);
        return p;
    }

//    private List<Person22463> getPerson2() {
//        List<Person22463> p = new ArrayList<>();
//        Person22463 p1 = new Person22463();
//        p1.setId(1l);
//        p1.setName("updated name1");
//        Person22463 p2 = new Person22463();
//        p2.setId(2l);
//        p2.setName("updated name2");
//        p.add(p1);
//        p.add(p2);
//        return p;
//    }
}