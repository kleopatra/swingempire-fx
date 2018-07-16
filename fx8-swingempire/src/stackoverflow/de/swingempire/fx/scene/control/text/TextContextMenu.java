/*
 * Created on 16.07.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/51307577/203657
 * CustomMenuItem with TextField - action of item above is triggered.
 * 
 * Part is a bug in ContextMenuContainer:
 * - registers a keyHandler on ENTER that calls MenuItemContainer.doSelect on
 *  the item at currentFocusedIndex
 * - currentFocusedIndex is not updated when clicking into the textField, mainly
 *   because it's not updated on entering the custom item? All is fine if
 *   a) navigation handled via keyboard (additional tab to focus the textfield)
 *   or b) click in menu to open then move mouse from the side or below into textField
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextContextMenu extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        MenuButton menu = new MenuButton("Fancy Menu...");

        MenuItem hello = new MenuItem("Hello");
        hello.setOnAction(
                event -> System.out.println("Hello | " + event.getSource()));

        MenuItem world = new MenuItem("World!");
        world.setOnAction(
                event -> System.out.println("World! | " + event.getSource()));

        /*
         * Set the cursor into the TextField, maybe type something, and hit
         * enter. --> Expected: "ADD: <Text you typed> ..." --> Actual:
         * "ADD: <Text you typed> ..." AND "World! ..." - so the button above
         * gets triggered as well. If I activate listener (II) or (III),
         * everything works fine - even the empty action in (III) does is job,
         * but this is ugly... (And I can't use (II), because I need (I).
         */
        TextField textField = new TextField();
        CustomMenuItem custom = new CustomMenuItem(textField, false);
        // menuItemContainer registers a Mouse_clicked handler that fires
//        textField.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> e.consume());
        /* I */
        textField.addEventHandler(ActionEvent.ACTION, event -> {
            System.out.println("ADD: " + textField.getText() + " | "
                    + textField.getOnAction()
//                     event.getSource()
            );
            if (custom.getParentPopup() != null) {

                custom.getParentPopup().hide();
            }
            event.consume();
//            custom.fire();

        });
        /* II */
//         textField.setOnAction(event -> {
//             System.out.println("SET: " + textField.getText() + " | " + event.getSource());
//             if (custom.getParentPopup() != null) {
//                 
//                 custom.getParentPopup().hide();
//             }
//             custom.fire();
//         }); 
        /* III */
//         textField.setOnAction(__ -> {/* do nothing */});

        custom.setOnAction(e -> {
//            if (custom.getParentPopup() != null) {
//                
//                custom.getParentPopup().hide();
//            }

            System.out.println("action custom menu");

        });

        menu.getItems().addAll(hello, world, custom);

        Scene scene = new Scene(menu);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        scene.focusOwnerProperty().addListener((src, ov, nv) -> System.out.println("owner: " + nv));
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}

