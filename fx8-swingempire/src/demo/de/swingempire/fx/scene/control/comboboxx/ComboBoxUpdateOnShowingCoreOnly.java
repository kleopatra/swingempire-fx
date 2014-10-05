/*
 * Created on 01.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.logging.Logger;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.swingempire.fx.scene.control.selection.AbstractChoiceInterfaceSelectionIssues.ChoiceControl;
import de.swingempire.fx.scene.control.selection.ChoiceSelectionIssues.ChoiceCoreControl;
import de.swingempire.fx.scene.control.selection.ChoiceXSelectionIssues.ChoiceXControl;
import de.swingempire.fx.scene.control.selection.ComboSelectionIssues.ComboCoreControl;
import de.swingempire.fx.scene.control.selection.ComboXSelectionIssues.ComboXControl;

/**
 * Regression guard against: https://javafx-jira.kenai.com/browse/RT-22572
 * 
 * - Select the item from the ComboBox menu.
 * - Click on the menuButton withoiut selecting anything : the value is removed.
 * 
 * Regression guard against: https://javafx-jira.kenai.com/browse/RT-22937
 * ?? somehow related to action handler?
 * 
 * Regression guard against: https://javafx-jira.kenai.com/browse/RT-20945
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
 * But: 
 * - still cleared if resetting the list (which is functionally equivalent to setAll)
 * - choicebox clears always
 * 
 * Here we use only core components (X blows in 8u40, too lazy to find the 
 * reason)
 * 
 * @author jfdenise
 * @see ComboboxSelectionRT_26079
 */
public class ComboBoxUpdateOnShowingCoreOnly extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        // choicebox has no setOnAction!
//        ChoiceBox b = new ChoiceBox();
//        b.setOnAction(e -> {});
        primaryStage.setTitle("Hello World!");
//        ComboBox combo = new ComboBox();
//        configureCombo(combo, "core combo");
        ComboCoreControl combo = new ComboCoreControl();
        configureChoice(combo, "core combo");
        ChoiceCoreControl choice = new ChoiceCoreControl();
        configureChoice(choice, "core choice");
        ComboBoxListViewSkin t;
        Button button = new Button("null selected");
        button.setOnAction(e -> {
            combo.getSelectionModel().select(null);
        });
        Pane combos = new HBox(combo, choice);
        Pane buttons = new HBox(button);
        
        Pane content = new VBox(combos, buttons);
        primaryStage.setScene(new Scene(content, 300, 250));
        primaryStage.show();
    }

    private void configureChoice(ChoiceControl cb, String initialValue) {
        // dynamic reset of content on showing
        cb.showingProperty().addListener(new ChangeListener<Boolean>() {
            
            @Override
            public void changed(ObservableValue arg0, Boolean oldValue, Boolean newValue) {
                LOG.info("type: " + cb.getClass());
                if (newValue) {                   
                    cb.getItems().setAll("" + System.currentTimeMillis());
//                    cb.setItems(FXCollections.observableArrayList("" + System.currentTimeMillis()));
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
        // intial items
        cb.getItems().add("Toto");
//        cb.setEditable(true);
        // selectedItem/value uncontained
        cb.setValue(initialValue);
        
        
    }
    
    private void configureCombo(ComboBox cb, String initialValue) {
            // dynamic reset of content on showing
            cb.showingProperty().addListener(new ChangeListener<Boolean>() {
                
                @Override
                public void changed(ObservableValue arg0, Boolean oldValue, Boolean newValue) {
                    LOG.info("type: " + cb.getClass());
                    if (newValue) {                   
                        cb.getItems().setAll("" + System.currentTimeMillis());
    //                    cb.setItems(FXCollections.observableArrayList("" + System.currentTimeMillis()));
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
            // intial items
            cb.getItems().add("Toto");
    //        cb.setEditable(true);
            // selectedItem/value uncontained
            cb.setValue(initialValue);
            
            
        }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBoxUpdateOnShowingCoreOnly.class.getName());
}