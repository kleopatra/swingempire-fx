/*
 * Created on 03.03.2015
 *
 */
package de.swingempire.fx.scene.control.et;


import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * ContextMenu:
 * user interaction broken when activated by keyboard.
 * https://javafx-jira.kenai.com/browse/RT-40175
 * 
 * To reproduce, compile, run and activate contextMenu by keyboard (shift-f10 on win)
 * 
 * variant A:
 * - press DOWN
 * - expected: next menu item highlighted
 * - actual: system menu of window opens
 * 
 * variant B:
 * - press ESC 
 * - expected: contextMenu hidden
 * - actual: nothing happens (needs a second ESC to hide)
 * 
 * variant C
 * - move mouse over menu
 * - expected: menu item below mouse is highlighted
 * - actual: no visual change
 */
public class ContextMenuKeyboard extends Application {

    private Parent getContent() {
        Button button = new Button("simple button with contextMenu");
        button.setContextMenu(new ContextMenu(new MenuItem("one"), new MenuItem("two")));
        Parent pane = new HBox(button);
        return pane;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
      primaryStage.setScene(scene);
      primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
