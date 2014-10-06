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

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

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
        // choicebox has no setOnAction!
//        ChoiceBox b = new ChoiceBox();
//        b.setOnAction(e -> {});
        primaryStage.setTitle("Hello World!");
//        ComboBox combo = new ComboBox();
//        configureCombo(combo, "core combo");
        ComboCoreControl combo = new ComboCoreControl();
        configureCombo(combo, "core combo");
        ComboXControl comboX = new ComboXControl();
        configureCombo(comboX, "x combo");
        ChoiceCoreControl choice = new ChoiceCoreControl();
        configureChoice(choice, "core choice");
        ChoiceXControl choiceX = new ChoiceXControl();
        configureChoice(choiceX, "x choice");
        ComboBoxListViewSkin t;
        Button button = new Button("null selected");
        button.setOnAction(e -> {
            combo.getSelectionModel().select(null);
        });
        Pane combos = new HBox(combo, comboX, choice, choiceX );
        Pane buttons = new HBox(button);
        
        Pane content = new VBox(combos, buttons);
        primaryStage.setScene(new Scene(content, 600, 400));
        primaryStage.setTitle(System.getProperty("java.version"));
        primaryStage.show();
    }

    private void configureChoice(ChoiceControl cb, String initialValue) {
        // dynamic reset of content on showing
        cb.showingProperty().addListener(new ChangeListener<Boolean>() {
            
            @Override
            public void changed(ObservableValue arg0, Boolean oldValue, Boolean newValue) {
                if (newValue) {                   
                    LOG.info("filling items ");
                    cb.getItems().setAll("" + System.currentTimeMillis());
//                    cb.setItems(FXCollections.observableArrayList("" + System.currentTimeMillis()));
                    LOG.info("filling items done");
                
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
    
    private void configureCombo(ChoiceControl cb, String initialValue) {
            // dynamic reset of content on showing
            cb.showingProperty().addListener(new ChangeListener<Boolean>() {
                
                @Override
                public void changed(ObservableValue arg0, Boolean oldValue, Boolean newValue) {
                    if (newValue) {                   
                        cb.getItems().setAll("" + System.currentTimeMillis());
//                        cb.setItems(FXCollections.observableArrayList("" + System.currentTimeMillis()));
                        LOG.info("filling items done");
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
//            cb.setEditable(true);
            // selectedItem/value uncontained
            cb.setValue(initialValue);
            
            
        }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBoxUpdateOnShowingRT_20945.class.getName());
}