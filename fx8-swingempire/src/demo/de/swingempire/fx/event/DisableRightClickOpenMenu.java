/*
 * Created on 26.01.2018
 *
 */
package de.swingempire.fx.event;

import java.net.URL;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/48438436/203657
 * 
 * Menu should open only on left-click (not right).
 * 
 * Note: don't need a skin but doesn't hurt.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DisableRightClickOpenMenu extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        MenuBar menuBar = new MenuBar();

//        menuBar.addEventFilter(MouseEvent.MOUSE_PRESSED, ev -> {
//            if (ev.getButton() == MouseButton.SECONDARY) {
//                LOG.info("consumed");
//                ev.consume();
//            }
//        });
//
        Menu hello = new Menu("hello");
        menuBar.getMenus().addAll(hello);
        Menu world = new Menu("world");
        menuBar.getMenus().addAll(world);
        root.setCenter(menuBar);

        MenuItem item = new MenuItem("laugh");
        hello.getItems().add(item);
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        URL uri = getClass().getResource("contextskin.css");
        primaryStage.getScene().getStylesheets().add(uri.toExternalForm());

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DisableRightClickOpenMenu.class.getName());

}
