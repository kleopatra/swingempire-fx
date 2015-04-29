/*
 * Created on 21.05.2014
 *
 */
package de.swingempire.fx.property.albumdemo;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class AlbumFxDriver extends Application {

    private CheckBox checkBox;
    private TextField textField;
    private AlbumFx album;
    private Label label;

    public AlbumFxDriver() {
        album = new AlbumFx();
//        album.setClassical(true);
        initComponents();
        bindComponents();
    }
    /**
     * 
     */
    private void bindComponents() {
        checkBox.selectedProperty().bindBidirectional(album.classicalProperty());
        textField.textProperty().bindBidirectional(album.composerProperty());
        // following line to see self-protection fail with IllegalStateExcetpion
        // album.composerProperty().bind(textField.textProperty());
        label.textProperty().bind(album.composerProperty());
    }
    /**
     * @return
     */
    private Parent createContent() {
        Pane root = new VBox();
        root.getChildren().addAll(checkBox, textField, label);
        return root;
    }
    protected void initComponents() {
        checkBox = new CheckBox("classical");
        textField = new TextField();
        label = new Label();
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
