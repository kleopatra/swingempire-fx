/*
 * Created on 22.05.2014
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Using BufferedObjectProperties.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AlbumFxViewDriver extends Application {
    
    private AlbumFxView editor;
    private AlbumFxForm logger;
    
    private AlbumFxModel model;
    private EventHandler<ActionEvent> createAlbumAction;
    private EventHandler<ActionEvent> resetAlbumAction;
    private Button createButton;
    private Button resetButton;
    
    public AlbumFxViewDriver() {
        initActions();
//        model = new AlbumFxModel();
//        setCurrent(new AlbumFx());
    }

    /**
     * 
     */
    private void initActions() {
        createAlbumAction = event ->
        {
            setCurrent(new AlbumFx());
        };       
        resetAlbumAction = event ->
        {
            setCurrent(null);
        };
        
    }

    protected void setCurrent(AlbumFx albumFx) {
        if (model == null) {
            model = new AlbumFxModel();
        }
        if (editor != null && editor.getAlbumModel() != model) {
            editor.setAlbumModel(model);
        }
        model.setBean(albumFx);
        if (logger != null) {
            logger.setAlbum(model.getBean());
        }
    }

    /**
     * @return
     */
    private Parent createContent() {
        if (editor == null) {
            editor = new AlbumFxView();
            logger = new AlbumFxForm();
//            logger.setAlbum(model.getBean());
            logger.getContent().setDisable(true);
        }
        
        Pane center = new HBox();
        center.getChildren().addAll(editor.getContent(), logger.getContent());
        Pane south = new FlowPane();
        createButton = new Button("New Album");
        createButton.setOnAction(createAlbumAction);
        resetButton = new Button("Null Album");
        resetButton.setOnAction(resetAlbumAction);
        south.getChildren().addAll(createButton, resetButton);
        
        BorderPane content = new BorderPane();
        content.setCenter(center);
        content.setBottom(south);
        return content;
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
