/*
 * Created on 26.11.2015
 *
 */
package de.swingempire.fx.control;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * http://stackoverflow.com/q/33926424/203657
 * close all tabs except the selected 
 */
public class CloseOtherTabs extends Application
{    
    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setTitle("Tabs");
        Group root = new Group();
        Scene scene = new Scene(root, 400, 250, Color.WHITE);

        final TabPane tabPane = new TabPane();

        BorderPane borderPane = new BorderPane();
        for (int i = 0; i < 5; i++)
        {
            Tab tab = new Tab();
            tab.setText("Tab" + i);
            HBox hbox = new HBox();
            hbox.getChildren().add(new Label("Tab" + i));
            hbox.setAlignment(Pos.CENTER);
            tab.setContent(hbox);
            tabPane.getTabs().add(tab);

            ContextMenu contextMenu = new ContextMenu();
            MenuItem close = new MenuItem();
            MenuItem closeOthers = new MenuItem();
            MenuItem closeAll = new MenuItem();

            close.setText("Close");
            closeOthers.setText("Close Others");
            closeAll.setText("Close All");
            contextMenu.getItems().addAll(close, closeOthers, closeAll);
            tab.setContextMenu(contextMenu);

            final ObservableList<Tab> tablist = tabPane.getTabs();

            close.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedItem());
                }
            });

            closeOthers.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
//                    LOG.info("owner: " + contextMenu.getOwnerNode());
                    tabPane.getTabs().retainAll(tab);
//                    tabPane.getTabs().retainAll(tabPane.getSelectionModel().getSelectedItem());
//                    tabPane.getTabs().removeAll();
                }
            });

            closeAll.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    tabPane.getTabs().removeAll(tablist);
                }
            });
        }

        // bind to take available space
        borderPane.prefHeightProperty().bind(scene.heightProperty());
        borderPane.prefWidthProperty().bind(scene.widthProperty());

        borderPane.setCenter(tabPane);
        root.getChildren().add(borderPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(CloseOtherTabs.class
            .getName());
}

