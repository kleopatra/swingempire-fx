/*
 * Created on 05.12.2017
 *
 */
package de.swingempire.fx.scene.control.skin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/47616221/203657
 * Switching tabs doesn't change their position. Seems to be fixed
 * in fx9
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TabRemoveAddBug extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = sizeScene();
        primaryStage.setMinHeight(200);
        primaryStage.setWidth(475);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Scene sizeScene(){
        TabPane tabPane = new TabPane();
        tabPane.setTabMinWidth(200);
        tabPane.getTabs().addAll(newTabs(3));
        Scene scene = new Scene(tabPane);
        scene.setOnKeyPressed(e -> tabPane.getTabs().add(1, tabPane.getTabs().remove(0)));
        return scene;
    }

    private static Tab[] newTabs(int numTabs){
        Tab[] tabs = new Tab[numTabs];
        for(int i = 0; i < numTabs; i++) {
            Label label = new Label("Tab Number " + (i + 1));
            Tab tab = new Tab();
            tab.setGraphic(label);
            tabs[i] = tab;
        }
        return tabs;
    }

    public static void main(String[] args) {
        launch();
    }

}

