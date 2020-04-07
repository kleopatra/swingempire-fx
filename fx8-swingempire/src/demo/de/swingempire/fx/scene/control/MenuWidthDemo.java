/*
 * Created on 10.02.2020
 *
 */
package de.swingempire.fx.scene.control;

import javafx.application.Application;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8238730 broken layout when toggling
 * menu text
 * 
 * When the text of a Menu in e.g. a MenuButton changes to a shorter text, the
 * width of the Menu becomes shorter than that of the parent and the triangle
 * indicating that it's a Menu is moved away from the right border.
 * 
 * Same for ContextMenu - no wonder, the popup of the MenuButton is-a ContextMenu.
 * 
 * <p>
 * 
 * Also taken as test for https://bugs.openjdk.java.net/browse/JDK-8241710
 * NPE when entering empty submenu
 * 
 * 
 */
public class MenuWidthDemo extends Application {

    private static final String shortText = "Short text";
    private static final String longText = "A long text for demonstration";

    // toolbar and MenuButton
    private ToolBar bar = new ToolBar();
    private MenuButton button = new MenuButton("Menu");
    
    // menus/submenus
    private MenuItem switchItem = new MenuItem("Switch Text");
    private Menu menu = new Menu(shortText);
    private MenuItem otherItem = new MenuItem("other item");
    private MenuItem otherSubItem = new MenuItem("other sub");
    private Menu otherMenu = new Menu("other Menu");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        configureMenuTree();

        // original
        // menubutton
//        button.getItems().add(otherItem);
//        button.getItems().add(menu);
//        button.getItems().add(otherMenu);
//        // in Toolbar
//        bar.getItems().add(button);
//
//        Scene scene = new Scene(button);
        
        // ContextMenu
        Button dummy = new Button("dummy ........................ ");
        dummy.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(otherItem, menu, otherMenu);
        dummy.setContextMenu(contextMenu);
        
        Scene scene = new Scene(dummy);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 
     */
    protected void configureMenuTree() {
        switchItem.setOnAction(event -> {
            if (shortText.equals(menu.getText())) {
                menu.setText(longText);
            } else {
                menu.setText(shortText);
            }
        });

        menu.getItems().add(switchItem);
//        otherMenu.getItems().add(otherSubItem);
    }
}