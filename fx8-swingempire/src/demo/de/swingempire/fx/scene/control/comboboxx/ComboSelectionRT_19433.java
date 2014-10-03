/*
 * Created on 02.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import de.swingempire.fx.util.DebugUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://javafx-jira.kenai.com/browse/RT-19433
 * no notification of selectedIndex if committing an uncontained value
 * in editable combo.
 * 
 * Bug: selectedIndex not updated, must be cleared
 * 
 * 
 * 
 * @author Alexander Kirov
 */
public class ComboSelectionRT_19433 extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    final ComboBox<String> testedComboBox = new ComboBox<String>();
    static int counter = 0;

    @Override
    public void start(Stage stage) throws Exception {
        VBox pane = new VBox();
        pane.setPrefHeight(200);
        pane.setPrefWidth(200);
        testedComboBox.setEditable(true);

        testedComboBox.getItems().addAll("1", "2", "3");

        VBox vb = new VBox();

        testedComboBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                System.out.println("Selected index " + testedComboBox.getSelectionModel().getSelectedIndex());
            }
        });
        
        testedComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>(){
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                System.out.println("Selected item " + testedComboBox.getSelectionModel().getSelectedItem());
            }
        });
        
        testedComboBox.valueProperty().addListener(new ChangeListener<String>(){

            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                System.out.println("New value : " + t1);
            }
        });
   
        Button button = new Button("print selection state");
        button.setOnAction(e -> {
            DebugUtils.printSelectionState(testedComboBox);
        });
        
        // https://javafx-jira.kenai.com/browse/RT-26447
        // aftervclearing selection the selectedIndex can't be selected again
        // fixed in 8
        Button clear = new Button("clear selection");
        clear.setOnAction(e -> {
            testedComboBox.getSelectionModel().clearSelection();
        });
        pane.getChildren().addAll(testedComboBox, button, clear);        
        
        vb.getChildren().addAll(pane);
        Scene scene = new Scene(vb, 400, 400);
        stage.setScene(scene);
        stage.show();
    }
}
