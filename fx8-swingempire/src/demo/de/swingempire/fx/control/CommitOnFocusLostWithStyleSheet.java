/*
 * Created on 23.09.2015
 *
 */
package de.swingempire.fx.control;

import com.sun.javafx.css.StyleManager;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Use custom stylesheet to
 * @author Jeanette Winzenburg, Berlin
 */
public class CommitOnFocusLostWithStyleSheet extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        String commitCSS = getClass().getResource("commitonfocuslost.css").toExternalForm();
        // 1. this adds the style for all stylable nodes of the types in the css
        // but only per-scene, needs to be duplicated for each subscene (popups as well!)
//        scene.getStylesheets().add(commitCSS);
        // 2. next two looses all default styles!
        // scene.setUserAgentStylesheet(getClass().getResource("commitonfocuslost.css").toExternalForm());
//        Application.setUserAgentStylesheet(commitCSS);
        // 3. using StyleManager we can add the sheet application-wide
        // but it is internal api, won't make it into jdk9
        StyleManager s = StyleManager.getInstance();
        s.addUserAgentStylesheet(commitCSS);
        primaryStage.setScene(scene);
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    /**
     * @return
     */
    private Parent getContent() {
        ComboBox box = new ComboBox(FXCollections.observableArrayList("one", "two", "threee"));
        box.setEditable(true);
        box.setValue("initial");
        Label label = new Label();
        label.textProperty().bind(box.valueProperty());
        Button button = new Button("dummy to focus - and open dialog");
        Dialog d;
        HBox grid = new HBox(10, box, label, button);
        return grid;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
