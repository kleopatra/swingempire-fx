/*
 * Created on 01.08.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import de.swingempire.fx.util.ListChangeReport;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Requirement: clip longish tab texts with "text fading" as in browser tabs
 * -fx-text-overrun - not supported
 * https://stackoverflow.com/q/58075969/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TabTextClipping extends Application {

    private Parent createContent() {
        TabPane pane = new TabPane();
        pane.getTabs().addAll(createTabs(5));
        
        pane.setTabMaxWidth(50);
        
        ListChangeReport report = new ListChangeReport(pane.getTabs());
        Button reverse = new Button("reverse");
        reverse.setOnAction(e -> {
        });
        
        Button changeText = new Button("change text of tab at 0");
        changeText.setOnAction(e -> {
        });
        
        HBox buttons = new HBox(10, reverse, changeText);
        BorderPane content = new BorderPane(pane);
        content.setBottom(buttons);
        return content;
    }

    /**
     * @param i
     * @return
     */
    private Tab createTab(int i) {
        Tab tab = new Tab("longish text " + i);
        return tab;
    }
    
    /**
     * @param i
     * @return
     */
    private List<Tab> createTabs(int size) {
        List<Tab> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(createTab(i));
        }
        return list;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        
//        URL uri = getClass().getResource("tabclipping.css");
//        stage.getScene().getStylesheets().add(uri.toExternalForm());
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TabTextClipping.class.getName());

}
