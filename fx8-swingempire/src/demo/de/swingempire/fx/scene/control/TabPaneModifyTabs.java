/*
 * Created on 01.08.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static javafx.collections.FXCollections.*;

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
 * Bug: visual nodes that represent the tabs are not updated on modification
 * of the tabs-list - happens if order of tabs is changed. Here: reversed.
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TabPaneModifyTabs extends Application {

    private Parent createContent() {
        TabPane pane = new TabPane();
        pane.getTabs().addAll(createTabs(10));
        
        ListChangeReport report = new ListChangeReport(pane.getTabs());
        Button reverse = new Button("reverse");
        reverse.setOnAction(e -> {
            reverse(pane.getTabs());
            report.prettyPrint();
        });
        
        Button changeText = new Button("change text of tab at 0");
        changeText.setOnAction(e -> {
            Tab tab = pane.getTabs().get(0);
            tab.setText(tab.getText() + "+");
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
        Tab tab = new Tab("" + i);
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
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TabPaneModifyTabs.class.getName());

}
