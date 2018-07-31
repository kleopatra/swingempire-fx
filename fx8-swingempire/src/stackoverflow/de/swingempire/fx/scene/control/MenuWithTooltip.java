/*
 * Created on 26.07.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

/**
 * Install a tooltip on a menu
 * https://stackoverflow.com/q/51522815/203657
 * 
 * Problem is that the menuItem is-not a node.
 * For a menuItem the ugly way to go is a customMenuItem and set the tip on its content.
 * Not available for Menu.
 * 
 * For both, another option is to install the tooltip on its styleableNode
 * once it gets available.
 * 
 * Missing api? Or suboptimal ux?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class MenuWithTooltip extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        
        MenuButton menuButton = new MenuButton("Menu");
        Label helloLabel = new Label("Hello...");
        helloLabel.tooltipProperty().setValue(new Tooltip("World!"));
        menuButton.getItems().add(new CustomMenuItem(helloLabel));
        
        String tooltipKey = "TOOL_TIP";
        MenuItem normalItem = new MenuItem("Good .. ");
        normalItem.getProperties().put(tooltipKey, new Tooltip("Morning!"));
        menuButton.getItems().add(normalItem);
        Menu submenu = new Menu("This Submenu needs a ToolTip!");
        submenu.getProperties().put(tooltipKey, new Tooltip("It's meee!"));
        menuButton.setOnShown(e -> {
            menuButton.getItems().forEach(item -> {
                Node node = item.getStyleableNode();
                if (node != null && item.getProperties().get(tooltipKey) instanceof Tooltip) {
                    Tooltip tip = (Tooltip) item.getProperties().get(tooltipKey);
                    Tooltip.install(node, tip);
                }
            });
            
        });
        submenu.getItems().add(new MenuItem("Some other Item"));
        menuButton.getItems().add(submenu);
//        menuButton.addEventHandler(MenuButton.ON_SHOWING, e -> {
//            LOG.info("handler: parentMenu/popup " + submenu.getParentMenu() + " / " + submenu.getParentPopup());
//            
//        });
//

        // quick-check sizing: is respected, but resizing cursors and button still available
//        primaryStage.setMaxHeight(400);
//        primaryStage.setMinHeight(400);
//        primaryStage.setMaxWidth(400);
//        primaryStage.setMinWidth(400);
//
        primaryStage.setScene(new Scene(menuButton));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MenuWithTooltip.class.getName());
}

