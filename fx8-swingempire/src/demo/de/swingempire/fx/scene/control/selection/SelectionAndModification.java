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

import de.swingempire.fx.util.DebugUtils.Facade;
import de.swingempire.fx.util.DebugUtils.ListFacade;
import de.swingempire.fx.util.DebugUtils.TableFacade;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


/**
 * fx9: removed references to ListXView (doesn't compile for fx9)
 * 
 * -----------
 * Test driver for selection/focus issues on modifying the items. 
 * 
 * Unrelated PENDING
 * - re-invent actionMaps?
 * - unified handling of KeyCode/KeyCodeCombination - how to?
 *
 * TableView
 * 
 * ReplaceAtSelected:
 * - press down several times
 * - press f4 to replace item at selection
 * - expected: selection on replaced item
 * - actual: selection moved to first row
 * 
 * InsertAtSelected
 * - press down several times
 * - press f2 to insert at selection
 * - expected: selection at old (+/-1, modulo RT-30931)
 * - actual: selection at first
 * - do again: down several times, press to insert f2 at selection
 * - expected as above, actual: selection at old + 2
 * 
 * InsertAt0 if first selected (initial state)
 * - press f1 to insert at 0
 * - expected: selection/focus on second item
 * - actual: focus on first, selection on second
 * 
 * 
 * InsertAt0
 * - press down several times
 * - press f1 to insert at 0
 * - expected: selection/focus stick to selected item
 * - actual: focus at 0 (selection as expected)
 * - press f1 again to insert at 0
 * - expected same as above
 * - actual: focus one item below selected (selection as expected) 
 * 
 * ListView
 * ReplaceAtSelected - works at expected
 * 
 * InsertAtSelected
 * - same bug as "do again" in tableView
 * 
 * InsertAt0
 * - press down several times
 * - press f1 to insert at 0
 * - expected: selection/focus sticks to selected item
 * - actual: selection/focus moved to item below old selected
 * 
 * ListViewA
 * ReplaceAtSelected - works as expected
 * 
 * InsertAtSelected
 * - same bug as "do again" in tableView
 * 
 * InsertAt0
 * - press down several times
 * - press f1 to insert at 0
 * - expected: selection/focus sticks to selected item
 * - actual: selection sticks, focus is one below (the delta increases with repeated f1)
 * 
 * PENDING ALL:
 * weird update if removeAll(...) which leads to multiple changes 
 * - focus moved independent of selection
 * - selection half-way correct
 * - tableView unselects if selection is between first/last removed, focus unpredictable
 * - listView disables navigation if selection on first removed
 * - listViewA has same problems as core
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SelectionAndModification extends Application {

    String[] actionKeys = {"insertAt0", "insertAtSelectedIndex", "removeAtSelectedIndex",
            "setAtSelectedIndex", "removeAll(3, 5, 7)", "removeAt0", "setAtSecondarySelected"};
    // PENDING - how to unify KeyCode and KeyCombination?
    KeyCode[] keys = {KeyCode.F1, KeyCode.F2, KeyCode.F3, KeyCode.F4, KeyCode.F5, KeyCode.F6, KeyCode.F7};
    KeyCombination.Modifier[] modifiers = {null, null, null, null, null, null, null};
    
    protected Map<String, Consumer<Facade>> createActions() {
        Map<String, Consumer<Facade>> actions = new HashMap<>();
        actions.put("insertAt0", f -> {
            f.getItems().add(0, createItem(0));
        });
        actions.put("insertAtSelectedIndex", f -> {
            if (f.getSelectedIndex() < 0) return;
            f.getItems().add(f.getSelectedIndex(), createItem(f.getSelectedIndex()));
//            printSelectionState("insertAtSelected", f);
        });
        actions.put("removeAtSelectedIndex", f -> {
            if (f.getSelectedIndex() < 0) return;
            f.getItems().remove(f.getSelectedIndex());
        });
        actions.put("setAtSelectedIndex", f -> {
            if (f.getSelectedIndex() < 0) return;
            f.getItems().set(f.getSelectedIndex(), createItem(f.getSelectedIndex()));
//            printSelectionState("setAtSelected", f);
        });
        actions.put("removeAll(3, 5, 7)", f -> {
            f.getItems().removeAll(f.getItems().get(3), f.getItems().get(5), f.getItems().get(7));
        });
        actions.put("removeAt0", f -> {
            f.getItems().remove(0);
        });
        actions.put("setAtSecondarySelected", f -> {
            int size = f.getSelectionModel().getSelectedIndices().size();
            if (size < 2) return;
            int selectedIndex = f.getSelectedIndex();
            for(int i = 0; i < size; i++) {
                int secondary = (int) f.getSelectionModel().getSelectedIndices().get(i);
                if (secondary != selectedIndex) {
                    f.getItems().set(secondary, "new Item");
                    break;
                }
            }
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
                    // PENDING JW: without consuming we get weird selection in TableView?
                    e.consume();
                }
            });
        });
    }
//-------------------------- infrastructure

    int count;
    private Locale createItem(int i) {
        return new Locale("language-" + count++, "country-" + i, "var-" + i);
    }
    
    private ObservableList<Locale> createList() {
        ObservableList<Locale> list = FXCollections.observableArrayList(Locale.getAvailableLocales());
        list.remove(0); // is an empty locale on my maschine
        return list;
    }
    
    protected Parent getContent() {
        
        SelectionMode initialMode = SelectionMode.MULTIPLE;
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
        
        table.getSelectionModel().setSelectionMode(initialMode);
        table.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Locale>) ch -> {
            // table seems to fire correct events
            while (ch.next()) {
                if (ch.wasAdded()) {
                    System.out.println("+" + ch.getAddedSubList());
                }
            }
        });
        ListFacade listView = new ListFacade<>();
        // quick check for auto-focus/select
//        // disable selecting the first item on focus gain - this is
//        // not what is expected in the ComboBox control (unlike the
//        // ListView control, which does this).
//        listView.getProperties().put("selectOnFocusGain", false);
//        // introduced between 8u20 and 8u40b7
//        // with this, testfailures back to normal
//        listView.getProperties().put("selectFirstRowByDefault", false);

        listView.setItems(createList());
        listView.getSelectionModel().setSelectionMode(initialMode);

        ListFacade listSView = new ListFacade<>();
        listSView.setSelectionModel(new SimpleListSelectionModel<>(listSView));
        listSView.setItems(createList());
        listSView.getSelectionModel().setSelectionMode(initialMode);

        // quick check for shift-down
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) ch -> {
            while (ch.next()) {
                if (ch.wasAdded()) {
                    System.out.println("+" + ch.getAddedSubList());
                }
            }
        });

        listSView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listSView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) ch -> {
            while (ch.next()) {
                if (ch.wasAdded()) {
                    System.out.println("+" + ch.getAddedSubList());
                }
            }
        });
        // ------- end quick check for shift-down
        
        // fx9: removed - custom skin doesn't compile
//        ListXFacade listXView = new ListXFacade();
//        listXView.setItems(createList());
//        
//        ListXFacade listXAView = new ListXFacade();
//        listXAView.setSelectionModel(new SimpleASelectionModel<>(listXAView));
//        listXAView.setItems(createList());
        
        Map<String, Consumer<Facade>> actionMap = createActions();
        Map<KeyCodeCombination, String> inputMap = createKeyMap();
        Map<String, KeyCodeCombination> inversInputMap = invertMap(inputMap);
        configureActions(table, actionMap, inputMap);
        configureActions(listView, actionMap, inputMap);
//        configureActions(listXView, actionMap, inputMap);
//        configureActions(listXAView, actionMap, inputMap);
        configureActions(listSView, actionMap, inputMap);
        
        GridPane info = new GridPane ();
        info.setPadding(new Insets(20));
        info.setVgap(10);
        info.setHgap(10);
        for (int i = 0; i < actionKeys.length; i++) {
            info.add(new Label(actionKeys[i]), 0, i);
            info.add(new Label(inversInputMap.get(actionKeys[i]).getDisplayText()), 1, i);
        }
        Pane content = new HBox(table,  listView, listSView, /* listXView, listXAView,*/ info);
        Pane header = new HBox(/*new Label("table"), */ new Label("  core list "), 
                new Label("  core list + simple  ") /*new Label("listX"), new Label("listX + simpleA")*/);
        CheckBox check = new CheckBox("MultipleMode");
        check.setSelected(initialMode == SelectionMode.MULTIPLE);
        check.setOnAction(e -> {
            SelectionMode old = listView.getSelectionModel().getSelectionMode();
            SelectionMode newMode = check.isSelected() ? initialMode : initialMode;
            listView.getSelectionModel().setSelectionMode(newMode);
            table.getSelectionModel().setSelectionMode(newMode);
            listSView.getSelectionModel().setSelectionMode(newMode);
//            listXView.getSelectionModel().setSelectionMode(newMode);
//            listXAView.getSelectionModel().setSelectionMode(newMode);
        });
        Pane buttons = new HBox(check);
//        Pane content = new HBox(table, listView, info);
//        Pane content = new HBox(table, info);
        
        BorderPane borderPane = new BorderPane(content);
        borderPane.setTop(header);
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


    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F10), () -> LOG.info("got accelerator "));
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
