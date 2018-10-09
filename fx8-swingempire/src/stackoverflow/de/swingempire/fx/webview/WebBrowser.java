/*
 * Created on 09.10.2018
 *
 */
package de.swingempire.fx.webview;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * https://stackoverflow.com/q/52713497/203657
 * not showing the link
 * 
 * here: can't even run, getting no access error
 * Caused by: java.lang.IllegalAccessError: superclass access check failed: 
 * class com.sun.javafx.sg.prism.web.NGWebView (in unnamed module @0xa01f14a) 
 *      cannot access class com.sun.javafx.sg.prism.NGGroup (in module javafx.graphics) 
 *      because module javafx.graphics does not export com.sun.javafx.sg.prism to unnamed module @0xa01f14a
 * After adding that particular one to fx-11-opens, needed to add another bunch (right
 *   to the end of the graphics opens)     
 *   
 * Alternatively: added javafx.web explicitly as module (new line in fx-11-modules) 
 *  kept the list of add-opens as xx-bak  
 */
public class WebBrowser extends Application {

    TabPane root;
    @Override
    public void start(Stage stage) {


        WebView browser = new WebView();
        browser.getEngine().load("http://www.google.com");

        Tab browserTab = new Tab("New Tab", browser);
        Tab addTab = new Tab("+", null);
        addTab.setClosable(false);        
        addTab.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                addNewTab();
            }
        });
        root = new TabPane(browserTab, addTab);
        Scene scene = new Scene(root, 500, 200);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        stage.setScene(scene);
        stage.setTitle("Browser");
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.show();

    }
    
    private void openBrowser() {
        
    }
      final void addNewTab() {

            WebView browser = new WebView();
            Tab browserTab = new Tab("New Tab", browser);
            root.getTabs().add(root.getTabs().size() - 1, browserTab);
            root.getSelectionModel().select(browserTab);
            browser.getEngine().load("http://www.google.com");
        }


    public static void main(String[] args) 
    {
        launch(args);
    }
}

