/*
 * Created on 01.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;


import java.util.logging.Logger;



import de.swingempire.fx.util.DebugUtils;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Regression guard against RT-22572
 * https://javafx-jira.kenai.com/browse/RT-22572
 * 
 * was:
 * open popup, select item
 * click on arrow, don't select: selected removed
 * 
 * fixed for 2.2
 * 
 * another:
 * comment setEditable
 * left-click on button to show popup
 * left-click to select item
 * left-click into textfield
 * expected: selected item kept
 * actual, selected item cleared
 * 
 * fixed as well
 * 
 * @author jfdenise
 */
public class ComboBoxValueOnOpeningPopupRT_22572 extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
        TextField tf = new TextField();
        final ComboBox cb = new ComboBox();
        cb.setMaxWidth(100);
        cb.setMinWidth(100);
        // filling the box dynamically
        cb.setOnShowing(new EventHandler<Event>() {

            @Override
            public void handle(Event arg0) {
                // here we replace all items
                cb.getItems().setAll("" + System.currentTimeMillis());
//                cb.setItems(FXCollections.observableArrayList("" + System.currentTimeMillis()));
            }
        });

        Button nullSelected = new Button("Null selectedItem");
        nullSelected.setOnAction(e -> {
            cb.getSelectionModel().select(null);
            DebugUtils.printSelectionState(cb);
            
        });
                
//                new EventHandler() {
//            @Override
//            public void handle(Event arg0) {
//            }
//
//        });
        
        Button uncontained = new Button("Set uncontained selectedItem");
        uncontained.setOnAction(e -> {
            cb.getSelectionModel().select("uncontained");
            DebugUtils.printSelectionState(cb);
        });
        Button clear = new Button("Clear selection");
        uncontained.setOnAction(e -> {
            cb.getSelectionModel().select(-1);
            DebugUtils.printSelectionState(cb);
        });
        // was RT-20945: receiving action event on opening the popup
        // if was dynamically filled
        // don't understand bug description: there is only one
        // item after showing?
//        cb.setOnAction(e -> LOG.info("got action: " + e));
        cb.getItems().add("Toto"); // initial
        cb.setEditable(true);
        cb.setValue("Tata");
        // ... end test code 20945 
        
        cb.setEditable(true);
  
        cb.setPromptText("X");

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(tf, cb, nullSelected, uncontained, clear);
        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(vbox);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBoxValueOnOpeningPopupRT_22572.class.getName());
}
