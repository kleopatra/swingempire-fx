/*
 * Created on 25.01.2020
 *
 */
package de.swingempire.fx.scene.control;

import de.swingempire.testfx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
 
/**
 * same as experimenting (answer) version except for showing
 * the menu with fine-tuning TOP
 */
public class ContextMenuWithLimitedItemsOrg extends Application {
 
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
 
        Button btn = new Button();
        btn.setText("Show ContextMenu");
        ContextMenu ctx = new MaxSizedContextMenu();
        for (int i = 0; i < 100; i++) {
            ctx.getItems().add(new MenuItem("Testing" + i));
        }
        // not visible
        //        ctx.setShowRelativeToWindow(true);
        // access reflectively, no change
        FXUtils.invokeGetMethodValue(ContextMenu.class, ctx, 
                "setShowRelativeToWindow", Boolean.TYPE, true);
        btn.setOnAction(event -> {
            // Vertical location of the popup is wrong
            ctx.show(btn, Side.TOP, 0, 0);
//            Bounds local = btn.getBoundsInLocal();
//            Bounds screen = btn.localToScreen(local);
//            ctx.show(btn, screen.getMinX(), screen.getMinY());
        });
 
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }
 
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public static class MaxSizedContextMenu extends ContextMenu {
 
        public MaxSizedContextMenu() {
            setPrefHeight(300);
            setMaxHeight(Region.USE_PREF_SIZE);
            setMaxHeight(301.0);
 
            addEventHandler(Menu.ON_SHOWING, e -> {
                Node content = getSkin().getNode();
                if (content instanceof Region) {
                    ((Region) content).setMaxHeight(getMaxHeight());
//                    ((Region) content).setMaxHeight(getMaxHeight());
                    
                }
            });
        }
    }

    }

