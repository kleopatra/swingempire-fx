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
 * Direct binding of properties <--> components in form.
 * @author Jeanette Winzenburg, Berlin
 */
public class AlbumFxFormDriver extends Application {
    
    private AlbumFxForm editor;
    private AlbumFxForm logger;
    
    private AlbumFx current;
    private EventHandler<ActionEvent> createAlbumAction;
    private EventHandler<ActionEvent> resetAlbumAction;
    private Button createButton;
    private Button resetButton;
    
    public AlbumFxFormDriver() {
        initActions();
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
        current = albumFx;
        editor.setAlbum(current);
        logger.setAlbum(current);
    }

    /**
     * @return
     */
    private Parent createContent() {
        if (editor == null) {
            editor = new AlbumFxForm();
            logger = new AlbumFxForm();
            logger.getContent().setDisable(true);
            setCurrent(new AlbumFx());
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
