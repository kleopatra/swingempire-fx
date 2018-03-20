/*
 * Created on 20.03.2018
 *
 */
package de.swingempire.fx.scene;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Close Alert programmatically
 * question: https://stackoverflow.com/q/49360820/203657
 * To work, the alert needs its result set, as found by fabian in
 * answer to question
 * 
 * commented fabian's answer:
 * okay, think I understand the logic: 
 * a) programmatic close is in the same "abnormal" category as closing by window button 
 * b) api doc of dialog.close specifies rules when abnormal closing is allowed to actually close. 
 * Which is 
 * i) having exactly one button or 
 * ii) having multple buttons one of which is cancel_close. 
 * AlertType.NONE doesn't have buttons, so it hits the (unspecified? 
 * seen in the implementation of fxDialog.permissionToClose) 
 * special case that the result must be != null. You found a perfect hack for a corner case :)
 * 
 * example from similar question:
 * https://stackoverflow.com/a/30757009/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AlertClose extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Alert alert = new Alert(
                Alert.AlertType.NONE, 
                // all alertTypes except none create at least one button
//                AlertType.CONFIRMATION,
                "nothing but wait ..."
                // no button: close denied without result
                ); 
                // single button: close allowed, irrespective of type
//                , ButtonType.NEXT);
                // multiple buttons, none of which is of type cancel_close
                // close denied
//                ButtonType.NEXT, ButtonType.PREVIOUS); 
            //Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Title");
//        alert.setHeaderText("Some Text");
//        alert.setContentText("Choose your option.");
        
        LOG.info("alert: " + alert.getResult());
        // does not help, the converter is used only if there are buttons
//        alert.setResultConverter(bt -> ButtonType.OK);
        // doesn't matter which type as long as it is not null
        alert.setResult(ButtonType.NEXT);
//        ButtonType buttonTypeOne = new ButtonType("Yes");
//        ButtonType buttonTypeCancel = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
//        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);

        Thread thread = new Thread(() -> {
            try {
                // Wait for 5 secs
                Thread.sleep(5000);
                if (alert.isShowing()) {
                    Platform.runLater(() -> alert.close());
                }
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
        alert.show();
//        Optional<ButtonType> result = alert.showAndWait();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AlertClose.class.getName());
}

