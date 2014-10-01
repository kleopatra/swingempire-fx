/*
 * Created on 01.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Note: this use-case was the argument for the crazyness of selectFirst
 * behavior (ComboboxSelectionRT_26079)!
 * 
 * 
 * Regressin guard against: https://javafx-jira.kenai.com/browse/RT-20945
 *  
 * - click button and select item in list: value updated
 * - click on button: action handler fired
 * 
 * Note: the issue here is that the items are always reset on showing!
 * 
 * Also note:
 * - select once
 * - open popup again
 * - press esc: value must not be cleared
 *  
 * fixed in 2.2 
 * 
 * @author jfdenise
 * @see ComboboxSelectionRT_26079
 */
public class ComboBoxUpdateOnShowingRT_20945 extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
        final ComboBox cb = new ComboBox();
        // dynamic reset of content on showing
        cb.showingProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue arg0, Boolean oldValue, Boolean newValue) {
                if (newValue) {                   
                    cb.getItems().setAll("" + System.currentTimeMillis());
                }
            }
        });
        cb.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("OnAction called");
            }
        });
        // intial items
        cb.getItems().add("Toto");
//        cb.setEditable(true);
        // selectedItem/value uncontained
        cb.setValue("Tata");
        
        StackPane root = new StackPane();
        root.getChildren().add(cb);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }
}