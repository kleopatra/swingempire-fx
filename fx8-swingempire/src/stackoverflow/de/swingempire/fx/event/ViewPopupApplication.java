/*
 * Created on 21.11.2019
 *
 */
package de.swingempire.fx.event;

import java.util.logging.Logger;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.EventDispatchChain;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/58973367/203657
 * debugging: some key pressed don't reach the textField
 * 
 * reason: all keys are redirected to the popup scene's focusOwner - here
 * the table - which consumes some in its inputMap.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ViewPopupApplication extends Application {

    TextField textField;
    
    private Parent createContent() {
        Popup popUp = new Popup();
        TableView<Object> table = new TableView<>();
        table.setFocusTraversable(false);
        
        TextField textField = new TextField() {

            @Override
            public EventDispatchChain buildEventDispatchChain(
                    EventDispatchChain tail) {
                if (popUp.isShowing()) {
                    String dummy = "nothing";
                }
                return super.buildEventDispatchChain(tail);
            }
            
        };
        textField.setPrefColumnCount(20);
        
        

        table.prefWidthProperty().bind(textField.widthProperty());
        popUp.getContent().add(table);
//        popUp.setHideOnEscape(true);
//        popUp.setAutoHide(true);
        
        

        textField.setText("something to see");
        // To see if the KeyEvent is triggered
        textField.addEventFilter(KeyEvent.ANY, e -> {
            System.out.println("got key: " + e);  
            if (popUp.isShowing() && e.getCode() == LEFT && e.getEventType() == KEY_PRESSED) {
                
            }
        });
                
//                System.out::println);

        textField.setOnKeyTyped(event -> {
            if(!popUp.isShowing()){
                popUp.show(
                        textField.getScene().getWindow(),
                        textField.getScene().getWindow().getX()
                                + textField.localToScene(0, 0).getX()
                                + textField.getScene().getX(),
                        textField.getScene().getWindow().getY()
                                + textField.localToScene(0, 0).getY()
                                + textField.getScene().getY()
                                + textField.getHeight() - 1);
            }
        });

        BorderPane content = new BorderPane(textField);
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
            .getLogger(ViewPopupApplication.class.getName());

}
