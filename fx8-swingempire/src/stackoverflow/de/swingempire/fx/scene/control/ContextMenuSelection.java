/*
 * Created on 15.07.2019
 *
 */
package de.swingempire.fx.scene.control;

import java.util.logging.Logger;

import com.sun.javafx.scene.control.ContextMenuContent;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.skin.ContextMenuSkin;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/57033827/203657
 * on open, first item is selected without hover
 * 
 * Culprit is the normal focusTraversal of the scene that contains the contextMenu:
 * on first showing, it transfers focus to the first focusable node which is the 
 * first menuItem.
 * 
 * To workaround: 
 * - register a onShown listener on the contextMenu
 * - on first showing, register a listener on the scene's focusOwner (need a listener
 *   because at the time it's not yet set)
 * - on first change to the property, request focus on the scene's roolt
 * - optional: cleanup by removing the listeners  
 * 
 * comment the bug report: https://bugs.openjdk.java.net/browse/JDK-8227679
 * and answered on SO
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ContextMenuSelection extends Application {
    // labels
    Label l;

    public static void main(String args[]) {
        // launch the application
        launch(args);
    }

    // launch the application
    public void start(Stage stage) {
        ContextMenuSkin s;
        // set title for the stage
        stage.setTitle("creating contextMenu ");

        // create a label
        Label label1 = new Label("This is a ContextMenu example ");

        // create a menu
        ContextMenu contextMenu = new ContextMenu();

        contextMenu.setOnShown(e -> {
            Scene scene = contextMenu.getScene();
            scene.focusOwnerProperty().addListener((src, ov, nv) -> {
                // focusOwner set after first showing
                if (ov == null) {
                    // transfer focus to root
                    // old hack (see the beware section) on why it doesn't work
                    //  scene.getRoot().requestFocus();
                    
                    // next try: 
                    // grab the containing ContextMenuContainer and force the internal
                    // book-keeping into no-item-focused state
                    Parent parent = nv.getParent().getParent();
                    parent.requestFocus();
                    FXUtils.invokeSetFieldValue(ContextMenuContent.class, parent, "currentFocusedIndex", -1);
                    // cleanup
                    contextMenu.setOnShown(null);
                }
            });
        });

        // create menuitems
        MenuItem menuItem1 = new MenuItem("menu item 1");
        MenuItem menuItem2 = new MenuItem("menu item 2");
        MenuItem menuItem3 = new MenuItem("menu item 3");

        // add menu items to menu
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);
        contextMenu.getItems().add(menuItem3);

        // quick check: action triggered after 
        // - hover mouse
        // - move mouse out
        // - press enter
        // result: action of last hover is triggered
        contextMenu.getItems().forEach(item -> {
            item.setOnAction(e -> {
                LOG.info("" + item.getText());
            });
        });
        // create a tilepane
//        TilePane tilePane = new TilePane(label1);
        BorderPane content = new BorderPane(label1);

        // setContextMenu to label
        label1.setContextMenu(contextMenu);

        // create a scene
//        Scene sc = new Scene(tilePane, 200, 200);
        Scene sc = new Scene(content, 200, 200);

        sc.focusOwnerProperty().addListener((src, ov, nv) -> {
            LOG.info("focusOwner: " + nv);
        });

        // set the scene
        stage.setScene(sc);

        stage.show();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ContextMenuSelection.class.getName());
}

