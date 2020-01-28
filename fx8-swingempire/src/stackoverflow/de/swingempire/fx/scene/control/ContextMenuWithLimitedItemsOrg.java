/*
 * Created on 25.01.2020
 *
 */
package de.swingempire.fx.scene.control;

import de.swingempire.testfx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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
        btn.setText("Show ContextMenu Tweaked");
        ContextMenu ctx = new MaxSizedContextMenu();
        for (int i = 0; i < 100; i++) {
            ctx.getItems().add(new MenuItem("Testing" + i));
        }
        // not visible
        //        ctx.setShowRelativeToWindow(true);
        // access reflectively, no change
//        FXUtils.invokeGetMethodValue(ContextMenu.class, ctx, 
//                "setShowRelativeToWindow", Boolean.TYPE, true);
        btn.setOnAction(event -> {
            // Vertical location of the popup is wrong
            showContextMenu(btn, ctx);
//            Bounds local = btn.getBoundsInLocal();
//            Bounds screen = btn.localToScreen(local);
//            ctx.show(btn, screen.getMinX(), screen.getMinY());
        });
 
        Button normal = new Button("normal short");
        ContextMenu normalCtx = new ContextMenu();
        for (int i = 0; i < 8; i++) {
            normalCtx.getItems().add(new MenuItem("Normal" + i));
        }
        normal.setOnAction(e -> showContextMenu(normal, normalCtx));
        
        HBox root = new HBox(10);
        root.getChildren().addAll(btn, normal);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }
 
    private void showContextMenu(Node anchor, ContextMenu ctx) {
//        ctx.show(anchor, Side.TOP, 0, 0);
        ctx.show(anchor, 0, 0);
        System.out.println("pref: " + ctx.prefHeight(-1));
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public static class MaxSizedContextMenu extends ContextMenu {
 
        public MaxSizedContextMenu() {
//            setPrefHeight(300);
//            setMaxHeight(Region.USE_PREF_SIZE);
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

