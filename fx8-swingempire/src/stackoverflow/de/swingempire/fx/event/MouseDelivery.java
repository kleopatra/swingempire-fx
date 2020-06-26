/*
 * Created on 26.06.2020
 *
 */
package de.swingempire.fx.event;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * https://stackoverflow.com/q/62558671/203657
 * unusual requirement: open contestMenu with right pressed, then move and fire
 * menuItem on released.
 * 
 * answer by Duncan: use eventFilter.
 * 
 * Not really possible, events are delivered to the source and its children
 * @author Jeanette Winzenburg, Berlin
 */
public class MouseDelivery extends Application {
    
    private Parent createContent() {
        Button first = new Button("first");
        Button other = new Button("other");
        
        
        Button buttonTest = new Button("Right-Click me!");

        ContextMenu cm = new ContextMenu();
        MenuItem miTest = new MenuItem("Test");
        miTest.setOnAction(e -> System.out.println("mi action"));
//            new
//                Alert(Alert.AlertType.INFORMATION,"Test").showAndWait());
        cm.getItems().addAll(miTest, new MenuItem("other"));
        
        buttonTest.setContextMenu(cm);
        buttonTest.addEventFilter(MouseEvent.ANY, e -> {
            if ((e.getEventType() != MouseEvent.MOUSE_MOVED) && e.getEventType() != MouseEvent.MOUSE_DRAGGED)
                System.out.println("on button: " + e);
            if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
//                if (cm.isShowing()) {
//                    cm.hide();
//                }
                if (e.isSecondaryButtonDown()) {
                    
//                    Window stage = buttonTest.getScene().getWindow();
//                    cm.show(stage);
                    e.consume();
                    
                    ContextMenuEvent context = new ContextMenuEvent(ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                            e.getSceneX(), e.getSceneY(), e.getScreenX(), e.getScreenY(), 
                            false, null);
                    Event.fireEvent(buttonTest, context);
                }
            }
        });
        
        BorderPane content = new BorderPane(buttonTest);
        content.setBottom(new HBox(10, first, other));
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
            .getLogger(MouseDelivery.class.getName());

}
