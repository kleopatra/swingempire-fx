/*
 * Created on 06.08.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * prompt text with line breaks
 * https://stackoverflow.com/q/51698600/203657
 * 
 * Reason: promptTextProperty replaces linebreaks ..
 * hackaround: don't us \n instead use \r
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextPromtWithWrap extends Application {

    private Parent createContent() {
        TextArea paragraph = new TextArea();
        paragraph.setWrapText(true);
        paragraph.setPromptText(
            "Stuff done today:\r"
            + "\r"
            + "- Went to the grocery store\r"
            + "- Ate some cookies\r"
            + "- Watched a tv show"
        );
        
        BorderPane pane = new BorderPane(paragraph);
        pane.setTop(new TextField("dummy"));
        return pane;
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
            .getLogger(TextPromtWithWrap.class.getName());

}
