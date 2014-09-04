/*
 * Created on 14.07.2014
 *
 */
package de.swingempire.fx.control;

import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.control.selection.ListViewAnchored;
import fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 */
public class ListFocusedCell extends Application {
    private final ObservableList<Locale> data =
            FXCollections.observableArrayList(Locale.getAvailableLocales());
   
//    private final ListView<Locale> list = new ListViewAnchored<>();
    private final ListView<Locale> list = new ListView<>();
    
    @Override
    public void start(Stage stage) {
        stage.setTitle(list.getClass().getSimpleName() + " Focus/Anchor Bug");
        // add a listener to see loosing the column
        list.getFocusModel().focusedIndexProperty().addListener((p, oldValue, newValue)-> {
            LOG.info("focused old/new " + oldValue + "\n  " + newValue);
        });
        
        list.getSelectionModel().selectedIndexProperty().addListener((p, oldValue, newValue) -> {
            LOG.info("selected old/new " + oldValue + "\n  " + newValue);
        });
        // prevent selection on focusGained
//        list.getProperties().put("selectOnFocusGain", Boolean.FALSE);
        list.setItems(data);
        
        // https://javafx-jira.kenai.com/browse/RT-38491
        // incorrect extend selection after inserting item
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        list.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F1) {
                int before = FXUtils.getAnchorIndex(list);
                data.add(0, new Locale("dummy"));
                LOG.info("anchor before/after insert: " + before + "/" + FXUtils.getAnchorIndex(list));
            }
        });
        
        // https://javafx-jira.kenai.com/browse/RT-30931
        // remove selected item - the issue is still open
        list.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F6) {
                int before = FXUtils.getAnchorIndex(list);
                data.remove(list.getSelectionModel().getSelectedIndex());
                LOG.info("anchor before/after insert: " + before + "/" + FXUtils.getAnchorIndex(list));
            }
        });
        
        // extend selection after programmatically select?
        list.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F3) {
                list.getSelectionModel().clearAndSelect(2);
                LOG.info("anchor after clearAndSelect 2: " + FXUtils.getAnchorIndex(list) );
            }
        });
        
        // clear selected?
        list.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F5) {
                list.getSelectionModel().select(3);
                list.getSelectionModel().clearSelection(list.getSelectionModel().getSelectedIndex());
                LOG.info("focus/anchor after clearSelection(selected): " 
                   + list.getFocusModel().getFocusedIndex() + "/" + FXUtils.getAnchorIndex(list) );
            }
        });
        
        // extend selection after programmatically select?
        list.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F4) {
                list.getSelectionModel().clearSelection(list.getSelectionModel().getSelectedIndex());
                list.getSelectionModel().selectRange(2, 4);
                LOG.info("anchor after range: " + list.getProperties().get("anchor"));
            }
        });
        
        
        Button button = new Button("Add");
        button.setOnAction(ev -> {
            data.add(0, new Locale("dummy"));
        });
        Button clear = new Button("Clear Selection");
        clear.setOnAction(ev -> {
            list.getSelectionModel().clearSelection();;
        });
        BorderPane root = new BorderPane(list);
        root.setLeft(button);
        root.setTop(clear);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ListFocusedCell.class
            .getName());
}