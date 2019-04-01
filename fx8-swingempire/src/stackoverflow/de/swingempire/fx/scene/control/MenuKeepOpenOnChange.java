/*
 * Created on 23.02.2019
 *
 */
package de.swingempire.fx.scene.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.javafx.scene.control.ContextMenuContent;

import static de.swingempire.fx.util.DebugUtils.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import test.combobox.MenuModifyItemsWhileShowing;

/**
 * Keep menu open on dynamic change of 
 * 
 * https://stackoverflow.com/q/54834206/203657
 * 
 * It's a bug in ContextMenuContent:
 * - the content of the menu is responsible for showing/hiding/configuring the contextMenu
 *   of the open submenu
 * - the items of the contextMenu of the subMenu are set once on showing, nobody is
 *   listening to the items of the menu such that they are not kept in synch
 * - a way out is to reflectively access the submenu and update its items on 
 *   modifications of those of the menu    
 *   
 * reported: https://bugs.openjdk.java.net/browse/JDK-8219620
 *   
 * @see test.combobox.MenuModifyItemsWhileShowing
 *   
 */
public class MenuKeepOpenOnChange extends Application {

    ContextMenuContent container;
    boolean bound;
    
    @Override
    public void start(Stage primaryStage) {
        final Menu menu = new Menu("MENU");
        final MenuButton mbutton = new MenuButton("menu");
        mbutton.getItems().setAll(menu);
//        ContextMenu cmenu = new ContextMenu();
//        cmenu.getItems().setAll(menu);
//        Button button = new Button("for contextMenu");
//        button.setContextMenu(cmenu);
        
        final List<String> options = Arrays.asList(
                "AbC",
                "dfjksdljf",
                "skdlfj",
                "stackoverflow");

        final StringProperty currentSelection = new SimpleStringProperty(null);

        final TextField fuzzySearchField = new TextField(null);
        final CustomMenuItem fuzzySearchItem = new CustomMenuItem(fuzzySearchField, false);
        menu.getItems().add(fuzzySearchItem);
        // TODO unfortunately we seem to have to grab focus like this!
        fuzzySearchField.addEventFilter(MouseEvent.MOUSE_MOVED, e-> {
            fuzzySearchField.requestFocus(); 
            fuzzySearchField.selectEnd();
            });
        final ObservableList<String> currentMatches = FXCollections.observableArrayList();
        // just some dummy matching here
        fuzzySearchField.textProperty().addListener((obs, oldv, newv) -> { 
            currentMatches.setAll(
                    options.stream().filter(s -> s.toLowerCase().contains(newv))
                    .collect(Collectors.toList()));
            
        });
        currentMatches.addListener((ListChangeListener<String>)change -> {
            
            List<MenuItem> items = new ArrayList<>();
            // this is updating the custom event as well as the others, try to keep
            // doesn't help: items not updated
//            menu.getItems().retainAll(fuzzySearchItem);
            items.add(fuzzySearchItem);
            currentMatches.stream().map(MenuItem::new).forEach(items::add);
            
            printMenuItemContext("menu: ", menu);
            
            
            menu.getItems().setAll(items);
            
            if (container !=  null) {
                ContextMenu submenu = (ContextMenu) 
                        FXUtils.invokeGetFieldValue(ContextMenuContent.class, container, "submenu");
//                if (!bound) {
//                    // bindings not really working ... throw AIOOB when ??
//                    bound = true;
//                    Bindings.bindContent(submenu.getItems(), menu.getItems());
//                }
                submenu.getItems().setAll(items);
//                LOG.info("same items?" + (menu.getItems().equals(submenu.getItems())));
//                FXUtils.invokeMethod(ContextMenuContent.class, container, "hideSubmenu");
//                FXUtils.invokeGetMethodValue(ContextMenuContent.class, container, "showSubmenu", Menu.class, menu);
                
            };
        });
        
        fuzzySearchField.textProperty().addListener(
                (obs, oldv, newv) -> currentSelection.setValue(currentMatches.size() > 0 ? currentMatches.get(0) : null));
        fuzzySearchField.setText("");

        menu.setOnShown(e -> {
            
            fuzzySearchField.requestFocus();
            if (menu.getStyleableNode() != null) {
                Parent menuBox = menu.getStyleableNode().getParent();
                Parent contextMenuContent = menuBox.getParent();
                if (container == null && contextMenuContent != null) {
                    container = (ContextMenuContent) contextMenuContent;
                }
                LOG.info("same? " + contextMenuContent + " / " + (container == contextMenuContent) );
                
            }
        }
            );
        
        
        final Scene scene = new Scene(mbutton, 300, 50);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MenuKeepOpenOnChange.class.getName());
}

