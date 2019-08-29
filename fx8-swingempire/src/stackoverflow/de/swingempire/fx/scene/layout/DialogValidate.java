/*
 * Created on 28.08.2019
 *
 */
package de.swingempire.fx.scene.layout;

import java.util.Optional;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Problem (layout? or other as well?)
 * https://stackoverflow.com/q/57682554/203657
 * 
 * Don't we see the spurious not found? Maybe not ... or else ... 
 * what's so happening? it's gone quickly before it manifests itself
 * really tricky 
 * 
 * Do we get the spurious not found or not? can't dderijeknr
 * kldjflkerid f
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DialogValidate extends Application {
    private Dialog<String> d;
    
     public void showCustomDialog(ActionEvent ac) {
        // layout
        TextField field =  new TextField();
        field.setPrefColumnCount(20);
        field.setPromptText("Input must not be empty");
        
        Label label = new Label("Some Value");
        HBox box = new HBox(20, field, label);
        
        // config dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.getDialogPane().setContent(box);
        // having a cancel button allows "abnormal closing" 
        // (via window decoration or other OS specific means)
        // implies by-passing the close request handler
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button -> {
            LOG.info("result ? " + button);
            if (button == ButtonType.OK) {
                return field.getText();
            } else if (button == ButtonType.CANCEL) {
                return "cancelled";
            }
            
            return null;
        });
        
        // wiring
        dialog.getDialogPane().lookupButton(ButtonType.OK);
        dialog.getContentText();
        
        // onCloseRequest for dialog != onCloseRequest for window
        // bug or feature?
        dialog.setOnCloseRequest(e -> {
            LOG.info("receiving onCloseRequest in dialog");
            if (field.getText().isEmpty()) {
                e.consume();
            }
        });
        
        dialog.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> {
            LOG.info("receiving onCloseRequest in window");
            if (field.getText().isEmpty()) {
                e.consume();
            }
            
        });
        Optional<String> value = dialog.showAndWait();
        LOG.info("value: " + value);
        
    }
    
    // original in question
    public void showDialog() {
        Dialog<String> dialog = new Dialog<>();
        
//        dialog.setResizable(false);

        final Window window = dialog.getDialogPane().getScene().getWindow();
        Stage stage = (Stage) window;
        // was not resizable in original ... thus not showing the breakage 
        stage.setResizable(true);
        stage.setMinHeight(600);
        stage.setMinWidth(400);
        TextField tf = new TextField();
        tf.setLayoutX(10);
        tf.setLayoutY(50);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getChildren().add(tf);

        dialog.getDialogPane().setContent(tf);

        // Create an event filter that consumes the action if the text is empty
        EventHandler<ActionEvent> filter = event -> {
            if (tf.getText().isEmpty()) {
                event.consume();
            }
        };

        // lookup the buttons
        ButtonBase okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);

        // add the event-filter
        okButton.addEventFilter(ActionEvent.ACTION, filter);
        cancelButton.addEventFilter(ActionEvent.ACTION, filter);

        stage.setOnCloseRequest(event -> {
            if (tf.getText().isEmpty()) {
                event.consume();
            }
        });

        //Scene scene = new Scene(root);

        //dialogStage.setScene(scene);
        dialog.initModality(Modality.APPLICATION_MODAL);
        //dialogStage.setAlwaysOnTop(true);
        //dialogStage.setResizable(false);
        tf.setPromptText("This Works ?");
        tf.requestFocus();// This does not work
        dialog.showAndWait();
    }

    // from answer
    public void dialog(){

        Label lblAmt = new Label("Enter Amount");
        Button btnOK = new Button("OK");
        TextField txtAmt = new TextField();

        AnchorPane secondaryLayout = new AnchorPane();
        secondaryLayout.setStyle("-fx-border-color:red;-fx-border-width:10px; -fx-background-color: lightblue;");
        secondaryLayout.getChildren().addAll(lblAmt,btnOK,txtAmt);
        lblAmt.setLayoutX(30);
        lblAmt.setLayoutY(30);
        txtAmt.setLayoutX(164);
        txtAmt.setLayoutY(25);
        txtAmt.setMaxWidth(116);
        btnOK.setLayoutX(190);
        btnOK.setLayoutY(100);
        btnOK.setStyle("-fx-font-size: 18px;-fx-font-weight: bold;");
        lblAmt.setStyle("-fx-font-size: 18px;-fx-font-weight: bold;");
        txtAmt.setStyle("-fx-font-size: 18px;-fx-font-weight: bold;");

        Scene secondScene = new Scene(secondaryLayout, 200, 180);

        EventHandler<ActionEvent> filter = event -> {
        if(txtAmt.getText().isEmpty()) {
event.consume();
        }
        };

        // New window (Stage)
        Stage newWindow = new Stage();
        newWindow.initStyle(StageStyle.UNDECORATED);
        //newWindow.initModality(Modality.APPLICATION_MODAL);
//        newWindow.setResizable(false);
        newWindow.setTitle("Second Stage");
        newWindow.setScene(secondScene);
        btnOK.addEventHandler(ActionEvent.ACTION,filter);
        btnOK.setOnAction(evt -> {
        String str = txtAmt.getText();
        System.out.println("@@@@@@@@@@@@@@@@ str "+str);
        if(txtAmt.getText().equals("")) {
            evt.consume();
           txtAmt.requestFocus();
        }else{
          newWindow.close();  
        }
        });

        newWindow.setOnCloseRequest(event -> {
            if(txtAmt.getText().isEmpty()) {
            event.consume();
        }
        });
        txtAmt.requestFocus();
        newWindow.showAndWait();
}

    private Parent createContent() {
        Button button = new Button("show");
//        button.setOnAction(e-> showDialog());
//        button.setOnAction(e-> dialog());
        button.setOnAction(this::showCustomDialog);
        return button;
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
            .getLogger(DialogValidate.class.getName());

}
