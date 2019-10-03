/*
 * Created on 03.10.2019
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.logging.Logger;

import com.sun.javafx.scene.text.TextLayout;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Access line count (actual: hard breaks and wrapped) of TextArea
 * 
 * 
 * Needs tweaked module access:
 * 
 * compiletime: 
 *   --add-exports javafx.graphics/com.sun.javafx.scene.text=ALL_UNNAMED
 *   
 * runtime:
 *   --add-opens javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED
 *   --add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED
 *      
 * @author Jeanette Winzenburg, Berlin
 */
public class TextAreaLineCount extends Application {

    String info = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Nam tortor felis, pulvinar in scelerisque cursus, pulvinar at ante. " +
            "Nulla consequat congue lectus in sodales.";

    private Parent createContent() {
        TextArea area = new TextArea(info);
        area.setWrapText(true);
        
        area.appendText("\n" + info);
        
        Button append = new Button("append paragraph");
        append.setOnAction(e -> {
            area.appendText("\n " + info);
            LOG.info("paragraphs: " + area.getParagraphs().size());
        });
        Button logLines = new Button("log lines");
        logLines.setOnAction(e -> {
            Text text = (Text) area.lookup(".text");
            // getTextLayout is a private method in text, have to access reflectively
            // this is my utility method, use your own :)
            TextLayout layout = (TextLayout) FXUtils.invokeGetMethodValue(Text.class, text, "getTextLayout");
            LOG.info("" + layout.getLines().length);
        });
        BorderPane content = new BorderPane(area);
        content.setBottom(new HBox(10, append, logLines));
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
            .getLogger(TextAreaLineCount.class.getName());

}
