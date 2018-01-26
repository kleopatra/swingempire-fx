/*
 * Created on 26.01.2018
 *
 */
package de.swingempire.fx.event;

import javafx.scene.control.MenuBar;
import javafx.scene.control.skin.MenuBarSkin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ExMenuBarSkin extends MenuBarSkin {

    /**
     * Instantiates a skin for the given MenuBar. Registers an
     * event filter that consumes right mouse press.
     *  
     * @param menuBar
     */
    public ExMenuBarSkin(MenuBar menuBar) {
        super(menuBar);
        menuBar.addEventFilter(MouseEvent.MOUSE_PRESSED, ev -> {
            if (ev.getButton() == MouseButton.SECONDARY) {
                ev.consume();
            }
        });

    }

}
