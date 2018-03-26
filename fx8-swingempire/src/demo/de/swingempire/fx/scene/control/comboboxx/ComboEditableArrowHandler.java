/*
 * Created on 04.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import java.util.logging.Logger;

import static javafx.scene.control.TextFormatter.*;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * ComboBoxBaseSkin installs mouse handlers on the arrow only if the combo is
 * editable at the time of creating the skin.
 * 
 * Compare behavior on mouse predded/released on the arrow
 * click on arrow while popup open 
 * - initial editable: popup is hidden
 * - dynamic editable: popup is hidden and shown again
 * 
 * reported
 * https://bugs.openjdk.java.net/browse/JDK-8150960
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ComboEditableArrowHandler extends Application {

    private Parent getContent() {
        ComboBox box = new ComboBox(FXCollections.observableArrayList("one", "two", "three"));
        box.setEditable(true);
        box.setValue("initial editable");
        installListeners("initial", box);
        ComboBox core = new ComboBox(FXCollections.observableArrayList("one", "two", "three"));
        core.setValue("dynamic editable");
        installListeners("dynamic", core);
        Button toggle = new Button("toggle editable");
        toggle.setOnAction(e -> {
            core.setEditable(!core.isEditable());
        });
        
        TextField field = new TextField("and here?");
        TextFormatter<String> formatter = new TextFormatter<>(IDENTITY_STRING_CONVERTER, "initial");
        field.setTextFormatter(formatter);
        installListeners("textField ", field);
        
//        FocusTraversalInputMap<Node>
        return new VBox(10, box, core, toggle, field);
    }

    /** 
     * quick check: sequence of listener notification
     * https://bugs.openjdk.java.net/browse/JDK-8151129
     * 
     * variant of the ol' correlated-properties problem: there's
     * no guarantee about the state of another property when
     * listening to any property! Simply don't assume anything.
     * In particular: if there are low-level properties that trigger
     * change in semantically higher properties, listen to the 
     * highest.
     */
    private void installListeners(String text, ComboBox box) {
        box.focusedProperty().addListener((src, ov, nv) -> {
            if (!nv) {
                LOG.info(" focusLost in " + text + ": " + box.getValue());
            }
        });
        box.valueProperty().addListener((src, ov, nv) -> {
            LOG.info(" value in " + text + ": " + box.getValue());
        });
        box.getSelectionModel().selectedItemProperty().addListener((src, ov, nv) -> {
            LOG.info(" selected in " + text + ": " + box.getValue());
        });
    }
    
    private void installListeners(String text, TextField box) {
        box.focusedProperty().addListener((src, ov, nv) -> {
            if (!nv) {
                LOG.info(" focusLost in " + text + ": " + box.getTextFormatter().getValue());
            }
        });
        box.textProperty().addListener((src, ov, nv) -> {
                LOG.info(" text in " + text + ": " + box.getText());
        });
        box.getTextFormatter().valueProperty().addListener((src, ov, nv) -> {
                LOG.info(" value in " + text + ": " + box.getText());
        });
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboEditableArrowHandler.class.getName());
}
