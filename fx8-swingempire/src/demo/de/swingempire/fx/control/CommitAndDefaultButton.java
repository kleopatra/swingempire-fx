/*
 * Created on 07.12.2017
 *
 */
package de.swingempire.fx.control;

import java.time.LocalDate;

import static javafx.scene.control.TextFormatter.*;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * [ComboBox, DatePicker] Buttons set to default/cancel are not reacting to ComboBox enter/esc keys
 * https://bugs.openjdk.java.net/browse/JDK-8096725
 * 
 * Click on the TextField (the second element) and press enter.
 *  You will see that the button (which is set to "defaultButton") is pressed.
 *  Now click on the ComboBox textField like you would want to type something. 
 *  Press enter and you will see that the button is not being pressed. 
 */
public class CommitAndDefaultButton extends Application {
    
    Node oldFocusOwner;
    
    @Override public void start(Stage stage) {
        stage.setWidth(450);
        stage.setHeight(550);

        VBox vbox = new VBox(10);

        // MenuBar mb =
        //     new MenuBar(new Menu("_File", null,
        //                          new MenuItem("E_xit") { { setOnAction(e -> { System.exit(0); }); } }
        //                          ));

        ComboBox<String> box = new ComboBox<>();
        box.getItems().add("test");
        box.setEditable(true);

        DatePicker dp = new DatePicker(LocalDate.now());

        TextField textfield = new TextField();
        // JW: added formatter
        TextFormatter<String> formatter = new TextFormatter<>(IDENTITY_STRING_CONVERTER, "initial");
        textfield.setTextFormatter(formatter);
        // JW: added spinner
        Spinner<Integer> spinner = new Spinner<>(0, 200, 20);
        spinner.setEditable(true);
        
        Button setActionButton = new Button("Set Action Handlers");
        setActionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                box.setOnAction(e -> {
                    System.err.println("ComboBox Action " + box.getValue());
                });
                dp.setOnAction(e -> {
                    System.err.println("DatePicker Action " + dp.getValue());
                }); 
                textfield.setOnAction(e -> {
                    TextFormatter formatter = textfield.getTextFormatter();
                    Object value = formatter != null ? formatter.getValue() : " text only: " + textfield.getText();
                    System.err.println("TextField Action " + formatter.getValue());
                });
                // JW: spinner has no onAction
            }
        });

        Button defaultButton = new Button("OK");
        defaultButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                Node focusOwner = defaultButton.getScene().getFocusOwner();
                if (focusOwner == defaultButton) focusOwner = oldFocusOwner;
                Object value = null;
                if (focusOwner instanceof ComboBox) {
                    value = ((ComboBox) focusOwner).getValue();
                }
                if (focusOwner instanceof DatePicker) {
                    value = ((DatePicker) focusOwner).getValue();
                }
                if (focusOwner instanceof Spinner) {
                    value = ((Spinner) focusOwner).getValue();
                } 
                if (focusOwner instanceof TextField) {
                    TextFormatter formatter = ((TextField) focusOwner).getTextFormatter();
                    if (formatter != null) {
                        value = formatter.getValue();
                    }
                } 
                System.out.println("OK " + value + " / " + focusOwner);
            }
        });
        defaultButton.setDefaultButton(true);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                System.out.println("Cancel");
            }
        });
        cancelButton.setCancelButton(true);

// Comment out this line for the second test
// box.onActionProperty().bind(defaultButton.onActionProperty());
// dp.onActionProperty().bind(defaultButton.onActionProperty());

        vbox.getChildren().addAll(/*mb, */box, dp, textfield, spinner, setActionButton, defaultButton, cancelButton);
        Scene scene = new Scene(vbox);
        scene.focusOwnerProperty().addListener((src, ov, nv) -> oldFocusOwner = ov);
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
/* code examples for Dialog
 
 public static void editConcludedLicense(SpdxFile file, SpdxDocumentContainer container) {
    LicenseEditControl control = new LicenseEditControl(container, file, true);
    if (file.getLicenseConcluded() != null) {
        control.setInitialValue(file.getLicenseConcluded());
    }

    DialogPane dialogPane = new DialogPane();
    dialogPane.getButtonTypes().add(ButtonType.OK);
    dialogPane.setContent(control.getUi());

    Dialog<AnyLicenseInfo> dialog = new Dialog<>();
    dialog.setDialogPane(dialogPane);
    dialog.setResultConverter(buttonType -> {
        if (buttonType == ButtonType.OK) {
            return control.getValue();
        } else {
            throw new RuntimeException("Unexepected button!");
        }
    });

    dialog.showAndWait();

    try {
        file.setLicenseConcluded(control.getValue());
    } catch (InvalidSPDXAnalysisException e) {
        throw new RuntimeException(e);

    }

}
    
 */
///
    
/*
 other example
 
 

public String retrievePassword() {      
String password = this.preferences.get("password", ""); 
if(password.isEmpty()) {            
    final Dialog<String> passwordDialog = new Dialog<>();
    passwordDialog.setTitle(resources.getString("enterPasswordDialog.title"));
    passwordDialog.setHeaderText(null);
    final DialogPane passwordDialogPane = passwordDialog.getDialogPane();
    
    final PasswordField passwordField = new PasswordField();
    passwordField.setPromptText(resources.getString("enterPasswordDialog.passwordFieldPrompt"));
    // Create and add new button type for confirmation  
    final ButtonType confirmButtonType = new ButtonType(resources.getString("enterPasswordDialog.title"), ButtonData.OK_DONE);
    passwordDialogPane.getButtonTypes().add(confirmButtonType);
    // Retrieve node
    final Node confirmButton = passwordDialogPane.lookupButton(confirmButtonType);
    confirmButton.disableProperty().bind(passwordField.textProperty().isEmpty());

    // Result converter
    passwordDialog.setResultConverter(dialogButton -> {
        final ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
        return data == ButtonData.OK_DONE ? passwordField.getText() : null;
    });

    // Create content
    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setMaxWidth(Double.MAX_VALUE);
    grid.setAlignment(Pos.CENTER_LEFT);

    // Do layout
    passwordField.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(passwordField, Priority.ALWAYS);
    GridPane.setFillWidth(passwordField, true);

    grid.add(new Label(passwordField.getPromptText()), 0, 0);
    grid.add(passwordField, 1, 0);
    passwordDialogPane.setContent(grid);

    Platform.runLater(() -> passwordField.requestFocus());
    password = passwordDialog.showAndWait().orElse(null);
}
return password;
   }
 


 */     
 
}
