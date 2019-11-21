/*
 * Created on 21.11.2019
 *
 */
package de.swingempire.fx.event;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;

/**
 * https://stackoverflow.com/q/58973367/203657
 * standard key bindings for text are not working if popup is showing
 */
public class ViewPopupController implements Initializable {

//    @FXML
    private TextField textField;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Popup popUp = new Popup();
        TableView<Object> table = new TableView<>();

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
    }
}

