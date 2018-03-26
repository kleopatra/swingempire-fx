/*
 * Created on 14.07.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Locale;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Reported: https://javafx-jira.kenai.com/browse/RT-38517
 * Regression: no way to unselect a list
 *
 * To reproduce:
 * 
 * - run: first item selected
 * - click button: selection cleared
 * - tab in list: first item selected again
 * 
 * Hack around: set clientProperty
 * 
 */
public class ListFocusedCellDefault_38517 extends Application {
    private final ObservableList<Locale> data =
            FXCollections.observableArrayList(Locale.getAvailableLocales());
   
    private final ListView<Locale> list = new ListView<>(data);
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("List Focus/Anchor Bug");
        // hack for ListView, not available for TableView
        list.getProperties().put("selectOnFocusGain", Boolean.FALSE);
        Button clear = new Button("Clear Selection");
        clear.setOnAction(ev -> {
            list.getSelectionModel().clearSelection();;
        });
        BorderPane root = new BorderPane(list);
        root.setTop(clear);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ListFocusedCellDefault_38517.class
            .getName());
}