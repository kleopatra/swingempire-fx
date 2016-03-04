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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


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
 * reported: 
 * https://javafx-jira.kenai.com/browse/RT-38927
 * in new bug coordinate:
 * https://bugs.openjdk.java.net/browse/JDK-8087523
 * 
 * Still same in 9-ea-107 
 */
public class IssueRT_19227 extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    
    // similar error in comboboxx
//    final ComboBoxX testedComboBox = new ComboBoxX();
    final ComboBox testedComboBox = new ComboBox();

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
            ListView list = getListView(testedComboBox);
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

    protected ListView getListView(Control box) {
        if (box instanceof ComboBox) {
            ComboBoxListViewSkin skin = (ComboBoxListViewSkin) box.getSkin();
            ListView list = (ListView) skin.getPopupContent();
            return list;
            
        }
        ComboBoxXListViewSkin skin = (ComboBoxXListViewSkin) box.getSkin();
        ListView list = skin.getListView();
        return list;
    }

    private HBox getAddItemHBox() {
        HBox hb = new HBox();
        Label lb = new Label("Add item");
        final TextField tf = new TextField(); //TextFieldBuilder.create().prefWidth(50).build();
        Label atLb = new Label("at pos");
        final TextField tfPos = new TextField();// TextFieldBuilder.create().prefWidth(50).build();
        Button bt = new Button("Add"); //ButtonBuilder.create().text("Add!").build();
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
