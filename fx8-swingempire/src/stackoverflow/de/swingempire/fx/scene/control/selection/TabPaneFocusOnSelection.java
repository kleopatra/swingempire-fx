/*
 * Created on 26.02.2020
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Requirement: auto-focus first child of tabContent on selection change
 * https://stackoverflow.com/q/60404526/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TabPaneFocusOnSelection extends Application {

    private Parent createContent() {
        tabPane = new TabPane();
        for (int i = 0; i < 3; i++) {
            VBox tabContent = new VBox();
            tabContent.getChildren().addAll(new Button("dummy " +i), new TextField("just a field " + i));
            Tab tab = new Tab("Tab " + i, tabContent);
            tabPane.getTabs().add(tab);
            // not the content itself is toggled in visibility but its parent (TabContentRegion)
//            tabContent.visibleProperty().addListener((src, ov, nv) -> {
//                System.out.println("visible: for " + nv + tab.getText());
//            });
        }
        tabPane.getTabs().add(new Tab("no content"));
        tabPane.getTabs().add(new Tab("not focusable content", new Label("me!")));
        tabPane.getSelectionModel().selectedItemProperty().addListener((src, ov, nv) -> {
            System.out.println(" selectedTab: " + nv.getText());
            Tab selected = nv;
            if (selected != null && selected.getContent() != null) {
                System.out.println("selected in focusListener: " + selected.getText());
                final KeyEvent tabEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB,
                        false, false, false, false);
                Event.fireEvent(tabPane, tabEvent);
//                Node content = selected.getContent();
//                selected.getContent().requestFocus();
            }
            
//            Parent content = (Parent) nv.getContent();
//            // doesn't work
//            content.getChildrenUnmodifiable().get(0).requestFocus();
//            Platform.runLater(() -> {
//                // wrapping into runlater does work
//            });
        });
//        tabPane.getSelectionModel().selectedIndexProperty().addListener((src, ov, nv) -> {
//            Tab selected = tabPane.getTabs().get(nv.intValue());
//            
//            System.out.println(" selected index: " + nv +  " /" + selected.getText());
//            Parent content = (Parent) selected.getContent();
//            // doesn't work
//            if (content != null && content.getChildrenUnmodifiable().size() > 0)
//                content.getChildrenUnmodifiable().get(0).requestFocus();
//            Platform.runLater(() -> {
//                // wrapping into runlater does work
//            });
//        });
        
        tabPane.focusedProperty().addListener((src, ov, nv) -> {
            if (nv) {
                Tab selected = tabPane.getSelectionModel().getSelectedItem();
                if (selected != null && selected.getContent() != null) {
                    System.out.println("selected in focusListener: " + selected.getText());
                    final KeyEvent tabEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB,
                            false, false, false, false);
                    Event.fireEvent(tabPane, tabEvent);
//                    Node content = selected.getContent();
//                    selected.getContent().requestFocus();
                }
            }
        });
        BorderPane content = new BorderPane(tabPane);
        return content;
    }

    
    /**
     * @param scene
     */
    private void installListener(Scene scene) {
        scene.focusOwnerProperty().addListener((src, ov, nv) -> {
            System.out.println(" new focusOwner: " + nv);
            // digging into who's switching focus back onto tabpane
            // new RuntimeException("who is calling? ").printStackTrace();
        });
        
//        tabPane.getTabs().forEach(tab -> {
//            Node content = tab.getContent();
//            if (content != null) {
//                Parent contentParent = content.getParent();
//                contentParent.visibleProperty().addListener((src, ov, nv) -> {
//                    System.out.println("parent visible: for " + nv + tab.getText());
//                    if (nv && content instanceof Parent) {
//                        // does transfer the focus to the child, but then some internals ??
//                        // transfers it back to the tabPane
//                        // it's TabPaneBehavior.moveSelection: at end of method requests
//                        // focus back onto tabpane (if selected via keyBoard ctrl-TAB)
//                        // same for any mousePressed ..
//                    ((Parent) content).getChildrenUnmodifiable().get(0).requestFocus();
//                    }
//                });
//            }
//            
//        });
        
        tabPane.getSelectionModel().selectedItemProperty().addListener((src, ov, nv) -> {
            System.out.println(" selectedTab: " + nv.getText());
            Tab selected = nv;
            if (selected != null && selected.getContent() != null) {
//                System.out.println("selected in focusListener: " + selected.getText());
//                final KeyEvent tabEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB,
//                        false, false, false, false);
//                Event.fireEvent(tabPane, tabEvent);
//                Node content = selected.getContent();
//                selected.getContent().requestFocus();
                Parent content = (Parent) selected.getContent();
                // doesn't work
                content.getChildrenUnmodifiable().get(0).requestFocus();
                Platform.runLater(() -> {
                    // wrapping into runlater does work
                });
            }
            
        });

        
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
        installListener(stage.getScene());
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TabPaneFocusOnSelection.class.getName());
    private TabPane tabPane;

}
