/*
 * Created on 01.04.2019
 *
 */
package de.swingempire.fx.scene.css;

import java.net.URL;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Quick check: style only the buttoncell (not the cells in listview)
 * need a child combinator (vs. a descendant)
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboButtonCellStyle extends Application {

    private Parent createContent() {
        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(" one ---- ", " two ----- "));
        combo.setValue("other");
        
        return new VBox(10, combo);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        URL uri = getClass().getResource("buttoncell.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboButtonCellStyle.class.getName());

}
