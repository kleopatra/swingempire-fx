/*
 * Created on 23.12.2019
 *
 */
package de.swingempire.fx.fxml;

import java.io.IOException;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Quick check: possible to access the controller 
 * - without loading? No - this throws NPE.
 * - without application? No - throws IllegalStateException (toolkitNotInitialized)
 * 
 * https://stackoverflow.com/q/59435261/203657
 */
public class ChoiceBoxMainLoad extends Application {

    ChoiceBoxController controller;
    
    private void getController() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("choicebox.fxml"));
        loader.load();
        controller = loader.getController();
        System.out.println(controller.getClass());
        
    }

    private Parent createContent() throws IOException {
        getController();
        BorderPane content = new BorderPane();
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
            .getLogger(ChoiceBoxMainLoad.class.getName());

}
