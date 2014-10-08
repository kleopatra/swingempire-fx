/*
 * Created on 08.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;


import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import com.sun.javafx.scene.control.skin.ComboBoxPopupControl;


/**
 * Adding one item more than once.
 * 
 * - add 0 at position 0 five times
 * - open popup and select 0 at pos 3
 * - expected: output selectedIndex = 3
 * - actual: output selectedIndex = 0
 * 
 * fixed as of 2.2
 * 
 * but (8u40b7): 
 * - open popup again
 * - expected: item at 3 highlighted
 * - actual: item at 0 highlighted
 * 
 * reported: https://javafx-jira.kenai.com/browse/RT-38927
 */
public class IssueRT_19227 extends Application {

    public static void main(String[] args) {
        launch(args);
    }
//    final ComboBoxX testedComboBox = new ComboBoxX();
    final ComboBox testedComboBox = new ComboBox();

    @Override
    public void start(Stage stage) throws Exception {
        Pane pane = new Pane();
        pane.setPrefHeight(200);
        pane.setPrefWidth(200);

        pane.getChildren().add(testedComboBox);

        testedComboBox.setVisibleRowCount(5);

        testedComboBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                System.out.println("Selected index " + testedComboBox.getSelectionModel().getSelectedIndex());
            }
        });

        testedComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                System.out.println("Selected item " + t1);
            }
        });

        testedComboBox.showingProperty().addListener((o, old, value) -> {
            }); 
        testedComboBox.setOnShown(e -> {
            System.out.println("shown selectedIndex " + testedComboBox.getSelectionModel().getSelectedIndex());
            ComboBoxListViewSkin skin = (ComboBoxListViewSkin) testedComboBox.getSkin();
            ListView list = skin.getListView();
            System.out.println("list selected " + list.getSelectionModel().getSelectedIndex());
        });
        Button setNewItemList = new Button("set new items list");
        setNewItemList.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent t) {
                testedComboBox.setItems(FXCollections.observableArrayList());
            }
        });

        VBox vb = new VBox();
        vb.getChildren().addAll(pane, getAddItemHBox(), setNewItemList);
        Scene scene = new Scene(vb, 400, 400);
        stage.setScene(scene);
        stage.setTitle(System.getProperty("java.version"));
        stage.show();
    }

    private HBox getAddItemHBox() {
        HBox hb = new HBox();
        Label lb = new Label("Add item");
        final TextField tf = TextFieldBuilder.create().prefWidth(50).build();
        Label atLb = new Label("at pos");
        final TextField tfPos = TextFieldBuilder.create().prefWidth(50).build();
        Button bt = ButtonBuilder.create().text("Add!").build();
        bt.setOnAction(new EventHandler() {

            public void handle(Event t) {
                int index = Integer.parseInt(tfPos.getText());
                 testedComboBox.getItems().add(index, tf.getText());
            }
        });
        hb.getChildren().addAll(lb, tf, atLb, tfPos, bt);
        return hb;
    }
}
