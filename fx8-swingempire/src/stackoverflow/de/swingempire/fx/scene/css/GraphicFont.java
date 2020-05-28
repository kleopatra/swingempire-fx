/*
 * Created on 25.05.2020
 *
 */
package de.swingempire.fx.scene.css;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/62000744/203657
 * fonts for text/graphic cannot be set independently 
 * worksforme
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class GraphicFont extends Application {

    @Override
    public void start (Stage stage) {
        initUI(stage);
    }

    private void initUI (Stage stage) {
        StackPane main = new StackPane();
        Button btn = new Button("Test me :)");
        Label graphic = new Label("Graphic");
        graphic.getStyleClass().add("graphic");
        btn.setGraphic(graphic);
        main.getChildren().add(btn);

        Scene scene = new Scene(main, 200, 200);

        scene.getStylesheets().add(GraphicFont.class.getResource("graphicfont.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}