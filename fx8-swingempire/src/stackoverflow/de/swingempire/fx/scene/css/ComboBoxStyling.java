/*
 * Created 19.09.2021 
 */

package de.swingempire.fx.scene.css;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


/**
 * https://stackoverflow.com/q/69226828/203657
 * Style comboBox cell: add check mark to selected cell via css
 * 
 * 
 */
public class ComboBoxStyling extends Application {

    private Parent createContent() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("one", "two", "and a bit longer");
        
        Label label = new Label("with graphic?");
        BorderPane content = new BorderPane(combo);
        content.setBottom(new HBox(10, label));
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 300, 300));
        stage.getScene().getStylesheets().add(getClass().getResource("combostyling.css").toExternalForm());
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ComboBoxStyling.class.getName());

}
