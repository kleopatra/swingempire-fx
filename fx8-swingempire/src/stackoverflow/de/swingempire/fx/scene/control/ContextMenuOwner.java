/*
 * Created on 16.03.2019
 *
 */
package de.swingempire.fx.scene.control;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ContextMenuOwner extends Application {

    private Parent createContent() {
        TextField text = new TextField();
        Label label1 = new Label("hello");
        Label label2 = new Label("world");
        Label label3 = new Label("java");

        ContextMenu menu = new ContextMenu() {

            // force menu to have an owner node: this being the case, it is not hidden 
            // on mouse events inside its owner
//            @Override
//            public void show(Node anchor, double screenX, double screenY) {
//                ReadOnlyObjectWrapper<Node> owner = 
//                        (ReadOnlyObjectWrapper<Node>) 
//                        FXUtils.invokeGetFieldValue(PopupWindow.class, this, "ownerNode");
//                owner.set(anchor);
//                super.show(anchor, screenX, screenY);
//            }
            
        };
        MenuItem item = new MenuItem("copy to text field");
        menu.getItems().add(item);
        item.setOnAction(e -> {
//            LOG.info("" + e.getSource() + "\n " + e.getTarget());
//            LOG.info("" + item.getStyleableNode() + "\n " + item.getStyleableParent());
            
            LOG.info("" + menu.getOwnerNode());
        });

        label1.setContextMenu(menu);
        label2.setContextMenu(menu);
        label3.setContextMenu(menu);
        text.setContextMenu(menu);
        // same effect as forcing the owner node - 
        // setting to true was introduced as fix for
        // https://bugs.openjdk.java.net/browse/JDK-8114638
        FXUtils.invokeGetMethodValue(ContextMenu.class, menu, "setShowRelativeToWindow", Boolean.TYPE, false);
        
        
        
        VBox content = new VBox(10, text, label1, label2, label3);
        
        content.addEventFilter(ContextMenuEvent.ANY, e -> {
            LOG.info("filter: " + e);
        });
        content.addEventHandler(ContextMenuEvent.ANY, e -> {
            LOG.info("handler: " + e);
        });
        return content;
        
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 400, 200));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ContextMenuOwner.class.getName());

}
