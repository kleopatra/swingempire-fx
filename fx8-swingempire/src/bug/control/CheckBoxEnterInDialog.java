/*
 * Created on 29.01.2020
 *
 */
package control;

import java.util.Optional;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8238078
 * 
 * enter when dialog's checkbox is focused - expected: dialog closed with ok -
 * actual: checkbox selection toggled
 * 
 * regression from fx8, not on mac
 * 
 * seems to be intentionally added in fx9+ (keyMapping for Enter in ButtonBehavior)
 * but why? checkBox is a state control (_not_ an actionable control) and as such
 * should react only to space, not enter (both seem to be okay for button, though, 
 * according to UX and accessibilty guidelines)
 */
public class CheckBoxEnterInDialog extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        showDialog();
    }

    private void showDialog() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Look, a Confirmation Dialog");
        alert.setContentText("Are you ok with this?");
        alert.getDialogPane().setContent(new CheckBox());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            System.out.println("OK");
        } else {
            System.out.println("Cancel");
        }
    }
}
