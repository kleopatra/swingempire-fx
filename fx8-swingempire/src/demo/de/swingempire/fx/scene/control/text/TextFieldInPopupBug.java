/*
 * Created on 07.08.2019
 *
 */
package de.swingempire.fx.scene.control.text;

import de.swingempire.fx.scene.control.skin.XTextFieldSkin;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 * Focused textField in popup prevents closing of popup by ESC
 * https://bugs.openjdk.java.net/browse/JDK-8090230
 * 
 * always in fx8: the old mechanism in behaviorbase consumes if it finds
 * a matching action in its list of bindings
 * 
 * fixed in fx9: consume the esc only if formatter set, then the 
 * bug is back
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldInPopupBug  extends Application {

        public static void main(String[] args) {
            launch(args);
            
        }

        @Override
        public void start(Stage stage) {
            final Button button = new Button("Click to Show Popup");

            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    Popup popup = new Popup();
                    popup.setAutoHide(true);
                    popup.setHideOnEscape(true);
                    // informal testing: XTextFieldSkin is okay
                    TextField textField = new TextField("Focus on this one, ESCAPE doesn't work") {

//                        @Override
//                        protected Skin<?> createDefaultSkin() {
//                            return new XTextFieldSkin(this);//super.createDefaultSkin();
//                        }
                        
                    };
                    textField.setTextFormatter(new TextFormatter(TextFormatter.IDENTITY_STRING_CONVERTER));
                    VBox box = new VBox(10, textField, new CheckBox("Focus on this one, ESCAPE works"));
                    box.setPadding(new Insets(10));
                    popup.getContent().addAll(box);
                    Point2D p = button.localToScreen(0, 0);
                    popup.show(button, p.getX(), p.getY() + button.getHeight());
                }
            });

            VBox vbox = new VBox(6, button, new Label("Clicking on the button to show a popup. Focus on the text field and press ESCAPE. The popup doesn't hide." +
                    "\nYou can read comment in the file for more information"));
            vbox.setPadding(new Insets(10));

            Scene scene = new Scene(new Group());
            stage.setTitle("TextField in Popup Bug");
            ((Group) scene.getRoot()).getChildren().addAll(vbox);
            stage.setScene(scene);
            stage.show();
        }
    }

