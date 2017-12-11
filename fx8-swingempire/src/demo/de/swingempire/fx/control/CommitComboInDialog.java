/*
 * Created on 08.12.2017
 *
 */
package de.swingempire.fx.control;

import java.util.Optional;

import com.sun.javafx.stage.StageHelper;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class CommitComboInDialog extends Application {

    private Parent getContent() {
        
        // dialog content: editable combo
        ComboBox<String> box = new ComboBox<>(FXCollections.observableArrayList("initial"));
        // PENDING JW: if selectedItem is null -> the returned value is no value
        // dialog doesn't return null then but iterates through buttons ...
        box.getSelectionModel().selectFirst();
        box.setEditable(true);

        VBox dialogContent = new VBox(10, box);
        Dialog<String> dialog = new Dialog<>();
        dialog.getDialogPane().setContent(dialogContent);
        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        // Convert the result to combo.value when ok-button is clicked.
        dialog.setResultConverter(dialogButton -> {
            System.out.println("dialogButton? " + dialogButton);
            if (dialogButton == ButtonType.OK) {
                return box.getValue();
            } 
            return null;
        });
        
        Button openDialog = new Button("Open Dialog");
        openDialog.setOnAction(e -> {
            Platform.runLater(() -> box.requestFocus());
            Optional<String> result = dialog.showAndWait();
            System.out.println(result);
        });
        HBox buttons = new HBox(10, openDialog);
        BorderPane content = new BorderPane(buttons);
        return content;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // fx9: gets only windows that are showing (vs. all created)
        // but: was same in fx8 StageHelper.getStages
//        Window.getWindows();
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
