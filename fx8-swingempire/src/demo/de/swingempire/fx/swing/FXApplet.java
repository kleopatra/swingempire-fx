/*
 * Created on 15.04.2015
 *
 */
package de.swingempire.fx.swing;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;

/**
 * Focus problem with popup in swing/fx intermix
 * http://stackoverflow.com/q/29646127/203657
 */
public class FXApplet extends JApplet { 
    protected Scene scene;
    protected Parent root;

    @Override
    public final void init() {
        SwingUtilities.invokeLater(() -> initSwing());
    }

    private void initSwing() {
        JFXPanel fxPanel = new JFXPanel();
        add(fxPanel);

        Platform.runLater(() -> {
            initFX(fxPanel);
            initApplet();
        });
    }

    private void initFX(JFXPanel fxPanel) {
        Button button = new Button("dummy");
        root = new HBox(button);
        scene = new Scene(root);
        fxPanel.setScene(scene);
    }

    public void initApplet() {
        Popup popup = new Popup();

        popup.setAutoHide(false);
        popup.getContent().add(new TextField());
        popup.show(scene.getWindow());
    }
}

