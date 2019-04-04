/*
 * Created on 04.04.2019
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * SO: textArea not scrolled to bottom on input
 * happens "early" - "later" it's default to scroll to bottom
 * @author Jeanette Winzenburg, Berlin
 */
public class TextAreaScrollToBottom extends Application {

    TextArea outputTextArea;
    ScrollPane scroll;
    
    private Parent createContent() {
        outputTextArea = new TextArea();
        outputTextArea.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
//                System.out.println("Changed!");
//                outputTextArea.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
//
//                System.out.println(outputTextArea.getScrollTop());
//                //use Double.MIN_VALUE to scroll to the top
            }
        });
        
        Button add =  new Button("add bunch lines");
        add.setOnAction(this::addBunch);
        
        Button addSingle = new Button("add single line");
        addSingle.setOnAction(this::addSingle);
        
        BorderPane content = new BorderPane(outputTextArea);
        content.setBottom(new HBox(10, add, addSingle));
        return content;
    }

    int count = 0;
    protected void addBunch(Object dummy) {
        if (scroll ==  null) {
            scroll = (ScrollPane) outputTextArea.lookup(".scroll-pane");
        }
        if (scroll !=  null) {
            LOG.info("scroll: " + scroll.getVmin() + " / " + scroll.getVvalue() + " / " + scroll.getVmax()
            + "text top: " + outputTextArea.getScrollTop());
            LOG.info("viewport: " + scroll.getViewportBounds());
        }
        for (int i = 0; i < 12; i++) {
            outputTextArea.appendText("Hello" + count++ +"\n");
        }
    }
    
    protected void addSingle(Object dummy) {
        outputTextArea.appendText("Hello" + count++ +"\n");
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
            .getLogger(TextAreaScrollToBottom.class.getName());

}
