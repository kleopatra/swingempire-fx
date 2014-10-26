/*
 * Created on 26.10.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * ChangeEvents incorrect:
 * https://javafx-jira.kenai.com/browse/RT-39088
 * fixed: http://hg.openjdk.java.net/openjfx/8u-dev/rt/rev/87400ea438c6
 * 
 * OTN:
 * https://community.oracle.com/thread/3621013?sr=stream&ru=547944 
 * 
 * steps:
 * ctrl-A to select all: too many events
 * 
 * other
 * - shift-end to select all
 * - shift-up to remove last selected: AIOOB (but intermittent?)
 * 
 */
public class TestBindingsList extends Application {  
    
    @Override  
    public void start(Stage stage) throws Exception {  
        System.out.println(System.getProperty("java.version"));  
        List<String> items = Arrays.asList("One", "Two", "Three");  
//        ListView<String> listView = new ListViewAnchored<String>(FXCollections.observableArrayList(items));  
        ListView<String> listView = new ListView<String>(FXCollections.observableArrayList(items));  
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);  
        listView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<String>() {  
            @Override  
            public void onChanged(Change<? extends String> c) {  
                FXUtils.prettyPrint(c);
            }  
        });  
        List<String> list = new ArrayList<String>();  
        Bindings.bindContent(list, listView.getSelectionModel().getSelectedItems());  
        Button btnTest = new Button("Test");  
        btnTest.setOnAction(new EventHandler<ActionEvent>() {  
            @Override public void handle(ActionEvent e) {  
                System.out.println(list);  
            }  
        });  
  
        VBox vBox = new VBox(8);  
        Scene scene = new Scene(vBox);  
        vBox.getChildren().addAll(listView, btnTest);  
        stage.setScene(scene);  
        stage.show();  
    }  
  
    public static void main(String[] args) {  
        Application.launch(args);  
    }  
  
}  

