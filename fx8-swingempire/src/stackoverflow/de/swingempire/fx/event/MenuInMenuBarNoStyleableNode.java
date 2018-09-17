/*
 * Created on 14.09.2018
 *
 */
package de.swingempire.fx.event;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class MenuInMenuBarNoStyleableNode extends Application {

    private Parent createContent() {
        MenuBar bar = new MenuBar();
        Menu menu = createMenu("first");
        bar.getMenus().addAll(menu);
        // to be certain that all visuals are created
        menu.setOnShown(e -> {
            LOG.info("styleable node of top-level menu: " + menu.getText() + menu.getStyleableNode() 
                     + "\n" + menu.getStyleableParent()
                     + "\n " + menu.getProperties()
                    );
            menu.getItems().forEach(item -> {
                // for comparison: 
                // both menu and menuItem have a styleable node
                LOG.info("styleable node of menuItems: " + item.getText() + item.getStyleableNode() + 
                        "\n" + item.getStyleableParent());
            });
        });
        BorderPane content = new BorderPane();
        content.setTop(bar);
        return content;
    }

    private Menu createMenu(String text) {
        Menu menu = new Menu(text)
//        {
//
//            @Override
//            public Node getStyleableNode() {
//                Node node = super.getStyleableNode();
//                if (node == null) {
//                    // check if we are in a menuBar
//                    
//                }
//                return node;
//            }
//            
//        }
        ;
        Menu submenu = new Menu("sub - " + text);
        submenu.getItems().addAll(new MenuItem("childofsub"));
        menu.getItems().addAll(submenu, new MenuItem(text + "child1"));
        return menu;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MenuInMenuBarNoStyleableNode.class.getName());

}
