/*
 * Created on 25.08.2019
 *
 */
package de.swingempire.fx.fxml;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 * https://stackoverflow.com/q/57642167/203657
 * menuItem setting bold on action doesn't work
 * 
 * worksforme - throws NPE (menu id in fxml incorrectly was "Menu"), though,
 * might run another?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class MenuItemBoldController implements Initializable{

    @FXML private Menu menu;

    private List<MenuItem> menuItems;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        menuItems = menu.getItems();
        menuItems.get(0).setStyle("-fx-font-weight: bold");
    }

    public void setFontBold() {
        menuItems.get(1).setStyle("-fx-font-weight: bold");
        System.out.println(menuItems.get(1).getStyle());
        System.out.println("Font set to bold in menuitem with index 1 has no effect...");
    }
}
