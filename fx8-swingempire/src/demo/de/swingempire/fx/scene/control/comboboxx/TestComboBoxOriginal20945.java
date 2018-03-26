/*
 * Created on 07.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Original: RT-20945
 * 
 * - initially has selectedItem that's not in the list
 * - open (items changed on showing) and select new item
 * - open again (items changed again on showing)
 *   - changed by setAll: selectedItem kept (fix of 20945)
 *   - changed by setItems: selectedItem cleared (reported as 38899)
 *    
 * @author jfdenise
 */
public class TestComboBoxOriginal20945 extends Application {

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
        cb.showingProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue arg0, Boolean oldValue, Boolean newValue) {
                if (newValue) {                   
                    // original: use setAll to change
//                    cb.getItems().setAll("" + System.currentTimeMillis());
                    // variant: use setItems to change 
                    cb.setItems(FXCollections.observableArrayList("" + System.currentTimeMillis()));
                }
            }
        });
//        cb.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent event) {
//                System.out.println("OnAction called");
//            }
//        });
        cb.getItems().add("Toto");
//        cb.setItems(FXCollections.observableArrayList("Toto"));
        cb.setEditable(true);
        cb.setValue("Tata");
        
        StackPane root = new StackPane();
        root.getChildren().add(cb);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }
}