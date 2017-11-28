/*
 * Created on 10.12.2015
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Original bug report: https://bugs.openjdk.java.net/browse/JDK-8088068
 * 
 * @see AcceleratorOnTab
 */
public class AcceleratorOnText extends Application {

    Stage stage;

    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        TextArea ta1 = new TextArea();
        TextArea ta2 = new TextArea();

        MenuItem item1 = new MenuItem("Insert \"Hello\"");
        item1.setOnAction((ActionEvent) -> ta1.setText("Hello"));
        item1.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));

        MenuItem item2 = new MenuItem("Insert \"World!\"");
        item2.setOnAction((ActionEvent) -> ta2.setText("World!"));
        item2.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));

        ContextMenu context1 = new ContextMenu();
        context1.getItems().add(item1);

        ContextMenu context2 = new ContextMenu();
        context2.getItems().add(item2);

        ta1.setContextMenu(context1);
        ta2.setContextMenu(context2);

        VBox vBox = new VBox();
        vBox.getChildren().add(ta1);
        vBox.getChildren().add(ta2);

        Scene scene = new Scene(vBox, 300, 300);
        // win
//        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcut);
        // original
//        scene.addEventFilter(KeyEvent.KEY_RELEASED, this::handleShortcut);
        
        primaryStage.setTitle("Accelerator Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** workaround by Robert Lichtenberger:
     *  handler that walks the contextMenu of the
     *  focused node and looks for a matching accelerator.
     *  If found, manually fires the item and consumes the event.
     *  Installed as handler on the scene.
     */
    private void handleShortcut(KeyEvent e) {
        Node n = findFocused(stage.getScene().getRoot());
        n = stage.getScene().getFocusOwner();
        if (n != null && n instanceof Control) {
            Control c = (Control) n;
            if (c.getContextMenu() != null) {
                for (MenuItem item : c.getContextMenu().getItems()) {
                    if (item.getAccelerator() != null
                            && item.getAccelerator().match(e)) {
                        item.fire();
                        e.consume();
                    }
                }
            }
        }
    }

    public static Node findFocused(Node e) {
        if (e instanceof Parent) {
            Parent parent = (Parent) e;
            for (Node node : parent.getChildrenUnmodifiable()) {
                node = findFocused(node);
                if (node != null)
                    return node;
            }
        }
        if (e.isFocusTraversable()) {
            if (e.isFocused()) {
                return e;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
