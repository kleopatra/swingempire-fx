/*
 * Created on 27.02.2020
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.List;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TabPaneFocusOnSelectionWithSkin extends Application {
    
    /**
     * Custom skin that tries to focus the first child of selected tab when 
     * selection changed.
     * 
     */
    public static class MyTabPaneSkin extends TabPaneSkin {

        private boolean isSelecting = true;
        /**
         * @param control
         */
        public MyTabPaneSkin(TabPane control) {
            super(control);
            // TBD: dynamic update on changing tabs at runtime
            addTabContentVisibilityListener(getChildren());
            registerChangeListener(control.focusedProperty(), this::focusChanged);
            registerChangeListener(control.getSelectionModel().selectedItemProperty(), e -> {
                isSelecting = true;
            });
        }

        /**
         * Callback from listener to skinnable's focusedProperty.
         * 
         * @param focusedProperty the property that's changed
         */
        private void focusChanged(ObservableValue focusedProperty) {
            System.out.println("focusChanged .. " + getSkinnable().getScene().getFocusOwner());
            if (getSkinnable().isFocused() && isSelecting) {
                transferFocus();
                isSelecting = false;
            }
        }

        /**
         * No public api to transfer focus "away" from any node, hack by firing
         * a TAB key on the TabPane.
         */
        protected void transferFocus() {
            Tab selected = getSkinnable().getSelectionModel().getSelectedItem();
            System.out.println("selected in focusListener: "
                    + selected.getText() + " selecting? " + isSelecting);
            final KeyEvent tabEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "", "",
                    KeyCode.TAB, false, false, false, false);
            Event.fireEvent(getSkinnable(), tabEvent);
        }

        /**
         * Callback from listener to tab visibility.
         * 
         * @param visibleProperty the property that's changed 
         */ 
        protected void tabVisibilityChanged(ObservableValue visibleProperty) {
            BooleanProperty b = (BooleanProperty) visibleProperty;
            if (b.get()) {
                System.out.println("visibleChanged: " + visibleProperty + " selecting: " + isSelecting);
                transferFocus();
            }
        }
        
        /**
         * Register the visibilityListener to each child in the given list that 
         * is a TabContentArea.
         * 
         */
        protected void addTabContentVisibilityListener(List<? extends Node> children) {
            children.forEach(node -> {
                if (node.getStyleClass().contains("tab-content-area")) {
                    registerChangeListener(node.visibleProperty(), this::tabVisibilityChanged);
                }
            });
        }
        
        /**
         * Wire listener to visibleProperty of tab's content parent.
         */
        private void installTabContentVisibilityListener() {
            getChildren().addListener((ListChangeListener<Node>) c -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        addTabContentVisibilityListener(c.getAddedSubList());
                    }
                    // TBD: cleanup on remove
                }
            });
            addTabContentVisibilityListener(getChildren());
        }
        
    }

    private TabPane tabPane;
    
    private Parent createContent() {
        tabPane = new TabPane() {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new MyTabPaneSkin(this);
            }
            
        };
        for (int i = 0; i < 3; i++) {
            VBox tabContent = new VBox();
            tabContent.getChildren().addAll(new Button("dummy " +i), new TextField("just a field " + i));
            Tab tab = new Tab("Tab " + i, tabContent);
            tabPane.getTabs().add(tab);
        }
        tabPane.getTabs().add(new Tab("no content"));
        tabPane.getTabs().add(new Tab("not focusable content", new Label("me!")));
        
        BorderPane content = new BorderPane(tabPane);
        return content;

    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version() + " custom skin ");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}