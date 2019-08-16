/*
 * Created on 16.07.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.logging.Logger;

import com.sun.javafx.scene.control.ContextMenuContent;
import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/51307577/203657
 * CustomMenuItem with TextField - action of item above is triggered.
 * reported by OP as https://bugs.openjdk.java.net/browse/JDK-8207385
 * 
 * same fix as https://bugs.openjdk.java.net/browse/JDK-8145515
 * use flag "TextInputControlBehavior.disableForwardToParent" in textfield's
 * properties map for core
 * 
 * Part is a bug in ContextMenuContainer:
 * - registers a keyHandler on ENTER that calls MenuItemContainer.doSelect on
 *  the item at currentFocusedIndex
 * - currentFocusedIndex is not updated when clicking into the textField, mainly
 *   because it's not updated on entering the custom item? All is fine if
 *   a) navigation handled via keyboard (additional tab to focus the textfield)
 *   or b) click in menu to open then move mouse from the side or below into textField
 * 
 * Further digging:
 * - MenuItemContainer has a listener on its focusedProperty which updates the currentFocusedIndex 
 *    if true
 * - the listener is installed only for normal items, not customMenuItems   
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
        // menuItemContainer registers a mouseClicked handler that fires
        // need to consume before it reaches the container
        textField.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> e.consume());
//        // hack to update internal state of ContextMenuItem
//        textField.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
//            ContextMenuContent cmContent = null;
//            Node node = textField;
//            while (node != null) {
//                node = node.getParent();
//                if (node instanceof ContextMenuContent) {
//                    cmContent = (ContextMenuContent) node;
//                    break;
//                }
//            }
//            if (cmContent != null) {
//                Parent menuItemContainer = textField.getParent();
//                Parent menuBox = menuItemContainer.getParent();
//                int index = menuBox.getChildrenUnmodifiable().indexOf(menuItemContainer);
//                // hack internal state
//                cmContent.requestFocusOnIndex(index);
//            }
//        });
//        /* I */
//        textField.getProperties().put("TextInputControlBehavior.disableForwardToParent", true);
        
        textField.addEventHandler(ActionEvent.ACTION, event -> {
            System.out.println("ADD: " + textField.getText() + " | "
                    + event
            );
            // consume to prevent item to fire twice
            // has no effect on the level of TextFieldBehaviour: 
            // conditional in fire to forwardToParent 
            //         if (onAction == null && !actionEvent.isConsumed()) { // forward
            // is always true because the actionEvent is copied during dispatch
            // here we consume the copy, has no effect on the original
//            event.consume();

        });

//        ComboBoxPopupControl c;
        custom.setOnAction(e -> {
            // someone needs to hide the popup
            // could be done in textField actionHandler as well
            if (custom.getParentPopup() != null) {
                custom.getParentPopup().hide();
            }
            System.out.println("action custom menu " + e.getSource());
        });

//        /* II */
//         textField.setOnAction(event -> {
//             System.out.println("SET: " + textField.getText() + " | " + event.getSource());
//             if (custom.getParentPopup() != null) {
//                 
//                 custom.getParentPopup().hide();
//             }
////             custom.fire();
//         }); 
        /* III */
//         textField.setOnAction(__ -> {/* do nothing */});


        menu.getItems().addAll(hello, world, custom);

        Scene scene = new Scene(menu);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        scene.focusOwnerProperty().addListener((src, ov, nv) -> System.out.println("owner: " + nv));
    }
    
    // below: trying to replace fire handling in binding .. no success
//    protected void replaceEnter(TextField field) {
//        TextFieldBehavior behavior = (TextFieldBehavior) FXUtils.invokeGetFieldValue(
//                TextFieldSkin.class, field.getSkin(), "behavior");
//        InputMap inputMap = behavior.getInputMap();
//        KeyBinding binding = new KeyBinding(KeyCode.ENTER);
//        
//        KeyMapping keyMapping = new KeyMapping(binding, e -> {
////            e.consume();
//            fire(field);
//        });
//        // note: this fails prior to 9-ea-108
//        // due to https://bugs.openjdk.java.net/browse/JDK-8150636
//        inputMap.getMappings().remove(keyMapping); 
//        inputMap.getMappings().add(keyMapping);
//    }
//    
//    protected void fire(TextField textField) {
//        EventHandler<ActionEvent> onAction = textField.getOnAction();
//        ActionEvent actionEvent = new ActionEvent(textField, null);
//        // first commit, then fire
//        textField.commitValue();
//        textField.fireEvent(actionEvent);
//        // PENDING JW: missing forwardToParent
//        if (onAction == null && !actionEvent.isConsumed()) {
//            forwardToParent(textField, new KeyEvent(KeyCode.ENTER));
//        }
//    }
//
//    protected void forwardToParent(TextField field, KeyEvent event) {
//        // fix for JDK-8145515
////        if (field.getProperties().containsKey(DISABLE_FORWARD_TO_PARENT)) {
////            return;
////        }
//
//        if (field.getParent() != null) {
//           field.getParent().fireEvent(event);
//        }
//    }
//

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextContextMenu.class.getName());

}

