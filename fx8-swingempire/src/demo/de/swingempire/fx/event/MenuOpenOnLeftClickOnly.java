/*
 * Created on 26.01.2018
 *
 */
package de.swingempire.fx.event;

import java.net.URL;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.MenuBarSkin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/48438436/203657
 * 
 * Menu should open only on left-click (not right).
 * 
 * The basic idea is to install a eventFilter for mousePressed on the menuBar
 * that consumes on right button. 
 * 
 * Side-note: Node.setEventHandler() is specified to call that handler as last, somewhere
 * else (noticed while digging into this requirement ... Window.setEventHandler)
 * the spec is the other way round: is the first to be notified. CHECK!
 * Also EventHandlerManager has a setEventHandler - without spec as to when it is
 * called (compared with added handlers)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class MenuOpenOnLeftClickOnly extends Application {

    public static class DisabledRightMenuBarSkin extends MenuBarSkin {

        public DisabledRightMenuBarSkin(MenuBar menuBar) {
            super(menuBar);
            menuBar.addEventFilter(MouseEvent.MOUSE_PRESSED, ev -> {
                if (ev.getButton() == MouseButton.SECONDARY) {
                    LOG.info("consume");
                    ev.consume();
                }
            });
        }
        
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        MenuBar menuBar = new MenuBar() {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new DisabledRightMenuBarSkin(this);
            }
            
        };

        // working okay, though popping up everywhere, that is over the menuButtons as well
//        ContextMenu barContextMenu = new ContextMenu();
//        barContextMenu.getItems().addAll(new MenuItem("dummy"));
//        menuBar.setContextMenu(barContextMenu);
        Menu hello = new Menu("hello");
        Menu world = new Menu("world");
        Menu helloSub = new Menu("hello sub");
        helloSub.getItems().addAll(createItem("hello sub 1"), createItem("hello sub 2"));
        hello.getItems().addAll(createItem("laugh"), createItem("loud"), helloSub);
        

        menuBar.getMenus().addAll(hello, world);
        root.setTop(menuBar);
        ContextMenu context = new ContextMenu();
        Menu contextSub = new Menu("context sub");
        contextSub.getItems().addAll(createItem("sub 1"), contextSub, createItem("sub 2"));
        context.getItems().addAll(createItem("context 1"), createItem("context 2"));
        
        Label label = new Label("some dummy label");
        label.setContextMenu(context);
        root.setCenter(label);
        
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        URL uri = getClass().getResource("contextskin.css");
        primaryStage.getScene().getStylesheets().add(uri.toExternalForm());

        primaryStage.show();
    }
    
    protected MenuItem createItem(String text) {
        MenuItem c1 = new MenuItem(text);
        c1.setOnAction(e -> LOG.info(c1.getText()));
        return c1;
    }
    public static void main(String[] args) {
        launch(args);
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MenuOpenOnLeftClickOnly.class.getName());
}
