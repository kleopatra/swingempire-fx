/*
 * Created on 05.08.2019
 *
 */
package de.swingempire.fx.event;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class SeparatorMenuItemWithLabel extends Application {

    private Parent createContent() {
        Button button = new Button("dummy");
        
        ContextMenu menu =  new ContextMenu();
        MenuItem first = new MenuItem("first");
        MenuItem last = new MenuItem("lasst");
        
        SeparatorMenuItem sep = new SeparatorMenuItem();
        sep.setText("separator");
        
        menu.getItems().addAll(first, new MenuItem("really"), sep, new MenuItem("nothing ... only longish"), last);
//        menu.getItems().addAll(first, sep, last);
        button.setContextMenu(menu);
        
        BorderPane content = new BorderPane(button);
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
            .getLogger(SeparatorMenuItemWithLabel.class.getName());

}
