/*
 * Created on 16.03.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Commit semantics: on-focusLost, on-action
 * SO: 
 * 
 * Here also check default button: is triggered on enter ..
 * https://stackoverflow.com/q/49311803/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextInputCommitWithFormatter extends Application {

    private Parent createTextContent() {
        TextFormatter<String> fieldFormatter = new TextFormatter<>(
                TextFormatter.IDENTITY_STRING_CONVERTER, "textField ...");
        TextField field = new TextField();
        field.setTextFormatter(fieldFormatter);
        Label fieldValue = new Label();
        fieldValue.textProperty().bind(fieldFormatter.valueProperty());
        TextFormatter<String> areaFormatter = new TextFormatter<>(
                TextFormatter.IDENTITY_STRING_CONVERTER, "textArea ...");
        TextArea area = new TextArea();
        area.setTextFormatter(areaFormatter);
        Label areaValue =  new Label();
        areaValue.textProperty().bind(areaFormatter.valueProperty());
        
        HBox fields = new HBox(100, field, fieldValue);
        BorderPane content = new BorderPane(area);
        content.setTop(fields);
        content.setBottom(areaValue);
        return content;
    }

    private Parent createContent() {
        Button button = new Button("I'm the default");
        button.setDefaultButton(true);
        button.setOnAction(e -> LOG.info("triggered: " + button.getText()));
        Button cancel = new Button("I'm the cancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> LOG.info("triggered: " + button.getText()));
        BorderPane content = new BorderPane( createTextContent());
        content.setBottom(new HBox(10, button, cancel));
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
            .getLogger(TextInputCommitWithFormatter.class.getName());

}
