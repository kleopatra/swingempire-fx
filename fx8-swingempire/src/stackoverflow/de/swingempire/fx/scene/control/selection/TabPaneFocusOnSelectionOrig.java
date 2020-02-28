/*
 * Created on 26.02.2020
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.scene.Node;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Requirement: auto-focus first child of tabContent on selection change
 * https://stackoverflow.com/q/60404526/203657
 * 
 * when listening to selection changes, the visual rep of tabContent (its parent)
 * is not yet visible -> requestFocus is lost
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TabPaneFocusOnSelectionOrig extends Application {

    private TabPane tabPane;
    private Parent createContent() {
        tabPane = new TabPane();
        for (int i = 0; i < 3; i++) {
            VBox tabContent = new VBox();
            tabContent.getChildren().addAll(new Button("dummy " + i),
                    new TextField("just a field " + i));
            Tab tab = new Tab("Tab " + i, tabContent);
            tabPane.getTabs().add(tab);

        }
//        tabPane.getTabs().add(new Tab("no content"));
        tabPane.getTabs()
                .add(new Tab("not focusable content", new Label("me!")));
        
        BorderPane content = new BorderPane(tabPane);
        return content;
    }
    
    /**
     * no public api to transfer focus away from a node, hack by firing a TAB
     */
    private void tabForward() {
        final KeyEvent tabEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB,
                false, false, false, false);
        Event.fireEvent(tabPane, tabEvent);
    }
    
    /**
     * @param scene
     */
    private void installListener(Scene scene) {
        ObjectProperty<Node> focusOwner = new SimpleObjectProperty<>();
        scene.focusOwnerProperty().addListener((src, ov, nv) -> {
            focusOwner.set(ov);
            System.out.println("scene listener -"
                    + "\n      old focusOwner: " + ov + 
                    "\n      new focusOwner: " + nv);
            // digging into who's switching focus back onto tabpane
            // new RuntimeException("who is calling? ").printStackTrace();
        });
        tabPane.focusedProperty().addListener((src, ov, nv) -> {
            if (nv) {
                Tab selected = tabPane.getSelectionModel().getSelectedItem();
                System.out.println("tabPane focusProperty - selected: " + selected.getText()
                // sanity: tabPane really is focusOwner
//                        + "\n    focusOwner: " + scene.getFocusOwner()
                        + "\n    last owner: " + focusOwner.get()
                        );
                if (selected != null && selected.getContent() != null) {
                    // check if previous owner was on same tab
                    if (focusOwner.get() != null && !((Pane) selected.getContent()).getChildren().contains(focusOwner.get()))
//                    if (isChildOf(focusOwner.get(), selected))
                        tabForward();
                }
            }
        });

        installVisibilityListeners();
    }

    private boolean isChildOf(Node child, Tab tab) {
        if (child == null || tab.getContent() == null) return false;
        if (child == tab.getContent()) return true;
        if (tab.getContent() instanceof Parent) {
            return ((Parent) tab.getContent()).getChildrenUnmodifiable().contains(child);
        }
        return false;
    }
    private void installVisibilityListeners() {
        tabPane.getTabs().forEach(tab -> {
            Node content = tab.getContent();
            if (content != null) {
                Parent contentParent = content.getParent();
                contentParent.visibleProperty().addListener((src, ov, nv) -> {
                    System.out.println(
                            "parent visible: for " + nv + tab.getText() 
                            + "\n      focusOwner " + tabPane.getScene().getFocusOwner());
                    if (nv && content instanceof Parent) {
                        // does transfer the focus to the child, but then some
                        // internals ??
                        // transfers it back to the tabPane
                        // it's TabPaneBehavior.moveSelection: at end of method
                        // requests
                        // focus back onto tabpane (if selected via keyBoard
                        // ctrl-TAB)
                        // same for any mousePressed ..
//                        ((Parent) content).getChildrenUnmodifiable().get(0)
//                                .requestFocus();
                        // at this time, the child of the now invisible tab is still focusOwner
                        tabForward();
                    }
                });
            }
        });
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
        stage.setX(stage.getX() - 200);
        installListener(stage.getScene());
//        installSelectionListeners();
    }

    /**
         *  for analysis, to get an idea on when which state change is called.
         *  
         */
        private void installSelectionListeners() {
            tabPane.getSelectionModel().selectedItemProperty().addListener((src, ov, nv) -> {
                Tab selected = nv;
                if (selected != null && selected.getContent() != null) {
                    Parent content = (Parent) selected.getContent();
                    System.out.println("selectedItem listener: " + selected.getText() + content.getParent().isVisible());
                    // doesn't work
    //                if (content != null)
    //                    content.getChildrenUnmodifiable().get(0).requestFocus();
    //                Platform.runLater(() -> {
    //                    // wrapping into runlater does work
    //                });
                }
                
            });
            
            tabPane.getSelectionModel().selectedIndexProperty().addListener((src, ov, nv) -> {
                Tab selected = tabPane.getTabs().get(nv.intValue());
                if (selected != null && selected.getContent() != null) {
                    Parent content = (Parent) selected.getContent();
                    System.out.println("selectedIndex listener: " + selected.getText() + content.getParent().isVisible());
                    // doesn't work
    //                if (content != null)
    //                    content.getChildrenUnmodifiable().get(0).requestFocus();
    //                Platform.runLater(() -> {
    //                    // wrapping into runlater does work
    //                });
                }
                
            });
            
            tabPane.getTabs().forEach(tab -> {
                tab.setOnSelectionChanged(event -> {
                    Node tabContent = tab.getContent();
                    if (tab.isSelected() && tab.getContent() != null && tab.getContent().getParent() != null ) {
                        System.out.println("onSelection " + tab.getText() + tabContent.getParent().isVisible());
    //                    Node ta = tabContent.getChildren().get(0);
    //                    ta.requestFocus();
                    }
                });
                
            });
        }


    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TabPaneFocusOnSelectionOrig.class.getName());

}
