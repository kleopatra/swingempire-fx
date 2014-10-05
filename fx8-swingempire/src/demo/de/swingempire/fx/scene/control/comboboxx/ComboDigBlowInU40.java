/*
 * Created on 04.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.swingempire.fx.util.DebugUtils;


/**
 * Seen ComboBoxUpdateOnShowing throw NPE in updateDisplayValue - 
 * not reproducible?
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboDigBlowInU40 extends Application {

    /**
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Parent getContent() {
        ComboBoxX box = new ComboBoxX(FXCollections.observableArrayList("one", "two", "three"));
        box.showingProperty().addListener((o, old, value) -> {
            if (value) {
                box.getItems().setAll("" + System.currentTimeMillis());
            }
        });
        box.setValue("something else");
        ComboBox core = new ComboBox(FXCollections.observableArrayList("one", "two", "three"));
        core.showingProperty().addListener((o, old, value) -> {
            if (value) {
                core.getItems().setAll("" + System.currentTimeMillis());
            }
        });
        core.setValue("something else");
        
        Button log = new Button("log selection");
        log.setOnAction(e -> {
            // BUG in comboX: selectedItem/value/display forced to first item
            // all fine at configure, bad after ?? what exactly ?
            // introduced between 8u20 and 8u40b7
            DebugUtils.printSelectionState(box);
            DebugUtils.printSelectionState(core);
        });
        
        Pane xLane = new HBox(new Label("combo boxX"), box);
        Pane coreLane = new HBox(new Label("combo core"), core);
        
        Pane buttonLane = new HBox(log);
        return new VBox(xLane, coreLane, buttonLane);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
