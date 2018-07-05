/*
 * Created on 05.07.2018
 *
 */
package de.swingempire.fx.control;

import java.util.Locale;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.ModifierValue;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Accelerators not localized.
 * 
 * Question on SO:
 * https://stackoverflow.com/q/51161764/203657
 * 
 * Bug:
 * https://bugs.openjdk.java.net/browse/JDK-8205655
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class MenuItemAcceleratorTest extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    public static class BugHackMenu extends Menu {
        
        public BugHackMenu(String text) {
            super(text);
            setOnShown(e -> hackAcceleratorText());
        }

        private void hackAcceleratorText() {
            getItems().stream().forEach(item -> {
                if (!needsHack(item)) return;
                Node menuRow = item.getStyleableNode();
                if (menuRow != null) {
                    Node accText = menuRow.lookup(".accelerator-text");
                    if (accText instanceof Label) {
                        Label acc = (Label) accText;
                        acc.setText(getHackedText(item.getAccelerator()));
                    }
                }

            });
        }
        
        private String getHackedText(KeyCombination accelerator) {
            return accelerator.getDisplayText().replace("Ctrl", "Strg");
        }

        private boolean needsHack(MenuItem item) {
            KeyCombination acc = item.getAccelerator();
            return acc != null && acc.getControl() == ModifierValue.DOWN;
        }
        
    }
    
    @Override
    public void start(Stage primaryStage) {
        // Locale.setDefault(new Locale("de", "DE"));
        Locale.setDefault(Locale.GERMANY);
        // Menu name
        Menu filemenu = new BugHackMenu("_File");

        // Create and add menu items to menu
        MenuItem newfile = new MenuItem("New File...");
        newfile.setAccelerator(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        // newfile.setAccelerator(KeyCombination.keyCombination("CTRL+N"));
        newfile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("CTR+N");
            }
        });
        filemenu.getItems().add(newfile);

//        filemenu.setOnShown(e -> {
//            LOG.info("showing menu " + e);
//            Node menuRow = newfile.getStyleableNode();
//            LOG.info("row: " + menuRow);
//            if (menuRow != null) {
//                Node accText = menuRow.lookup(".accelerator-text");
//                LOG.info("label: " + accText);
//                if (accText instanceof Label) {
//                    Label acc = (Label) accText;
//                    acc.setText("replaced ... what happens with very long");
//                }
//            }
//            
//        });
        // Main menu bar
        MenuBar menubar = new MenuBar();
        menubar.getMenus().addAll(filemenu);

        
        Button log = new Button("find acceleratorRegion");
        log.setOnAction(e -> {
            LOG.info("handler: " + filemenu.getOnShowing());
        });
        
        HBox buttons = new HBox(10, log);
        BorderPane layout = new BorderPane();
        layout.setTop(menubar);

//        layout.setBottom(buttons);
        Scene scene1 = new Scene(layout, 300, 300);
        primaryStage.setScene(scene1);

        primaryStage.show();

        
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MenuItemAcceleratorTest.class.getName());
}