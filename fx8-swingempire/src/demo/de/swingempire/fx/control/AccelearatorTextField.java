/*
 * Created on 28.11.2017
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Accelerators on Scene not working when TextField is focused
 * https://stackoverflow.com/q/28239019/203657
 * 
 * <p>
 * Question is from 2015, seems to work with fx9.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AccelearatorTextField extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(getContent());
        stage.setScene(scene);
        installMenu(scene);
        stage.show();
    }

    /**
     * @param scene
     */
    private void installMenu(Scene scene) {
        BorderPane content = (BorderPane) scene.getRoot();
        MenuItem action = new MenuItem("do stuff, really");
        action.setOnAction(e -> System.out.println("hit me!"));
        action.setAccelerator(KeyCombination.keyCombination("F1"));
        MenuItem other = new MenuItem("do other stuff");
        other.setOnAction(e -> System.out.println("hit me harder!"));
        other.setAccelerator(KeyCombination.keyCombination("Ctrl+F1"));
        Menu menu = new Menu("something", null, action, other);
        MenuBar bar = new MenuBar(menu);
        content.setTop(bar);
        scene.getAccelerators().put(
//                new KeyCodeCombination(KeyCode.F3),
                KeyCombination.keyCombination("F3"), 
                () -> System.out.println("independent"));
        scene.getAccelerators().put(KeyCombination.keyCombination("Ctrl+F3"), 
                () -> System.out.println("independent control"));
    }

    /**
     * @return
     */
    private Parent getContent() {
        
        Button button = new Button("dummy button");
        TextField field = new TextField("nothing but text");
        CheckBox box = new CheckBox("silent");
        VBox pane = new VBox(10, button, field, box);
        BorderPane content = new BorderPane(pane);
        return content;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
