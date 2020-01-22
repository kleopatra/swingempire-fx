/*
 * Created on 31.07.2018
 *
 */
package de.swingempire.fx.scene.css;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * trying to set font family as var in css ... no success
 * https://stackoverflow.com/q/59834854/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class FontFamilyGlobal extends Application {

    private Parent createContent() {
        System.out.println(Font.getFamilies());
        System.out.println(Font.getFontNames());
        
        Button button = new Button("button");
        
        BorderPane pane = new BorderPane(button);
        pane.setBottom(new HBox(10, new Button("plain")));
//        pane.setBottom(new TextField("something in here"));
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        scene.getStylesheets().add(getClass().getResource("fontfamily.css").toExternalForm());
        stage.setScene(scene);

        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(FontFamilyGlobal.class.getName());

}
