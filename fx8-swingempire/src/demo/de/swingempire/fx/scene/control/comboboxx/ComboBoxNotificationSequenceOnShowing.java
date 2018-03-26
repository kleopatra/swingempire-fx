/*
 * Created on 01.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;


import java.util.logging.Logger;

import de.swingempire.fx.util.DebugUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Quick test: who's called in which sequence?
 * 
 * expected;
 * - beforeShown in comboBoxX
 * - ON_SHOWING event handler
 * - invalidationListener on showingProperty
 * - changeListener on showingProperty 
 * - afterShown in comboBoxX
 * 
 * as expected for both ComboX and Combo - show/hide is the root of all 
 * opening/hiding of popup
 */
public class ComboBoxNotificationSequenceOnShowing extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(System.getProperty("java.version"));
        ComboBoxX cb = new ComboBoxX() {
//        ComboBox cb = new ComboBox() {

            @Override
            public void show() {
                LOG.info("before show");
                super.show();
                LOG.info("after show");
            }
            
        };
        cb.getItems().addAll("Toto", "Tati", "Tuto");
        cb.setOnShowing(e -> {
            LOG.info("OnShowing");
        });

        cb.showingProperty().addListener(e -> LOG.info("invalidated showingProperty"));
        cb.showingProperty().addListener((o, old, value) -> {
            LOG.info("changed showingProperty");
        });
        Button nullSelected = new Button("Null selectedItem");
        nullSelected.setOnAction(e -> {
            cb.getSelectionModel().select(null);
            DebugUtils.printSelectionState(cb);
            
        });
                
        Button uncontained = new Button("Set uncontained selectedItem");
        uncontained.setOnAction(e -> {
            cb.getSelectionModel().select("uncontained");
            DebugUtils.printSelectionState(cb);
        });
        Button clear = new Button("Clear selection");
        clear.setOnAction(e -> {
            cb.getSelectionModel().select(-1);
            DebugUtils.printSelectionState(cb);
        });
  
        cb.setPromptText("X");

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(cb, nullSelected, uncontained, clear);
        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(vbox);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBoxNotificationSequenceOnShowing.class.getName());
}
