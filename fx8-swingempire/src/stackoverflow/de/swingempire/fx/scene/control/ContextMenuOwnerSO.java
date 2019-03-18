/*
 * Created on 16.03.2019
 *
 */
package de.swingempire.fx.scene.control;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;

/**
 * In action of a menuItem, find the ownerNode.
 * Prepare answer for
 * https://stackoverflow.com/q/29149098/203657 (oldisch, from 2015, with a recent answer)
 * all answers are a bit hacky, workarounds.
 * 
 * Basic problem is that ContextMenu.show(Node node, ...) doesn't honor its contract. Which is:
 * 
 * The popup is associated with the specified owner node. 
 * 
 * Instead, probably in fixing https://bugs.openjdk.java.net/browse/JDK-8114638, it ignores the
 * ownerNode once set as value of the contextMenu property of any control. 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ContextMenuOwnerSO extends Application {

    private Parent createContent() {
        
        TextField text = new TextField();
        // the general approach to grab a property from the Node
        // that the ContextMenu was opened on
        EventHandler<ActionEvent> copyText = e -> {
            MenuItem source = (MenuItem) e.getTarget();
            ContextMenu popup = source.getParentPopup();
            String ownerText = "<not available>";
            if (popup != null) {
                Node ownerNode = popup.getOwnerNode();
                if (ownerNode instanceof Labeled) {
                    ownerText = ((Label) ownerNode).getText();
                } else if (ownerNode instanceof Text) {
                    ownerText = ((Text) ownerNode).getText();
                }
            }
            text.setText(ownerText);
        };
        
        MenuItem printOwner = new MenuItem("copy to text field");
        printOwner.setOnAction(copyText);
        
        // verify with manual managing of contextMenu
        Text textNode = new Text("I DON'T HAVE a contextMenu property");
        Label textNode2 = new Label("I'm NOT USING the contextMenu roperty");
        ContextMenu nodeMenu = new ContextMenu();
        nodeMenu.getItems().addAll(printOwner);
        EventHandler<ContextMenuEvent> openRequest = e -> {
            nodeMenu.show((Node) e.getSource(), Side.BOTTOM, 0, 0);
            e.consume();
        };
        
        textNode.setOnContextMenuRequested(openRequest);
        textNode2.setOnContextMenuRequested(openRequest);
        
        Label label1 = new Label("I'm USING the contextMenu property");

        ContextMenu menu = new ContextMenu() {

            // force menu to have an owner node: this being the case, it is not hidden 
            // on mouse events inside its owner
            //@Override
            //public void show(Node anchor, double screenX, double screenY) {
            //    ReadOnlyObjectWrapper<Node> owner = 
            //            (ReadOnlyObjectWrapper<Node>) 
            //            FXUtils.invokeGetFieldValue(PopupWindow.class, this, "ownerNode");
            //    owner.set(anchor);
            //    super.show(anchor, screenX, screenY);
            //}
            
        };
        MenuItem item = new MenuItem("copy to text field");
        menu.getItems().add(item);
        item.setOnAction(copyText);
        
        label1.setContextMenu(menu);
        // same effect as forcing the owner node 
        // has to be done after the last setting of contextMenuProperty 
        // setting to true was introduced as fix for
        // https://bugs.openjdk.java.net/browse/JDK-8114638
        //FXUtils.invokeGetMethodValue(ContextMenu.class, menu, "setShowRelativeToWindow", Boolean.TYPE, false);
        
        VBox content = new VBox(10, textNode, textNode2, text, label1);
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
            .getLogger(ContextMenuOwnerSO.class.getName());

}
