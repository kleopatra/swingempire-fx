/*
 * Created on 13.04.2020
 *
 */
package de.swingempire.fx.scene.control.text;

import de.swingempire.testfx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.NodeOrientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/61184745/203657
 * left/right doesn't move the cursor
 * 
 * reproducible in fx11, fx8 is fine - plus in both
 * shift-left/right do move the cursor (and extend selection)  
 * but into the wrong direction
 */
public class FxTxtFld extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        TextField txtFld = new TextField("sampler");
        Group root = new Group(txtFld);
        Scene scene = new Scene(root);
        scene.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        primaryStage.setScene(scene);
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}