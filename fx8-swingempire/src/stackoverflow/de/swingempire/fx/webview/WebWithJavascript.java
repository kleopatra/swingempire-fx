/*
 * Created 15.10.2021
 */
package de.swingempire.fx.webview;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/69577162/203657
 * Three pages: should switch every 10 secs, one showing charts, one showing images
 * doesn't .. bug or feature?
 */
public class WebWithJavascript extends Application {

    @Override
    public void start(final Stage pStage) {
        initStage(pStage);

    }

    private void initStage(Stage pStage){
        WebView lWebView = new WebView();


        lWebView.getEngine().setJavaScriptEnabled(true);
        lWebView.getEngine().load("http://it-topics.com/index3.html");
        
        VBox lVBox = new VBox(lWebView);

        pStage.setScene(new Scene(lVBox));
        pStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

