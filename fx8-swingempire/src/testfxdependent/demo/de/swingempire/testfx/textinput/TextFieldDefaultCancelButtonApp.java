/*
 * Created on 08.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.logging.Logger;

import de.swingempire.fx.scene.control.skin.XTextFieldSkin;
import de.swingempire.fx.util.FXUtils;
import de.swingempire.testfx.textinput.TextFieldDefaultCancelButtonTest.TextFieldDefaultCancelButtonPane;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldDefaultCancelButtonApp extends Application {

    private Parent createContent() {
        TextFieldDefaultCancelButtonPane pane = new TextFieldDefaultCancelButtonPane(field -> new XTextFieldSkin(field));
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
            .getLogger(TextFieldDefaultCancelButtonApp.class.getName());

}
