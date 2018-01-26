/*
 * Created on 26.01.2018
 *
 */
package de.swingempire.fx.event;

import java.util.logging.Logger;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.skin.ContextMenuSkin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ExContextMenuSkin extends ContextMenuSkin {

    /**
     * @param contextMenu
     */
    public ExContextMenuSkin(ContextMenu contextMenu) {
        super(contextMenu);
        Node root = getNode();
        root.addEventFilter(MouseEvent.MOUSE_RELEASED, ev -> {
            if (ev.getButton() == MouseButton.SECONDARY) {
                LOG.info("consume");
                ev.consume();
            }
        });

    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ExContextMenuSkin.class.getName());
}
