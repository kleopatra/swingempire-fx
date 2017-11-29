/*
 * Created on 29.11.2017
 *
 */
package de.swingempire.fx.scene.control.et;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Can't get the node the contextMenu/menuItem was invoked on.
 * 
 * https://stackoverflow.com/q/29149098/203657
 * <p>
 * only solution is to use different contextMenu per control and store a
 * reference to it (as suggested in the answers).
 */
public class FindOwnerNode extends Application {

    TextField text = new TextField();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {


        Label label1 = new Label("hello");
        Label label2 = new Label("world");
        Label label3 = new Label("java");

        Text plainNode = new Text("plainNode");
        ContextMenu menu = new ContextMenu();
        MenuItem item = new MenuItem("copy to text field");
        menu.getItems().add(item);
        item.setOnAction(e -> {
            MenuItem source = (MenuItem) e.getTarget();
            Menu parent = source.getParentMenu();
            ContextMenu context = source.getParentPopup();
            Node ownerNode = context.getOwnerNode();
            System.out.println("ownerNode " + ownerNode + " window " + context.getOwnerWindow());
            e.consume();
        });
        label1.setContextMenu(menu);
        label2.setContextMenu(menu);
        label3.setContextMenu(menu);
        // even here we don't get the "owner" - it's null
        plainNode.setOnContextMenuRequested(e -> menu.show(plainNode, 0, 0));
        HBox root = new HBox(10, text, label1, label2, label3, plainNode);

        Scene scene = new Scene(root, 300, 100);

        primaryStage.setScene(scene);
        primaryStage.show();

    }

//    private class MyContextMenu extends ContextMenu {
//
//        public MyContextMenu(Label label) {
//
//            MenuItem item = new MenuItem("copy to text field");
//            item.setOnAction(event -> {
//
//                // I want to copy the text of the Label I clicked to TextField
//                text.setText(label.getText());
//
//                event.consume();
//            });
//
//            getItems().add(item);
//
//        }
//
//    }
}

