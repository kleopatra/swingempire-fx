/*
 * Created on 29.11.2017
 *
 */
package de.swingempire.fx.event;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Listen to changes on the clipboard .. needs access to internal api and
 * access permissions
 * 
 * https://stackoverflow.com/a/47550034/203657
 */
public class  ClipboardListener extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        final Clipboard systemClipboard = Clipboard.getSystemClipboard();

        new com.sun.glass.ui.ClipboardAssistance(com.sun.glass.ui.Clipboard.SYSTEM) {
            @Override
            public void contentChanged() {
                System.out.print("System clipboard content changed: ");
                if ( systemClipboard.hasImage() ) {
                    System.out.println("image");
                } else if ( systemClipboard.hasString() ) {
                    System.out.println("string");
                } else if ( systemClipboard.hasFiles() ) {
                    System.out.println("files");
                }
            }
        };

        primaryStage.setScene(new Scene(new StackPane()));
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}

