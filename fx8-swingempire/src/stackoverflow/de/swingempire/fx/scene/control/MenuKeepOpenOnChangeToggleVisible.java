/*
 * Created on 23.02.2019
 *
 */
package de.swingempire.fx.scene.control;

import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Keep menu open on dynamic change of 
 * 
 * Here: try to toggle visibility of: working but doesn't meet the requirements
 * (order of options - and maybe the content - should change while the menu is
 * open).
 *  
 * https://stackoverflow.com/q/54834206/203657
 */
public class MenuKeepOpenOnChangeToggleVisible extends Application {

    @Override
    public void start(Stage primaryStage) {
        final Menu menu = new Menu("MENU");

        final List<String> options = Arrays.asList(
                "AbC",
                "dfjksdljf",
                "skdlfj",
                "stackoverflow");

        final TextField fuzzySearchField = new TextField();
        final CustomMenuItem fuzzySearchItem = new CustomMenuItem(fuzzySearchField, false);
        menu.getItems().add(fuzzySearchItem);
        options.stream()
            .map(MenuItem::new)
            .forEach(item -> menu.getItems().add(item));
        
        // just some dummy matching here
        fuzzySearchField.textProperty().addListener(
                (obs, oldv, newv) -> {
                    for(int i = 1; i < menu.getItems().size(); i++) {
                        MenuItem current = menu.getItems().get(i);
                        current.setVisible(current.getText().toLowerCase().contains(newv));
                    }
                    
                });
        menu.setOnShown(e -> fuzzySearchField.requestFocus());
        final MenuButton button = new MenuButton("menu");
        button.getItems().setAll(menu);

        final Scene scene = new Scene(button);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
    
    public static void main(String[] args) {
        launch(args);
    }

}

