/*
 * Created on 22.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * https://javafx-jira.kenai.com/browse/RT-15793  
 * - select first
 * - click button to remove first
 * - selectedIndex still 0
 * 
 * Hacked by calling into the selectionModel (if of default type)
 * 
 * for anchored: not possible to select first item, looks 
 * like the model doesn't update itself correctly?
 * 
 * That's because there is no event fired if the values are
 * equal. ExpressionHelper doesn't seem to have the option
 * to force an identity check. Most interested parties
 * (skin, selectionModel, ...) install a ChangeHandler on
 * the itemsProperty
 * 
 * The hack is implemented for ListView, TableView, ComboBox - not for
 * ChoiceBox. For ComboBox it's fixed for initial list only: resetting
 * an equal list later on, doesn't update correctly. Reason are aliases
 * in ComboBoxListViewSkin: hack calls updateListViewItems which updates 
 * the list's items to the _alias_ of the comboBoxItems. Without calling 
 * updateComboBoxItems as well, that's still the old reference.
 * 
 * @author jfdenise
 */
public class ListViewSelectionRT_15793 extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
       Pane ap = new VBox();
//       final ListView lv = new ListViewAnchored<>();
       
       final ListView lv = new ListView<>();
       
//       final ComboBox lv = new ComboBox();
       // invalidationListener fires (as it doesn't check any value by definition)
//       lv.itemsProperty().addListener(o -> LOG.info("got invalidation!"));
//       // changeListener doesn't fires because it checks for equality of old/new value
//       lv.itemsProperty().addListener((o, old, value) -> LOG.info("got change!"));
       final ObservableList lst = FXCollections.observableArrayList();
       lv.setItems(lst);
       lst.add("toto");
            
       ap.getChildren().addAll(lv);
//--------------- added to test hack-around for custom selectionModel
       String[] data = {"one", "two"};
       Button setList = new Button("set list");
       setList.setOnAction(e -> {
           lv.setItems(FXCollections.observableArrayList(data));
       });
       Button setListAndAdd = new Button("set list and add");
       setListAndAdd.setOnAction(e -> {
           ObservableList<String> cl = FXCollections.observableArrayList(data);
           lv.setItems(cl);
           cl.add("added");
       });
       ap.getChildren().addAll(setList, setListAndAdd);
//--------------- end of added       
       
       Scene scene = new Scene(ap);
       System.out.println("THE SELECTED INDEX " + lv.getSelectionModel().getSelectedIndex());
       Button b = new Button("remove selected in list");
       b.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if(lv.getSelectionModel().getSelectedIndex() != -1)
                    lv.getItems().remove(lv.getSelectionModel().getSelectedIndex());
                
                System.out.println("THE SELECTED INDEX, should be -1 but is " + lv.getSelectionModel().getSelectedIndex());

            }
        });
       ap.getChildren().addAll(b);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ListViewSelectionRT_15793.class.getName());
}