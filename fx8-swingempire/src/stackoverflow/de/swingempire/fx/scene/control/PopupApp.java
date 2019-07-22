/*
 * Created on 21.07.2019
 *
 */
package de.swingempire.fx.scene.control;


import com.sun.javafx.event.RedirectedEvent;
import com.sun.javafx.scene.control.behavior.ButtonBehavior;
import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.inputmap.InputMap;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ButtonSkin;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 * Popup swallows left/right keys.
 * https://stackoverflow.com/q/57131955/203657
 * 
 * reported by OP:
 * https://bugs.openjdk.java.net/browse/JDK-8228459
 * 
 * related issue (editable combo) by me:
 * https://bugs.openjdk.java.net/browse/JDK-8209788
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class PopupApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        TextField textField = new TextField();
        StackPane stackPane = new StackPane(textField);

        stage.setScene(new Scene(stackPane));
        stage.show();

        // buttonBehaviour installs FocusTraversalInputMap
        Button button = new Button("Option1");
        VBox content = new VBox(button); //, new Button("Option2"));
        // Label doesn't eat - has no behaviour
//        VBox content = new VBox(new Label("Option1"), new Label("Option2"));
        // parent type doesn't matter
//        Group c = new Group(new Button("Option1"), new Button("Option2"));
        // scrollPane doesn't mappter
        ScrollPane scrollPane = new ScrollPane(content);
        Popup popup = new Popup();
        popup.getContent().add(scrollPane);

        // suggestion from answer: working as expected - must be set before showing!
        popup.getScene().getWindow().setEventDispatcher((event, tail) -> {
            if (event.getEventType() != RedirectedEvent.REDIRECTED) {
                tail.dispatchEvent(event);
            }
            return null;
        });
        
        Point2D pinPoint = textField.localToScreen(0., textField.getHeight());
        popup.show(textField, pinPoint.getX(), pinPoint.getY());

        // suggestion from answer: no typing allowed
//        popup.getScene().getWindow().setEventDispatcher((event, tail) -> {
//            if (event.getEventType() != RedirectedEvent.REDIRECTED) {
//                tail.dispatchEvent(event);
//            }
//            return null;
//        });
        
        // remove focusTraversalMappings from ButtonSkin: doesn't help here
//        ButtonSkin skin = (ButtonSkin) button.getSkin();
//        ButtonBehavior behavior = (ButtonBehavior) FXUtils.invokeGetFieldValue(
//                ButtonSkin.class, skin, "behavior"); 
//        InputMap<?> map = behavior.getInputMap();
//        map.getMappings().removeAll(FocusTraversalInputMap.getFocusTraversalMappings()); 
        
        textField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            System.out.println("KEY_RELEASED " + event);
        });
        
        textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            System.out.println("KEY_PRESSED " + event);
        });
//        textField.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
//            System.out.println("KEY_RELEASED " + event);
//        });
//
//        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//            System.out.println("KEY_PRESSED " + event);
//        });
    }
}

