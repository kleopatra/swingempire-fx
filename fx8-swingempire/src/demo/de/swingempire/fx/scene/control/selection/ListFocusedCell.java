/*
 * Created on 14.07.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 */
public class ListFocusedCell extends Application {
    private final ObservableList<Locale> data =
            FXCollections.observableArrayList(Locale.getAvailableLocales());
   
//    private final ListView<Locale> list = new ListViewAnchored<>();
//    private final ListView<Locale> list = new ListView<>();
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Focus/Anchor Bug " + System.getProperty("java.version"));
        ListView<Locale> list = new ListView<Locale>();
        ListView<Locale> anchored = new ListViewAnchored<Locale>();
        configureList(list);
        configureList(anchored);
        
        
        Button remove = new Button("Remove");
        remove.setOnAction(ev -> {
            data.remove(0);
        });
        Button add = new Button("Add");
        add.setOnAction(ev -> {
            data.add(0, new Locale("dummy", "OO"));
        });
        Button clear = new Button("Clear Selection");
        clear.setOnAction(ev -> {
            list.getSelectionModel().clearSelection();
            anchored.getSelectionModel().clearSelection();
        });
        Button clearAt = new Button("ClearAt SelectedIndex");
        clearAt.setOnAction( ev -> {
            int coreSelected = list.getSelectionModel().getSelectedIndex();
            list.getSelectionModel().clearSelection(coreSelected);
            
            int anchorSelected = anchored.getSelectionModel().getSelectedIndex();
            anchored.getSelectionModel().clearSelection(anchorSelected);
        });
        Button toggleMode = new Button("ToggleSelectionMode");
        toggleMode.setOnAction(ev -> {
            SelectionMode old = list.getSelectionModel().getSelectionMode();
            SelectionMode newMode = old == SelectionMode.SINGLE ? SelectionMode.MULTIPLE : SelectionMode.SINGLE;
            list.getSelectionModel().setSelectionMode(newMode);
            anchored.getSelectionModel().setSelectionMode(newMode);
        });
        
        Pane buttonPane = new FlowPane(clear, clearAt, toggleMode, add, remove);
        Pane listPane = new VBox(new Label(list.getClass().getSimpleName()), list);
        Pane anchoredPane = new VBox(new Label(anchored.getClass().getSimpleName()), anchored);
        Pane box = new HBox(listPane, anchoredPane);
        BorderPane root = new BorderPane(box);
        root.setTop(buttonPane);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    protected void configureList(ListView<Locale> list) {
        // add a listener to see loosing the column
//        list.getFocusModel().focusedIndexProperty().addListener((p, oldValue, newValue)-> {
//            LOG.info("focused old/new " + oldValue + " / " + newValue);
//        });
//        
//        list.getSelectionModel().selectedIndexProperty().addListener((p, oldValue, newValue) -> {
//            LOG.info("selected old/new " + oldValue + " / " + newValue);
//        });
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
                LOG.info("anchor before/after remove: " + before + "/" + FXUtils.getAnchorIndex(list));
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
    }

    public static void main(String[] args) {
        launch(args);
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ListFocusedCell.class
            .getName());
}