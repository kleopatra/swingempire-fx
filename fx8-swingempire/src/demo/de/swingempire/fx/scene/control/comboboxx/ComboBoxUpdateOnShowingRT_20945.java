/*
 * Created on 01.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import de.swingempire.fx.scene.control.comboboxx.ComboBoxX;

/**
 * 
 * Basic test context: behaviour if items are modified while opening the popup.
 * 
 * Basic requirement: selectedItem must not change by opening.
 * 
 * Test setup:
 * each tab contains a editable/not-editable choice control (if supported)
 * each combo's initial value is external to the list
 * initial items can be varied (single/multiple items)
 * items mutations can be varied
 * 
 * Test steps:
 * - click arrow to open popup: initial value unchanged
 * - select first item (that's the position to be modified) 
 * - click arrow again
 * - expected: previously selected item must be shown
 * - if bug, then actual: selected item changed
 * 
 * As of 8u40b7, the failures are
 * - control.setItems: clears selection
 * - items.replaceAll(UnaryOp): "sticky" selection at last 
 * - items.remove(0): clears selection
 * 
 * Related to: 
 * https://javafx-jira.kenai.com/browse/RT-22572 - covers items.setAll, fixed
 * https://javafx-jira.kenai.com/browse/RT-20945 - covers items.setAll, fixed
 * https://javafx-jira.kenai.com/browse/RT-38899 - covers control.setItems, open
 * 
 * ---------------
 * Regression guard against: https://javafx-jira.kenai.com/browse/RT-22572
 * 
 * - Select the item from the ComboBox menu.
 * - Click on the menuButton without selecting anything : the value is removed.
 * 
 * Regression guard against: https://javafx-jira.kenai.com/browse/RT-22937
 * ?? somehow related to action handler?
 * 
 * Regression guard against: https://javafx-jira.kenai.com/browse/RT-20945
 *  
 * - click button and select item in list: value updated
 * - click on button: action handler fired
 * 
 * Note: the issue here is that the items are always reset on showing!
 * 
 * Also note:
 * - select once
 * - open popup again
 * - press esc: value must not be cleared
 *  
 * fixed in 2.2 
 * 
 * But: 
 * - still cleared if resetting the list (which is functionally equivalent to setAll)
 * - choicebox clears always
 * - other dynamic updates aren't handled correctly
 * 
 * Here trying to dig into other dynamic updates:
 * replaceAll via unaryOperator
 * - behaviour of x combo: replaces changed if contained - why? Only on very first
 *   dynamic update. Invalidation problem? With debugging log, the value is correct
 *   but the list shows the old selected as selected
 * - core combo: completely incorrect selection
 * 
 * Note to self: extracted core-only for bug report
 * @author jfdenise
 * @see ComboboxSelectionRT_26079
 * @see ComboBoxUpdateOnShowingCore
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ComboBoxUpdateOnShowingRT_20945 extends Application {

    protected Parent createContent() {
        TabPane tabPane = createComboBoxTabs();
        TabPane tabPaneX = createComboBoxXTabs();
        Pane box = new VBox(20, tabPane, tabPaneX);
        return box;
    }

    protected TabPane createComboBoxTabs() {
        TabPane tabPane = new TabPane();
        // single item initially
        Callback<Void, Facade> singleItem = (p) -> new ComboFacade(
                FXCollections.observableArrayList("Toto")); 
        // multiple items initially
        Callback<Void, Facade> multipleItems = (p) -> new ComboFacade(
                FXCollections.observableArrayList("Toto", "Tato", "Tati", "Tata")); 
        addTestTabs(tabPane, singleItem, multipleItems);
        return tabPane;
    }
    
    protected TabPane createComboBoxXTabs() {
        TabPane tabPane = new TabPane();
        // single item initially
        Callback<Void, Facade> singleItem = (p) -> new ComboXFacade(
                FXCollections.observableArrayList("Toto")); 
        // multiple items initially
        Callback<Void, Facade> multipleItems = (p) -> new ComboXFacade(
                FXCollections.observableArrayList("Toto", "Tato", "Tati", "Tata")); 
        addTestTabs(tabPane, singleItem, multipleItems);
        return tabPane;
    }

    protected void addTestTabs(TabPane tabPane,
            Callback<Void, Facade> singleItem,
            Callback<Void, Facade> multipleItems) {
        addTab(tabPane, "single, setAll", singleItem,
                control -> {
                    control.getItems().setAll("" +System.currentTimeMillis());
                    return null;
                }
                );
        addTab(tabPane, "single, setItems", singleItem,
                control -> {
                    control.setItems(FXCollections.observableArrayList(System.currentTimeMillis()));
                    return null;
                }
         );
        
        addTab(tabPane, "multiple, setAll", multipleItems,
                control -> {
                    control.getItems().setAll("" +System.currentTimeMillis(), "constant");
                    return null;
                }
                );
        addTab(tabPane, "multiple, setItems", multipleItems,
                control -> {
                    control.setItems(FXCollections.observableArrayList("" +System.currentTimeMillis(), 
                            "constant"));
                    return null;
                }
                );
        
        addTab(tabPane, "multiple, replace", multipleItems,
                control -> {
                    UnaryOperator<String> op = p -> {
                        if (p.endsWith("o")) {
                            return p + "o";
                        }
                        return p;
                    };
                    control.getItems().replaceAll(op);
                    return null;
                }
                );
        addTab(tabPane, "multiple, remove", multipleItems,
                control -> {
                    control.getItems().remove(0);
                    return null;
                }
                );
        addTab(tabPane, "dev select", multipleItems, 
                control -> {
                    // dev can override default by explicitly setting the value
                    control.getItems().setAll("completely diff", "other");
                    control.setValue(control.getItems().get(0));
                    return null;
                }
                );
    }

    /**
     * Creates testItems from the given callbacks, one for edtiable/non-editable each
     * and adds its controls to the tab. 
     */
    private void addTab(TabPane tabPane, String string,
            Callback<Void, Facade> factory, Callback <Facade, Void> updater) {
        TestItem editable = new TestItem(factory.call(null), updater, true);
        TestItem notEditable = new TestItem(factory.call(null), updater, false);
        Pane content = new FlowPane(editable.getControl(), notEditable.getControl());
        Tab tab = new Tab(string);
        tab.setContent(content);
        tabPane.getTabs().addAll(tab);
    }

    /**
     * Convenience test configurator.
     */
    public static class TestItem<V extends Control> {
        final Facade<Object, V> comboControl;
        final Callback<Facade, Void> updater;
        final boolean initialContained;
        final boolean editable;
        
        public TestItem(Facade comboControl, 
                Callback<Facade, Void> updater, 
                boolean editable) {
            this.comboControl = comboControl;
            this.updater = updater;
            this.initialContained = false;
            this.editable = editable;
            configureControl();
        }

        public V getControl() {
            return comboControl.getComboControl();
        }

        private void configureControl() {
            comboControl.setEditable(editable);
            if (initialContained) {
                comboControl.setValue(comboControl.getItems().get(0));
            } else {
                comboControl.setValue(comboControl.getComboControl().getClass().getSimpleName());
            }
            comboControl.showingProperty().addListener((o, old, value) -> {
                if (value) {
                    updater.call(comboControl);
                }
            });
        }
    }

    /**
     * Common api that the TestEntry can manage.
     */
    public static interface Facade<T, V extends Control> {
        V getComboControl();
        ObservableList<T> getItems();
        void setItems(ObservableList<T> items);
        ReadOnlyBooleanProperty showingProperty();
        void setEditable(boolean editable);
        void setValue(T value);
    }
    
    /**
     * Facade for core ComboBox.
     */
    public static class ComboFacade<T> extends ComboBox<T> implements Facade<T, ComboBox<T>> {
        
        
        public ComboFacade(ObservableList<T> items) {
            super(items);
        }
        @Override
        public ComboBox<T> getComboControl() {
            return this;
        }
        
    }
    /**
     * Facade for core ComboBox.
     */
    public static class ComboXFacade<T> extends ComboBoxX<T> implements Facade<T, ComboBoxX<T>> {


        public ComboXFacade(ObservableList<T> items) {
            super(items);
        }
        @Override
        public ComboBoxX<T> getComboControl() {
            return this;
        }

    }
    @Override
    public void start(Stage primaryStage) {
        Parent tabPane = createContent();
        
        primaryStage.setScene(new Scene(tabPane, 600, 400));
        primaryStage.setTitle(System.getProperty("java.version"));
        primaryStage.show();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBoxUpdateOnShowingRT_20945.class.getName());
}