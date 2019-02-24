/*
 * Created on 23.02.2019
 *
 */
package de.swingempire.fx.scene.control;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Keep menu open on dynamic change of
 * 
 * https://stackoverflow.com/q/54834206/203657
 */
public class MenuKeepOpenOnChangeAnswer extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        final Menu menu = new Menu("MENU");
        final MenuButton button = new MenuButton("menu");
        final List<String> options = Arrays.asList(
                "AbC",
                "dfjksdljf",
                "skdlfj",
                "stackoverflow");

        final StringProperty currentSelection = new SimpleStringProperty(null);

        final TextField fuzzySearchField = new TextField(null);
        final CustomMenuItem fuzzySearchItem = new CustomMenuItem(fuzzySearchField, false);
        // TODO unfortunately we seem to have to grab focus like this!
        fuzzySearchField.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            fuzzySearchField.requestFocus();
            fuzzySearchField.selectEnd();
        });
        final ObservableList<String> currentMatches = FXCollections.observableArrayList();
        // just some dummy matching here
        fuzzySearchField.textProperty().addListener((obs, oldv, newv) -> {

            currentMatches.setAll(options.stream().filter(s -> s.toLowerCase().contains(newv)).collect(Collectors.toList()));

        });
        //changed from ArrayList to ObservableArray
        ObservableList<MenuItem> items =  FXCollections.observableArrayList(); 
        currentMatches.addListener((ListChangeListener<String>) change -> {
            items.clear();//Clearing items to in-case of duplicates and NULL duplicates.
            items.add(fuzzySearchItem);

            currentMatches.stream().map(MenuItem::new).forEach(items::add);

            System.out.println("Updating menu items!");
            menu.getItems().setAll(items);


        });
        // Binding to Observable items.
        Bindings.bindContent(menu.getItems(), items); 
        fuzzySearchField.textProperty().addListener((obs, oldv, newv) -> currentSelection.setValue(currentMatches.size() > 0 ? currentMatches.get(0) : null));
        fuzzySearchField.setText("");


        button.getItems().setAll(menu);


        final Scene scene = new Scene(button);

        primaryStage.setScene(scene);
        primaryStage.show();

    }
}