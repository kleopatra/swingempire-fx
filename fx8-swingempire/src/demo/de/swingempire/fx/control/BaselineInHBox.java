/*
 * Created on 12.12.2014
 *
 */
package de.swingempire.fx.control;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Alignment problems: http://stackoverflow.com/q/27435805/203657
 * @author Jeanette Winzenburg, Berlin
 */
public class BaselineInHBox extends Application {

//        FlowPane box = new FlowPane(new TextField("something"), 
//                new CheckBox("soso"), new Button("hello"));
    /**
     * @return
     */
    private Parent getContent() {
        HBox box = new HBox(new TextField("something"), 
                new CheckBox("soso"), new Button("hello"));
        box.setAlignment(Pos.BASELINE_CENTER);
        return box;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(BaselineInHBox.class
            .getName());
}
