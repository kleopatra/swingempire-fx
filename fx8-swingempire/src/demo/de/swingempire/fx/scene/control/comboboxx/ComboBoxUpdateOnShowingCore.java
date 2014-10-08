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

/**
 * Basic test context: behaviour if items are modified while opening the popup.
 * 
 * Basic requirement: selectedItem must not change by opening.
 * 
 * Test setup:
 * each tab contains a editable/not-editable choice control (if supported)
 * each control's initial value is external to the list
 * initial items can be varied (single/multiple items)
 * item mutations can be varied
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
 * 
 * Related to: 
 * https://javafx-jira.kenai.com/browse/RT-22572 - covers items.setAll, fixed
 * https://javafx-jira.kenai.com/browse/RT-20945 - covers items.setAll, fixed
 * https://javafx-jira.kenai.com/browse/RT-38899 - covers control.setItems, open
 * 
 * (Note to self: extract for bug report)
 * 
 * @see ComboBoxUpdateOnShowingRT_20945
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ComboBoxUpdateOnShowingCore extends Application {

    protected Parent createContent() {
        TabPane tabPane1 = new TabPane();
        // single item initially
        Callback<Void, Facade> singleItem = (p) -> new ComboFacade(
                FXCollections.observableArrayList("Toto")); 
        // multiple items initially
        Callback<Void, Facade> multipleItems = (p) -> new ComboFacade(
                FXCollections.observableArrayList("Toto", "Tato", "Tati", "Tata")); 
        addTestTabs(tabPane1, singleItem, multipleItems);
        TabPane tabPane = tabPane1;
        Pane box = new VBox(20, tabPane);
        return box;
    }

    protected void addTestTabs(TabPane tabPane,
            Callback<Void, Facade> singleInitialItemsFacadeFactory,
            Callback<Void, Facade> multipleInitialItemsFacadeFactory) {
        addTab(tabPane, "single, setAll", singleInitialItemsFacadeFactory,
                control -> {
                    control.getItems().setAll("" +System.currentTimeMillis());
                    return null;
                }
                );
        addTab(tabPane, "single, setItems", singleInitialItemsFacadeFactory,
                control -> {
                    control.setItems(FXCollections.observableArrayList("" +System.currentTimeMillis()));
                    return null;
                }
         );
        
        addTab(tabPane, "multiple, setAll", multipleInitialItemsFacadeFactory,
                control -> {
                    control.getItems().setAll("" +System.currentTimeMillis(), "constant");
                    return null;
                }
                );
        addTab(tabPane, "multiple, setItems", multipleInitialItemsFacadeFactory,
                control -> {
                    control.setItems(FXCollections.observableArrayList("" +System.currentTimeMillis(), 
                            "constant"));
                    return null;
                }
                );
        
        addTab(tabPane, "multiple, replace", multipleInitialItemsFacadeFactory,
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
        addTab(tabPane, "multiple, remove", multipleInitialItemsFacadeFactory,
                control -> {
                    control.getItems().remove(0);
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
        
        public TestItem(Facade comboControl, Callback<Facade, Void> updater, 
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
     * Common api that the TestItem can manage.
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

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        Parent tabPane = createContent();
        
        primaryStage.setScene(new Scene(tabPane));
        primaryStage.setTitle(System.getProperty("java.version"));
        primaryStage.show();
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBoxUpdateOnShowingCore.class.getName());
}