/*
 * Created on 03.10.2019
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 *  https://stackoverflow.com/q/59059100/203657
 *  treat 4 consecutive spaces as 1 char
 *  
 *  trying: use formatter - might work, but don't fully understand the 
 *  requirement. In fact, does work, but too lazy to really do it (one-off errors)
 *      
 * @author Jeanette Winzenburg, Berlin
 */
public class TextAreaFormatter extends Application {

    String info = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Nam tortor felis, pulvinar in scelerisque cursus, pulvinar at ante. " +
            "Nulla consequat congue lectus in sodales.";
    
    private Parent createContent() {
        TextArea area = new TextArea(info);
        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (change.isContentChange()) {
                if (change.getText().equals(" ")) {
                    int start = change.getRangeStart();
                    for (int i = start-1; i >= 0; i--) {
                        String ch = change.getControlText().substring(i, i+1);
                        if (!ch.equals(" ")) break;
                        start--;
                    }
                    int end = change.getRangeEnd();
                    for (int i = end; i < change.getControlText().length(); i++) {
                        String ch = change.getControlText().substring(i, i+1);
                        if (!ch.equals(" ")) break;
                        end++;
                    }
                    System.out.println("start/end " + start + "/" + end + 
                            " text: [" + change.getControlText().substring(start, end)+ "]");
                    if (end - start == 3) {
                        System.out.println("found 4 spaces");
                        change.setRange(start, end);
                        change.setText("\t");
                        change.selectRange(start+1, start+1);
                    }
                }
            }
            return change;
        };

        TextFormatter<String> formatter = new TextFormatter<>(filter);
        area.setTextFormatter(formatter);
        area.setWrapText(true);
        
        area.appendText("\n" + info);
        
        Button append = new Button("append paragraph");
        Button logLines = new Button("log lines");
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
            .getLogger(TextAreaFormatter.class.getName());

}
