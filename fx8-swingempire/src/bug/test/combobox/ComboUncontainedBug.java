/*
 * Created on 31.03.2019
 *
 */
package test.combobox;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Reported as regression 
 * https://bugs.openjdk.java.net/browse/JDK-8221722
 * 
 * see report for complete analysis (no solution except hacking, so far)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboUncontainedBug extends Application {

    private Parent createContent() {
        ComboBox<String> combo = new ComboBox<>(
                FXCollections.observableArrayList("Option 1", "Option 2", "Option 3")) ;
        Button uncontained = new Button("set value to uncontained");
        uncontained.setOnAction(e -> combo.setValue("uncontained value"));
        VBox content = new VBox(10, combo, uncontained);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        //stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboUncontainedBug.class.getName());

}
