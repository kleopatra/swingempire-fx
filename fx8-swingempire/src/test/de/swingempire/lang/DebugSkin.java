/*
 * Created on 31.07.2017
 *
 */
package de.swingempire.lang;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ButtonSkin;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DebugSkin extends Application {


    public static class MyButtonSkin extends ButtonSkin {

        public MyButtonSkin(Button button) {
            // pass null to produce a stacktracece
            super(null);
        }
        
    }
    private Parent getContent() {
        Button buttonx = new Button("");
        Button button = new Button("Just a button") {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new MyButtonSkin(this);
            }
            
        };
        BorderPane pane = new BorderPane(button);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 600, 400));
        // primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
