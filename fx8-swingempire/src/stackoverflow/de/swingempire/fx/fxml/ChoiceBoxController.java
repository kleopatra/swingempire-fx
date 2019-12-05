/*
 * Created on 18.04.2019
 *
 */
package de.swingempire.fx.fxml;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;

public class ChoiceBoxController {

    @FXML
    private ChoiceBox choiceBox;
    @FXML
    private Button button;

    public ChoiceBox getChoiceBox() {
        return choiceBox;
    }

    public void setChoiceBox(ChoiceBox choiceBox) {
        this.choiceBox = choiceBox;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }
    
    // quick check: setting singleton event handlers
    @FXML
    private void over(MouseEvent e) {
        System.out.println(e);
    }
}