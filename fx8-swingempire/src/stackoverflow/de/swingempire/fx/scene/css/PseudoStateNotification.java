/*
 * Created on 01.12.2018
 *
 */
package de.swingempire.fx.scene.css;

import java.net.URL;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Debug pseudo-state notification. 
 * 
 * Custom button with special property:
 * - no stylesheet: notification working as expected
 * - stylesheet: notification broken once the custom style is set
 * 
 * asked on SO:
 * https://stackoverflow.com/q/53580773/203657
 * 
 * unrelated to custom state: the culprit is Node which returns a 
 * unmodifiable wrapper around the states which has no strong reference
 * except when client code keeps it, so will be garbage collected.
 * reported: https://bugs.openjdk.java.net/browse/JDK-8214699
 * 
 * --------- weird:
 * 
 * this is a exact copy of the version in bug report (except for the package declaration), 
 * but the notification is okay, doesn't seem to break off .. what's the difference?
 * 
 * breaks off later? after focusing another window? really unreliable ..
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class PseudoStateNotification extends Application {
    private SpecialButton specialButton;
    private SetChangeListener sl;

    public static class SpecialButton extends Button {
        private static PseudoClass SPECIAL = PseudoClass.getPseudoClass("special");
        private BooleanProperty special = new SimpleBooleanProperty(this, "special", false) {

            @Override
            protected void invalidated() {
                pseudoClassStateChanged(SPECIAL, get());
            }

        };

        public SpecialButton(String text) {
            super(text);
            //getStyleClass().add("special_button");
        }

        public void setSpecial(boolean sp) {
            special.set(sp);;
        }

        public boolean isSpecial() {
            return special.get();
        }

        public BooleanProperty specialProperty() {
            return special;
        }
    }

    private Parent createContent() {
        sl = change -> LOG.info("pseudo-changed: " + change.getSet());

        specialButton =  new SpecialButton("custom buttom");
        specialButton.getPseudoClassStates().addListener(sl);
        LOG.info("" + (specialButton.getPseudoClassStates() == specialButton.getPseudoClassStates()));
        BorderPane pane = new BorderPane(specialButton);
        Button toggle = new Button("no action");
        toggle.setOnAction(e -> {
            specialButton.setSpecial(!specialButton.isSpecial());
            LOG.info("set query: " +  specialButton.getPseudoClassStates());
        });
        pane.setBottom(toggle);
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 200, 200));
        URL uri = getClass().getResource("pseudo.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
    .getLogger(PseudoStateNotification.class.getName());

}
