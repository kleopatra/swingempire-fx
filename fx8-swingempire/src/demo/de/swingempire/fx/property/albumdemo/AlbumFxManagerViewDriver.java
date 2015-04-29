/*
 * Created on 01.06.2014
 *
 */
package de.swingempire.fx.property.albumdemo;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class AlbumFxManagerViewDriver extends Application {

    private AlbumFxManagerModel model;
    private AlbumFxManagerView view;

    private EventHandler<ActionEvent> createAction;
    private EventHandler<ActionEvent> resetAction;
//    private Button createButton;
//    private Button resetButton;
//    private BorderPane content;
    
    public AlbumFxManagerViewDriver() {
        initActions();
    }
    /**
     * 
     */
    private void initActions() {
        createAction = e -> {
            setModel(new AlbumFxManagerModel(new AlbumFxManager()));  
        };
        resetAction = e -> {
            setModel(null);
        };
        
    }
    /**
     * @param albumFxManagerModel
     */
    private void setModel(AlbumFxManagerModel albumFxManagerModel) {
        if (view != null) {
            view.setModel(albumFxManagerModel);
        }
    }
    /**
     * @return
     */
    private Parent createContent() {
        if (view == null) {
            view = new AlbumFxManagerView();
        }
        BorderPane pane = new BorderPane();
        pane.setCenter(view.getContent());
        
        Button createButton = new Button("Create Model");
        createButton.setOnAction(createAction);
        Button resetButton = new Button("Null Model");
        resetButton.setOnAction(resetAction);
        
        FlowPane buttons = new FlowPane();
        buttons.getChildren().addAll(createButton, resetButton);
        pane.setBottom(buttons);
        return pane;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = createContent();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }

    public static void main(String[] args) {
        launch();
    }
}
