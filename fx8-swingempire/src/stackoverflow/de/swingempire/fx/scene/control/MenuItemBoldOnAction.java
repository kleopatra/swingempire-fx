/*
 * Created on 25.08.2019
 *
 */
package de.swingempire.fx.scene.control;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/57642167/203657
 * menuItem setting bold on action doesn't work
 * 
 * worksforme .. also fxml (after fixing incorrect id of menu in  fxml)
 * issue in fx8: style only set initially (in MenuItemContainer.createChildren), no listener
 * to keep in sync. fx11 binds the style of the label.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class MenuItemBoldOnAction extends Application {
    
    private Parent createContent() {
        MenuBar bar = new MenuBar();
        Menu menu = new Menu("container");
        MenuItem first = new MenuItem("first");
        MenuItem second = new MenuItem("second");
        second.setOnAction(e -> second.setStyle("-fx-font-weight: bold"));
        menu.getItems().addAll(first, second);
        bar.getMenus().addAll(menu);
        
        BorderPane content = new BorderPane(bar);
        return content;
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

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MenuItemBoldOnAction.class.getName());

}
