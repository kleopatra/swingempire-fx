/*
 * Created on 08.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import de.swingempire.fx.util.DebugUtils;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeSelectionAndModification extends Application {
    
    
    String[] actionKeys = {"insertAt0", "insertAtSelectedIndex", "removeAtSelectedIndex",
            "setAtSelectedIndex", "removeAll(3, 5, 7)", "removeAt0", "clear", "resetInitial"};
    // PENDING - how to unify KeyCode and KeyCombination?
    KeyCode[] keys = {KeyCode.F1, KeyCode.F2, KeyCode.F3, KeyCode.F4, KeyCode.F5, 
            KeyCode.F6, KeyCode.F7, KeyCode.F8};
    KeyCombination.Modifier[] modifiers = {null, null, null, null, null, null, null, null};
    
    protected Map<String, Consumer<TreeView>> createActions() {
        Map<String, Consumer<TreeView>> actions = new HashMap<>();
        actions.put("insertAt0", f -> {
            f.getRoot().getChildren().add(0, createItem(0));
        });
        actions.put("insertAtSelectedIndex", f -> {
            int index = f.getSelectionModel().getSelectedIndex();
            if (index < 0) return;
            if (f.isShowRoot()) index--;
            f.getRoot().getChildren().add(index, createItem(index));
//            printSelectionState("insertAtSelected", f);
        });
        actions.put("removeAtSelectedIndex", f -> {
            int index = f.getSelectionModel().getSelectedIndex();
            if (index < 0) return;
            if (f.isShowRoot()) index--;
            f.getRoot().getChildren().remove(index);
        });
        actions.put("setAtSelectedIndex", f -> {
            int index = f.getSelectionModel().getSelectedIndex();
            if (index < 0) return;
            if (f.isShowRoot()) index--;
            f.getRoot().getChildren().set(index, createItem(index));
//            printSelectionState("setAtSelected", f);
        });
        actions.put("removeAll(3, 5, 7)", f -> {
            ObservableList children = f.getRoot().getChildren();
            children.removeAll(children.get(3), children.get(5), children.get(7));
        });
        actions.put("removeAt0", f -> {
            if (f.getRoot().getChildren().size() == 0) return;
            f.getRoot().getChildren().remove(0);
            Platform.runLater(() -> {
                DebugUtils.printSelectionState(f);
                
            });
//            LOG.info("focus: " + f.getSelectionModel().getSelectedIndex());
        });
        actions.put("clear", f -> {
            if (f.getRoot().getChildren().size() == 0) return;
            f.getRoot().getChildren().clear();
            Platform.runLater(() -> {
                DebugUtils.printSelectionState(f);
            });
            
        });
        actions.put("resetInitial", f -> {
            f.getRoot().getChildren().setAll(createRootChildren());
        });
        
        
        return actions ;
    }

    protected Map<KeyCodeCombination, String> createKeyMap() {
        if (actionKeys.length != keys.length || actionKeys.length != modifiers.length)
            throw new IllegalStateException("config arrays size must be same but was (action/keys/mods: "
                    + actionKeys.length + "/" + keys.length + "/" + modifiers.length);
            
        Map<KeyCodeCombination, String> keyMap = new HashMap<>();
        for (int i = 0; i < actionKeys.length; i++) {
            KeyCodeCombination combi = modifiers[i] != null ? new KeyCodeCombination(keys[i], modifiers[i]) :
                new KeyCodeCombination(keys[i]);
            keyMap.put(combi, actionKeys[i]);
        }
        return keyMap;
    }
    

    protected void configureActions(TreeView facade, Map<String, Consumer<TreeView>> actionMap, Map<KeyCodeCombination, String> inputMap) {
        inputMap.forEach((combi, s) -> {
            Consumer c = actionMap.get(s);
            facade.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == combi.getCode()) {
                    c.accept(facade);   
                    // PENDING JW: without consuming we get weird selection in TableView?
                    e.consume();
                }
            });
        });
    }

    @SuppressWarnings("rawtypes")
    protected Parent getContent() {
        
        TreeView tree = new TreeView(createRoot());
        tree.getRoot().setExpanded(true);
//        tree.setShowRoot(false);

        Map<String, Consumer<TreeView>> actionMap = createActions();
        Map<KeyCodeCombination, String> inputMap = createKeyMap();
        Map<String, KeyCodeCombination> inversInputMap = invertMap(inputMap);
        configureActions(tree, actionMap, inputMap);
        GridPane info = new GridPane ();
        info.setPadding(new Insets(20));
        info.setVgap(10);
        info.setHgap(10);
        for (int i = 0; i < actionKeys.length; i++) {
            info.add(new Label(actionKeys[i]), 0, i);
            info.add(new Label(inversInputMap.get(actionKeys[i]).getDisplayText()), 1, i);
        }
        Pane content = new HBox(tree, info);
        CheckBox check = new CheckBox("MultipleMode");
        check.setOnAction(e -> {
            SelectionMode old = tree.getSelectionModel().getSelectionMode();
            SelectionMode newMode = check.isSelected() ? SelectionMode.MULTIPLE : SelectionMode.MULTIPLE;
            tree.getSelectionModel().setSelectionMode(newMode);
        });
        Pane buttons = new HBox(check);
//        Pane content = new HBox(table, listView, info);
//        Pane content = new HBox(table, info);
        BorderPane borderPane = new BorderPane(content);
        borderPane.setBottom(buttons);
        return borderPane;
    }    
    
    /**
     * @param inputMap
     * @return
     */
    private Map<String, KeyCodeCombination> invertMap(
            Map<KeyCodeCombination, String> inputMap) {
        Map<String, KeyCodeCombination> invers = new HashMap<>();
        inputMap.forEach((combi, s) -> {
            invers.put(s,  combi);
        });
        return invers;
    }

    /**
     * @return
     */
    private TreeItem createRoot() {
//        ObservableList content = FXCollections.observableArrayList(
//                "9-item", "8-item", "7-item", "6-item", 
//                "5-item", "4-item", "3-item", "2-item", "1-item");
//        for (Object object : content) {
//            root.getChildren().add(createItem(object));
//        }
        TreeItem root = createItem("root");
        root.getChildren().setAll(createRootChildren());
        return root;
    }
    
    private List<TreeItem> createRootChildren() {
        ObservableList content = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        List<TreeItem> root = new ArrayList<>();
        content.forEach(item -> root.add(createItem(item)));
        return root;
    }
    
    protected TreeItem createItem(Object item) {
        return new TreeItem(item);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.setTitle(System.getProperty("java.version"));
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TreeSelectionAndModification.class.getName());
}
