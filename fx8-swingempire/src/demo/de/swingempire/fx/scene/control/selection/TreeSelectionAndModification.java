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
import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.util.DebugUtils;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeSelectionAndModification extends Application {
    
    
    String[] actionKeys = {"insertAt0", "insertAtSelectedIndex", "removeAtSelectedIndex",
            "setAtParentOfSelectedIndex",
            "setAtSelectedIndex", "removeAll(3, 5, 7)", "removeAt0", 
            "clearRoot", "resetInitial", "insertBranchAtSelected", "setBranchAtSelected",
            "removeGrandParent", "toggleRoot"};
    // PENDING - how to unify KeyCode and KeyCombination?
    KeyCode[] keys = {KeyCode.F1, KeyCode.F2, KeyCode.DELETE, KeyCode.F3, KeyCode.F4, KeyCode.F5, 
            KeyCode.F6, KeyCode.F7, KeyCode.F8, KeyCode.F9, KeyCode.F10, KeyCode.F11, KeyCode.F12};
    KeyCombination.Modifier[] modifiers = {null, null, null, null, null, null, 
            null, null, null, null, null, null, null};
    private int count;
    
    protected Map<String, Consumer<TreeView>> createActions() {
        Map<String, Consumer<TreeView>> actions = new HashMap<>();
        actions.put("insertAt0", f -> {
            f.getRoot().getChildren().add(0, createItem(count++ + "-newItem", f));
        });
        actions.put("insertAtSelectedIndex", f -> {
//            int index = f.getSelectionModel().getSelectedIndex();
//            if (index < 0) return;
            TreeItem node = (TreeItem) f.getSelectionModel().getSelectedItem();
            if (node == null || node.getParent() == null) return;
            TreeItem child = createItem(count++ + "insert-newItem", f);
            int index = node.getParent().getChildren().indexOf(node);
            node.getParent().getChildren().add(index, child);
//            printSelectionState("insertAtSelected", f);
        });
        actions.put("removeAtSelectedIndex", f -> {
            TreeItem node = (TreeItem) f.getSelectionModel().getSelectedItem();
            if (node == null || node.getParent() == null) return;
            node.getParent().getChildren().remove(node);
        });
        actions.put("setAtParentOfSelectedIndex", f -> {
            int selectedIndex = f.getSelectionModel().getSelectedIndex();
            TreeItem child = f.getTreeItem(selectedIndex);
            // need a selected grandchild
            if (child == null || child.getParent() == null || child.getParent().getParent() == null) return;
            TreeItem grandParent = child.getParent().getParent();
            int childIndex = grandParent.getChildren().indexOf(child.getParent());
            TreeItem otherBranch = createBranch(count++ + "replacedParentOfSelected", f);
            grandParent.getChildren().set(childIndex, otherBranch);
            
        });
        actions.put("setAtSelectedIndex", f -> {
            int selectedIndex = f.getSelectionModel().getSelectedIndex();
            TreeItem child = f.getTreeItem(selectedIndex);
            if (child == null || child.getParent() == null) return;
            TreeItem parent = child.getParent();
            int childIndex = parent.getChildren().indexOf(child);
            parent.getChildren().set(childIndex, createItem(count++ + " set-newItem", f));
//            TreeItem node = (TreeItem) f.getSelectionModel().getSelectedItem();
//            if (node == null || node.getParent() == null) return;
//            int index = node.getParent().getChildren().indexOf(node);
//            node.getParent().getChildren().set(index, createItem(count++ +"set-newItem", f));
////            printSelectionState("setAtSelected", f);
        });
        actions.put("removeAll(3, 5, 7)", f -> {
            ObservableList children = f.getRoot().getChildren();
            if (children.size() < 8) return;
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
        actions.put("clearRoot", f -> {
            if (f.getRoot().getChildren().size() == 0) return;
            f.getRoot().getChildren().clear();
            Platform.runLater(() -> {
                DebugUtils.printSelectionState(f);
            });
            
        });
        actions.put("resetInitial", f -> {
            f.getRoot().getChildren().setAll(createRootChildren(f.getRoot() instanceof TreeItemX));
        });
        
        actions.put("insertBranchAtSelected", f -> {
//          int index = f.getSelectionModel().getSelectedIndex();
//          if (index < 0) return;
          TreeItem node = (TreeItem) f.getSelectionModel().getSelectedItem();
          if (node == null || node.getParent() == null) return;
          TreeItem child = createBranch(count++ + "insert-Branch", f);
          int index = node.getParent().getChildren().indexOf(node);
          node.getParent().getChildren().add(index, child);
//          printSelectionState("insertAtSelected", f);
      });
        actions.put("setBranchAtSelected", f -> {
            TreeItem node = (TreeItem) f.getSelectionModel().getSelectedItem();
            if (node == null || node.getParent() == null) return;
            int index = node.getParent().getChildren().indexOf(node);
            node.getParent().getChildren().set(index, createBranch(count++ +"set-Branch", f));
//          DebugUtils.printSelectionState(f);
        });
      actions.put("removeGrandParent", f -> {
          TreeItem node = (TreeItem) f.getSelectionModel().getSelectedItem();
          if (node == null || node.getParent() == null) return;
          TreeItem grandParent = node.getParent().getParent();
          // root involved
          if (grandParent == null || grandParent.getParent() == null ) return;
          grandParent.getParent().getChildren().remove(grandParent);
          LOG.info("" + f.getSelectionModel().getSelectedItems());
      });
      actions.put("toggleRoot", f -> {
          f.setShowRoot(!f.isShowRoot());
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
        
        TreeView tree = new TreeView(createBranch("Root"));
        tree.getRoot().setExpanded(true);
//        tree.setShowRoot(false);

        Map<String, Consumer<TreeView>> actionMap = createActions();
        Map<KeyCodeCombination, String> inputMap = createKeyMap();
        Map<String, KeyCodeCombination> inversInputMap = invertMap(inputMap);
        configureActions(tree, actionMap, inputMap);
        
        TreeView treeX = new TreeView(createBranch("RootX", true));
        treeX.getRoot().setExpanded(true);
        treeX.setSelectionModel(new SimpleTreeSelectionModel<>(treeX));
        configureActions(treeX, actionMap, inputMap);
        
        GridPane info = new GridPane ();
        info.setPadding(new Insets(20));
        info.setVgap(10);
        info.setHgap(10);
        for (int i = 0; i < actionKeys.length; i++) {
            info.add(new Label(actionKeys[i]), 0, i);
            info.add(new Label(inversInputMap.get(actionKeys[i]).getDisplayText()), 1, i);
        }
        Pane content = new HBox(tree, treeX, info);
        CheckBox check = new CheckBox("MultipleMode");
        check.setOnAction(e -> {
            SelectionMode old = tree.getSelectionModel().getSelectionMode();
            SelectionMode newMode = check.isSelected() ? SelectionMode.MULTIPLE : SelectionMode.MULTIPLE;
            tree.getSelectionModel().setSelectionMode(newMode);
            treeX.getSelectionModel().setSelectionMode(newMode);
        });
        Pane buttons = new HBox(check);
//        Pane content = new HBox(tree, treeX, info);
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

    protected TreeItem createBranch(String value) {
        return createBranch(value, false);
    }
    
    protected TreeItem createBranch(String value, TreeView view) {
        return createBranch(value, view != null && view.getRoot() instanceof TreeItemX);
    }
    protected TreeItem createBranch(String value, boolean xItem) {
        TreeItem root = createItem(value, xItem);
        root.getChildren().setAll(createRootChildren(xItem));
        return root;
    }
    
    private List<TreeItem> createRootChildren(boolean xItem) {
        ObservableList content = FXCollections.observableArrayList(
                "9-item", "8-item", "7-item", "6-item", 
                "5-item", "4-item", "3-item", "2-item", "1-item");
        List<TreeItem> root = new ArrayList<>();
        content.forEach(item -> root.add(createItem(item, xItem)));
        return root;
    }
    
    protected TreeItem createItem(Object item) {
        return createItem(item, false);
    }

    protected TreeItem createItem(Object value, TreeView view) {
        TreeItem root = view.getRoot();
        return createItem(value, root != null && root instanceof TreeItemX);
//        return root instanceof TreeItemX ? new TreeItemX(value) : new TreeItem(value);
    }
    
    protected TreeItem createItem(Object value, boolean xItem) {
        return xItem ? new TreeItemX(value) : new TreeItem(value);
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
