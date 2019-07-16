/*
 * Created on 15.07.2019
 *
 */
package de.swingempire.fx.scene.control;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/57033827/203657
 * on open, first item is selected without hover
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
        // set title for the stage
        stage.setTitle("creating contextMenu ");

        // create a label
        Label label1 = new Label("This is a ContextMenu example ");

        // create a menu
        ContextMenu contextMenu = new ContextMenu();

        // create menuitems
        MenuItem menuItem1 = new MenuItem("menu item 1");
        MenuItem menuItem2 = new MenuItem("menu item 2");
        MenuItem menuItem3 = new MenuItem("menu item 3");

        // add menu items to menu
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);
        contextMenu.getItems().add(menuItem3);

        // create a tilepane
//        TilePane tilePane = new TilePane(label1);
        BorderPane content = new BorderPane(label1);
        
        // setContextMenu to label
        label1.setContextMenu(contextMenu);

        // create a scene
//        Scene sc = new Scene(tilePane, 200, 200);
        Scene sc = new Scene(content, 200, 200);

        // set the scene
        stage.setScene(sc);

        stage.show();
    }
}

