/*
 * Created on 15.10.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


/**
 * Test driver for selection/focus issues on modifying the items. 
 * 
 * Unrelated PENDING
 * - re-invent actionMaps?
 * - unified handling of KeyCode/KeyCodeCombination - how to?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SelectionAndModification extends Application {

    String[] actionKeys = {"insertAt0", "insertAtSelectedIndex", "removeAtSelectedIndex",
            "setAtSelectedIndex"};
    // PENDING - how to unify KeyCode and KeyCombination?
    KeyCode[] keys = {KeyCode.F1, KeyCode.F2, KeyCode.F3, KeyCode.F4};
    KeyCombination.Modifier[] modifiers = {null, null, null, null};
    
    protected Map<String, Consumer<Facade>> createActions() {
        Map<String, Consumer<Facade>> actions = new HashMap<>();
        actions.put("insertAt0", f -> {
            f.getItems().add(0, createItem(0));
        });
        actions.put("insertAtSelectedIndex", f -> {
            if (f.getSelectedIndex() < 0) return;
            f.getItems().add(f.getSelectedIndex(), createItem(f.getSelectedIndex()));
        });
        actions.put("removeAtSelectedIndex", f -> {
            if (f.getSelectedIndex() < 0) return;
            f.getItems().remove(f.getSelectedIndex());
        });
        actions.put("setAtSelectedIndex", f -> {
            if (f.getSelectedIndex() < 0) return;
            f.getItems().set(f.getSelectedIndex(), createItem(f.getSelectedIndex()));
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
    

    protected void configureActions(Facade facade, Map<String, Consumer<Facade>> actionMap, Map<KeyCodeCombination, String> inputMap) {
        inputMap.forEach((combi, s) -> {
            Consumer c = actionMap.get(s);
            facade.getControl().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == combi.getCode()) {
                    c.accept(facade);   
                }
            });
        });
    }
//-------------------------- infrastructure

    private Locale createItem(int i) {
        return new Locale("language-" + i, "country-" + i, "var-" + i);
    }
    
    private ObservableList<Locale> createList() {
        ObservableList<Locale> list = FXCollections.observableArrayList(Locale.getAvailableLocales());
        list.remove(0); // is an empty locale on my maschine
        return list;
    }
    
    protected Parent getContent() {
        
        TableFacade<Locale> table = new TableFacade<>();
        table.setItems(createList());
        TableColumn<Locale, String> language = new TableColumn<>(
                "Language");
        language.setCellValueFactory(new PropertyValueFactory<>("displayLanguage"));
        TableColumn<Locale, String> country = new TableColumn<>("Country");
        country.setCellValueFactory(new PropertyValueFactory<>("country"));
        TableColumn<Locale, String> variant = new TableColumn<>("Variant");
        variant.setCellValueFactory(new PropertyValueFactory<>("displayVariant"));
        table.getColumns().addAll(language, country, variant);
        
        ListFacade listView = new ListFacade<>();
        listView.setItems(createList());

        ListXFacade listXView = new ListXFacade();
        listXView.setItems(createList());
        
        Map<String, Consumer<Facade>> actionMap = createActions();
        Map<KeyCodeCombination, String> inputMap = createKeyMap();
        Map<String, KeyCodeCombination> inversInputMap = invertMap(inputMap);
        configureActions(table, actionMap, inputMap);
        configureActions(listView, actionMap, inputMap);
        configureActions(listXView, actionMap, inputMap);
        
        ContextMenu menu = new ContextMenu();
        GridPane info = new GridPane ();
        info.setPadding(new Insets(20));
        info.setVgap(10);
        info.setHgap(10);
        for (int i = 0; i < actionKeys.length; i++) {
            MenuItem mi = new MenuItem(actionKeys[i]);
            Consumer c = actionMap.get(actionKeys[i]);
            mi.setOnAction(e -> {
                // PENDING JW: how to get the component that triggered the
                // showing of the context?
                ContextMenu m = mi.getParentPopup();
                Node owner = m.getOwnerNode();
                // this is wrong ...
                c.accept(owner);
            });
            mi.setAccelerator(inversInputMap.get(actionKeys[i]));
            mi.setDisable(true);
            menu.getItems().add(mi);
            
            info.add(new Label(actionKeys[i]), 0, i);
            info.add(new Label(inversInputMap.get(actionKeys[i]).getDisplayText()), 1, i);
        }
        
        TabPane tabPane = new TabPane();
        
        Tab tab = new Tab("dummy.me");
        Pane content = new HBox(table, listView, listXView, info);
        table.setContextMenu(menu);
        tab.setContent(content);
        // information only - should use a tooltip instead
        tabPane.setContextMenu(menu);
        tabPane.getTabs().add(tab);
        return content;
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

    public static class ListXFacade<T> extends ListViewAnchored<T>
        implements Facade<T, ListViewAnchored<T>, MultipleSelectionModel<T>> {

        @Override
        public ListViewAnchored<T> getControl() {
            return this;
        }
        
    }
    public static class ListFacade<T> extends ListView<T>
        implements Facade<T, ListView<T>, MultipleSelectionModel<T>> {

        @Override
        public ListView<T> getControl() {
            return this;
        }
    }
  
    public static class TableFacade<T> extends TableView<T> 
        implements Facade<T, TableView<T>, MultipleSelectionModel<T>> {

        @Override
        public TableView<T> getControl() {
            return this;
        }

    }
    /**
     * Common api that the TestEntry can manage.
     */
    public static interface Facade<T, V extends Control, S extends SelectionModel<T>> {
        V getControl();
        S getSelectionModel();
        default int getSelectedIndex() {
            return getSelectionModel() != null ? getSelectionModel().getSelectedIndex() : - 1;
        }
        ObservableList<T> getItems();
        void setItems(ObservableList<T> items);
        FocusModel getFocusModel();
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
            .getLogger(SelectionAndModification.class.getName());
}
