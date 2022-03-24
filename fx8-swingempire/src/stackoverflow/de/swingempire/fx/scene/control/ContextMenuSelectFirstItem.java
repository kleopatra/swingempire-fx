 /* Created 16.03.2022
 */

package de.swingempire.fx.scene.control;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * https://stackoverflow.com/q/27332981/203657
 * select first item on showing
 *
 * old question (2014),
 * original accepted answer by Jose: explicitly request focus on
 * first item after showing
 *
 * new answer: with Jose's answer needs firing a keyEvent to be accessible for Enter
 * context of that answer is for fx11
 *
 *
 */
public class ContextMenuSelectFirstItem extends Application {

    @Override
    public void start(Stage primaryStage) {

        MenuItem cmItem1 = new MenuItem("Item 1");
        cmItem1.setOnAction(e->System.out.println("Item 1"));
        MenuItem cmItem2 = new MenuItem("Item 2");
        cmItem2.setOnAction(e->System.out.println("Item 2"));

        final ContextMenu cm = new ContextMenu(cmItem1,cmItem2);

//        Label labelWithContextMenu = new Label("just a label - right click to manually open contextMenu");
        Button labelWithContextMenu = new Button("just a label - right click to manually open contextMenu");
        labelWithContextMenu.setOnMouseClicked(t -> {
            if(t.getButton()==MouseButton.SECONDARY){
                cm.show(labelWithContextMenu,t.getScreenX(),t.getScreenY());

                // Request focus on first item
//                cm.getSkin().getNode().lookup(".menu-item").requestFocus();
            }
        });

        Scene scene = new Scene(new VBox(labelWithContextMenu, new Button("just a button")),
                300, 250);

        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setX(10);

//        scene.setOnMouseClicked(t -> {
//            if(t.getButton()==MouseButton.SECONDARY){
//                cm.show(scene.getWindow(),t.getScreenX(),t.getScreenY());
//
//                // Request focus on first item
//                cm.getSkin().getNode().lookup(".menu-item").requestFocus();
//            }
//        });
    }
    public static void main(String[] args) {
        launch(args);
    }


}
