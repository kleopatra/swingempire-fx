/*
 * Created on 24.02.2019
 *
 */
package test.combobox;

import java.util.Arrays;
import java.util.List;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Issue: modifying the items of a Menu (aka: submenu) while it is showing
 * does not update the items shown. For comparison: modifying the list of
 * direct items of the contextMenu is working as expected.
 * 
 * To reproduce, compile, run:
 * see tab "subMenu"
 * - right click in button the show contextmenu
 * - move mouse over menu to open submenu and make sure it remains open 
 *   during the steps below
 * - press f1 
 * - expected: first item of open submenu visibly removed 
 * - actual: no visual change
 * - to verify that items are removed from the items list, press f1 until message
 *   "empty" is printed
 *   
 * for comparison, see tab "direct" and do the same: 
 * - as expected, items are visibly removed from the showing popup   
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class MenuModifyItemsWhileShowing extends Application {

    protected Node createTabContent(boolean asSubMenu) {
        final List<String> options = Arrays.asList(
                "AbC",
                "dfjksdljf",
                "skdlfj",
                "stackoverflow");

        ContextMenu cmenu = new ContextMenu();
        List<MenuItem> items;;
        if (asSubMenu) {
            //---------- submenu: bug
            final Menu menu = new Menu("MENU");
            cmenu.getItems().setAll(menu);
            items = menu.getItems();
        } else {
            // for comparison: directly added - behaves as expected
            items = cmenu.getItems();
        }
        options.stream().map(MenuItem::new).forEach(items::add);

        Button button = new Button("for contextMenu");
        button.setContextMenu(cmenu);
        // on pressing f1, remove first menuItem until list empty
        cmenu.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F1) { 
                if (!items.isEmpty()) {
                    items.remove(0);
                } else {
                    System.out.println("no more items");
                }
            }
        });
        return button;
    }

    private Parent createContent() {
        TabPane pane = new TabPane();
        pane.getTabs().addAll(
                new Tab("subMenu", createTabContent(true)), 
                new Tab("direct", createTabContent(false)));
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    
}
