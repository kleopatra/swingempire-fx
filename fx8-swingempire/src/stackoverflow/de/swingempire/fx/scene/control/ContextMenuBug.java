/*
 * Created on 17.03.2019
 *
 */
package de.swingempire.fx.scene.control;

import de.swingempire.fx.util.FXUtils;

/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 */

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8114638
 * original example, slightly adapted to compile
 *  to reproduce:
 1) run the attached HelloPopupMenu
2) bring the context menu up in the text control using the right mouse button
3) click in the text control (the menu goes away and focus is in the control)
4) bring the conext menu up again in the text control
5) click in the text control (the context menu does not go away)

The i-beam moves but you cannot select text. If you click in another control 
(make the text control lose focus), the menu is hidden. 
 * @author paru
 */
public class ContextMenuBug extends Application {

    @Override public void start(Stage stage) {
        stage.setTitle("Hello PopupMenu");
        stage.setWidth(500);
        stage.setHeight(500);
        Scene scene = createScene();
        scene.setFill(Color.WHITE);

        stage.setScene(scene);
        stage.show();
    }

    private Scene createScene() {
        final Scene scene = new Scene(new Group());
        final ContextMenu popupMenu = new ContextMenu();
        final Button button = new Button("Click me");
        
        popupMenu.setOnShowing(new EventHandler<WindowEvent>() {
            @Override public void handle(WindowEvent t) {
                System.out.println("showing");
            }
        });
        
        popupMenu.setOnShown(new EventHandler<WindowEvent>() {
            @Override public void handle(WindowEvent t) {
                System.out.println("shown: " + popupMenu.getOwnerNode());
                
            }
        });
        popupMenu.setOnHiding(new EventHandler<WindowEvent>() {
            @Override public void handle(WindowEvent t) {
                System.out.println("hiding");
            }
        });
        popupMenu.setOnHidden(new EventHandler<WindowEvent>() {
            @Override public void handle(WindowEvent t) {
                System.out.println("hidden");
            }
        });
        popupMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent t) {
                System.out.println("on Action: " + t.getTarget());
            }
        });

        MenuItem item1 = new MenuItem("About");
        item1.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                System.out.println("About");
            }
        });

        MenuItem item2 = new MenuItem("Preferences");
        item2.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                System.out.println("Preferences");
            }
        });

        MenuItem item3 = new MenuItem("Templates");
        item3.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                System.out.println("Templates");
            }
        });

        popupMenu.getItems().add(item1);
        popupMenu.getItems().add(item2);
        popupMenu.getItems().add(item3);

        button.setContextMenu(popupMenu);
        
        final TextArea textArea = new TextArea ();
        textArea.setText("Request a context menu");
        textArea.setLayoutY(100);
        textArea.setContextMenu(popupMenu);
        
        // same effect as forcing the owner node - 
        // setting to true was introduced as fix for
        // https://bugs.openjdk.java.net/browse/JDK-8114638
        // reverting: 
        // now the popup doesn't hide, but all interaction in the textField possible, 
        // so the issue of not being able to select anything is  
        FXUtils.invokeGetMethodValue(ContextMenu.class, popupMenu, "setShowRelativeToWindow", Boolean.TYPE, false);
        

        Group root = (Group) scene.getRoot();
        root.getChildren().clear();
        root.getChildren().addAll(button, textArea);
        return scene;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
